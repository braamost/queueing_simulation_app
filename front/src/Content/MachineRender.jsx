import React from "react";
import Machine from "../Machine/Machine";

const MachineRender = ({ machines, isConnectionMode, positions, setPositions, onClick }) => {
  return (
    <div className="content" onClick={onClick}>
      {machines.map((machine, index) => (
        <Machine
          id={`machine-${index}`}
          key={index}
          content={machine}
          isDraggable={!isConnectionMode}
          type="machine"
          positions={positions}
          setPositions={setPositions}
        />
      ))}
    </div>
  );
};

export default MachineRender;