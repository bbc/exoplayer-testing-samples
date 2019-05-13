package player;

import android.content.Context;
import android.net.Uri;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.SingleSampleMediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.MimeTypes;

public class PlayerFactory {
    private Context context;

    public PlayerFactory(Context context) {
        this.context = context;
    }

    public Player playerForUrl(String s) {
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter.Builder()
                                                            .setInitialBitrateEstimate(86000)
                                                            .build();
        SimpleExoPlayer exoplayer = createExoplayer(bandwidthMeter);
        MediaSource dashDataSource = createDashDataSource(s,bandwidthMeter);

        Player player = new Player(exoplayer);

        exoplayer.prepare(dashDataSource);

        return player;
    }


    public Player playerForUrlWithSubs(String mpdUrl, String subtitleUri) {
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter.Builder()
                .setInitialBitrateEstimate(86000)
                .build();
        SimpleExoPlayer exoplayer = createExoplayer(bandwidthMeter);
        MediaSource dashDataSource = createDashDataSource(mpdUrl,bandwidthMeter);


        // The subtitle language. May be null.
        MediaSource subtitleSource = createSubsDataSource(subtitleUri, bandwidthMeter);

        // Plays the video with the sideloaded subtitle.
        MergingMediaSource mergedSource =
                new MergingMediaSource(dashDataSource, subtitleSource);

        Player player = new Player(exoplayer);

        exoplayer.prepare(mergedSource);

        return player;
    }


    private MediaSource createSubsDataSource(String subtitleUri, DefaultBandwidthMeter defaultBandwidthMeter) {
        DefaultHttpDataSourceFactory dataSourceFactory = new DefaultHttpDataSourceFactory("integrationtests",defaultBandwidthMeter);

        SingleSampleMediaSource.Factory factory = new SingleSampleMediaSource.Factory(dataSourceFactory);
        Uri uri = Uri.parse(subtitleUri);

        Format subtitleFormat = Format.createTextSampleFormat(
                null,
                MimeTypes.APPLICATION_TTML,
                C.SELECTION_FLAG_DEFAULT, // Selection flags for the track.
                null);

        return factory.createMediaSource(uri, subtitleFormat, C.TIME_UNSET);
    }


    private MediaSource createDashDataSource(String s, DefaultBandwidthMeter defaultBandwidthMeter) {
        DefaultHttpDataSourceFactory dataSourceFactory = new DefaultHttpDataSourceFactory("integrationtests",defaultBandwidthMeter);
        DefaultDashChunkSource.Factory chunkSourceFactory = new DefaultDashChunkSource.Factory(dataSourceFactory);
        Uri uri = Uri.parse(s);
        DashMediaSource.Factory factory = new DashMediaSource.Factory(chunkSourceFactory, dataSourceFactory);
        return factory.createMediaSource(uri);
    }

    private SimpleExoPlayer createExoplayer(DefaultBandwidthMeter defaultBandwidthMeter) {

        BandwidthMeter bandwidthMeter = defaultBandwidthMeter;
        AdaptiveTrackSelection.Factory factory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        DefaultTrackSelector defaultTrackSelector = new DefaultTrackSelector(factory);

        return ExoPlayerFactory.newSimpleInstance(context,defaultTrackSelector);
    }
}
