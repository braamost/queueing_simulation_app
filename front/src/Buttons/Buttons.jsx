import React, { useState } from "react";

const Buttons = ({ onAddMachine, onAddQueue, onConnect, onDelete, onReplay, onStartSim, onStop }) => {
  return (
    <>
      <button onClick={onAddMachine} title="Circle" className="button">M ⚫</button>
      <button onClick={onAddQueue} title="Square" className="button">Q ⬛</button>
      <button onClick={onConnect} title="arrow" className="button">arrow ↗</button>
      <button onClick={onDelete} title="Delete" className="button">Delete</button>
      <button onClick={onReplay} title="Replay" className="button">Replay</button>
      <button onClick={onStartSim} title="Start" className="button">Start</button>
      <button onClick={onStop} title="Stop" className="button">Stop</button>
    </>
  );
};

export default Buttons;