import React from "react";

const ToggleButton = ({ isActive, onToggle, labels, small }) => (
  <div className="btn-group btn-group-toggle" style={{ borderRadius: "50px", overflow: "hidden", border: "1px solid #28a745", fontSize: small ? "12px" : "14px"}}>

    {labels.map((label, index) => (
      <button
        key={label}
        className={`btn btn-sm ${isActive === (index === 0) ? "btn-success text-white" : "btn-outline-success"}`}
        style={{ flex: 1, padding: small ? "4px 8px" : "6px 12px", }}
        onClick={() => onToggle(index === 0)}
      >
        {label}
      </button>
    ))}
  </div>
);

export default ToggleButton;
