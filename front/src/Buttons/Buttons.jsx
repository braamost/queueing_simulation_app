import React, { useState } from "react";
import "./Buttons.css";

const Buttons = ({ onAddMachine, onAddQueue, onConnect, onDelete, onReplay, onStartSim, onStop , products , setProducts }) => {
  const addClick=()=>{
    const item = document.getElementById("products");
    if (item.value === "" || item.value <= 0) {
      alert("Please enter a valid number");
      return;
    }
    else {setProducts(item.value);
    console.log(products)
    item.value = "";}
  }
  return (
    <>
      <button onClick={onAddMachine} title="Circle" className="button">M ⚫</button>
      <button onClick={onAddQueue} title="Square" className="button">Q ⬛</button>
      <button onClick={onConnect} title="arrow" className="button">arrow ↗</button>
      <button onClick={onDelete} title="Delete" className="button">Delete</button>
      <button onClick={onReplay} title="Replay" className="button">Replay</button>
      <button onClick={onStartSim} title="Start" className="button">Start</button>
      <button onClick={onStop} title="Stop" className="button">Stop</button>
      <div className="prod">
        <div className="input">
          <label htmlFor="products">Number Of Products</label>
          <input
            type="number"
            id="products"
            min={1}
            placeholder="Enter number of products..."
            onChange={(item) => {
              if (item.target.value === "" || item.target.value <= 0) {
                return;
              }
              else {setProducts(item.target.value);
              console.log(products)
              }
            }}
          />
        </div>

        <button onClick={addClick}>
          Add Products
        </button>
    </div>
    </>
  );
};

export default Buttons;