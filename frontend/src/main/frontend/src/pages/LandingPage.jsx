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
    if (message) {
      setAlert({ type: "success", message });
      localStorage.removeItem("alertMessage");
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
    <div className="container vh-100 d-flex flex-column">
      <div className="d-flex flex-column justify-content-center align-items-center" style={{ height: "33vh" }}>
        <h1 className="mb-3">Quote Web App</h1>

        <input
          type="text"
          className="form-control w-50"
          placeholder="Enter keyword, author, or tag..."
          value={searchQuery}
          onChange={handleSearchChange}
        />
        <input
          type="text"
          className="form-control w-50"
          placeholder="Enter your own quote and press enter"
          value={quoteText}
          onChange={(e) => setQuoteText(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === 'Enter') {
              handleUploadQuote(); 
            }
          }}
        />

        <button className="btn btn-primary mt-3" onClick={handleSavedQuotesRedirect}>
          View Saved Quotes
        </button>
      </div>

      <QuoteUploadModal
        isVisible={showModal}
        onClose={handleCloseModal}
        onSubmit={handleSubmitQuote}
        quoteText={quoteText}
        setQuoteText={setQuoteText}
      />

      <div className="flex-grow-1 d-flex justify-content-center">
        <div className="row w-100">
          {loading ? (
            <p className="text-center w-100">Loading quotes...</p>
          ) : error ? (
            <p className="text-center w-100">{error}</p>
          ) : filteredQuotes.length > 0 ? (
            filteredQuotes.map((quote) => (
              <QuoteCard key={quote._id} quote={quote} />
            ))
          ) : (
            <p className="text-center w-100">No quotes found.</p>
          )}
        </div>
      </div>
    </div>
  );
};

export default LandingPage;
