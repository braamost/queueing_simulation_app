import React, { useState } from "react";
import "./Bar.css";
import axios from "axios";

export const Bar = ({setStartShape, setTypeOFStart, isConnectionMode, setIsConnectionMode, onAddQueue, onAddMachine ,setMachines, setQueues, setConnections}) => {
  const [numberOfProducts, setNumberOfProducts] = useState(0);
  const [error, setError] = useState(null);

  function addClick() {
    const item = document.getElementById("products");
    if (item.value === "" || item.value <= 0) {
      setError("Please enter a valid number");
      return;
    }
    setNumberOfProducts(item.value);
    item.value = "";
  } 

  const handleToggleConnectionMode = () => {
    setIsConnectionMode(!isConnectionMode);
    setStartShape(null);
    setTypeOFStart(null);
  }

  const handleDelete = () => {
    setNumberOfProducts(0);
    setError(null);
    setStartShape(null);
    setTypeOFStart(null);
    setIsConnectionMode(false);
    setMachines([]);
    setQueues([]);
    setConnections([]);
  }
  const simulate =async()=>{
    try{
        const response=axios.get(`http://localhost:8080/api/${numberOfProducts}`);
    }catch(err){}
  }
  return (
    <div className="toolbar">
      <button className="QButton" onClick={onAddQueue}>
        Add Q
      </button>

      <button className="MButton" onClick={onAddMachine}>
        Add M
      </button>

      <button className="connectionButton" onClick={handleToggleConnectionMode}>{isConnectionMode ? "Drawing Arrows (Click to Exit)" : "Connect"}</button>
      <button className="runButton" onClick={simulate}>Run</button>
      <button className="stopButton">Stop</button>
      <button className="resetButton">Reset</button>
      <button className="deleteButton" onClick={handleDelete}>Delete</button>

      <div className="input">
        <input
          type="number"
          id="products"
          min="0"
          placeholder="Enter number"
          onChange={() => setError(null)}
        />
        {error && <div className="error-message">{error}</div>}
        <label htmlFor="products">Number Of Products</label>
      </div>

      <button className="addButton" onClick={addClick}>
        Add
      </button>
    </div>
  );
};

export default Bar;
