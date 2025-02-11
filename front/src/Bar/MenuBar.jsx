import { useRef, useCallback, useState } from "react";
import Draggable from "react-draggable";
import useWebSocket from "react-use-websocket";
import Buttons from "../Buttons/Buttons";
import { transformData } from "./TransformData";
import "./MenuBar.css";

const WS_URL = "ws://localhost:8080/simulation";

// Separate component for draggable machines
const DraggableMachine = ({ machine, onDragStop, onClick, isSelected }) => (
  <Draggable
    position={{ x: machine.x, y: machine.y }}
    onStop={(e, data) => onDragStop(machine.id, data)}
  >
    <div
      className={`draggable-machine ${isSelected ? 'selected' : ''}`}
      style={{
        backgroundColor: machine.backgroundColor || (isSelected ? "purple" : "rgba(143, 143, 143, 0.6)"),
      }}
      onClick={() => onClick(machine)}
    >
      {machine.id}
    </div>
  </Draggable>
);

// Separate component for draggable queues
const DraggableQueue = ({ queue, onDragStop, onClick, isSelected }) => (
  <Draggable
    position={{ x: queue.x, y: queue.y }}
    onStop={(e, data) => onDragStop(queue.id, data)}
  >
    <div
      className={`draggable-queue ${isSelected ? 'selected' : ''}`}
      style={{
        backgroundColor: isSelected ? "purple" : "rgba(0, 141, 184, 0.96)",
      }}
      onClick={() => onClick(queue)}
    >
      {`${queue.id}: ${queue.processes || 0}`}
    </div>
  </Draggable>
);

// Separate component for connections
const ConnectionLines = ({ connections, machines, queues }) => (
  <svg className="connections-container">
    {connections.map((connection, index) => {
      const fromObject = [...machines, ...queues].find(
        obj => obj.id === connection.from
      );
      const toObject = [...machines, ...queues].find(
        obj => obj.id === connection.to
      );

      if (!fromObject || !toObject) return null;

      const angle = Math.atan2(
        toObject.y - fromObject.y,
        toObject.x - fromObject.x
      ) * (180 / Math.PI);

      return (
        <g key={index} className="connection-line">
          <line
            x1={fromObject.x + 30}
            y1={fromObject.y + 30}
            x2={toObject.x + 30}
            y2={toObject.y + 30}
          />
          <polygon
            points={`${toObject.x + 30},${toObject.y + 30} 
                    ${toObject.x + 20},${toObject.y + 25} 
                    ${toObject.x + 20},${toObject.y + 35}`}
            transform={`rotate(${angle}, ${toObject.x + 30}, ${toObject.y + 30})`}
          />
        </g>
      );
    })}
  </svg>
);

function MenuBar() {
  // State management
  const [products, setProducts] = useState(0);
  const [machines, setMachines] = useState([]);
  const [queues, setQueues] = useState([]);
  const [connections, setConnections] = useState([]);
  const [simulationStarted, setSimulationStarted] = useState(false);
  const [selectedStart, setSelectedStart] = useState(null);
  const [selectedEnd, setSelectedEnd] = useState(null);
  const [shouldReconnect, setShouldReconnect] = useState(true);
  const [isPaused, setIsPaused] = useState(false);

  // Message queue management
  const messageQueueRef = useRef([]);
  const isProcessingRef = useRef(false);

  // WebSocket message processing
  const processMessage = useCallback(async (message) => {
    const { machineStates, queueStates } = message;

    if (machineStates) {
      setMachines(prevMachines => {
        const newMachines = [...prevMachines];
        Object.entries(machineStates).forEach(([_, { id, color }]) => {
          const machineIndex = newMachines.findIndex(m => m.id === id);
          if (machineIndex !== -1) {
            const { red = 143, green = 143, blue = 143 } = color || {};
            newMachines[machineIndex] = {
              ...newMachines[machineIndex],
              backgroundColor: `rgba(${red}, ${green}, ${blue}, ${0.6})`,
            };
          }
        });
        return newMachines;
      });
    }

    if (queueStates) {
      setQueues(prevQueues => {
        const newQueues = [...prevQueues];
        Object.entries(queueStates).forEach(([_, { id, processCount }]) => {
          const queueIndex = newQueues.findIndex(q => q.id === id);
          if (queueIndex !== -1) {
            newQueues[queueIndex] = {
              ...newQueues[queueIndex],
              processes: processCount
            };
          }
        });
        return newQueues;
      });
    }
  }, []);

  const processMessageQueue = useCallback(async () => {
    if (isProcessingRef.current) return;
    isProcessingRef.current = true;

    while (messageQueueRef.current.length > 0) {
      const message = messageQueueRef.current.shift();
      await processMessage(message);
      // await new Promise(resolve => setTimeout(resolve, 0));
    }

    isProcessingRef.current = false;
  }, [processMessage]);

  // WebSocket setup
  const { sendJsonMessage } = useWebSocket(WS_URL, {
    onOpen: () => console.log("WebSocket connection established."),
    onClose: () => {
      console.log("WebSocket connection closed.");
      if (shouldReconnect) {
        setTimeout(() => setShouldReconnect(true), 1000);
      }
    },
    onError: (error) => console.error("WebSocket error:", error),
    onMessage: (event) => {
      const message = JSON.parse(event.data);
      messageQueueRef.current.push(message);
      processMessageQueue();
    },
    shouldReconnect: () => shouldReconnect,
    reconnectAttempts: 10,
    reconnectInterval: 1000,
  });

  // Action handlers
  const handleAddMachine = () => {
    if (simulationStarted) return;
    const newMachine = {
      id: `M${machines.length + 1}`,
      type: "machine",
      connect: 0,
      backgroundColor: null,
      x: 50 + 75 * machines.length,
      y: 50,
    };
    setMachines([...machines, newMachine]);
  };

  const handleAddQueue = () => {
    if (simulationStarted) return;
    const newQueue = {
      id: `Q${queues.length + 1}`,
      type: "queue",
      connect: 0,
      x: 50 + 75 * queues.length,
      y: 150,
      processes: 0,
    };
    setQueues([...queues, newQueue]);
  };

  const handleConnect = () => {
    if (simulationStarted || !selectedStart || !selectedEnd) return;

    const isSameType = (
      (selectedStart.id.startsWith("M") && selectedEnd.id.startsWith("M")) ||
      (selectedStart.id.startsWith("Q") && selectedEnd.id.startsWith("Q"))
    );

    if (isSameType) {
      alert("Cannot connect two machines or two queues.");
      resetSelection();
      return;
    }

    if (selectedStart.id.startsWith("M") && selectedStart.connect !== 0) {
      alert("Cannot connect a machine with more than one queue.");
      resetSelection();
      return;
    }

    const newConnection = {
      from: selectedStart.id,
      to: selectedEnd.id,
    };

    selectedStart.connect++;
    setConnections([...connections, newConnection]);
    resetSelection();
  };

  const handleDelete = () => {
    if (simulationStarted || (!selectedEnd && !selectedStart)) return;

    const objectsToDelete = [selectedStart, selectedEnd].filter(Boolean);
    const objectIds = objectsToDelete.map(obj => obj.id);

    setMachines(prevMachines => 
      prevMachines.filter(machine => !objectIds.includes(machine.id))
    );

    setQueues(prevQueues => 
      prevQueues.filter(queue => !objectIds.includes(queue.id))
    );

    setConnections(prevConnections => 
      prevConnections.filter(conn => 
        !objectIds.includes(conn.from) && !objectIds.includes(conn.to)
      )
    );

    // Update machine connections
    objectsToDelete.forEach(obj => {
      if (obj?.id.startsWith("Q")) {
        machines.forEach(machine => {
          if (connections.some(conn => 
            conn.from === machine.id && conn.to === obj.id
          )) {
            machine.connect--;
          }
        });
      }
    });

    resetSelection();
  };

  const handleStartSim = () => {
    const initData = transformData(machines, queues, connections);
    sendJsonMessage({
      type: "INIT_SIMULATION",
      data: initData,
    });
    setSimulationStarted(true);
  };

  const handleStop = () => {
    sendJsonMessage({ type: "STOP_SIMULATION" });
    setSimulationStarted(false);
  };

  const handlePause = () => {
    sendJsonMessage({ type: "PAUSE_SIMULATION" });
    setIsPaused(true);
  }
  const handleResume = () => {
    sendJsonMessage({ type: "RESUME_SIMULATION" });
    setIsPaused(false);
  }

  const handleObjectClick = (object) => {
    if (!selectedStart) {
      setSelectedStart(object);
    } else if (!selectedEnd && object !== selectedStart) {
      setSelectedEnd(object);
    } else {
      resetSelection();
    }
  };

  const resetSelection = () => {
    setSelectedStart(null);
    setSelectedEnd(null);
  };

  return (
    <div className="menubar">
      <div className="menubar-buttons">
        <Buttons
          onAddMachine={handleAddMachine}
          onAddQueue={handleAddQueue}
          onConnect={handleConnect}
          onDelete={handleDelete}
          onStartSim={handleStartSim}
          onStop={handleStop}
          products={products}
          setProducts={setProducts}
          simulationStarted={simulationStarted}
          startingId={selectedStart}
          setStartingId={setSelectedStart}
          sendJsonMessage={sendJsonMessage}
          onPause={handlePause}
          onResume={handleResume}
          isPaused={isPaused}
        />
      </div>

      <div className="simulation-container">
        {machines.map(machine => (
          <DraggableMachine
            key={machine.id}
            machine={machine}
            onDragStop={(id, data) => {
              setMachines(prev => prev.map(m => 
                m.id === id ? { ...m, x: data.x, y: data.y } : m
              ));
            }}
            onClick={handleObjectClick}
            isSelected={machine === selectedStart || machine === selectedEnd}
          />
        ))}

        {queues.map(queue => (
          <DraggableQueue
            key={queue.id}
            queue={queue}
            onDragStop={(id, data) => {
              setQueues(prev => prev.map(q => 
                q.id === id ? { ...q, x: data.x, y: data.y } : q
              ));
            }}
            onClick={handleObjectClick}
            isSelected={queue === selectedStart || queue === selectedEnd}
          />
        ))}

        <ConnectionLines 
          connections={connections}
          machines={machines}
          queues={queues}
        />
      </div>
    </div>
  );
}

export default MenuBar;