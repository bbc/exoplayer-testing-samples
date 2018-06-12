package exoplayertestingsamples;

import android.Manifest;
import android.os.SystemClock;
import android.support.test.rule.GrantPermissionRule;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;

public class JustWireMock {

    @Rule
    public WireMockRule rule = new WireMockRule();

    @Rule
    public GrantPermissionRule mRuntimePermissionRule
            = GrantPermissionRule.grant(Manifest.permission.INTERNET);



    @Test
    public void
    isConfiguredCorrectlyUsingTheWireMockRule()
    {
        //Make the mock webserver respond with OK 200 for every request it receives    (the same as writing aResponse().withStatus(200) )
        WireMock.stubFor(WireMock.get(anyUrl()).willReturn(WireMock.ok()));

        //we send a request to the mock webserver
        makeHTTPRequestTo("http://localhost:"+rule.port() + "/testingIsFun");

        //wait a bit
        SystemClock.sleep(2000);

        //and check we got the request, any request in fact
        WireMock.verify(WireMock.getRequestedFor(anyUrl()));
    }

    private void makeHTTPRequestTo(String urlString) {
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) new URL(urlString).openConnection();
            urlConnection.connect();
            urlConnection.getResponseCode();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) urlConnection.disconnect();
        }
    }

}
