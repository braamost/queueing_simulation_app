package com.back.Singleton;

import java.util.concurrent.atomic.AtomicBoolean;

public class PausingMechanism {
    private final AtomicBoolean paused = new AtomicBoolean(false);
    private final Object lockPause = new Object();
    private final Object lockReplay = new Object();
    private static volatile PausingMechanism pausingMechanism;
    private final AtomicBoolean replaying = new AtomicBoolean(false);

    private PausingMechanism() {}

    public static PausingMechanism getInstance(){
        if(pausingMechanism == null)
            synchronized (PausingMechanism.class) {
                if(pausingMechanism == null){
                    pausingMechanism = new PausingMechanism();
                }
            }
        return pausingMechanism;
    }

    public void pause (){
        paused.set(true);
    }
    public void replay(){
        synchronized (lockReplay){
            replaying.set(true);
            lockReplay.notifyAll();
        }
    }
    public void stopReplay() {
        synchronized (lockReplay) {
            replaying.set(false);
            lockReplay.notifyAll(); // Wake up any waiting threads
        }
    }
    public void resume (){
        synchronized (lockPause){
            paused.set(false);
            lockPause.notifyAll();
        }
    }
    public void checkPaused() throws InterruptedException {
        synchronized (lockPause) {
            while (paused.get()) {
                lockPause.wait();
            }
        }
    }

    public boolean isPaused() { return paused.get(); }
    public boolean isReplaying() { return replaying.get(); }
}
