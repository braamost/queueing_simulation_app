import React, { useRef, useEffect } from "react";
import Draggable from "react-draggable";

const Queue = ({id, key, content, isDraggable, type, positions, setPositions}) => {
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
            width: "50px",
            height: "50px",
            backgroundColor: "#ffa500",
            borderRadius: "10%",
            marginRight: "5px",
            color: "white",
            border: "none",
            fontSize: "14px",
            fontWeight: "bold",
            cursor: "grab",
          }}
        >
          {content || "Queue"}
        </button>
      </div>
    </Draggable>
  );
};

export default Queue;