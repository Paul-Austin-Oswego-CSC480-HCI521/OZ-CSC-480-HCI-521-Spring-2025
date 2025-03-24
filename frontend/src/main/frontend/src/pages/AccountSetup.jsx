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
    <div className="modal show d-block" tabIndex="-1" role="dialog">
      <div className="modal-dialog modal-dialog-centered" role="document">
        <div className="modal-content" style={{ backgroundColor: "#ffffff" }}>
          <div className="modal-header" style={{ backgroundColor: "#146C43", color: "#fff" }}>
            <h5 className="modal-title">Account Setup</h5>
          </div>
          <div className="modal-body">
            {error && <div className="alert alert-danger">{error}</div>}
            <div className="mb-3">
              <label className="form-label">Profession</label>
              <input
                type="text"
                className="form-control"
                value={profession}
                onChange={(e) => setProfession(e.target.value)}
                placeholder="Enter your profession"
              />
            </div>
            <div className="mb-3">
              <label className="form-label">Personal Quote</label>
              <input
                type="text"
                className="form-control"
                value={personalQuote}
                onChange={(e) => setPersonalQuote(e.target.value)}
                placeholder="Enter your personal quote"
              />
            </div>
          </div>
          <div className="modal-footer d-flex justify-content-between">
            <button className="btn btn-secondary" onClick={() => navigate("/")}>
              Cancel
            </button>
            <button className="btn btn-primary" style={{ backgroundColor: "#5AD478", borderColor: "#5AD478" }} onClick={handleSave}>
              Save
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AccountSetup;