package exoplayertestingsamples;

import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import player.Player;
import player.PlayerFactory;
import testSupport.CapturingPlayerStateListener;
import wiremockextensions.FileSourceAndroidAssetFolder;
import wiremockextensions.WireMockStaticFileFromRequestPathTransformerWithChunkedDelay;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

public class E_ABRsThroughRepresentations {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig()
            .fileSource(new FileSourceAndroidAssetFolder(InstrumentationRegistry.getContext(), "streams"))
            .extensions(new WireMockStaticFileFromRequestPathTransformerWithChunkedDelay())
    );


    @Rule
    public ActivityTestRule<Activity> activityTestRule = new ActivityTestRule<>(Activity.class);
    private Player player;

    @Before
    public void
    enableMappingsForStream() {
        WireMock.stubFor(get(anyUrl())
                .willReturn(aResponse()
                        .withTransformers("static-file-from-path-with-delay")
                ));
    }


    @Test
    public void
    reportsBitrate() {
        final Context context = InstrumentationRegistry.getContext();
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                player = new PlayerFactory(context).playerForUrl("http://localhost:8080/bigbuckbunny/bigbuckbunny.mpd");
            }
        });

        CapturingPlayerStateListener playerStateListener = new CapturingPlayerStateListener();

        player.addStateListener(playerStateListener);

        playerStateListener.awaitReady();

        TextureView textureView = createTextureViewInActivity();

        player.play();

        playerStateListener.awaitReady();

        int threeStepsOnTheABRLadder_threeSegmentsAtEachStep_sevenSecondsLong = 3 * 3 * 7 * 1_000;

        int lastObservedBitrate = playerStateListener.awaitForBitrateWithTimeout(281000, threeStepsOnTheABRLadder_threeSegmentsAtEachStep_sevenSecondsLong, TimeUnit.SECONDS);

        assertThat("Expecting to have stepped up through some of the representations", playerStateListener.bitrateJournal(), contains(86000, 156000, 281000));

    }

    @After
    public void release() {
        player.release();
    }

    private TextureView createTextureViewInActivity() {
        final Activity activity = activityTestRule.getActivity();

        final TextureView textureView = new TextureView(activity);
        textureView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        textureView.setDrawingCacheEnabled(true);
        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(final SurfaceTexture surface, int width, int height) {
                player.attachSurface(new Surface(textureView.getSurfaceTexture()));

            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });


        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(textureView);
            }
        });

        if (textureView.isAvailable()) {
            player.attachSurface(new Surface(textureView.getSurfaceTexture()));
        }
        return textureView;
    }

}
