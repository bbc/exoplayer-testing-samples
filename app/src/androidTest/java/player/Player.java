package player;

import android.view.Surface;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Player {

    private final EventListenerAdapter listener;
    private final SimpleExoPlayer exoplayer;
    private List<PlayerStateListener> listeners;

    Player(SimpleExoPlayer exoplayer) {
        this.exoplayer = exoplayer;
        listeners = new CopyOnWriteArrayList<>();
        listener = new EventListenerAdapter(listeners);
        exoplayer.addListener(listener);
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

    private static class EventListenerAdapter implements com.google.android.exoplayer2.Player.EventListener {
        private final List<PlayerStateListener> listeners;

        public EventListenerAdapter(List<PlayerStateListener> listeners) {

            this.listeners = listeners;
        }

        @Override
        public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {

        }

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

        }

        @Override
        public void onLoadingChanged(boolean isLoading) {

        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            for (PlayerStateListener listener : listeners) {
                if(playbackState==com.google.android.exoplayer2.Player.STATE_READY) {
                    listener.ready();
                }
            }

        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {

        }

        @Override
        public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
        }

        @Override
        public void onPositionDiscontinuity(int reason) {

        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

        }

        @Override
        public void onSeekProcessed() {

        }
    }
}
