import { useEffect, useState } from "react";
import "./MenuBar.css";
import Draggable from "react-draggable";
import Buttons from "../Buttons/Buttons.jsx";
import useWebSocket from "react-use-websocket";
import { transformData } from "./TransformData.jsx";

const WS_URL = "ws://localhost:8080/simulation"; // WebSocket server URL

function MenuBar() {
  const [products, setProducts] = useState(0);
  const [machines, setMachines] = useState([]);
  const [queues, setQueues] = useState([]);
  const [connections, setConnections] = useState([]);
  const [startingId, setStartingId] = useState(null);
  const [isConnectionMode, setIsConnectionMode] = useState(false);
  const [simulationStarted, setSimulationStarted] = useState(false);
  const [selectedObject, setSelectedObject] = useState(null); // ID of the selected object
  const [selectedStart, setSelectedStart] = useState(null); // ID of the start object
  const [selectedEnd, setSelectedEnd] = useState(null); // ID of the end object
  const [webMessage, setWebMessage] = useState(null);
  const [shouldReconnect, setShouldReconnect] = useState(true);

  // WebSocket hook
  const { sendJsonMessage } = useWebSocket(WS_URL, {
    onOpen: () => console.log("WebSocket connection established."),
    onClose: () => {
      console.log("WebSocket connection closed.");
      if (shouldReconnect) {
        console.log("Reopening WebSocket connection...");
        setTimeout(() => {
          setShouldReconnect(true); // Trigger reconnection
        }, 1000); // 1-second delay before reopening
      }
    },
    onError: (error) => console.error("WebSocket error:", error),
    onMessage: (event) => {
      const message = JSON.parse(event.data);
      console.log("Received message from server:", message);
      setWebMessage(message);
    },
    shouldReconnect: () => shouldReconnect, // Control reconnection behavior
    reconnectAttempts: 10, // Maximum number of reconnection attempts
    reconnectInterval: 1000, // Delay between reconnection attempts (1 second)
  });

  useEffect(() => {
    console.log("main useEffect");
    if (webMessage) {
      console.log("main useEffect accepts webMessage");
      // Extract the machineStates from the WebSocket message
      const machineStates = webMessage.machineStates; 
      const queueStates = webMessage.queueStates;

      // Iterate over the machineStates to find the machine with the matching id
      for (const machineKey in machineStates) {
        if (machineStates.hasOwnProperty(machineKey)) {

          const machineState = machineStates[machineKey];
          const { id, color } = machineState;

          // Find the machine with the matching id
          const machineIndex = machines.findIndex(
            (machine) => machine.id === id
          );
          if (machineIndex !== -1) {
            // Extract the color values from the color object
            const { blue, green, red } =
              color === null ? { blue: 0, green: 0, red: 0 } : color;

            // Create a CSS color string (e.g., "rgb(124, 236, 110)")
            const backgroundColor = `rgb(${red}, ${green}, ${blue})`;
            console.log("Machine found at index:", machineIndex);
            console.log("Extracted id:", id, "and color:", color);

            // Update the machine's background color
            setMachines((prevMachines) =>
              prevMachines.map((machine, index) =>
                index === machineIndex
                  ? { ...machine, backgroundColor:backgroundColor } // Update the background color
                  : machine
              )
            );
          }
        }
      }

      // Iterate over the queueStates to find the queue with the matching id
      for (const queueKey in queueStates) {
        if (queueStates.hasOwnProperty(queueKey)) {
          const queueState = queueStates[queueKey];
          const { id, processCount } = queueState;
          const queueIndex = queues.findIndex((queue) => queue.id === id);
          if (queueIndex !== -1) {
            console.log("queue found at index:", queueIndex);
            console.log("Process COunter is :- "+ processCount )
            console.log("Extracted id:", id);
            console.log (queueState.id);
            setQueues((prevQueues) => {
            const updatedQueues = [...prevQueues]; 
            // Find the queue index in the copied array
            const queueIndex = updatedQueues.findIndex((queue) => queue.id === id);
              // Update the queue processes count
              updatedQueues[queueIndex] = { 
                ...updatedQueues[queueIndex], 
                processes: processCount 
             };
  
            return updatedQueues;
          });
          }
        }
      }
    }
  }, [webMessage]);
  useEffect(() => {
    console.log("entered useEffect for checking all machines");
    // Check if all machines are black
    const allMachinesAreBlack = machines.every((machine) => {
      return machine.backgroundColor === "rgb(0, 0, 0)"; // Check if the color is black
    });
  
    // If all machines are black, set simulationStarted to false
    if (allMachinesAreBlack) {
      setSimulationStarted(false);
      sendJsonMessage({ type: "STOP_SIMULATION" });
      console.log("All machines are black. Simulation stopped.");
    }
  }, [machines]); // This effect runs only when `machines` changes
  

  const addMachine = () => {
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

  const addQueue = () => {
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
  const connect = () => {
    if (simulationStarted) return;
    if (selectedStart && selectedEnd) {
      if (
        (selectedStart.id.startsWith("M") && selectedEnd.id.startsWith("M")) ||
        (selectedStart.id.startsWith("Q") && selectedEnd.id.startsWith("Q"))
      ) {
        alert("Cannot connect two machines or two queues.");
        setSelectedStart(null);
        setSelectedEnd(null);
        return;
      }
      console.log(selectedStart);
      console.log(selectedEnd);
      if (selectedStart.id.startsWith("M") && selectedStart.connect != 0) {
        alert("Cannot connect a machine with more than one queue.");
        setSelectedStart(null);
        setSelectedEnd(null);
        return;
      }
      const newConnection = {
        from: selectedStart.id,
        to: selectedEnd.id,
      };
      selectedStart.connect++;

      setConnections([...connections, newConnection]);
      setSelectedStart(null); // Reset selection
      setSelectedEnd(null); // Reset selection
    } else {
      alert("Please select a start and end object for the connection.");
    }
  };
  const handleObjectClick = (id) => {
    if (!selectedStart) {
      setSelectedStart(id); // Set as start object
    } else if (!selectedEnd && id !== selectedStart) {
      setSelectedEnd(id); // Set as end object
    } else {
      // Reset selection if the same object is clicked again
      setSelectedStart(null);
      setSelectedEnd(null);
    }
  };

  // Function to delete the last added object
  const Delete = () => {
    if (simulationStarted) return;
    if (selectedEnd || selectedStart) {
      if (selectedStart && selectedEnd) {
        if (
          selectedStart.id.startsWith("M") ||
          selectedEnd.id.startsWith("M")
        ) {
          setMachines(
            machines.filter(
              (machine) =>
                machine.id !== selectedStart.id && machine.id !== selectedEnd.id
            )
          );
        }
        if (
          selectedStart.id.startsWith("Q") ||
          selectedEnd.id.startsWith("Q")
        ) {
          setQueues(
            queues.filter(
              (queue) =>
                queue.id !== selectedStart.id && queue.id !== selectedEnd.id
            )
          );
        }
        setConnections((prevConnections) =>
          prevConnections.filter(
            (connection) =>
              (connection.from !== selectedStart.id &&
                connection.to !== selectedStart.id) ||
              (connection.from !== selectedEnd.id &&
                connection.to !== selectedEnd.id)
          )
        );
        if (selectedStart.id.startsWith("Q")) {
          const connectedMachines = machines.filter(
            (machine) =>
              machine.connect > 0 &&
              connections.some(
                (conn) =>
                  conn.from === machine.id && conn.to === selectedStart.id
              )
          );
          connectedMachines.forEach((machine) => machine.connect--);
        }
        if (selectedEnd.id.startsWith("Q")) {
          const connectedMachines = machines.filter(
            (machine) =>
              machine.connect > 0 &&
              connections.some(
                (conn) => conn.from === machine.id && conn.to === selectedEnd.id
              )
          );
          connectedMachines.forEach((machine) => machine.connect--);
        }
      } else if (selectedStart) {
        if (selectedStart.id.startsWith("M")) {
          setMachines(
            machines.filter((machine) => machine.id !== selectedStart.id)
          );
        }
        if (selectedStart.id.startsWith("Q")) {
          setQueues(queues.filter((queue) => queue.id !== selectedStart.id));
          const connectedMachines = machines.filter(
            (machine) =>
              machine.connect > 0 &&
              connections.some(
                (conn) =>
                  conn.from === machine.id && conn.to === selectedStart.id
              )
          );
          connectedMachines.forEach((machine) => machine.connect--);
        }
        setConnections((prevConnections) =>
          prevConnections.filter(
            (connection) =>
              connection.from !== selectedStart.id &&
              connection.to !== selectedStart.id
          )
        );
      } else {
        if (selectedEnd.id.startsWith("M")) {
          setMachines(
            machines.filter((machine) => machine.id !== selectedEnd.id)
          );
        }
        if (selectedEnd.id.startsWith("Q")) {
          setQueues(queues.filter((queue) => queue.id !== selectedEnd.id));
          const connectedMachines = machines.filter(
            (machine) =>
              machine.connect > 0 &&
              connections.some(
                (conn) => conn.from === machine.id && conn.to === selectedEnd.id
              )
          );
          connectedMachines.forEach((machine) => machine.connect--);
        }
        setConnections((prevConnections) =>
          prevConnections.filter(
            (connection) =>
              connection.from !== selectedEnd.id &&
              connection.to !== selectedEnd.id
          )
        );
      }
      setSelectedEnd(null);
      setSelectedStart(null);
    } else {
      alert("Please select an object to delete.");
    }
  };

  const replay = () => {
    if (simulationStarted) return;
    console.log("Replaying simulation...");
  };

  // Function to start the simulation
  const startSim = () => {
    if (!selectedStart || !selectedStart.id.startsWith("Q")) {
      alert("Please select the starting Queue of simulation.");
      return;
    }
    const initData = transformData(machines, queues, connections, products, selectedStart.id);
    setProducts(0);
    console.log(initData);

    // Send the INIT_SIMULATION message
    sendJsonMessage({
      type: "INIT_SIMULATION",
      data: initData,
    });
    setStartingId(selectedStart.id);
    setSelectedStart(null);
    setSimulationStarted(true);
    console.log("Simulation started.");
  };
  const stop = () => {
    // Send the STOP_SIMULATION message
    sendJsonMessage({
      type: "STOP_SIMULATION",
    });

    setSimulationStarted(false);
    console.log("Simulation stopped.");
  };
  return (
    <div>
      <div className="menuBar_container">
        {/* Render the buttons */}
        <Buttons
          onAddMachine={addMachine}
          onAddQueue={addQueue}
          onConnect={connect}
          onDelete={Delete}
          onReplay={replay}
          onStartSim={startSim}
          onStop={stop}
          products={products}
          setProducts={setProducts}
          simulationStarted={simulationStarted}
          startingId={startingId}
          sendJsonMessage={sendJsonMessage}
        />
      </div>

      {/* Render draggable machines and queues */}
      <div
        style={{
          position: "relative",
          width: "100vw",
          height: "100vh",
          overflow: "visible",
        }}
      >
        {machines.map((machine) => (
          <Draggable
            key={machine.id}
            position={{ x: machine.x, y: machine.y }}
            onStop={(e, data) => {
              const updatedMachines = machines.map((m) =>
                m.id === machine.id ? { ...m, x: data.x, y: data.y } : m
              );
              setMachines(updatedMachines);
            }}
          >
            <div
              style={{
                position: "absolute",
                width: "60px",
                height: "60px",
                borderRadius: "50%",
                backgroundColor:
                  machine.backgroundColor === null
                    ? machine === selectedStart || machine === selectedEnd
                      ? "purple"
                      : "blue"
                    : machine.backgroundColor,
                color: "white",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                cursor: "move",
              }}
              onClick={() => handleObjectClick(machine)}
            >
              {machine.id}
            </div>
          </Draggable>
        ))}

        {queues.map((queue) => (
          <Draggable
            key={queue.id}
            position={{ x: queue.x, y: queue.y }}
            onStop={(e, data) => {
              const updatedQueues = queues.map((q) =>
                q.id === queue.id ? { ...q, x: data.x, y: data.y } : q
              );
              setQueues(updatedQueues);
            }}
          >
            <div
              style={{
                position: "absolute",
                width: "60px",
                height: "60px",
                backgroundColor:
                  queue === selectedStart || queue === selectedEnd
                    ? "purple"
                    : "green",
                color: "white",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                cursor: "move",
              }}
              onClick={() => handleObjectClick(queue)}
            >
              {`${queue.id}: ${queue.processes || 0}`}
              
            </div>
          </Draggable>
        ))}

        {/* Render connections (arrows) */}
        <svg
          style={{
            position: "absolute",
            top: 0,
            left: 0,
            width: "100%",
            height: "100%",
            pointerEvents: "none",
          }}
        >
          {connections.map((connection, index) => {
            const fromObject = [...machines, ...queues].find(
              (obj) => obj.id === connection.from
            );
            const toObject = [...machines, ...queues].find(
              (obj) => obj.id === connection.to
            );

            if (fromObject && toObject) {
              const angle =
                Math.atan2(
                  toObject.y - fromObject.y,
                  toObject.x - fromObject.x
                ) *
                (180 / Math.PI);
              return (
                <g key={index}>
                  <line
                    x1={fromObject.x + 30}
                    y1={fromObject.y + 30}
                    x2={toObject.x + 30}
                    y2={toObject.y + 30}
                    stroke="black"
                    strokeWidth="3"
                  />
                  <polygon
                    points={`${toObject.x + 30},${toObject.y + 30} ${
                      toObject.x + 20
                    },${toObject.y + 25} ${toObject.x + 20},${toObject.y + 35}`}
                    fill="black"
                    transform={`rotate(${angle}, ${toObject.x + 30}, ${
                      toObject.y + 30
                    })`}
                  />
                </g>
              );
            }
            return null;
          })}
        </svg>
      </div>
    </div>
  );
}

export default MenuBar;
