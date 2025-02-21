import React, { useState } from "react";
import { createQuote } from "../lib/api"; 

const QuoteUploadModal = ({ isVisible, onClose, onSubmit, quoteText, setQuoteText }) => {
  if (!isVisible) return null;

  const [author, setAuthor] = useState("");
  const [tagsInput, setTagsInput] = useState("example, test");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);

  const handleQuoteTextChange = (e) => setQuoteText(e.target.value);
  const handleAuthorChange = (e) => setAuthor(e.target.value);
  const handleTagsInputChange = (e) => setTagsInput(e.target.value);

  const handleSubmit = async () => {
    setLoading(true);
    setError(null);
    setSuccess(false);

    if (!quoteText.trim()) {  
      setError("Quote text cannot be empty.");
      setLoading(false);
      return;
    }

    const tagsArray = tagsInput
      .split(",")
      .map((tag) => tag.trim())
      .filter((tag) => tag.length > 0);

    const quoteData = {
      quote: quoteText.trim(),
      author: author.trim() || "Unknown",
      tags: tagsArray,
    };

    console.log("Sending Quote Data:", quoteData);

    try {
      const response = await createQuote(quoteData);
      console.log("Backend Response:", response); // Log backend response
      if (!response || response.error) { 
        throw new Error("Failed to create quote.");
      }

      setSuccess(true);
      setTimeout(() => setSuccess(false), 3000);
      onSubmit(quoteData);
      setQuoteText("");
      setAuthor("");
      setTagsInput("example, test");
    } catch (err) {
      console.error("Error submitting quote:", err);
      setError("Error submitting quote. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal show" style={{ display: "block" }} aria-labelledby="uploadQuoteModal">
      <div className="modal-dialog">
        <div className="modal-content">
          <div className="modal-header">
            <h5 className="modal-title" id="uploadQuoteModal">Upload Quote</h5>
            <button type="button" className="btn-close" onClick={onClose}></button>
          </div>
          <div className="modal-body">
            <p>Submit your new quote:</p>

            {error && <div className="alert alert-danger">{error}</div>}
            {success && <div className="alert alert-success">Quote submitted successfully!</div>}

            <textarea
              className="form-control"
              rows="3"
              value={quoteText}
              onChange={handleQuoteTextChange}
              placeholder="Enter your quote here"
            />

            <div className="mt-3">
              <label htmlFor="authorInput">Author:</label>
              <input
                id="authorInput"
                type="text"
                className="form-control"
                value={author}
                onChange={handleAuthorChange}
                placeholder="Enter author (default is Unknown)"
              />
            </div>

            <div className="mt-3">
              <label>Tags (comma-separated):</label>
              <input
                type="text"
                className="form-control"
                value={tagsInput}
                onChange={handleTagsInputChange}
                placeholder="Enter tags separated by commas"
              />
            </div>
          </div>
          <div className="modal-footer">
            <button type="button" className="btn btn-secondary" onClick={onClose}>Close</button>
            <button
              type="button"
              className="btn btn-primary"
              onClick={handleSubmit}
              disabled={loading}
            >
              {loading ? "Submitting..." : "Submit"}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default QuoteUploadModal;
