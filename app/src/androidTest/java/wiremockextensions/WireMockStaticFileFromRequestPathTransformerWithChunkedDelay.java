package wiremockextensions;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;

import static java.lang.Math.ceil;

public class WireMockStaticFileFromRequestPathTransformerWithChunkedDelay extends ResponseDefinitionTransformer {

    @Override
    public ResponseDefinition transform(Request request, ResponseDefinition responseDefinition, FileSource files, Parameters parameters) {

        ResponseDefinitionBuilder build = ResponseDefinitionBuilder.like(responseDefinition).but();
        try {
            buildWithAStaticFile(request, files, build);
        } catch (Exception e) {
            buildAFourOhFour(build, e);
        }

        return build.build();
    }

    private void buildWithAStaticFile(Request request, FileSource files, ResponseDefinitionBuilder build) {
        String fileName = fileNameIsUrlPathLessTheLeadingSlash(request);
        byte[] body = files.getBinaryFileNamed(fileName).readContents();
        build.withBody(body);
        potentiallyThrottleSegment(fileName,body,build);
    }

    private ResponseDefinitionBuilder potentiallyThrottleSegment(String name, byte[] bytesFromInputStream, ResponseDefinitionBuilder responseDefBuilder) {

        //                     0       1      2       3       4       5
        int[] abrLadderBPS = {86000, 156000, 281000, 437000, 827000, 1604000};
        if (name.contains("video") && name.endsWith("m4s")) {
            int bps = -1;

            if (nameEndsWithAnyOf(name, "-1.m4s", "-2.m4s", "-3.m4s")) {
                bps = abrLadderBPS[0];
            } else if (nameEndsWithAnyOf(name, "-4.m4s", "-5.m4s", "-6.m4s")) {
                bps = abrLadderBPS[1];
            } else if (nameEndsWithAnyOf(name, "-7.m4s", "-8.m4s", "-9.m4s")) {
                bps = abrLadderBPS[2];
            } else if (nameEndsWithAnyOf(name, "-10.m4s", "-11.m4s", "-12.m4s", "-13.m4s", "-14.m4s", "-15.m4s")) {
                bps = abrLadderBPS[3];
            }
            if (bps > -1) {
                responseDefBuilder = applySpeedRestriction(bps, responseDefBuilder, bytesFromInputStream.length);
            }

        }
        return responseDefBuilder;
    }

    private boolean nameEndsWithAnyOf(String name, String... suffixs) {
        boolean result=false;

        for (String suffix : suffixs) {
            if(name.endsWith(suffix)) {
                result = true;
            }
        }

        return result;
    }


    private  ResponseDefinitionBuilder applySpeedRestriction(int bps, ResponseDefinitionBuilder builder, int totalBytes) {
        int totalDuration = timeForRate(bps, totalBytes);
        int numberOfChunks = numberOf1KBChunks(totalBytes);
        return builder.withChunkedDribbleDelay(numberOfChunks, totalDuration);
    }


    private int numberOf1KBChunks(int length) {
        return length / 1024;
    }

    private int timeForRate(int bps, int totalBytes) {
        return (totalBytes * 8) / bps;
    }






    private String fileNameIsUrlPathLessTheLeadingSlash(Request request) {
        return request.getUrl().replaceFirst("/", "");
    }

    private void buildAFourOhFour(ResponseDefinitionBuilder build, Exception e) {
        build.withStatus(404);
        build.withBody(e.getMessage());
    }

    @Override
    public boolean applyGlobally() {
        return false;
    }

    @Override
    public String getName() {
        return "static-file-from-path-with-delay";
    }
}
