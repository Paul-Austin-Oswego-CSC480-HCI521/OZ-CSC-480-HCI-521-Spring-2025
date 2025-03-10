import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { bookmarkQuote, deleteBookmark } from "../lib/api";
import Tag from "./Tag";
import { BookmarkFill, Bookmark, Clipboard, Share, Flag } from 'react-bootstrap-icons';

const QuoteCard = ({ quote, onBookmarkToggle }) => {
  const navigate = useNavigate();
  const [isBookmarked, setIsBookmarked] = useState(false);
  const [bookmarkCount, setBookmarkCount] = useState(quote.bookmarks || 0);

  useEffect(() => {
    const bookmarkedQuotes = JSON.parse(localStorage.getItem('bookmarkedQuotes')) || [];
    if (bookmarkedQuotes.includes(quote._id)) {
      setIsBookmarked(true);
    }
  }, [quote._id]);

  const handleBookmarkClick = async (e) => {
    e.stopPropagation();

    const newBookmarkState = !isBookmarked;
    setIsBookmarked(newBookmarkState);
    setBookmarkCount((prevCount) => newBookmarkState ? prevCount + 1 : prevCount - 1);

    try {
      let updatedQuote;
      const bookmarkedQuotes = JSON.parse(localStorage.getItem('bookmarkedQuotes')) || [];
      if (newBookmarkState) {
        updatedQuote = await bookmarkQuote(quote._id);
        localStorage.setItem('bookmarkedQuotes', JSON.stringify([...bookmarkedQuotes, quote._id]));
      } else {
        await deleteBookmark(quote._id);
        localStorage.setItem('bookmarkedQuotes', JSON.stringify(bookmarkedQuotes.filter(id => id !== quote._id)));
      }
      onBookmarkToggle(updatedQuote || quote, newBookmarkState);
    } catch (error) {
      console.error("Error updating bookmark:", error);
    }
  };

  const handleClipboardClick = (e) => {
    e.stopPropagation();
    const textToCopy = `"${quote.quote}" - ${quote.author}`;
    navigator.clipboard.writeText(textToCopy).then(() => {
    });
  };

  const handleShareClick = (e) => {
    e.stopPropagation();
    const user = prompt("Enter the email to share this quote with:");
    if (user) {
      setShareUser(user);
      alert(`Quote shared with ${user}!`);
    }
  };

  const handleFlagClick = (e) => {
    e.stopPropagation();
    alert('Quote has been reported. Our team will review it shortly.');
  };

  const handleClick = () => {
    navigate(`/edit-quote/${quote._id}`, {
      state: {
        quote: {
          _id: quote._id,
          text: quote.quote,
          author: quote.author,
          tags: quote.tags || [],
          bookmarks: quote.bookmarks,
          shares: quote.shares,
          flags: quote.flags,
          date: quote.date
        }
      }
    });
  };

  const quoteTextStyle = {
    color: "#1E1E1E",
    fontFamily: "Inter",
    textAlign: "left",
    fontSize: "20px",
    fontStyle: "normal",
    fontWeight: "500",
    lineHeight: "normal",
  };

  return (
    <div
      onClick={handleClick}
      style={{
        background: "#D6F0C2",
        borderRadius: "23px",
        border: "1px solid rgba(0, 0, 0, 0.10)",
        padding: "20px",
        width: "100%",
        maxWidth: "378px",
        cursor: "pointer",
        display: "flex",
        flexDirection: "column",
        gap: "10px",
        position: "relative",
        minHeight: "200px",
      }}
    >
      {/* Tags */}
      <div style={{ display: "flex", gap: "2px", flexWrap: "wrap" }}>
        {quote.tags?.map((tag, index) => <Tag text={tag} key={index} />)}
      </div>

      <div style={{
        display: "flex",
        flexDirection: "column",
        alignItems: "start",
        justifyContent: "flex-start",
        width: "100%"
      }}>
        {/* Quote Text */}
        <span style={quoteTextStyle}>
          {'"' + quote.quote + '"'}
        </span>

        {/* Author */}
        <span style={{
          color: "#5A5A5A",
          fontWeight: "bold",
          marginTop: "10px",
          fontSize: "14px"
        }}>
          — {quote.author}
        </span>
      </div>

      {/* Icons */}
      <div style={{
        position: "absolute",
        bottom: "10px",
        right: "10px",
        display: "flex",
        gap: "12px"
      }}>
        <button onClick={handleClipboardClick} style={{ background: "none", border: "none", cursor: "pointer" }}>
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path id="Vector" d="M24 3.69231V16.6154C24 17.5946 23.6839 18.5338 23.1213 19.2262C22.5587 19.9187 21.7956 20.3077 21 20.3077C21 21.287 20.6839 22.2261 20.1213 22.9185C19.5587 23.611 18.7956 24 18 24H3C2.20435 24 1.44129 23.611 0.878681 22.9185C0.316071 22.2261 0 21.287 0 20.3077V7.38461C0 6.40535 0.316071 5.4662 0.878681 4.77376C1.44129 4.08132 2.20435 3.69231 3 3.69231C3 2.71305 3.31607 1.7739 3.87868 1.08145C4.44129 0.389011 5.20435 0 6 0H21C21.7956 0 22.5587 0.389011 23.1213 1.08145C23.6839 1.7739 24 2.71305 24 3.69231ZM4.5 3.69231H18C18.7956 3.69231 19.5587 4.08132 20.1213 4.77376C20.6839 5.4662 21 6.40535 21 7.38461V18.4615C21.3978 18.4615 21.7794 18.267 22.0607 17.9208C22.342 17.5746 22.5 17.105 22.5 16.6154V3.69231C22.5 3.20268 22.342 2.7331 22.0607 2.38688C21.7794 2.04066 21.3978 1.84615 21 1.84615H6C5.60217 1.84615 5.22064 2.04066 4.93934 2.38688C4.65804 2.7331 4.5 3.20268 4.5 3.69231ZM18 22.1538C18.3978 22.1538 18.7794 21.9593 19.0607 21.6131C19.342 21.2669 19.5 20.7973 19.5 20.3077V7.38461C19.5 6.89499 19.342 6.42541 19.0607 6.07919C18.7794 5.73297 18.3978 5.53846 18 5.53846H3C2.60217 5.53846 2.22064 5.73297 1.93934 6.07919C1.65804 6.42541 1.5 6.89499 1.5 7.38461V20.3077C1.5 20.7973 1.65804 21.2669 1.93934 21.6131C2.22064 21.9593 2.60217 22.1538 3 22.1538H18Z" fill="black"/>
          </svg>
        </button>

        <button onClick={handleBookmarkClick} style={{ background: "none", border: "none", cursor: "pointer", display: "flex", alignItems: "center", gap: "4px", color: isBookmarked ? "green" : "inherit" }}>
          <BookmarkFill size={22} />
          <span style={{ fontSize: "14px", fontWeight: "500", color: "#5A5A5A" }}>{bookmarkCount}</span>
        </button>

        <button onClick={handleShareClick} style={{ background: "none", border: "none", cursor: "pointer" }}>
          <Share size={22} />
        </button>

        <button onClick={handleFlagClick} style={{ background: "none", border: "none", cursor: "pointer", color: "#8B0000" }}>
          <Flag size={22} />
        </button>
      </div>
    </div>
  );
};

export default QuoteCard;
