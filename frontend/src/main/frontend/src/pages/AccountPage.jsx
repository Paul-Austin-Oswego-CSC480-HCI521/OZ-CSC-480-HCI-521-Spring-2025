import { useState, useEffect, useContext } from "react";
import "bootstrap/dist/css/bootstrap.min.css";
import { updateMe } from "../lib/api";
import { BsCheckSquare, BsPencilSquare } from "react-icons/bs"; // Import icons
import { useNavigate } from "react-router-dom";
import { AlertContext, UserContext } from "../lib/Contexts";

const AccountPage = () => {
  const [user, setUser] = useContext(UserContext);
  const [_, setAlert] = useContext(AlertContext);
  const [error, setError] = useState(null);
  const [isEditingProfession, setIsEditingProfession] = useState(false);
  const [isEditingPersonalQuote, setIsEditingPersonalQuote] = useState(false);
  const [updatedProfession, setUpdatedProfession] = useState("");
  const [updatedPersonalQuote, setUpdatedPersonalQuote] = useState("");
  const navigate = useNavigate();

  useEffect(() => {
    if (!user) {
      setError("No user");
      return;
    }

    setUpdatedPersonalQuote(user.PersonalQuote || "")
    setUpdatedProfession(user.Profession || "")
    setError(null);
  }, [user]);

  const handleSaveProfession = () => {
    if (!user) return;
  
    if (updatedProfession === user.Profession) {
      // No change, but still exit edit mode
      setIsEditingProfession(false);
      return;
    }
  
    updateMe({ Profession: updatedProfession })
      .then((updatedUser) => {
        setUser(updatedUser);
        setIsEditingProfession(false);
      })
      .catch((error) => {
        console.error("Failed to update profession:", error);
        setAlert(error);
        setIsEditingProfession(false); // Still exit edit mode on failure
      });
  };

  const handleSavePersonalQuote = () => {
    if (!user) return;
  
    if (updatedPersonalQuote === user.PersonalQuote) {
      setIsEditingPersonalQuote(false);
      return;
    }
  
    updateMe({ PersonalQuote: updatedPersonalQuote })
      .then((updatedUser) => {
        setUser(updatedUser);
        setIsEditingPersonalQuote(false);
      })
      .catch((error) => {
        console.error("Failed to update personal quote:", error);
        setAlert(error);
        setIsEditingPersonalQuote(false);
      });
  };

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

          <hr/>

          {/* Community Guidelines Link */}
          <div className="mt-4">
                <button
                  className="fw-bold text-start w-100"
                  style={{background:"none",border:"none"}}
                  onClick={() => navigate("/community-guidelines")}
                >
                Community Guidelines
                </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default AccountPage;