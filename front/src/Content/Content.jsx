import React, { useState, useEffect } from "react";
import "./Content.css";
import Bar from "../Bar/Bar";
import MenuBar from "../Bar/MenuBar";
import axios from 'axios'
import QueueRender from "./QueueRender";
import MachineRender from "./MachineRender";
import ConnectionLine from "./ConnectionLine";

const Content = () => {
  
  return (
    <div className="relative w-full min-h-screen">
      <MenuBar/>
    </div>
  );
};

export default Content;