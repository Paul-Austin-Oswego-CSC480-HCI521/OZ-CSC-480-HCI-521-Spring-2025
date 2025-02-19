import React, { useState } from "react";

const QuoteUploadModal = ({ isVisible, onClose, onSubmit, quoteText, setQuoteText }) => {
  if (!isVisible) return null;

  const [author, setAuthor] = useState("Unknown");
  const [tags, setTags] = useState(["inspirational", "motivating"]);
  const [customTag, setCustomTag] = useState(""); 

  const handleQuoteTextChange = (e) => {
    setQuoteText(e.target.value);
  };

  const handleAuthorChange = (e) => {
    setAuthor(e.target.value || "Unknown");
  };

  const handleTagChange = (e) => {
    const selectedTags = Array.from(e.target.selectedOptions, (option) => option.value);
    setTags(selectedTags);
  };

  const handleCustomTagChange = (e) => {
    setCustomTag(e.target.value);
  };

  const handleAddCustomTag = () => {
    if (customTag && !tags.includes(customTag)) {
      setTags([...tags, customTag]);
      setCustomTag(""); 
    }
  };

  const handleSubmit = () => {
    const quoteData = {
      text: quoteText,
      author,
      tags
    };
    onSubmit(quoteData);
    setQuoteText(""); 
    setAuthor("Unknown");
    setTags(["inspirational", "motivating"]);
    setCustomTag(""); 
  };

  return (
    <div className="modal show" style={{ display: 'block' }} aria-labelledby="uploadQuoteModal">
      <div className="modal-dialog">
        <div className="modal-content">
          <div className="modal-header">
            <h5 className="modal-title" id="uploadQuoteModal">Upload Quote</h5>
            <button type="button" className="btn-close" onClick={onClose}></button>
          </div>
          <div className="modal-body">
            <p>Submit your new quote:</p>

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
              <label>Tags:</label>
              <select
                className="form-select"
                multiple
                value={tags}
                onChange={handleTagChange}
                style={{ borderRadius: "25px" }}
              >
                <option value="inspirational">Inspirational</option>
                <option value="motivating">Motivating</option>
                <option value="thoughtful">Thoughtful</option>
                <option value="life">Life</option>
              </select>

              <div className="d-flex mt-2">
                <input
                  type="text"
                  className="form-control"
                  value={customTag}
                  onChange={handleCustomTagChange}
                  placeholder="Add your own tag"
                />
                <button
                  className="btn btn-outline-primary ms-2"
                  onClick={handleAddCustomTag}
                >
                  Add Tag
                </button>
              </div>
            </div>
          </div>
          <div className="modal-footer">
            <button type="button" className="btn btn-secondary" onClick={onClose}>Close</button>
            <button type="button" className="btn btn-primary" onClick={handleSubmit}>Submit</button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default QuoteUploadModal;
