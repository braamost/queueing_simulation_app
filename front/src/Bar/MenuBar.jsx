import { useRef, useCallback, useState, useEffect } from "react";
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
        backgroundColor: machine.backgroundColor || (isSelected ? "purple" : "rgba(115, 230, 8, 0.9)"),
      }}
      onClick={() => onClick(machine)}
      data-id={machine.id}
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
      data-id={queue.id}
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
  const [replayMode, setReplayMode] = useState(false);
  const [nextMachineId, setNextMachineId] = useState(1); // Start from 1
  const [nextQueueId, setNextQueueId] = useState(1); // Start from 1

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
            const { red = 115, green = 230, blue = 8 } = color || {};
            newMachines[machineIndex] = {
              ...newMachines[machineIndex],
              backgroundColor: `rgba(${red}, ${green}, ${blue}, ${0.9})`,
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
      try {
        await processMessage(message);
      } catch (error) {
        console.error("Error processing message:", error);
      }
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
      if (message.type === "endReplay") {
        setReplayMode(false);
        return;
      }
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
      id: `M${nextMachineId}`,
      type: "machine",
      connect: 0,
      backgroundColor: null,
      x: 50 + 75 * machines.length,
      y: 50,
    };
    setMachines([...machines, newMachine]);
    setNextMachineId(nextMachineId + 1);
  };

  const handleAddQueue = () => {
    if (simulationStarted) return;
    const newQueue = {
      id: `Q${nextQueueId}`,
      type: "queue",
      connect: 0,
      x: 50 + 75 * queues.length,
      y: 150,
      processes: 0,
    };
    setQueues([...queues, newQueue]);
    setNextQueueId(nextQueueId + 1);
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
    if (simulationStarted) return;

    // If no object is selected, show a message
    if (!selectedStart && !selectedEnd) {
      alert("Please select an object to delete.");
      return;
    }

    // Confirm deletion
    const confirmDelete = window.confirm("Are you sure you want to delete the selected object(s)?");
    if (!confirmDelete) return;

    // Add deletion animation
    const objectsToDelete = [selectedStart, selectedEnd].filter(Boolean);
    objectsToDelete.forEach(obj => {
      const element = document.querySelector(`.draggable-${obj.id.startsWith("M") ? "machine" : "queue"}[data-id="${obj.id}"]`);
      if (element) {
        element.classList.add("deleting");
      }
    });

    // Wait for the animation to complete before deleting
    setTimeout(() => {
      const objectIds = objectsToDelete.map(obj => obj.id);
      setMachines(prev => prev.filter(m => !objectIds.includes(m.id)));
      setQueues(prev => prev.filter(q => !objectIds.includes(q.id)));
      setConnections(prev => prev.filter(conn => 
        !objectIds.includes(conn.from) && !objectIds.includes(conn.to)
      ));
      resetSelection();
    }, 300); // Match the animation duration
  };

  const handleClearAll = () => {
    const confirmClear = window.confirm("Are you sure you want to clear everything?");
    if (!confirmClear) return;

    setMachines([]);
    setQueues([]);
    setConnections([]);
    setNextMachineId(1);
    setNextQueueId(1);
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

  const handleStop = async () => {
    await sendJsonMessage({ type: "STOP_SIMULATION" });
    messageQueueRef.current = [];
    isProcessingRef.current = false;
    setTimeout(() => {
      setMachines(machines => machines.map(machine => ({
        ...machine,
        backgroundColor: `rgba(255, 230, 0, 0.6)`,
      })));
      setQueues(queues => queues.map(queue => ({
        ...queue,
        processes: 0
      })));
      setSimulationStarted(false);
    }, 100);
  };

  const handlePause = () => {
    sendJsonMessage({ type: "PAUSE_SIMULATION" });
    setIsPaused(true);
  };

  const handleResume = () => {
    sendJsonMessage({ type: "RESUME_SIMULATION" });
    setIsPaused(false);
  };

  const handleReplay = () => {
    if (replayMode) {
      sendJsonMessage({ type: "END_REPLAY" });
      setReplayMode(false);
    } else {
      sendJsonMessage({ type: "REPLAY_SIMULATION" });
      setReplayMode(true);
    }
  };

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

  // Keyboard shortcuts
  useEffect(() => {
    const handleKeyDown = (e) => {
      if (e.key === "Delete") {
        handleDelete();
      } else if (e.key === "Escape") {
        resetSelection();
      }
    };

    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [handleDelete, resetSelection]);

  return (
    <div className="menubar">
      <div className="menubar-buttons">
        <Buttons
          onAddMachine={handleAddMachine}
          onAddQueue={handleAddQueue}
          onConnect={handleConnect}
          onDelete={handleDelete}
          onClearAll={handleClearAll}
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
          onReplay={handleReplay}
          replayMode={replayMode}
        />
      </div>

      <div className={`simulation-container ${replayMode ? "replay-mode" : ""}`}>
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