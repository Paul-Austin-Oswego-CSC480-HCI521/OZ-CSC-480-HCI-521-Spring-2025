import { useState, useEffect } from "react";
import "bootstrap/dist/css/bootstrap.min.css";
import QuoteCard from "../components/QuoteCard";
import QuoteUploadModal from "../components/QuoteUploadModal";
import LoginBox from "../components/Login";
import { fetchTopBookmarkedQuotes } from "../lib/api";  
import AlertMessage from "../components/AlertMessage";
import Splash from "../components/Splash";

const LandingPage = () => {
  const [alert, setAlert] = useState(null);
  const [searchQuery, setSearchQuery] = useState("");
  const [quoteText, setQuoteText] = useState(""); 
  const [isLoggedIn, setIsLoggedIn] = useState(true); 
  const [showModal, setShowModal] = useState(false);
  const [showLogin, setShowLogin] = useState(false);
  const [topQuotes, setTopQuotes] = useState([]); 
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const loadQuotes = async () => {
      try {
        console.log("Fetching top bookmarked quotes...");
        const data = await fetchTopBookmarkedQuotes();
        console.log("Fetched Quotes:", data);
        if (!data || data.length === 0) {
          console.warn("No quotes found from API.");
          // setError("No quotes yet! Try adding your own");

          setTopQuotes([
            { _id: 1, quote: "This is Quoteable.", author: "Anonymous" },
            { _id: 2, quote: "This is me.", author: "John Doe" }
          ]);

        } else {
          // console.log("Using API quotes...");
          setTopQuotes(data);
        }
      } catch (err) {
        console.error("Error fetching quotes:", err);
        setError("Failed to load quotes");
      } finally {
        setLoading(false);
      }
    };
    loadQuotes();
  }, []);


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
    if (message) {
      setAlert({ type: "success", message });
      localStorage.removeItem("alertMessage");
    }
  }, []);


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

  const handleGoogleLogin = () => {
    setIsLoggedIn(true);
    window.location.href = "http://localhost:9081/users/auth/login";
  };

  const handleGuestLogin = () => {
    setIsLoggedIn(false);
    setShowLogin(false);
  };

  const handleSubmitQuote = (quoteText) => {
    alert(`Quote Submitted: ${quoteText}`);
    setShowModal(false); 
  };

  const filteredQuotes = topQuotes.filter((quote) => {
    return (
      (quote.author && quote.author.toLowerCase().includes(searchQuery.toLowerCase())) ||
      (quote.quote && quote.quote.toLowerCase().includes(searchQuery.toLowerCase())) ||
      (quote.tags?.some(tag => tag.toLowerCase().includes(searchQuery.toLowerCase())))
    );
  });

  return (
    <>
      {showLogin && (
        <div className="position-fixed top-0 start-0 w-100 h-100 d-flex justify-content-center align-items-center" style={{ backgroundColor: "rgba(0, 0, 0, 0.5)", zIndex: 1050 }}>
          <div className="bg-white p-4">
            <LoginBox handleGoogleLogin={handleGoogleLogin} handleGuestLogin={handleGuestLogin} />
          </div>
        </div>
      )}

      {alert && (
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

        <div style={{padding: "40px", display: "flex", flexDirection: "column", gap: "24px", justifyContent: "center", alignItems: "center"}}>
          <h1>Top Quotes</h1>
          <div className="d-flex w-100" style={{gap: "40px", flexWrap: "wrap"}}>
            {loading ? (
              <p className="text-center w-100">Loading quotes...</p>
            ) : error ? (
              <p className="text-center w-100">{error}</p>
            ) : topQuotes.length > 0 ? (
              topQuotes.map((quote) => (
                <QuoteCard key={quote._id} quote={quote} />
              ))
            ) : (
              <p className="text-center w-100">No quotes found.</p>
            )}
          </div>
        </div>
    </>
  );
};

export default LandingPage;