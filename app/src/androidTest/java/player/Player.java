package player;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Player {

    private final EventListenerAdapter listener;
    private List<PlayerStateListener> listeners;

    Player(ExoPlayer exoplayer) {
        listeners = new CopyOnWriteArrayList<>();
        listener = new EventListenerAdapter(listeners);
        exoplayer.addListener(listener);
    }

    public void addStateListener(PlayerStateListener playerStateListener) {
        listeners.add(playerStateListener);

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
            System.out.printf(String.valueOf(error.type));
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
