import { Link, useNavigate, useLocation } from "react-router-dom";
import { useState } from "react";
import QuoteUploadModal from "./QuoteUploadModal";
import { BsPersonCircle } from "react-icons/bs";
import '../TopNav.css';

const TopNavigation = ({ user }) => {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [quoteText, setQuoteText] = useState(""); // State to manage quote input
  const navigate = useNavigate();
  const location = useLocation();

  // Helper function to determine if the path is active
  const isActive = (path) => location.pathname === path;

  

  return (
    <>
      <nav className="navbar navbar-expand-lg navbar-light bg-light sticky-top custom-nav">
        <Link className="navbar-brand pl-2" to="/">Logo</Link>
        <button className="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarNav" aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
          <span className="navbar-toggler-icon"></span>
        </button>
        <div className="collapse navbar-collapse pr-2" id="navbarNav">
          <ul className="navbar-nav ml-auto">
            <li className={`nav-item ${isActive("/") ? "active" : ""}`}>
              <Link className="nav-link" to="/">Home</Link>
            </li>
            <li className={`nav-item ${isActive("/add-quote") ? "active" : ""}`}>
                  <span 
                    className="nav-link" 
                    style={{ cursor: "pointer" }} 
                    onClick={() => setIsModalOpen(true)}
                  >
                    Add Quote
                  </span>
                </li>
                <li className={`nav-item ${isActive("/saved-quotes") ? "active" : ""}`}>
                  <Link className="nav-link" to="/saved-quotes">My Collection</Link>
                </li>
            {!user ? (
              <li className="nav-item">
                <button className="btn btn-dark" onClick={() => navigate("/login")}>Sign in</button>
              </li>
            ) : (
              <>
              </>
            )}
            <li className="nav-item ml-3 mr-3" title={user?.Username || "Click sign in to sign in"}>
                  <BsPersonCircle size={40} />
                </li>
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
