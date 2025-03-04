import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { FaBookmark, FaRegBookmark, FaShareAlt, FaFlag, FaClipboard } from 'react-icons/fa';

const QuoteCard = ({ quote, onBookmarkToggle }) => {
  const navigate = useNavigate();
  const [isBookmarked, setIsBookmarked] = useState(false);
  const [bookmarkCount, setBookmarkCount] = useState(quote.bookmarks || 0);
  const [shareUser, setShareUser] = useState("");

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

  const handleBookmarkClick = (e) => {
    e.stopPropagation();
    const newBookmarkState = !isBookmarked;
    setIsBookmarked(newBookmarkState);
    setBookmarkCount((prevCount) => newBookmarkState ? prevCount + 1 : prevCount - 1);
    onBookmarkToggle(quote._id, newBookmarkState);
  };

  const handleClipboardClick = (e) => {
    e.stopPropagation();
    const textToCopy = `"${quote.quote}" - ${quote.author}`;
    navigator.clipboard.writeText(textToCopy).then(() => {
      alert('Quote copied to clipboard!');
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

  return (
    <div className="col-md-4 mb-4" onClick={handleClick}>
      <div className="card shadow p-3">
        <div className="card-body">
          <p className="card-text">"{quote.quote || "No quote text provided"}"</p>
          <h6 className="text-muted">- {quote.author || "Unknown"}</h6>

          {quote.tags && quote.tags.length > 0 && (
            <div className="mb-2">
              <strong>Tags: </strong>
              {quote.tags.map((tag, index) => (
                <span key={index} className="badge bg-primary me-1">{tag}</span>
              ))}
            </div>
          )}

          <button className="btn btn-link me-2" onClick={handleBookmarkClick}>
            {isBookmarked ? <FaBookmark /> : <FaRegBookmark />}
          </button>
          <span className="me-2">{bookmarkCount}</span>
          <button className="btn btn-link me-2" onClick={handleClipboardClick}>
            <FaClipboard />
          </button>
          <button className="btn btn-link me-2" onClick={handleShareClick}>
            <FaShareAlt />
          </button>
          <button className="btn btn-link text-danger" onClick={handleFlagClick}>
            <FaFlag />
          </button>
        </div>
      </div>
    </div>
  );
};

export default QuoteCard;
