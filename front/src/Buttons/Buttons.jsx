import React from "react";

const Button = ({ onClick, title, children, disabled }) => (
  <button 
    onClick={onClick} 
    title={title} 
    className={`button ${title.toLowerCase()}`}
    disabled={disabled}
  >
    {children}
  </button>
);

const ProductInput = ({ products, setProducts, onAdd, startingId, simulationStarted, isPaused }) => {
  const handleSubmit = (e) => {
    e.preventDefault();
    onAdd();
  };

  return (
    <form onSubmit={handleSubmit} className="prod">
      <div className="input-group">
        <label htmlFor="products">Number of Products</label>
        <div className="input-with-button">
          <input
            type="number"
            id="products"
            min={1}
            placeholder="Enter number of products..."
            onChange={(e) => setProducts(e.target.value)}
            className="product-input"
          />
          <Button 
            onClick={onAdd} 
            title="Add Products"
            disabled={!startingId || !simulationStarted || isPaused}
          >
            Add Products
          </Button>
        </div>
      </div>
    </form>
  );
};

const Buttons = ({
  onAddMachine,
  onAddQueue,
  onConnect,
  onDelete,
  onClearAll,
  onStartSim,
  onStop,
  products,
  setProducts,
  simulationStarted,
  startingId,
  setStartingId,
  sendJsonMessage,
  onPause,
  onResume,
  isPaused,
  onReplay,
  replayMode
}) => {
  const handleAddProducts = () => {
    const productCount = parseInt(products);
    if (!productCount || productCount <= 0) {
      alert("Please enter a valid number");
      return;
    }
    if (!startingId || !startingId.id.startsWith("Q")) {
      alert("Please select a queue first");
      return;
    }

    if (simulationStarted) {
      sendJsonMessage({
        type: "UPDATE_PROCESS_COUNT",
        queueId: startingId.id,
        count: productCount,
      });
      setProducts(0);
      setStartingId(null);
      document.getElementById("products").value = "";
    }
  };

  return (
    <div className="buttons-container">
      {!simulationStarted ? (
        <div className="button-group">
          <Button onClick={onAddMachine} title="Machine">
            <span className="button-icon">⚫</span>
            <span className="button-text">Machine</span>
          </Button>
          <Button onClick={onAddQueue} title="Queue">
            <span className="button-icon">⬛</span>
            <span className="button-text">Queue</span>
          </Button>
          <Button onClick={onConnect} title="Arrow">
            <span className="button-icon">↗</span>
            <span className="button-text">Connect</span>
          </Button>
          <Button onClick={onDelete} title="Delete">
            <span className="button-icon">🗑️</span>
            <span className="button-text">Delete</span>
          </Button>
          <Button onClick={onClearAll} title="Clear All">
            <span className="button-icon">🗑️</span>
            <span className="button-text">Clear All</span>
          </Button>
          <Button onClick={onStartSim} title="Start">
            <span className="button-icon">▶</span>
            <span className="button-text">Start</span>
          </Button>
        </div>
      ) : (
        <div className="button-group">
          <Button onClick={onStop} title="Stop">
            <span className="button-icon">⏹</span>
            <span className="button-text">Stop</span>
          </Button>
          {!isPaused ? (
            <Button onClick={onPause} title="Pause">
              <span className="button-icon">⏸</span>
              <span className="button-text">Pause</span>
            </Button>
          ) : (
            <Button onClick={onResume} title="Resume">
              <span className="button-icon">▶</span>
              <span className="button-text">Resume</span>
            </Button>
          )}
          {!replayMode ? (
            <Button onClick={onReplay} title="Replay" disabled={!isPaused}>
              <span className="button-icon">🔄</span>
              <span className="button-text">Replay</span>
            </Button>
          ) : (
            <Button onClick={onReplay} title="Exit Replay" disabled={!isPaused}>
              <span className="button-icon">🔄</span>
              <span className="button-text">Exit Replay</span>
            </Button>
          )}
          <ProductInput
            products={products}
            setProducts={setProducts}
            onAdd={handleAddProducts}
            startingId={startingId}
            simulationStarted={simulationStarted}
            isPaused={isPaused}
          />
        </div>
      )}
    </div>
  );
};

export default Buttons;