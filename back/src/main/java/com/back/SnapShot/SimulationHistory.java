package com.back.SnapShot;

import com.back.DTO.SimulationStateDTO;

import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class SimulationHistory {
    private final Queue<SimulationMemento> history;
    private final Queue<SimulationMemento> tempHistory;

    public SimulationHistory() {
        this.history = new LinkedBlockingQueue<>();
        this.tempHistory = new LinkedBlockingQueue<>();
    }

    public void add(SimulationMemento memento) {
        // Create a deep copy of the state before adding
        SimulationStateDTO stateCopy = new SimulationStateDTO();
        stateCopy.setMachineStates(new HashMap<>(memento.getState().getMachineStates()));
        stateCopy.setQueueStates(new HashMap<>(memento.getState().getQueueStates()));
        stateCopy.setCurrentTime(memento.getState().getCurrentTime());
        stateCopy.setType(memento.getState().getType());
        history.add(new SimulationMemento(stateCopy));
    }

    public SimulationMemento get () {
        if(!history.isEmpty()) {
            SimulationMemento m = history.poll();
            tempHistory.add(m);
            return m;
        }else {
            Done();
            return null;
        }
    }

    public int size() {
        return history.size();
    }

    public void clear() {
        history.clear();
        tempHistory.clear();
    }

    private void Done() {
        history.addAll(tempHistory);
        tempHistory.clear();
    }

    public Queue<SimulationMemento> getHistory() {
        return history;
    }
}
