package testSupport;

import android.content.res.AssetManager;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static java.lang.Math.ceil;

public class WiremockTestSupport {

    //every three segments bump the bandwidth, reset when we see a -1.m4s

    public static void registerStubsForDashAssetWithDelay(WireMockRule rule, String assetName) {

        try {
            AssetManager assetManager = InstrumentationRegistry.getContext().getAssets();
            List<String> assetNamesForPath = getAssetNamesForPath(assetName, assetManager);

            for (String name : assetNamesForPath) {

                InputStream inputStream = assetManager.open(name);
                byte[] bytesFromInputStream = getBytesFromInputStream(inputStream);

                ResponseDefinitionBuilder responseDefBuilder = aResponse().withStatus(200).withBody(bytesFromInputStream);

                responseDefBuilder = potentiallyThrottleSegment(name, bytesFromInputStream, responseDefBuilder);

                rule.stubFor(get(urlEqualTo("/" + name)).willReturn(responseDefBuilder));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ResponseDefinitionBuilder potentiallyThrottleSegment(String name, byte[] bytesFromInputStream, ResponseDefinitionBuilder responseDefBuilder) {

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
                responseDefBuilder = applySpeedRestriction(bps, bytesFromInputStream, responseDefBuilder);
            }

        }
        return responseDefBuilder;
    }

    private static boolean nameEndsWithAnyOf(String name, String... suffixs) {
        boolean result=false;

        for (String suffix : suffixs) {
            if(name.endsWith(suffix)) {
                result = true;
            }
        }

        //result = name.endsWith(suffix) || name.endsWith(suffix2) || name.endsWith(suffix3);

        return result;
    }


    private static ResponseDefinitionBuilder applySpeedRestriction(int bps, byte[] bytesFromInputStream, ResponseDefinitionBuilder responseDefBuilder) {


        int totalDuration = totalTimeApproximatingRate(bps, bytesFromInputStream.length);
        int numberOfChunks = numberOf1KBChunks(bytesFromInputStream.length);
        responseDefBuilder = responseDefBuilder.withChunkedDribbleDelay(numberOfChunks, totalDuration);
        System.out.println(":::BITRATE delay " + totalDuration + " " + bps + " 1k chunbks " + numberOfChunks);
        return responseDefBuilder;
    }


    private static int numberOf1KBChunks(int length) {
        return length / 1024;
    }

    private static int totalTimeApproximatingRate(int bps, int inputStreamLength) {
        double timeForOneBytePerSecond = 1.0 / (bps / 8.0);
        int durationInSeconds = (int) (ceil(inputStreamLength * timeForOneBytePerSecond));
        return durationInSeconds * 1000;
    }


    public static void registerStubsForDashAsset(WireMockRule rule, String assetName) {
        try {
            AssetManager assetManager = InstrumentationRegistry.getContext().getAssets();
            List<String> assetNamesForPath = getAssetNamesForPath(assetName, assetManager);

            for (String name : assetNamesForPath) {
                InputStream inputStream = assetManager.open(name);
                byte[] bytesFromInputStream = getBytesFromInputStream(inputStream);
                ResponseDefinitionBuilder responseDefBuilder = aResponse().withStatus(200).withBody(bytesFromInputStream);
                rule.stubFor(get(urlEqualTo("/" + name)).willReturn(responseDefBuilder));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @NonNull
    private static List<String> getAssetNamesForPath(String path, AssetManager assetManager) throws IOException {
        ArrayList<String> arrayList = new ArrayList<>();
        String[] assetNameList = assetManager.list(path);

        for (String assetName : assetNameList) {
            String fullPathName = path + "/" + assetName;
            String[] list = assetManager.list(fullPathName);

            if (list.length > 0) {
                arrayList.addAll(getAssetNamesForPath(fullPathName, assetManager));
            } else {
                arrayList.add(fullPathName);
            }
        }
        return arrayList;
    }

    @NonNull
    public static byte[] getBytesFromInputStream(InputStream is) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        byte[] buffer = new byte[0xFFFF];
        for (int len = is.read(buffer); len != -1; len = is.read(buffer))
            os.write(buffer, 0, len);

        return os.toByteArray();
    }
}