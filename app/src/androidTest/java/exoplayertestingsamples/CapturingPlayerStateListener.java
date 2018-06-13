package exoplayertestingsamples;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import player.PlayerStateListener;

class CapturingPlayerStateListener implements PlayerStateListener {
    public static final String READY = "READY";

    private String lastObservedState = "UNKNOWN";

    private CountDownLatch latch = new CountDownLatch(1);

    @Override
    public void ready() {
        lastObservedState = "READY";
        latch.countDown();
    }

    public String awaitReady() {
        try {
            latch.await(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return lastObservedState;
    }
}
