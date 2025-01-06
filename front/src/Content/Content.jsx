import React, { useState, useEffect } from "react";
import "./Content.css";
import Bar from "../Bar/Bar";
import MenuBar from "../Bar/MenuBar";
import axios from 'axios'
import QueueRender from "./QueueRender";
import MachineRender from "./MachineRender";
import ConnectionLine from "./ConnectionLine";

const Content = () => {
  const [machines, setMachines] = useState([]);
  const [queues, setQueues] = useState([]);
  const [connections, setConnections] = useState([]);
  const [isConnectionMode, setIsConnectionMode] = useState(false);
  const [isSimulating, setIsSimulating] = useState(false);
   const [startShape, setStartShape] = useState(null);
   const [typeOFStart, setTypeOFStart] = useState(null);
   const [positions, setPositions] = useState({});

  const addMachine = () => {
    const newMachine = {
      id: `M${machines.length + 1}`,
      type: "machine",
      x: 50,
      y: 50,
    };
    setMachines([...machines, newMachine]);
  };

  // Function to add a queue
  const addQueue = () => {
    const newQueue = {
      id: `Q${queues.length + 1}`,
      type: "queue",
      x: 150,
      y: 50,
    };
    setQueues([...queues, newQueue]);
  };
  const connect = () => {
    // For simplicity, connect the last added machine and queue
    if (machines.length > 0 && queues.length > 0) {
      const newConnection = {
        from: machines[machines.length - 1].id,
        to: queues[queues.length - 1].id,
      };
      setConnections([...connections, newConnection]);
    }
  };

  // Function to delete the last added object
  const Delete = () => {
    if (machines.length > 0) {
      setMachines(machines.slice(0, -1));
    } else if (queues.length > 0) {
      setQueues(queues.slice(0, -1));
    }
  };

  // Function to replay the simulation
  const replay = () => {
    // Reset the simulation (for now, just log a message)
    console.log("Replaying simulation...");
  };

  // Function to start the simulation
  const startSim = () => {
    setIsSimulating(true);
    console.log("Simulation started...");
  };
  const stop = () => {
    setIsSimulating(false);
    console.log("Simulation stopped...");
  };

  const selectShape = (e) => {
    if (!isConnectionMode) return;

    const targetElement = e.target.closest('button');
    if (!targetElement) {
     
      return;
    }

    const shapeType = targetElement.dataset.type;
    const shapeId = targetElement.id;



    if (!startShape) {
    
        setTypeOFStart(shapeType);
        setStartShape(shapeId);
    } else {
      if (typeOFStart !== shapeType) {
     
        if (connections.some(connection => connection.start === startShape && connection.end === shapeId) || connections.some(connection => connection.start === shapeId && connection.end === startShape)) {
          alert("Connection already exists");
          setStartShape(null);
          setTypeOFStart(null);
          return;
        }
        
        const startElement = document.getElementById(startShape);
        const endElement = targetElement;
        
        if (startElement && endElement) {
          const startRect = startElement.getBoundingClientRect();
          const endRect = endElement.getBoundingClientRect();

          const newPositions = {
            ...positions,
            [startShape]: {
              x: startRect.left + startRect.width / 2,
              y: startRect.top + startRect.height / 2
            },
            [shapeId]: {
              x: endRect.left + endRect.width / 2,
              y: endRect.top + endRect.height / 2
            }
          };

          
          setPositions(newPositions);

          const newConnection = {
            id: Date.now(),
            start: startShape,
            end: shapeId
          };
          const [startShapeType, startShapeNumberStr] = startShape.split("-");
          const startShapeNumber = parseInt(startShapeNumberStr, 10); // Convert to number
          
          // Split shapeId
          const [shapeIdType, shapeIdNumberStr] = shapeId.split("-");
          const shapeIdNumber = parseInt(shapeIdNumberStr, 10); // Convert to number
          if (startShapeType==="machine"){
            try{
                axios.put(`http://localhost:8080/api/MTQ/${startShapeNumber+1}/${shapeIdNumber+1}`)
            }catch(err){}
            
          }else{
            try{
              axios.put(`http://localhost:8080/api/QTM/${shapeIdNumber+1}/${startShapeNumber+1}`)
          }catch(err){}
          }

          
          setConnections([...connections, newConnection]);
        }
      } else {
        alert('Arrows can only send from M to Q or from M to Q');
      }
      setStartShape(null);
      setTypeOFStart(null);
    }
  };

  return (
    <div className="relative w-full min-h-screen">
      <MenuBar/>
      <QueueRender
        queues={queues}
        isConnectionMode={isConnectionMode}
        positions={positions}
        setPositions={setPositions}
        onClick={selectShape}
      />

      <MachineRender
        machines={machines}
        isConnectionMode={isConnectionMode}
        positions={positions}
        setPositions={setPositions}
        onClick={selectShape}
      />

      {connections.map((arrow) => (
        <ConnectionLine key={arrow.id} arrow={arrow} positions={positions} />
      ))}
    </div>
  );
};

export default Content;