package testSupport;

import android.os.SystemClock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import player.PlayerStateListener;

public class CapturingPlayerStateListener implements PlayerStateListener {
    public static final String READY = "READY";

    private String lastObservedState = "UNKNOWN";

    private CountDownLatch readyLatch = new CountDownLatch(1);
    private int lastObservedBitrate = -1;
    private List<Integer> bitrateJournal = new ArrayList<>();

    @Override
    public void ready() {
        lastObservedState = "READY";
        readyLatch.countDown();
    }

    @Override
    public void videoBitrate(int bitrate) {
        System.out.println("::BITRATE" + bitrate);
        this.lastObservedBitrate = bitrate;
        this.bitrateJournal.add(bitrate);
    }

    public String awaitReady() {
        try {
            readyLatch.await(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return lastObservedState;
    }

    public int lastObservedBitrate() {
        return lastObservedBitrate;
    }

    public List<Integer> bitrateJournal() {
        return bitrateJournal;
    }

    public int awaitForBitrateWithTimeout(int minBitrate, int timeInterval, TimeUnit units) {
        while(lastObservedBitrate<minBitrate) {
            SystemClock.sleep(100);
        }

        return lastObservedBitrate;
    }
}
