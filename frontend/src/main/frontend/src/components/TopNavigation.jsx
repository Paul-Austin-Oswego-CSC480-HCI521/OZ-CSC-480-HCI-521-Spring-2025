import { Link, useNavigate, useLocation } from "react-router-dom";
import { useContext, useState } from "react";
import QuoteUploadModal from "./QuoteUploadModal";
import { BsPersonCircle } from "react-icons/bs";
import '../TopNav.css';
import { UserContext } from "../lib/Contexts";
import logo from "../assets/logo.png"; 

const TopNavigation = () => {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [quoteText, setQuoteText] = useState(""); // State to manage quote input
  const navigate = useNavigate();
  const location = useLocation();
  const [user] = useContext(UserContext);

  const isActive = (path) => location.pathname === path;

  return (
    <>
      <nav className="navbar navbar-expand-lg navbar-light sticky-top custom-nav">
        <Link className="navbar-brand pl-2" to="/">
          <img src={logo} alt="Logo" style={{ height: "40px" }} /> 
        </Link>
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
            <li className={`nav-item ${isActive("/my-collection") ? "active" : ""}`}>
              <Link className="nav-link" to="/my-collection">My Collection</Link> {/* Updated path */}
            </li>
            {!user ? (
              <li className="nav-item">
                <button className="btn btn-dark" onClick={() => navigate("/login")}>Sign in</button>
              </li>
            ) : (
              <></>

            )}
            <li className="nav-item ml-3 mr-3" title={user?.Username || "Click sign in to sign in"}>
              <Link to="/account">
                <BsPersonCircle size={40} style={{ color: "#146C43" }} />
              </Link>
            </li>
          </ul>
        </div>
      </nav>

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