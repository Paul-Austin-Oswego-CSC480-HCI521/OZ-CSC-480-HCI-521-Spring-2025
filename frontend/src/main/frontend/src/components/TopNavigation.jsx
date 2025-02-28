import { Link, useNavigate } from "react-router-dom";
import { useState } from "react";
import QuoteUploadModal from "./QuoteUploadModal";

const TopNavigation = ({ user }) => {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [quoteText, setQuoteText] = useState(""); // State to manage quote input
  const navigate = useNavigate();

  const circleStyle = {
    display: "inline-flex",
    justifyContent: "center",
    alignItems: "center",
    width: "36px",
    height: "36px",
    backgroundColor: "#007bff",
    borderRadius: "50%",
    color: "white",
    fontSize: "24px",
  };

  return (
    <>
      <nav className="navbar navbar-expand-lg navbar-light bg-light sticky-top">
        <Link className="navbar-brand pl-2" to="/">Logo</Link>
        <button className="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarNav" aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
          <span className="navbar-toggler-icon"></span>
        </button>
        <div className="collapse navbar-collapse pr-2" id="navbarNav">
          <ul className="navbar-nav ml-auto">
            <li className="nav-item">
              <Link className="nav-link" to="/">Home</Link>
            </li>
            {!user ? (
              <li className="nav-item">
                <button className="btn btn-dark" onClick={() => navigate("/login")}>Login</button>
              </li>
            ) : (
              <>
                <li className="nav-item">
                  <span 
                    className="nav-link text-primary" 
                    style={{ cursor: "pointer" }} 
                    onClick={() => setIsModalOpen(true)}
                  >
                    Upload Quote
                  </span>
                </li>
                <li className="nav-item">
                  <Link className="nav-link" to="/saved-quotes">Saved</Link>
                </li>
                <li className="nav-item">
                  <div style={circleStyle}>
                    <i className="bi bi-person"></i>
                  </div>
                  {user.Username}
                </li>
              </>
            )}
          </ul>
        </div>
      </nav>

      {/* Quote Upload Modal */}
      <QuoteUploadModal
        isVisible={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSubmit={() => setIsModalOpen(false)} // Close modal after submission
        quoteText={quoteText}
        setQuoteText={setQuoteText}
      />
    </>
  );
};

export default TopNavigation;
