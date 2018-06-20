package exoplayertestingsamples;

import android.content.Context;
import android.media.audiofx.Visualizer;
import android.os.SystemClock;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.GrantPermissionRule;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import player.Player;
import player.PlayerFactory;
import player.CapturingPlayerStateListener;
import wiremockextensions.FileSourceAndroidAssetFolder;
import wiremockextensions.WireMockStaticFileFromRequestPathTransformer;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

public class C_ExoplayerStartsPlaybackOfAudio {

    @Rule  //to "sample" audio we need some special permissions, this rule from the android test support library makes this easy to request
    public GrantPermissionRule mRuntimePermissionRule
            = GrantPermissionRule.grant(android.Manifest.permission.RECORD_AUDIO,           //the use of the visualizer requires the permission android.permission.RECORD_AUDIO
            android.Manifest.permission.MODIFY_AUDIO_SETTINGS); //Creating a Visualizer on the output mix (audio session 0) requires permission Manifest.permission.MODIFY_AUDIO_SETTINGS


    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig()
            .fileSource(new FileSourceAndroidAssetFolder(InstrumentationRegistry.getContext(), "streams"))
            .extensions(new WireMockStaticFileFromRequestPathTransformer())

    );

    @Before
    public void
    enableMappingsForStream() {
        WireMock.stubFor(get(anyUrl())
                .willReturn(aResponse()
                        .withTransformers("static-file-from-path")
                ));
    }


    @Test
    public void
    startsAudioPlayback() {
        Context context = InstrumentationRegistry.getContext();
        Player player = new PlayerFactory(context).playerForUrl("http://localhost:8080/audio50hz/audio50hz.mpd");

        CapturingPlayerStateListener playerStateListener = new CapturingPlayerStateListener();

        player.addStateListener(playerStateListener);

        playerStateListener.awaitReady();

        Visualizer visualizer = new Visualizer(0); //the output mix (audio session 0)
        visualizer.setCaptureSize(8);
        visualizer.setEnabled(true);

        //play for a bit
        player.play();
        SystemClock.sleep(3000);

        byte[] output = new byte[visualizer.getCaptureSize()];
        visualizer.getFft(output);
        visualizer.setEnabled(false);


        long total = addUpTotalNumberOfFrequenciesHeardKindOf(output); // you could determine which bucket has 50hz in and confirm the peak is seen in that bucket
        assertThat(total, greaterThan(0L));

    }


    long addUpTotalNumberOfFrequenciesHeardKindOf(byte[] output) {
        long total = 0;

        for (byte b : output) {
            total += Math.abs(b);
        }

        return total;
    }


}
