import React, { useState } from "react";

const ToggleButton = ({ defaultIndex = 0, onToggle, labels, small }) => {
  const [activeIndex, setActiveIndex] = useState(defaultIndex);

  const handleToggle = (index) => {
    setActiveIndex(index);
    onToggle(index);
  };

  return (
    <div
      className="btn-group btn-group-toggle"
      style={{
        borderRadius: "50px",
        overflow: "hidden",
        border: "1px solid #28a745",
        fontSize: small ? "12px" : "14px",
      }}
    >
      {labels.map((label, index) => (
        <button
          key={label}
          className={`btn btn-sm ${
            activeIndex === index ? "btn-success text-white" : "btn-outline-success"
          }`}
          style={{
            flex: 1,
            padding: small ? "4px 8px" : "6px 12px",
          }}
          onClick={() => handleToggle(index)}
        >
          {label}
        </button>
      ))}
    </div>
  );
};

export default ToggleButton;
