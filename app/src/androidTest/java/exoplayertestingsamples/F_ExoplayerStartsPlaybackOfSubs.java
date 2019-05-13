package exoplayertestingsamples;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.SystemClock;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.android.exoplayer2.ui.SubtitleView;

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

public class F_ExoplayerStartsPlaybackOfSubs {

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

        ViewGroup subsViewContainer = createSubsViewInActivity();

        player = new PlayerFactory(context).playerForUrlWithSubs("http://localhost:8080/redGreenVideo/redGreenOnlyVideo.mpd","http://localhost:8080/subs/some.ttml");

        player.attachViewGroupForSubs(subsViewContainer);

        CapturingPlayerStateListener playerStateListener = new CapturingPlayerStateListener();

        player.addStateListener(playerStateListener);

        playerStateListener.awaitReady();

        player.play();

        SystemClock.sleep(1_000); //wait subs to go red

        int sampledColour;

        int RED=0xff_ff_00_00; //aRGB
        sampledColour = getColourFromMiddleOfView(subsViewContainer);
        assertThat(sampledColour, is(RED));

        SystemClock.sleep(3_000); //wait subs to go green

        int GREEN= 0xff_00_ff_00; //aRGB
        sampledColour = getColourFromMiddleOfView(subsViewContainer);
        assertThat(sampledColour, is(GREEN));
    }

    private ViewGroup createSubsViewInActivity() {
        final Activity activity = activityTestRule.getActivity();

        final FrameLayout mainLayoutView = new FrameLayout(activity); // this could be your players main view

        final FrameLayout subsContainer = new FrameLayout(activity); // and this is the hole that you want exoplayer to render subs into
        subsContainer.setLayoutParams(new ViewGroup.LayoutParams(100, 100));
        mainLayoutView.addView(subsContainer);

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(mainLayoutView);
            }
        });

        return subsContainer;
    }

    private int getColourFromMiddleOfView(View targetView) {
        if (targetView != null) {


            int height = targetView.getHeight();
            int width = targetView.getWidth();
            Bitmap bitmap = Bitmap.createBitmap(width,
                    height,
                    Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(bitmap);
            targetView.draw(c);

            if(bitmap!=null) {
                int pixel = bitmap.getPixel(width/2, height/2);
                return pixel;
            }
        }
        return 0;
    }
}
