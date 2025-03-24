import React, { useState, useEffect } from "react";
import Switch from "react-switch";
import { useLocation, useNavigate } from "react-router-dom";
import { updateQuote, deleteQuote } from "../lib/api";
import Tag from "../components/Tag";

const QuoteForm = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { quote } = location.state || {};

  const [updateId, setUpdateId] = useState("");
  const [updateText, setUpdateText] = useState("");
  const [updateAuthor, setUpdateAuthor] = useState("");
  const [updateTagsInput, setUpdateTagsInput] = useState("");
  const [updateIsPrivate, setUpdateIsPrivate] = useState(quote?.private ?? false); // For setting Public/Private.

  useEffect(() => {
    //populate form fields with existing quote data when the component loads
    if (quote) {
      setUpdateId(quote._id);
      setUpdateText(quote.text || "");
      setUpdateAuthor(quote.author || "Unknown");
      setUpdateTagsInput(quote.tags ? quote.tags.join(", ") : "");
      setUpdateIsPrivate(quote.private);
    }
  }, [quote]);

  const handleUpdateQuote = async (e) => {
    //handle updating a quote when the form is submitted
    e.preventDefault();
    if (!updateId.trim()) return;

    const tagsArray = updateTagsInput
      .split(",")
      .map((tag) => tag.trim())
      .filter((tag) => tag.length > 0); //convert comma-separated tags into an array

    const payload = {
      _id: updateId.trim(),
      author: updateAuthor || "Unknown",
      quote: updateText || "",
      tags: tagsArray.length ? tagsArray : [],
      ["private"]: updateIsPrivate || false,
    };

    try {
      await updateQuote(payload); //send update request to API
      localStorage.setItem("alertMessage", "Quote updated successfully!");
      localStorage.setItem("alertType", "success");
      navigate("/"); //redirect back to home after updating
    } catch (error) {
      console.error("Error updating quote:", error);
      if (error.message.includes("authorized")) {
        localStorage.setItem("alertMessage", "You need to be logged in to update a quote.");
        localStorage.setItem("alertType", "danger");
        navigate("/");
      } else {
        localStorage.setItem("alertMessage", "Error updating quote.");
        localStorage.setItem("alertType", "danger");
      }
    }
  };

  const handleDeleteQuote = async (e) => {
    //handle deleting a quote when the delete button is clicked
    e.preventDefault();
    if (!updateId.trim()) return;

    try {
      await deleteQuote(updateId); //send delete request to API
      localStorage.setItem("alertMessage", "Quote deleted successfully!");
      localStorage.setItem("alertType", "success");
      navigate("/"); //redirect back to home after deletion
    } catch (error) {
      console.error("Error deleting quote:", error);
      if (error.message.includes("authorized")) {
        localStorage.setItem("alertMessage", "You need to be logged in to delete a quote.");
        localStorage.setItem("alertType", "danger");
        navigate("/");
      } else {
        localStorage.setItem("alertMessage", "Error deleting quote.");
        localStorage.setItem("alertType", "danger");
      }
    }
  };

  return (
    <div className="container vh-100 d-flex flex-column justify-content-center align-items-center">
      <h1>Edit Quote</h1>
      <form className="w-50" onSubmit={handleUpdateQuote} style={{ background: "#f8fdf1", padding: "20px", borderRadius: "10px", boxShadow: "0 2px 5px rgba(0,0,0,0.1)" }}>
        
        <div className="mb-3">
          <label htmlFor="quoteText" className="form-label">Quote Text</label>
          <textarea
            id="quoteText"
            className="form-control"
            value={updateText}
            onChange={(e) => setUpdateText(e.target.value)} //update text state when user types
            style={{ fontFamily: "Inter", fontSize: "16px", lineHeight: "24px" }}
          />
        </div>
        
        <div className="mb-3">
          <label htmlFor="author" className="form-label">Author</label>
          <input
            type="text"
            id="author"
            className="form-control"
            value={updateAuthor}
            onChange={(e) => setUpdateAuthor(e.target.value)} //update author state when user types
            style={{ fontFamily: "Inter", fontSize: "16px", lineHeight: "24px" }}
          />
        </div>
        
        <div className="mb-3">
          <label htmlFor="tags" className="form-label">Tags</label>
          <input
            type="text"
            id="tags"
            className="form-control"
            value={updateTagsInput}
            onChange={(e) => setUpdateTagsInput(e.target.value)} //update tags state when user types
            placeholder="Comma separated tags"
            style={{ fontFamily: "Inter", fontSize: "16px", lineHeight: "24px" }}
          />
          <div className="mt-2">
            {updateTagsInput.split(",").map((tag, index) => (
              <Tag text={tag.trim()} key={index} />
            ))}
          </div>
        </div>

        <div className="mb-3">
            <label htmlFor="ownership" className="form-label">Set Private</label>
            <Switch
              id="privateStatus"
              className="react-switch"
              checked={updateIsPrivate}
              onChange={(checked) => setUpdateIsPrivate(checked)}
            />
        </div>
        
        <button type="submit" className="btn btn-primary">Update Quote</button>
      </form>

      <form className="w-50 mt-3" onSubmit={handleDeleteQuote}>
        <button type="submit" className="btn btn-danger">Delete Quote</button>
      </form>
    </div>
  );
};

export default QuoteForm; //export the QuoteForm component for use in the app
