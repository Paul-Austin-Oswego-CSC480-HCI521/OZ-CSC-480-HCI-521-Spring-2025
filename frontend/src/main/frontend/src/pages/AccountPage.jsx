import React, { useState, useEffect } from "react";
import "bootstrap/dist/css/bootstrap.min.css";
import { fetchMe, updateMe } from "../lib/api";
import { BsCheckSquare, BsPencilSquare } from "react-icons/bs"; // Import icons
import { useNavigate } from "react-router-dom";

const AccountPage = () => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [isEditingProfession, setIsEditingProfession] = useState(false);
  const [isEditingPersonalQuote, setIsEditingPersonalQuote] = useState(false);
  const [updatedProfession, setUpdatedProfession] = useState("");
  const [updatedPersonalQuote, setUpdatedPersonalQuote] = useState("");
  const navigate = useNavigate();

  useEffect(() => {
    fetch("http://localhost:9081/users/accounts/whoami", { credentials: "include" })
      .then((response) => {
        if (!response.ok) {
          throw new Error("Failed to fetch user data");
        }
        return response.json();
      })
      .then((data) => {
        if (data) {
          setUser(data);
          setUpdatedProfession(data.Profession || "");
          setUpdatedPersonalQuote(data.PersonalQuote || "");
        }
      })
      .catch((err) => {
        console.error("Error fetching user:", err);
        setError(err.message);
      })
      .finally(() => setLoading(false));
  }, []);

  const handleSaveProfession = () => {
    if (!user) return;

    updateMe({ Profession: updatedProfession })
      .then(() => fetchMe()) 
      .then((updatedUser) => {
        setUser(updatedUser);
        setIsEditingProfession(false);
      })
      .catch((error) => console.error("Failed to update profession:", error));
  };

  const handleSavePersonalQuote = () => {
    if (!user) return;

    updateMe({ PersonalQuote: updatedPersonalQuote })
      .then(() => fetchMe())  
      .then((updatedUser) => {
        setUser(updatedUser);
        setIsEditingPersonalQuote(false);
      })
      .catch((error) => console.error("Failed to update personal quote:", error));
  };

  if (loading) return <div>Loading...</div>;
  if (error) return <div>Error: {error}</div>;

  return (
    <div className="container mt-4">
      <h2 className="text-start">Hello there!</h2>

      {user && (
        <div className="mt-4">
          {/* Profile Section */}
          <div className="d-flex align-items-center mb-3">
            <div
              className="rounded-circle d-flex align-items-center justify-content-center"
              style={{
                width: "60px",
                height: "60px",
                border: "2px solid black",
                fontSize: "24px",
                fontWeight: "bold",
              }}
            >
              {user.Username ? user.Username[0].toUpperCase() : "U"}
            </div>
            <div className="ms-3 text-start">
              <h5 className="fw-bold">{user.Username}</h5>
              
              {/* Editable Profession */}
              <p className="m-0">
                {isEditingProfession ? (
                  <>
                    <input
                      type="text"
                      value={updatedProfession}
                      onChange={(e) => setUpdatedProfession(e.target.value)}
                      className="form-control d-inline-block"
                      style={{ width: "400px" }}
                    />
                    <BsCheckSquare
                      onClick={handleSaveProfession}
                      className="ms-2"
                      style={{ cursor: "pointer", fontSize: "20px" }}
                    />
                  </>
                ) : (
                  <>
                    {user.Profession || "Not Set"}
                    <BsPencilSquare
                      className="ms-2 text-muted"
                      style={{ cursor: "pointer", fontSize: "18px" }}
                      onClick={() => setIsEditingProfession(true)}
                    />
                  </>
                )}
              </p>

              {/* Editable Personal Quote */}
              <p className="m-0">
                {isEditingPersonalQuote ? (
                  <>
                    <input
                      type="text"
                      value={updatedPersonalQuote}
                      onChange={(e) => setUpdatedPersonalQuote(e.target.value)}
                      className="form-control d-inline-block"
                      style={{ width: "400px" }}
                    />
                    <BsCheckSquare
                      onClick={handleSavePersonalQuote}
                      className="ms-2"
                      style={{ cursor: "pointer", fontSize: "20px" }}
                    />
                  </>
                ) : (
                  <>
                    {user.PersonalQuote || "Not Set"}
                    <BsPencilSquare
                      className="ms-2 text-muted"
                      style={{ cursor: "pointer", fontSize: "18px" }}
                      onClick={() => setIsEditingPersonalQuote(true)}
                    />
                  </>
                )}
              </p>
            </div>
          </div>


          <hr />

          {/* Quotes & Collection Section */}
          <div className="mt-4">
            <h6 className="fw-bold mb-3 text-start">Quotes & Collection</h6>
            <button 
              className="btn w-100 text-start text-black fw-normal" 
              style={{ background: "none", border: "none" }}
              onClick={() => navigate("/saved-quotes")}
            >
              Saved Quotes
            </button>
            <button 
              className="btn w-100 text-start text-black fw-normal" 
              style={{ background: "none", border: "none" }}
              onClick={() => navigate("/saved-quotes")}
            >
              Uploaded Quotes (PR/PB)
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default AccountPage;