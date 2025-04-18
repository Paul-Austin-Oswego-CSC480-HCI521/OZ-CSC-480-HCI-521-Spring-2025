import { useState, useEffect, useContext } from "react"; 
import "bootstrap/dist/css/bootstrap.min.css";
import QuoteUploadModal from "../components/QuoteUploadModal";
import Splash from "../components/Splash";
import LoginOverlay from "../components/LoginOverlay";
import QuoteList from "../components/QuoteList";
import AlertMessage from "../components/AlertMessage";
import { FetchTopQuotes } from "../lib/FetchTopQuotes";
import { UserContext } from "../lib/Contexts";
import AccountSetup from "../pages/AccountSetup"; 
import { logout } from "../lib/api"; 

const LandingPage = () => {
  const [alert, setAlert] = useState(null);
  const [quoteText, setQuoteText] = useState(""); 
  const [isLoggedIn, setIsLoggedIn] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [showLogin, setShowLogin] = useState(false);
  const [showAccountSetup, setShowAccountSetup] = useState(false);
  const [user, setUser] = useContext(UserContext); 

  const { topQuotes, loading, error } = FetchTopQuotes();

  useEffect(() => {
    if (!localStorage.getItem("hasLoggedIn")) {
      const timer = setTimeout(() => {
        setShowLogin(true);
        localStorage.setItem("hasLoggedIn", "true");
      }, 3000);
      return () => clearTimeout(timer);
    }
  }, []);

  useEffect(() => {
    const message = localStorage.getItem("alertMessage");
    const messageType = localStorage.getItem("alertType") || "success";
    if (message) {
      setAlert({ type: messageType, message });
      localStorage.removeItem("alertMessage");
      localStorage.removeItem("alertType");
    }
  }, []);

  useEffect(() => {
    if (user && (!user.Profession || !user.PersonalQuote)) {
      setShowAccountSetup(true);
    }
  }, [user]);

  const handleUploadQuote = () => {
    if (isLoggedIn) {
      setShowModal(true);
    } else {
      setAlert({ type: "danger", message: "Only registered users can upload quotes" });
      setShowLogin(true); 
    }
  };

  const handleCloseModal = () => {
    setShowModal(false); 
  };

  const handleSubmitQuote = (quoteText) => {
    alert(`Quote Submitted: ${quoteText}`);
    setShowModal(false); 
  };

  const handleLogout = async () => {
    try {
      const success = await logout();
      if (success) {
        localStorage.removeItem("hasLoggedIn");
        setIsLoggedIn(false);
        setUser(null); 
        setAlert({ type: "success", message: "Successfully logged out." });
        setShowLogin(true);
      } else {
        throw new Error();
      }
    } catch {
      setAlert({ type: "danger", message: "An error occurred during logout." });
    }
  };

  return (
    <>
      {showLogin && <LoginOverlay aria-label="Login Overlay" aria-live="assertive" setShowLogin={setShowLogin} setIsLoggedIn={setIsLoggedIn}/>}
      
      {alert && (
        <div className="position-fixed top-0 start-50 translate-middle-x mt-3 px-4" style={{ zIndex: 1050 }}>
          <AlertMessage type={alert.type} message={alert.message} autoDismiss={true} />
        </div>
      )}
      {isLoggedIn && user && (
        <button className="btn btn-danger position-absolute top-0 end-0 m-3" onClick={handleLogout}>
          Log Out
        </button>
      )}
      <Splash />

      <QuoteUploadModal
        isVisible={showModal}
        onClose={handleCloseModal}
        onSubmit={handleSubmitQuote}
        quoteText={quoteText}
        setQuoteText={setQuoteText}
      />

      {showAccountSetup && <AccountSetup user={user} onClose={() => setShowAccountSetup(false)} />}

      <QuoteList topQuotes={topQuotes} loading={loading} error={error} />
    </>
  );
};

export default LandingPage;
