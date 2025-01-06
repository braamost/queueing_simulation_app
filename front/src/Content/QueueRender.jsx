import React from "react";
import Queue from "../Queue/Queue";

const QueueRender= ({ queues, isConnectionMode, positions, setPositions, onClick }) => {
  return (
    <div className="content" onClick={onClick}>
      {queues.map((queue, index) => (
        <Queue
          id={`queue-${index}`}
          key={index}
          content={queue}
          isDraggable={!isConnectionMode}
          type="queue"
          positions={positions}
          setPositions={setPositions}
        />
      ))}
    </div>
  );
};

export default QueueRender;