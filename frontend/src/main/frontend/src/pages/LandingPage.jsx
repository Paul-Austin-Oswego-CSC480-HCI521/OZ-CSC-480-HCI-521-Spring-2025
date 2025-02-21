import React, { useState, useEffect } from "react";
import "bootstrap/dist/css/bootstrap.min.css";
import { useNavigate } from "react-router-dom";
import QuoteCard from "../components/QuoteCard";
import QuoteUploadModal from "../components/QuoteUploadModal";
import { fetchTopBookmarkedQuotes } from "../lib/api";

const LandingPage = () => {
  const [searchQuery, setSearchQuery] = useState("");
  const [quoteText, setQuoteText] = useState(""); 
  const [isLoggedIn, setIsLoggedIn] = useState(true); 
  const [showModal, setShowModal] = useState(false);
  const [quotes, setQuotes] = useState([]); 
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const navigate = useNavigate();

  // ✅ Fetching quotes using the same method as the Debug Page
  useEffect(() => {
    const loadQuotes = async () => {
      try {
        console.log("Fetching top bookmarked quotes..."); // Debugging log
        const data = await fetchTopBookmarkedQuotes();
        
        console.log("Fetched Quotes:", data); // ✅ Log fetched data

        if (!data || data.length === 0) {
          setError("No quotes yet! Try adding your own");
        } else {
          setQuotes(data);
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

  const handleSavedQuotesRedirect = () => {
    navigate("/saved-quotes");
  };

  const handleSearchChange = (e) => {
    setSearchQuery(e.target.value);
  };

  const handleUploadQuote = () => {
    if (isLoggedIn) {
      setShowModal(true); 
    } else {
      navigate("/login"); 
    }
  };

  const handleCloseModal = () => {
    setShowModal(false); 
  };

  const handleSubmitQuote = (quoteText) => {
    alert(`Quote Submitted: ${quoteText}`);
    setShowModal(false); 
  };

  // ✅ Ensure filtering works correctly based on the API response structure
  const filteredQuotes = quotes.filter((quote) => {
    return (
      (quote.author && quote.author.toLowerCase().includes(searchQuery.toLowerCase())) ||
      (quote.quote && quote.quote.toLowerCase().includes(searchQuery.toLowerCase())) ||
      (quote.tags?.some(tag => tag.toLowerCase().includes(searchQuery.toLowerCase())))
    );
  });

  return (
    <div>
      <div className="bg-light py-5">
        <div className="d-flex flex-column justify-content-center align-items-center" style={{ height: "33vh" }}>
          <h1 className="fw-bold">Find, Share & Save Quotes Effortlessly</h1>
          <h2 className="text-muted fs-5"> Find insightful quotes from various authors and themes</h2>

          <input
            type="text" 
            className="form-control w-50 mx-auto shadow-sm"
            placeholder="Search quotes, authors, or themes..."
            value={searchQuery}
            onChange={handleSearchChange}
          />

          <button className="btn btn-dark mt-3 px-4 shadow-sm" onClick={handleSavedQuotesRedirect}>
            View Saved Quotes
          </button>
        </div>
      </div>
      <div className="container my-5">
        <div className="text-center mb-5">
          <h2 className="mb-3"> Top Quotes </h2>
          <QuoteUploadModal
            isVisible={showModal}
            onClose={handleCloseModal}
            onSubmit={handleSubmitQuote}
            quoteText={quoteText}
            setQuoteText={setQuoteText}
          />
        </div>

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
        <hr className="my-5" />
        <div className="text-center my-5">
          <h2 className="mb-3"> Popular Topics </h2>
          <div className="d-flex justify-content-center gap-3">
            <span className="badge bg-primary p-2">Live</span>
            <span className="badge bg-secondary p-2">Laugh</span>
            <span className="badge bg-success p-2">Love</span>
          </div>
        </div>

        <div className="bg-light py-5">
          <h2 className="mb-3"> Add a Quote </h2>
        <input
            type="text"
            className="form-control w-50 mx-auto shadow-sm"
            placeholder="Enter your own quote and press enter"
            value={quoteText}
            onChange={(e) => setQuoteText(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === 'Enter') {
                handleUploadQuote(); 
              }
            }}
          />
          </div>
      
      </div>
    </div>
  );
};

export default LandingPage;
