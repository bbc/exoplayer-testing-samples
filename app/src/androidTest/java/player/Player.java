package player;

import android.view.Surface;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.analytics.AnalyticsListener;
import com.google.android.exoplayer2.analytics.DefaultAnalyticsListener;
import com.google.android.exoplayer2.source.MediaSourceEventListener;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.google.android.exoplayer2.C.TRACK_TYPE_VIDEO;

public class Player {

    private final EventListenerAdapter listener;
    private final SimpleExoPlayer exoplayer;
    private List<PlayerStateListener> listeners;

    Player(SimpleExoPlayer exoplayer) {
        this.exoplayer = exoplayer;
        listeners = new CopyOnWriteArrayList<>();
        listener = new EventListenerAdapter(listeners);
        exoplayer.addListener(listener);
        exoplayer.addAnalyticsListener(new DefaultAnalyticsListener() {
            @Override
            public void onDownstreamFormatChanged(EventTime eventTime, MediaSourceEventListener.MediaLoadData mediaLoadData) {
                if(mediaLoadData.trackType == TRACK_TYPE_VIDEO) {
                    for (PlayerStateListener playerStateListener : listeners) {
                        playerStateListener.videoBitrate(mediaLoadData.trackFormat.bitrate);
                    }
                }
            }
        });
    }

    public void addStateListener(PlayerStateListener playerStateListener) {
        listeners.add(playerStateListener);

    }

    public void play() {
        exoplayer.setPlayWhenReady(true);
    }

    public void attachSurface(Surface surface) {
        exoplayer.setVideoSurface(surface);
    }

    public void release() {
        exoplayer.release();
    }

    private static class EventListenerAdapter extends com.google.android.exoplayer2.Player.DefaultEventListener {
        private final List<PlayerStateListener> listeners;

        public EventListenerAdapter(List<PlayerStateListener> listeners) {

            this.listeners = listeners;
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            for (PlayerStateListener listener : listeners) {
                if(playbackState==com.google.android.exoplayer2.Player.STATE_READY) {
                    listener.ready();
                }
            }
        }
    }
}
