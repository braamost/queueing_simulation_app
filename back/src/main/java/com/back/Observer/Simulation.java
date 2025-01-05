package com.back.Observer;

import java.util.UUID;
import java.util.concurrent.*;

public class Simulation {
    public static void main(String[] args) {
        // Create queues
        Queue queue1 = new Queue(UUID.randomUUID());
        Queue queue2 = new Queue(UUID.randomUUID());

        // Create machines
        Machine machine1 = new Machine(UUID.randomUUID(), queue2);
        Machine machine2 = new Machine(UUID.randomUUID(), queue2);

        // Connect machines to queues
        queue1.connectMachine(machine1);
        queue1.connectMachine(machine2);

        // Add processes to the first queue
        for (int i = 0; i < 5; i++) {
            queue1.addProcess(new Process());
        }
    }
}
