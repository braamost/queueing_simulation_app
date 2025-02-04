export const transformData = (machines, queues, connections, products, startId) => {
    // Step 1: Create a map of queue IDs to their connected machine IDs
    const queueToMachinesMap = new Map();
  
    connections.forEach((connection) => {
      if (connection.from.startsWith("Q")) {
        // Connection from a queue to a machine
        const queueId = connection.from;
        const machineId = connection.to;
  
        if (!queueToMachinesMap.has(queueId)) {
          queueToMachinesMap.set(queueId, []);
        }
        queueToMachinesMap.get(queueId).push(machineId);
      }
    });
  
    // Step 2: Create the queues array
    const transformedQueues = queues.map((queue) => ({
      id: queue.id,
      nProcesses: queue.id === startId ? products : 0, // starting number of processes
      machineIds: queueToMachinesMap.get(queue.id) || [], // Get connected machines
    }));
  
    // Step 3: Create the machines array
    const transformedMachines = machines.map((machine) => ({
      id: machine.id,
      queueId: connections.find((conn) => conn.from === machine.id)?.to || "", // Find the queue connected to this machine
    }));
  
    // Step 4: Return the transformed data
    return {
      queues: transformedQueues,
      machines: transformedMachines,
    };
  };