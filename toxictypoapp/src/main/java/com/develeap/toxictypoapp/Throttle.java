package com.develeap.toxictypoapp;

public class Throttle {
    private static final int NUM_CALLS = 30;
    private static final long FAST = NUM_CALLS*50;
    private static final long SLOW = NUM_CALLS*120;

    private long[] calls = new long[NUM_CALLS];
    private int lastCallIndex=0;
    private int throttleLevel=0;

    public void throttle() {
        long now = System.currentTimeMillis();
        calls[lastCallIndex]=now;
        lastCallIndex = nextCallIndex();
        if (now - calls[lastCallIndex] < FAST) {
            // We got called at a rate of more than 1 per ms!
            // Lets throttle
            throttleLevel+=5;
        }
        if (now - calls[lastCallIndex] > SLOW) {
            throttleLevel = 0;
        }
        if (throttleLevel>0) {
            try {
                Thread.sleep(throttleLevel);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private int nextCallIndex() {
        return (lastCallIndex+1)%NUM_CALLS;
    }
}
