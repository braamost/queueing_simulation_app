import React, { useRef, useEffect } from "react";
import Draggable from "react-draggable";

const Machine = ({id, key, content, isDraggable, type, positions, setPositions}) => {
  const buttonRef = useRef(null);

  // Update position whenever drag occurs
  const handleDrag = (e, data) => {
    const rect = buttonRef.current.getBoundingClientRect();
    setPositions(prev => ({
      ...prev,
      [id]: { 
        x: rect.left + rect.width / 2,
        y: rect.top + rect.height / 2
      }
    }));
  };

  // Initialize position on mount
  useEffect(() => {
    if (buttonRef.current) {
      const rect = buttonRef.current.getBoundingClientRect();
      setPositions(prev => ({
        ...prev,
        [id]: { 
          x: rect.left + rect.width / 2,
          y: rect.top + rect.height / 2
        }
      }));
    }
  }, []);

  return (
    <Draggable 
      disabled={!isDraggable} 
      nodeRef={buttonRef}
      onDrag={handleDrag}
      onStop={handleDrag}
    >
      <div ref={buttonRef}>
        <button
          id={id}
          data-type={type}
          style={{
            width: "60px",
            height: "60px",
            borderRadius: "50%",
            marginRight: "5px",
            border: "none",
            backgroundColor: "black",
            color: "white",
            fontWeight: "bold",
            fontSize: "14px",
            cursor: "grab",
          }}
        >
          {content || "Machine"}
        </button>
      </div>
    </Draggable>
  );
};

export default Machine;