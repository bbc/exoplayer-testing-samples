package exoplayertestingsamples;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.os.SystemClock;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import player.CapturingPlayerStateListener;
import player.Player;
import player.PlayerFactory;
import wiremockextensions.FileSourceAndroidAssetFolder;
import wiremockextensions.WireMockStaticFileFromRequestPathTransformer;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class D_ExoplayerStartsPlaybackOfVideo {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig()
            .fileSource(new FileSourceAndroidAssetFolder(InstrumentationRegistry.getContext(), "streams"))
            .extensions(new WireMockStaticFileFromRequestPathTransformer())

    );

    @Rule
    public ActivityTestRule<Activity> activityTestRule = new ActivityTestRule<>(Activity.class);
    private Player player;

    @Before
    public void
    enableMappingsForStream() {
       // WiremockTestSupport.registerStubsForDashAsset(wireMockRule, "redGreenVideo");
        WireMock.stubFor(get(anyUrl())
                .willReturn(aResponse()
                        .withTransformers("static-file-from-path")
                ));
    }

    @Test
    public void
    rendersPixelsToTextureView() {
        final Context context = InstrumentationRegistry.getContext();

        player = new PlayerFactory(context).playerForUrl("http://localhost:8080/redGreenVideo/redGreenVideo.mpd");

        CapturingPlayerStateListener playerStateListener = new CapturingPlayerStateListener();

        player.addStateListener(playerStateListener);

        playerStateListener.awaitReady();

        TextureView textureView = createTextureViewInActivity();

        player.play();

        SystemClock.sleep(7_000); //wait for playback to advance into the green section

        int colour = getColour(textureView);

        int GREEN=-16609790;
        int RED=-55807;
        assertThat(colour, is(GREEN));
    }


    public int getColour(TextureView textureView) {
        if (textureView != null) {
            Bitmap bitmap = textureView.getBitmap();
            if(bitmap!=null) {
                return bitmap.getPixel(100, 100);
            }
        }
        return 0;
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
