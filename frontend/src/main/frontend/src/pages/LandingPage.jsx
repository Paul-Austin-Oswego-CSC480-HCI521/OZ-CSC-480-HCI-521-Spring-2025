import { useState, useEffect } from "react"; 
import "bootstrap/dist/css/bootstrap.min.css";
import QuoteUploadModal from "../components/QuoteUploadModal";
import Splash from "../components/Splash";
import LoginOverlay from "../components/LoginOverlay";
import QuoteList from "../components/QuoteList";
import AlertMessage from "../components/AlertMessage";
import { FetchTopQuotes } from "../lib/FetchTopQuotes"

const LandingPage = () => {
  const [alert, setAlert] = useState(null);
  const [quoteText, setQuoteText] = useState(""); 
  const [isLoggedIn, setIsLoggedIn] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [showLogin, setShowLogin] = useState(false);

  const { topQuotes, loading, error } = FetchTopQuotes();

  useEffect(() => {
    //check if user has logged in before, if not, show login prompt after 3 seconds
    if (!localStorage.getItem("hasLoggedIn")) {
      const timer = setTimeout(() => {
        setShowLogin(true);
        localStorage.setItem("hasLoggedIn", "true");
      }, 3000);
      return () => clearTimeout(timer);
    }
  }, []);

  useEffect(() => {
    //check if there's a saved alert message in local storage and display it
    const message = localStorage.getItem("alertMessage");
    const messageType = localStorage.getItem("alertType") || "success";
    if (message) {
      setAlert({ type: messageType, message });
      localStorage.removeItem("alertMessage");
      localStorage.removeItem("alertType");
    }
  }, []);

  const handleUploadQuote = () => {
    //show the upload modal if logged in, otherwise display an alert and prompt login
    if (isLoggedIn) {
      setShowModal(true);
    } else {
      setAlert({ type: "danger", message: "Only registered users can upload quotes" });
      setShowLogin(true); 
    }
  };

  const handleCloseModal = () => {
    //close the upload quote modal
    setShowModal(false); 
  };

  const handleSubmitQuote = (quoteText) => {
    //show an alert with the submitted quote text and close the modal
    alert(`Quote Submitted: ${quoteText}`);
    setShowModal(false); 
  };

  return (
    <>
      {showLogin && <LoginOverlay setShowLogin={setShowLogin} setIsLoggedIn={setIsLoggedIn} />}
      
      {alert && (
        //display an alert message at the top of the screen
        <div className="position-fixed top-0 start-50 translate-middle-x mt-3 px-4" style={{ zIndex: 1050 }}>
          <AlertMessage type={alert.type} message={alert.message} autoDismiss={true} />
        </div>
      )}
      
      <Splash />

      <QuoteUploadModal
        isVisible={showModal}
        onClose={handleCloseModal}
        onSubmit={handleSubmitQuote}
        quoteText={quoteText}
        setQuoteText={setQuoteText}
      />

      <QuoteList topQuotes={topQuotes} loading={loading} error={error} />
    </>
  );
};

export default LandingPage;
