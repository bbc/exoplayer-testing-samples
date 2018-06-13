package wiremockextensions;

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

public class WiremockTestSupport {


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
    private static byte[] getBytesFromInputStream(InputStream is) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        byte[] buffer = new byte[0xFFFF];
        for (int len = is.read(buffer); len != -1; len = is.read(buffer))
            os.write(buffer, 0, len);

        return os.toByteArray();
    }
}