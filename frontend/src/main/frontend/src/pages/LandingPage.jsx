import React, { useState } from "react";
import "bootstrap/dist/css/bootstrap.min.css";
import { useNavigate } from "react-router-dom";
import QuoteCard from "../components/QuoteCard";
import QuoteUploadModal from "../components/QuoteUploadModal";
import { userQuotes, bookmarkedQuotes } from "../placeholderdata"

const LandingPage = () => {
  const [searchQuery, setSearchQuery] = useState("");
  const [quoteText, setQuoteText] = useState(""); 
  const [isLoggedIn, setIsLoggedIn] = useState(true);
  const [showModal, setShowModal] = useState(false); 

  const navigate = useNavigate();

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

  const quotes = [...userQuotes, ...bookmarkedQuotes];

  const filteredQuotes = quotes.filter((quote) => {
    const matchesSearch = 
      quote.author.toLowerCase().includes(searchQuery.toLowerCase()) ||
      quote.text.toLowerCase().includes(searchQuery.toLowerCase()) ||
      quote.tags.some(tag => tag.toLowerCase().includes(searchQuery.toLowerCase()));

    const matchesVisibility = quote.visibility === "public" || (isLoggedIn && quote.uploadedById === 101);//placeholder

    return matchesSearch && matchesVisibility;
  });

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
          {filteredQuotes.length > 0 ? (
            filteredQuotes.map((quote) => (
              <QuoteCard key={quote.quoteId} quote={quote} />
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
