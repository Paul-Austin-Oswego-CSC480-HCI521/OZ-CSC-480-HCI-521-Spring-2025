import React, { useState } from "react";
import { createQuote } from "../lib/api";


const QuoteUploadModal = ({ isVisible, onClose, onSubmit, quoteText, setQuoteText }) => {
  if (!isVisible) return null; //if the modal is not visible, do not render anything

  const [author, setAuthor] = useState("");
  const [tags, setTags] = useState([]);
  const [customTag, setCustomTag] = useState("");
  //suggested tags for users to choose from
  const suggestedTags = ["Inspiration", "Motivation", "Life", "Success", "Wisdom"];

  const toggleTag = (tag) => {
    //add or remove a tag when clicked
    setTags((prevTags) =>
      prevTags.includes(tag) ? prevTags.filter((t) => t !== tag) : [...prevTags, tag]
    );
  };

  const addCustomTag = () => {
    //add a custom tag to the list if it is not empty or already included
    if (customTag.trim() && !tags.includes(customTag)) {
      setTags([...tags, customTag.trim()]);
    }
    setCustomTag(""); //clear input after adding
  };

  const handleSubmit = async () => {
    //handle submitting the quote to the API
    if (!quoteText.trim()) return;

    const quoteData = {
      quote: quoteText.trim(),
      author: author.trim() || "Unknown",
      tags,
    };

    try {
      await createQuote(quoteData); //send request to create a new quote
      onSubmit(quoteData); //call the parent function to handle submission
      setQuoteText(""); //clear input fields
      setAuthor("");
      setTags([]);
    } catch (err) {
      console.error("Error submitting quote:", err);
    }
  };

  return (
    <div className="modal show" style={{ display: "block" }}>
      <div className="modal-dialog">
        <div className="modal-content">
          <div className="modal-header">
            <h5 className="modal-title">Upload Quote</h5>
            <button type="button" className="btn-close" onClick={onClose}></button> {/*close modal */}
          </div>
          <div className="modal-body">
            <textarea
              className="form-control"
              rows="3"
              value={quoteText}
              onChange={(e) => setQuoteText(e.target.value)} //update quote text state when user types
              placeholder="Enter your quote here"
            />

            <div className="mt-3">
              <label>Author:</label>
              <input
                type="text"
                className="form-control"
                value={author}
                onChange={(e) => setAuthor(e.target.value)} //update author state when user types
                placeholder="Unknown"
              />
            </div>

            <div className="mt-3">
              <label>Tags:</label>
              <div className="mb-2">
                {suggestedTags.map((tag) => (
                  <button
                    key={tag}
                    className={`badge rounded-pill m-1 ${
                      tags.includes(tag) ? "bg-primary text-white" : "bg-light text-dark"
                    }`}
                    onClick={() => toggleTag(tag)} //toggle tag selection when clicked
                  >
                    {tag}
                  </button>
                ))}
              </div>
              <input
                type="text"
                className="form-control"
                value={customTag}
                onChange={(e) => setCustomTag(e.target.value)} //update custom tag input state
                onKeyPress={(e) => e.key === "Enter" && addCustomTag()} //add custom tag when Enter is pressed
                placeholder="Add custom tag and press Enter"
              />
              <div className="mt-2">
                {tags.map((tag) => (
                  <span key={tag} className="badge bg-success rounded-pill m-1">
                    {tag} ✕
                  </span>
                ))}
              </div>
            </div>
          </div>
          <div className="modal-footer">
            <button type="button" className="btn btn-secondary" onClick={onClose}>
              Close
            </button>
            <button type="button" className="btn btn-primary" onClick={handleSubmit}>
              Submit
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default QuoteUploadModal; //export the QuoteUploadModal component for use in the app
