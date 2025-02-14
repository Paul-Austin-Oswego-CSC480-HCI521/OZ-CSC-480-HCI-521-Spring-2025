import React, { useState } from "react";
import "bootstrap/dist/css/bootstrap.min.css";
import { useNavigate } from "react-router-dom";
import QuoteCard from "../components/QuoteCard";

const LandingPage = () => {
  const [searchQuery, setSearchQuery] = useState("");
  const quotes = [ //temp data, will replace with real quotes
    { id: 1, author: "Author 1", text: "This is a sample quote." },
    { id: 2, author: "Author 2", text: "Another inspiring quote." },
    { id: 3, author: "Author 3", text: "Yet another meaningful quote." }
  ]; 

  const navigate = useNavigate();

  const handleLoginRedirect = () => {
    navigate("/login"); 
  };

  return (
    <div className="container vh-100 d-flex flex-column">
      <div className="d-flex flex-column justify-content-center align-items-center" style={{ height: "33vh" }}>
        <h1 className="mb-3">Search for Quotes</h1>
        <input
          type="text"
          className="form-control w-50"
          placeholder="Enter keyword..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
        />
        <button className="btn btn-primary mt-3" onClick={handleLoginRedirect}>
          Login
        </button>
      </div>

      <div className="flex-grow-1 d-flex justify-content-center">
        <div className="row w-100">
          {quotes.map((quote) => (
            <QuoteCard key={quote.id} quote={quote} />
          ))}
        </div>
      </div>
    </div>
  );
};

export default LandingPage;
