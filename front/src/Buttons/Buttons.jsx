import React from "react";

const Buttons = ({
  onAddMachine,
  onAddQueue,
  onConnect,
  onDelete,
  onReplay,
  onStartSim,
  onStop,
  products,
  setProducts,
  simulationStarted,
  startingId,
  sendJsonMessage,
}) => {
  const addClick = () => {
    const item = document.getElementById("products");
    if (item.value === "" || item.value <= 0) {
      alert("Please enter a valid number");
      return;
    } else {
      setProducts(item.value);
      console.log(products);
      item.value = "";
      if (simulationStarted) {
        sendJsonMessage({
          type: "UPDATE_PROCESS_COUNT",
          queueId: startingId,
          count: products,
        });
        setProducts(0);
      }
    }
  };
  return (
    <>
      <button onClick={onAddMachine} title="Machine" className="button">
        M ⚫
      </button>
      <button onClick={onAddQueue} title="Queue" className="button">
        Q ⬛
      </button>
      <button onClick={onConnect} title="arrow" className="button">
        arrow ↗
      </button>
      <button onClick={onDelete} title="Delete" className="button">
        Delete
      </button>
      {simulationStarted ? (
        <button onClick={onStop} title="Stop" className="button">
          Stop
        </button>
      ) : (
        <button onClick={onStartSim} title="Start" className="button">
          Start
        </button>
      )}

      {simulationStarted && (<div className="prod">
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
              } else {
                setProducts(item.target.value);
                console.log(products);
              }
            }}
          />
        </div>

        <button onClick={addClick} title="Add Products" className="button">
          Add Products
        </button>
      </div>)}
    </>
  );
};

export default Buttons;
