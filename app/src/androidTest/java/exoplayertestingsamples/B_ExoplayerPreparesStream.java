package exoplayertestingsamples;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import player.Player;
import player.PlayerFactory;
import wiremockextensions.WiremockTestSupport;

import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;


public class B_ExoplayerPreparesStream {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig());

    @Before
    public void
    enableMappingsForStream() {
        WiremockTestSupport.registerStubsForDashAsset(wireMockRule, "audio50hz");
    }


    @Test
    public void
    requestsMPDAndSignalsReady() {
        Context context = InstrumentationRegistry.getContext();
        Player player = new PlayerFactory(context).playerForUrl("http://localhost:8080/audio50hz/audio50hz.mpd");

        CapturingPlayerStateListener playerStateListener = new CapturingPlayerStateListener();

        player.addStateListener(playerStateListener);


        String playerState = playerStateListener.awaitReady();

        assertThat(playerState, is(CapturingPlayerStateListener.READY));
        verify(getRequestedFor(urlEqualTo("/audio50hz/audio50hz.mpd")));

    }


}


