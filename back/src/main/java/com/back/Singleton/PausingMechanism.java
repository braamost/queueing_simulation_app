package com.back.Singleton;

import java.util.concurrent.atomic.AtomicBoolean;

public class PausingMechanism {
    private AtomicBoolean paused = new AtomicBoolean(false);
    private final Object lock = new Object();
    private static volatile PausingMechanism pausingMechanism;

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
    public void resume (){
        synchronized (lock){
            paused.set(false);
            lock.notifyAll();
        }
    }
    public void checkPaused() throws InterruptedException {
        synchronized (lock) {
            while (paused.get()) {
                lock.wait();
            }
        }
    }

    public boolean isPaused() { return paused.get(); }
}
