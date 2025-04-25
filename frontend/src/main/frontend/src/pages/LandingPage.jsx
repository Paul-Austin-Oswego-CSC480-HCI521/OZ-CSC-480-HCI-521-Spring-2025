import { useState, useEffect, useContext } from "react"; 
import "bootstrap/dist/css/bootstrap.min.css";
import QuoteUploadModal from "../components/QuoteUploadModal";
import Splash from "../components/Splash";
import LoginOverlay from "../components/LoginOverlay";
import QuoteList from "../components/QuoteList";
import { FetchTopQuotes } from "../lib/FetchTopQuotes";
import { AlertContext, UserContext } from "../lib/Contexts";
import AccountSetup from "../pages/AccountSetup"; // adjust path if it's in pages

const LandingPage = () => {
  const [_, setAlert] = useContext(AlertContext);
  const [quoteText, setQuoteText] = useState(""); 
  const [isLoggedIn, setIsLoggedIn] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [showLogin, setShowLogin] = useState(false);
  const [showAccountSetup, setShowAccountSetup] = useState(false);
  const [user] = useContext(UserContext);

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

  return (
    <>
      {showLogin && <LoginOverlay aria-label="Login Overlay" aria-live="assertive" setShowLogin={setShowLogin} setIsLoggedIn={setIsLoggedIn}/>}
      
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
