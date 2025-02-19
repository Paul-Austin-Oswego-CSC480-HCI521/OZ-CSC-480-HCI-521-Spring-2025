import React, { useState, useEffect } from "react";
import { useLocation } from "react-router-dom";

const QuoteForm = () => {
  const location = useLocation();
  const { quote } = location.state || {};

  const [quoteText, setQuoteText] = useState(quote ? quote.text : "");
  const [author, setAuthor] = useState(quote ? quote.author : "");
  const [tags, setTags] = useState(quote ? quote.tags.join(", ") : "");

  useEffect(() => {
    if (quote) {
      setQuoteText(quote.text);
      setAuthor(quote.author);
      setTags(quote.tags.join(", "));
    }
  }, [quote]);

  const handleSave = () => {
    alert("Quote saved!");
    // save functionality incomplete
  };

  return (
    <div className="container vh-100 d-flex flex-column justify-content-center align-items-center">
      <h1>Edit Quote</h1>
      <form className="w-50">
        <div className="mb-3">
          <label htmlFor="quoteText" className="form-label">Quote Text</label>
          <textarea
            id="quoteText"
            className="form-control"
            value={quoteText}
            onChange={(e) => setQuoteText(e.target.value)}
          />
        </div>
        <div className="mb-3">
          <label htmlFor="author" className="form-label">Author</label>
          <input
            type="text"
            id="author"
            className="form-control"
            value={author}
            onChange={(e) => setAuthor(e.target.value)}
          />
        </div>
        <div className="mb-3">
          <label htmlFor="tags" className="form-label">Tags</label>
          <input
            type="text"
            id="tags"
            className="form-control"
            value={tags}
            onChange={(e) => setTags(e.target.value)}
            placeholder="Comma separated tags"
          />
        </div>
        <button type="button" className="btn btn-primary" onClick={handleSave}>Save Quote</button>
      </form>
    </div>
  );
};

export default QuoteForm;