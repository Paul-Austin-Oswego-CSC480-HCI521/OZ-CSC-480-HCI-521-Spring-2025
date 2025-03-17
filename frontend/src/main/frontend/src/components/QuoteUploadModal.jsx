import React, { useState } from "react";
import { createQuote } from "../lib/api";
import Switch from "react-switch";

const QuoteUploadModal = ({ isVisible, onClose, onSubmit, quoteText, setQuoteText }) => {
  if (!isVisible) return null; //if the modal is not visible, do not render anything

  const [author, setAuthor] = useState("");
  const [tags, setTags] = useState([]);
  const [customTag, setCustomTag] = useState("");
  const suggestedTags = ["Love", "Willpower", "Life", "Success"];
  const [isPrivate, setIsPrivate] = useState(false);

  const toggleTag = (tag) => {
    setTags((prevTags) =>
      prevTags.includes(tag) ? prevTags.filter((t) => t !== tag) : [...prevTags, tag]
    );
  };

  const addCustomTag = () => {
    if (customTag.trim() && !tags.includes(customTag)) {
      setTags([...tags, customTag.trim()]);
    }
    setCustomTag("");
  };

  const handleSubmit = async () => {
    if (!quoteText.trim()) return;

    const quoteData = {
      quote: quoteText.trim(),
      author: author.trim() || "Unknown",
      tags,
      ["private"]: isPrivate || false
    };

    try {
      await createQuote(quoteData);
      onSubmit(quoteData);
      setQuoteText("");
      setAuthor("");
      setTags([]);
      setIsPrivate(false);
      onClose();
      window.location.href = "/";
    } catch (err) {
      console.error("Error submitting quote:", err);
    }
  };

  return (
    <div className="modal show" style={{ display: "block" }}>
      <div className="modal-dialog modal-dialog-centered">
        <div className="modal-content" style={{ backgroundColor: "#F8FDF1", borderRadius: "10px" }}>
          <div className="modal-header" style={{ backgroundColor: "#146C43", color: "#fff", borderTopLeftRadius: "10px", borderTopRightRadius: "10px" }}>
            <h5 className="modal-title">Upload Quote</h5>
            <button type="button" className="btn-close" onClick={onClose}></button>
          </div>
          <div className="modal-body">
            <div className="mb-3">
              <label className="form-label">Quote</label>
              <textarea
                className="form-control"
                rows="3"
                value={quoteText}
                onChange={(e) => setQuoteText(e.target.value)}
                placeholder="Enter your quote here"
                style={{ fontFamily: "Inter", fontSize: "16px", lineHeight: "24px" }}
              />
            </div>

            <div className="mb-3">
              <label className="form-label">Author</label>
              <input
                type="text"
                className="form-control"
                value={author}
                onChange={(e) => setAuthor(e.target.value)}
                placeholder="Unknown"
                style={{ fontFamily: "Inter", fontSize: "16px", lineHeight: "24px" }}
              />
            </div>

            <div className="mb-3">
              <label className="form-label">Tags</label>
              <div className="mb-2">
                {suggestedTags.map((tag) => (
                  <button
                    key={tag}
                    className={`badge rounded-pill m-1 ${tags.includes(tag) ? "bg-success text-white" : "bg-light text-dark"}`}
                    onClick={() => toggleTag(tag)}
                    style={{ border: "1px solid #5AD478", cursor: "pointer" }}
                  >
                    #{tag}
                  </button>
                ))}
              </div>
              <input
                type="text"
                className="form-control rounded-pill"
                value={customTag}
                onChange={(e) => setCustomTag(e.target.value)}
                onKeyPress={(e) => e.key === "Enter" && addCustomTag()}
                placeholder="Add custom tag and press Enter"
                style={{ fontFamily: "Inter", fontSize: "16px", lineHeight: "24px", padding: "10px 20px" }}
              />
              <div className="mt-2">
                {tags.map((tag) => (
                  <span key={tag} className="badge bg-success rounded-pill m-1" style={{ cursor: "pointer" }} onClick={() => toggleTag(tag)}>
                    #{tag} ✕
                  </span>
                ))}
              </div>
            </div>

            <div className="mb-3">
              <label className="form-label">Visibility</label>
              <div className="d-flex align-items-center">
                <button
                  type="button"
                  className={`btn ${!isPrivate ? "btn-success" : "btn-outline-secondary"} me-2`}
                  onClick={() => setIsPrivate(false)}
                  style={{ borderRadius: "20px", padding: "5px 15px" }}
                >
                  Public
                </button>
                <button
                  type="button"
                  className={`btn ${isPrivate ? "btn-success" : "btn-outline-secondary"}`}
                  onClick={() => setIsPrivate(true)}
                  style={{ borderRadius: "20px", padding: "5px 15px" }}
                >
                  Private
                </button>
              </div>
            </div>
          </div>
          <div className="modal-footer">
            <button type="button" className="btn btn-secondary" onClick={onClose}>
              Close
            </button>
            <button type="button" className="btn btn-primary" style={{ backgroundColor: "#5AD478", borderColor: "#5AD478" }} onClick={handleSubmit}>
              Upload
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default QuoteUploadModal;
