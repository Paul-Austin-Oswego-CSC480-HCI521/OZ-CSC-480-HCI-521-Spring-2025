import React, { useState, useEffect } from "react";
import "bootstrap/dist/css/bootstrap.min.css";
import { fetchMe, updateMe } from "../lib/api";

const AccountPage = () => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [isEditingProfession, setIsEditingProfession] = useState(false);
  const [isEditingPersonalQuote, setIsEditingPersonalQuote] = useState(false);
  const [updatedProfession, setUpdatedProfession] = useState("");
  const [updatedPersonalQuote, setUpdatedPersonalQuote] = useState("");

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
      .then(() => fetchMe())  // Re-fetch user after update
      .then((updatedUser) => {
        setUser(updatedUser);
        setIsEditingProfession(false);
      })
      .catch((error) => console.error("Failed to update profession:", error));
  };
  
  const handleSavePersonalQuote = () => {
    if (!user) return;
  
    updateMe({ PersonalQuote: updatedPersonalQuote })
      .then(() => fetchMe())  // Re-fetch user after update
      .then((updatedUser) => {
        setUser(updatedUser);
        setIsEditingPersonalQuote(false);
      })
      .catch((error) => console.error("Failed to update personal quote:", error));
  };

  if (loading) return <div>Loading...</div>;
  if (error) return <div>Error: {error}</div>;

  return (
    <div className="container mt-5">
      <h1>Hello There!</h1>
      {user ? (
        <div className="card">
          <div className="card-body">
            <h5 className="card-title">User Information</h5>
            <img
              src={user.profilePicture}
              alt="Profile"
              className="img-fluid rounded-circle mb-3"
              style={{ width: "150px", height: "150px" }}
            />
            <p className="card-text"><strong>Name:</strong> {user.Username}</p>
            <p className="card-text"><strong>Email:</strong> {user.Email}</p>

            {/* Profession Field */}
            <p className="card-text">
              <strong>Profession:</strong>
              {isEditingProfession ? (
                <>
                  <input
                    type="text"
                    value={updatedProfession}
                    onChange={(e) => setUpdatedProfession(e.target.value)}
                    className="form-control"
                  />
                  <button onClick={handleSaveProfession} className="btn btn-primary mt-2">Save</button>
                </>
              ) : (
                <>
                  {user.Profession || "Not Set"}
                  <button onClick={() => setIsEditingProfession(true)} className="btn btn-link">Edit</button>
                </>
              )}
            </p>

            {/* Personal Quote Field */}
            <p className="card-text">
              <strong>Personal Quote:</strong>
              {isEditingPersonalQuote ? (
                <>
                  <input
                    type="text"
                    value={updatedPersonalQuote}
                    onChange={(e) => setUpdatedPersonalQuote(e.target.value)}
                    className="form-control"
                  />
                  <button onClick={handleSavePersonalQuote} className="btn btn-primary mt-2">Save</button>
                </>
              ) : (
                <>
                  {user.PersonalQuote || "Not Set"}
                  <button onClick={() => setIsEditingPersonalQuote(true)} className="btn btn-link">Edit</button>
                </>
              )}
            </p>

          </div>
          {/* Preferences and Settings Card */}
          <div className="card mb-4">
            <div className="card-body">
              <h5 className="card-title">Preferences and Settings</h5>
              <button className="btn btn-link">Privacy Settings</button>
              <button className="btn btn-link">Quote Visibility Settings</button>
              <button className="btn btn-link">Theme Preferences</button>
            </div>
          </div>

          {/* Quotes & Collection Card */}
          <div className="card mb-4">
            <div className="card-body">
              <h5 className="card-title">Quotes & Collection</h5>
              <button className="btn btn-link">Saved Quotes</button>
              <button className="btn btn-link">Uploaded Quotes (PR/PB)</button>
            </div>
          </div>
        </div>
      ) : (
        <div>No user information available.</div>
      )}
    </div>
  );
};

export default AccountPage;
