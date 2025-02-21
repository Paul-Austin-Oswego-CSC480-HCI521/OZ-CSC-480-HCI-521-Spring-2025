import React, { useState } from "react";
import {
  createQuote,
  deleteQuote,
  searchQuotes,
  updateQuote,
  fetchTopBookmarkedQuotes,
  fetchTopSharedQuotes,
  fetchUserProfile,
} from "../lib/api.js"; // Adjust path if needed

// Example inline styles
const cardStyle = {
  border: "1px solid #ccc",
  padding: "1rem",
  marginBottom: "2rem",
  borderRadius: "8px",
  boxShadow: "0 2px 5px rgba(0,0,0,0.1)",
};

const formRowStyle = {
  display: "flex",
  alignItems: "center",
  marginBottom: "0.75rem",
};

const labelStyle = {
  width: "130px",
  fontWeight: "bold",
};

const inputStyle = {
  flex: "1",
  padding: "0.4rem",
  marginLeft: "0.5rem",
  borderRadius: "4px",
  border: "1px solid #ccc",
};

const DebugPage = () => {
  //
  // 1) CREATE QUOTE
  //
  const [createAuthor, setCreateAuthor] = useState("");
  const [createText, setCreateText] = useState("");
  const [createTagsInput, setCreateTagsInput] = useState("example, test");
  const [createResult, setCreateResult] = useState(null);

  const handleCreateQuote = async (e) => {
    e.preventDefault();
    const tagsArray = createTagsInput
      .split(",")
      .map((tag) => tag.trim())
      .filter((tag) => tag.length > 0);

    try {
      const result = await createQuote({
        quote: createText,
        author: createAuthor,
        tags: tagsArray,
      });
      setCreateResult(result);
    } catch (error) {
      setCreateResult(`Error creating quote: ${error.message}`);
    }
  };

  //
  // 2) DELETE QUOTE
  //
  const [deleteId, setDeleteId] = useState("");
  const [deleteResult, setDeleteResult] = useState(null);

  const handleDeleteQuote = async (e) => {
    e.preventDefault();
    try {
      const result = await deleteQuote(deleteId);
      setDeleteResult(result);
    } catch (error) {
      setDeleteResult(`Error deleting quote: ${error.message}`);
    }
  };

  //
  // 3) SEARCH QUOTE BY ID
  //
  const [searchId, setSearchId] = useState("");
  const [searchIdResult, setSearchIdResult] = useState(null);

  const handleSearchById = async (e) => {
    e.preventDefault();
    try {
      const result = await searchQuotes(searchId, true); // isQuoteID = true
      setSearchIdResult(result);
    } catch (error) {
      setSearchIdResult(`Error searching quote by ID: ${error.message}`);
    }
  };

  //
  // 4) SEARCH QUOTES BY TEXT
  //
  const [searchText, setSearchText] = useState("");
  const [searchTextResult, setSearchTextResult] = useState(null);

  const handleSearchByText = async (e) => {
    e.preventDefault();
    try {
      const result = await searchQuotes(searchText, false); // isQuoteID = false
      setSearchTextResult(result);
    } catch (error) {
      setSearchTextResult(`Error searching quotes by text: ${error.message}`);
    }
  };

  //
  // 5) UPDATE QUOTE
  //
  const [updateId, setUpdateId] = useState("");
  const [updateAuthor, setUpdateAuthor] = useState("");
  const [updateText, setUpdateText] = useState("");
  const [updateTagsInput, setUpdateTagsInput] = useState("");
  const [updateResult, setUpdateResult] = useState(null);

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
  

  //
  // 6) FETCH TOP BOOKMARKED
  //
  const [topBookmarkedResult, setTopBookmarkedResult] = useState(null);

  const handleFetchTopBookmarked = async () => {
    try {
      const data = await fetchTopBookmarkedQuotes();
      setTopBookmarkedResult(data);
    } catch (error) {
      setTopBookmarkedResult(`Error: ${error.message}`);
    }
  };

  //
  // 7) FETCH TOP SHARED
  //
  const [topSharedResult, setTopSharedResult] = useState(null);

  const handleFetchTopShared = async () => {
    try {
      const data = await fetchTopSharedQuotes();
      setTopSharedResult(data);
    } catch (error) {
      setTopSharedResult(`Error: ${error.message}`);
    }
  };

  //
  // 8) FETCH USER PROFILE
  //
  const [userId, setUserId] = useState("");
  const [userProfileResult, setUserProfileResult] = useState(null);

  const handleFetchUserProfile = async (e) => {
    e.preventDefault();
    try {
      const data = await fetchUserProfile(userId);
      setUserProfileResult(data);
    } catch (error) {
      setUserProfileResult(`Error: ${error.message}`);
    }
  };

  return (
    <div style={{ padding: "1rem", fontFamily: "Arial, sans-serif" }}>
      <h1 style={{ marginBottom: "1.5rem" }}>
        Debug Page - Quotes & Users API
      </h1>

      {/* 1) CREATE QUOTE */}
      <div style={cardStyle}>
        <h2>Create Quote</h2>
        <form onSubmit={handleCreateQuote}>
          <div style={formRowStyle}>
            <label style={labelStyle}>Author:</label>
            <input
              style={inputStyle}
              type="text"
              value={createAuthor}
              onChange={(e) => setCreateAuthor(e.target.value)}
              required
            />
          </div>
          <div style={formRowStyle}>
            <label style={labelStyle}>Text:</label>
            <input
              style={inputStyle}
              type="text"
              value={createText}
              onChange={(e) => setCreateText(e.target.value)}
              required
            />
          </div>
          <div style={formRowStyle}>
            <label style={labelStyle}>Tags (comma-separated):</label>
            <input
              style={inputStyle}
              type="text"
              value={createTagsInput}
              onChange={(e) => setCreateTagsInput(e.target.value)}
              required
            />
          </div>
          <button type="submit" style={{ marginTop: "0.5rem" }}>
            Create Quote
          </button>
        </form>
        {createResult && (
          <pre
            style={{
              background: "#f9f9f9",
              padding: "1rem",
              marginTop: "1rem",
            }}
          >
            {JSON.stringify(createResult, null, 2)}
          </pre>
        )}
      </div>

      {/* 2) DELETE QUOTE */}
      <div style={cardStyle}>
        <h2>Delete Quote</h2>
        <form onSubmit={handleDeleteQuote}>
          <div style={formRowStyle}>
            <label style={labelStyle}>Quote ID:</label>
            <input
              style={inputStyle}
              type="text"
              value={deleteId}
              onChange={(e) => setDeleteId(e.target.value)}
              required
            />
          </div>
          <button type="submit" style={{ marginTop: "0.5rem" }}>
            Delete
          </button>
        </form>
        {deleteResult && (
          <pre
            style={{
              background: "#f9f9f9",
              padding: "1rem",
              marginTop: "1rem",
            }}
          >
            {JSON.stringify(deleteResult, null, 2)}
          </pre>
        )}
      </div>

      {/* 3) SEARCH BY ID */}
      <div style={cardStyle}>
        <h2>Get Quote By ID</h2>
        <form onSubmit={handleSearchById}>
          <div style={formRowStyle}>
            <label style={labelStyle}>Quote ID:</label>
            <input
              style={inputStyle}
              type="text"
              value={searchId}
              onChange={(e) => setSearchId(e.target.value)}
              required
            />
          </div>
          <button type="submit" style={{ marginTop: "0.5rem" }}>
            Search by ID
          </button>
        </form>
        {searchIdResult && (
          <pre
            style={{
              background: "#f9f9f9",
              padding: "1rem",
              marginTop: "1rem",
            }}
          >
            {JSON.stringify(searchIdResult, null, 2)}
          </pre>
        )}
      </div>

      {/* 4) SEARCH BY TEXT */}
      <div style={cardStyle}>
        <h2>Search Quotes By Text</h2>
        <form onSubmit={handleSearchByText}>
          <div style={formRowStyle}>
            <label style={labelStyle}>Search Query:</label>
            <input
              style={inputStyle}
              type="text"
              value={searchText}
              onChange={(e) => setSearchText(e.target.value)}
              required
            />
          </div>
          <button type="submit" style={{ marginTop: "0.5rem" }}>
            Search
          </button>
        </form>
        {searchTextResult && (
          <pre
            style={{
              background: "#f9f9f9",
              padding: "1rem",
              marginTop: "1rem",
            }}
          >
            {JSON.stringify(searchTextResult, null, 2)}
          </pre>
        )}
      </div>

      {/* 5) UPDATE QUOTE */}
      <div style={cardStyle}>
        <h2>Update Quote</h2>
        <form onSubmit={handleUpdateQuote}>
          <div style={formRowStyle}>
            <label style={labelStyle}>Quote ID (required):</label>
            <input
              style={inputStyle}
              type="text"
              value={updateId}
              onChange={(e) => setUpdateId(e.target.value)}
              required
            />
          </div>
          <div style={formRowStyle}>
            <label style={labelStyle}>New Author (optional):</label>
            <input
              style={inputStyle}
              type="text"
              value={updateAuthor}
              onChange={(e) => setUpdateAuthor(e.target.value)}
            />
          </div>
          <div style={formRowStyle}>
            <label style={labelStyle}>New Text (optional):</label>
            <input
              style={inputStyle}
              type="text"
              value={updateText}
              onChange={(e) => setUpdateText(e.target.value)}
            />
          </div>
          <div style={formRowStyle}>
            <label style={labelStyle}>New Tags (optional):</label>
            <input
              style={inputStyle}
              type="text"
              value={updateTagsInput}
              onChange={(e) => setUpdateTagsInput(e.target.value)}
            />
          </div>
          <button type="submit" style={{ marginTop: "0.5rem" }}>
            Update Quote
          </button>
        </form>
        {updateResult && (
          <pre
            style={{
              background: "#f9f9f9",
              padding: "1rem",
              marginTop: "1rem",
            }}
          >
            {JSON.stringify(updateResult, null, 2)}
          </pre>
        )}
      </div>

      {/* 6) TOP BOOKMARKED QUOTES */}
      <div style={cardStyle}>
        <h2>Top Bookmarked Quotes</h2>
        <button onClick={handleFetchTopBookmarked}>Fetch Top Bookmarked</button>
        {topBookmarkedResult && (
          <pre
            style={{
              background: "#f9f9f9",
              padding: "1rem",
              marginTop: "1rem",
            }}
          >
            {JSON.stringify(topBookmarkedResult, null, 2)}
          </pre>
        )}
      </div>

      {/* 7) TOP SHARED QUOTES */}
      <div style={cardStyle}>
        <h2>Top Shared Quotes</h2>
        <button onClick={handleFetchTopShared}>Fetch Top Shared</button>
        {topSharedResult && (
          <pre
            style={{
              background: "#f9f9f9",
              padding: "1rem",
              marginTop: "1rem",
            }}
          >
            {JSON.stringify(topSharedResult, null, 2)}
          </pre>
        )}
      </div>

      {/* 8) FETCH USER PROFILE */}
      <div style={cardStyle}>
        <h2>Fetch User Profile</h2>
        <form onSubmit={handleFetchUserProfile}>
          <div style={formRowStyle}>
            <label style={labelStyle}>User ID:</label>
            <input
              style={inputStyle}
              type="text"
              value={userId}
              onChange={(e) => setUserId(e.target.value)}
              required
            />
          </div>
          <button type="submit" style={{ marginTop: "0.5rem" }}>
            Fetch User
          </button>
        </form>
        {userProfileResult && (
          <pre
            style={{
              background: "#f9f9f9",
              padding: "1rem",
              marginTop: "1rem",
            }}
          >
            {JSON.stringify(userProfileResult, null, 2)}
          </pre>
        )}
      </div>
    </div>
  );
};

export default DebugPage;
