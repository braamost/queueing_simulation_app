import React from "react";

const ConnectionLine = ({ arrow, positions }) => {
  const startPos = positions[arrow.start];
  const endPos = positions[arrow.end];

  if (!startPos || !endPos) {
    return null;
  }


  const startElement = document.getElementById(arrow.start);
  const endElement = document.getElementById(arrow.end);

  if (!startElement || !endElement) {
    return null;
  }


  const isMachineToMachine =
    startElement.dataset.type === "machine" && endElement.dataset.type === "machine";


  const startRect = startElement.getBoundingClientRect();
  const endRect = endElement.getBoundingClientRect();


  const startRadius = Math.min(startRect.width, startRect.height) / 2;
  const endRadius = Math.min(endRect.width, endRect.height) / 2;

  
  const defaultMarginOffset = 10; 
  const machineMarginOffset = 5; 
  const marginOffset = isMachineToMachine ? machineMarginOffset : defaultMarginOffset;

  const dx = endPos.x - startPos.x;
  const dy = endPos.y - startPos.y;
  const angle = Math.atan2(dy, dx); 


  const adjustedStart = {
    x: startPos.x + Math.cos(angle) * (startRadius + marginOffset),
    y: startPos.y + Math.sin(angle) * (startRadius + marginOffset),
  };
  const adjustedEnd = {
    x: endPos.x - Math.cos(angle) * (endRadius + marginOffset),
    y: endPos.y - Math.sin(angle) * (endRadius + marginOffset),
  };

 
  const adjustedDx = adjustedEnd.x - adjustedStart.x;
  const adjustedDy = adjustedEnd.y - adjustedStart.y;
  const adjustedLength = Math.sqrt(adjustedDx * adjustedDx + adjustedDy * adjustedDy);

  return (
    <div
      style={{
        position: 'fixed',
        left: `${adjustedStart.x}px`,
        top: `${adjustedStart.y}px`,
        width: `${adjustedLength}px`,
        height: '2px',
        backgroundColor: 'black',
        transformOrigin: '0 0',
        transform: `rotate(${angle * (180 / Math.PI)}deg)`,
        pointerEvents: 'none',
      }}
    >
      <div
        style={{
          position: 'absolute',
          right: '-6px',
          top: '-4px',
          width: 0,
          height: 0,
          borderTop: '5px solid transparent',
          borderBottom: '5px solid transparent',
          borderLeft: '8px solid black',
        }}
      />
    </div>
  );
};

export default ConnectionLine;