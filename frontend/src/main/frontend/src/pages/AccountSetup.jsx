import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { updateMe } from "../lib/api";

const AccountSetup = () => {
  const [profession, setProfession] = useState("");
  const [personalQuote, setPersonalQuote] = useState("");
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  const handleSave = async () => {
    try {
      await updateMe({ Profession: profession, PersonalQuote: personalQuote });
      navigate("/"); 
    } catch (error) {
      setError("Failed to update account information. Please try again.");
      console.error("Error updating account information:", error);
    }
  };

  return (
    <div className="container mt-5">
      <h1>Account Setup</h1>
      {error && <div className="alert alert-danger">{error}</div>}
      <div className="card">
        <div className="card-body">
          <div className="mb-3">
            <label className="form-label">Profession</label>
            <input
              type="text"
              className="form-control"
              value={profession}
              onChange={(e) => setProfession(e.target.value)}
            />
          </div>
          <div className="mb-3">
            <label className="form-label">Personal Quote</label>
            <input
              type="text"
              className="form-control"
              value={personalQuote}
              onChange={(e) => setPersonalQuote(e.target.value)}
            />
          </div>
          <button className="btn btn-primary" onClick={handleSave}>
            Save
          </button>
        </div>
      </div>
    </div>
  );
};

export default AccountSetup;