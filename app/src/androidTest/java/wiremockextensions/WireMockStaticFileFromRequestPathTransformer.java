package wiremockextensions;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;

public class WireMockStaticFileFromRequestPathTransformer extends ResponseDefinitionTransformer {

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
        //build.withBodyFile(fileName); //could never get this to work properly
        build.withBody(files.getBinaryFileNamed(fileName).readContents());
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
        return "static-file-from-path";
    }
}
