import React, { useState, useEffect } from "react"; 
import { useLocation } from "react-router-dom";
import { updateQuote } from "../lib/api";

const QuoteForm = () => {
  const location = useLocation();
  const { quote } = location.state || {}; // ✅ Get quote from navigation state

  const [updateId, setUpdateId] = useState("");
  const [updateText, setUpdateText] = useState("");
  const [updateAuthor, setUpdateAuthor] = useState("");
  const [updateTagsInput, setUpdateTagsInput] = useState("");
  const [updateResult, setUpdateResult] = useState(null);

  // ✅ Populate the form with the received quote data
  useEffect(() => {
    if (quote) {
      setUpdateId(quote._id);
      setUpdateText(quote.text || ""); // ✅ Use the correct key for text
      setUpdateAuthor(quote.author || "Unknown");
      setUpdateTagsInput(quote.tags ? quote.tags.join(", ") : "");
    }
  }, [quote]);

  const handleUpdateQuote = async (e) => {
    e.preventDefault();

    if (!updateId.trim()) {
      setUpdateResult("Error: Quote ID is required.");
      return;
    }

    const tagsArray = updateTagsInput
      .split(",")
      .map((tag) => tag.trim())
      .filter((tag) => tag.length > 0);

    const payload = {
      _id: updateId.trim(),
      author: updateAuthor || "Unknown",
      quote: updateText || "",
      tags: tagsArray.length ? tagsArray : [],
    };

    console.log("Sending Update Payload:", JSON.stringify(payload, null, 2));

    try {
      const result = await updateQuote(payload);
      console.log("Update Success:", result);
      setUpdateResult(result);
    } catch (error) {
      console.error("Error updating quote:", error);
      setUpdateResult({ error: `Failed to update quote: ${error.message}` });
    }
  };

  return (
    <div className="container vh-100 d-flex flex-column justify-content-center align-items-center">
      <h1>Edit Quote</h1>
      <form className="w-50" onSubmit={handleUpdateQuote}>
        <div className="mb-3">
          <label htmlFor="quoteText" className="form-label">Quote Text</label>
          <textarea
            id="quoteText"
            className="form-control"
            value={updateText}
            onChange={(e) => setUpdateText(e.target.value)}
          />
        </div>
        <div className="mb-3">
          <label htmlFor="author" className="form-label">Author</label>
          <input
            type="text"
            id="author"
            className="form-control"
            value={updateAuthor}
            onChange={(e) => setUpdateAuthor(e.target.value)}
          />
        </div>
        <div className="mb-3">
          <label htmlFor="tags" className="form-label">Tags</label>
          <input
            type="text"
            id="tags"
            className="form-control"
            value={updateTagsInput}
            onChange={(e) => setUpdateTagsInput(e.target.value)}
            placeholder="Comma separated tags"
          />
        </div>
        <button type="submit" className="btn btn-primary">Update Quote</button>
        {updateResult && <div className="alert alert-info mt-3">{JSON.stringify(updateResult)}</div>}
      </form>
    </div>
  );
};

export default QuoteForm;
