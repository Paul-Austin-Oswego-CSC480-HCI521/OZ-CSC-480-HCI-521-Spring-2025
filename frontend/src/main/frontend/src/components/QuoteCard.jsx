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
      <div
          className="card shadow-sm rounded-3 p-3"
          style={{ minWidth: "280px", cursor: "pointer", transition: "all 0.2s ease-in-out" }}
          onClick={handleClick}>

        <div className="card-body">
          <p className="fs-5 fw-semibold text-dark mb-3 text-center">"{quote.quote}"</p>

          <div className="d-flex align-items-center mb-3">
            <div
                className="rounded-circle bg-secondary d-flex justify-content-center align-items-center me-2"
                style={{ width: "32px", height: "32px", color: "white" }}>
              <i className="bi bi-person"></i>
            </div>
            <span className="text-muted fw-medium">{quote.author || "Unknown"}</span>
          </div>


          {quote.tags && quote.tags.length > 0 && (
              <div className="mt-2">
                {quote.tags.map((tag, index) => (
                    <span key={index} className="badge bg-light text-dark me-1">
                  #{tag}
                </span>
                ))}
              </div>
          )}

          <div className="d-flex justify-content-between align-items-center mt-3">
            <div>
              <button className="btn btn-outline-secondary btn-sm me-2" onClick={handleBookmarkClick}>
                {isBookmarked ? <FaBookmark /> : <FaRegBookmark />}
              </button>
              <span className="text-muted">{bookmarkCount}</span>
            </div>

            <div>
              <button className="btn btn-outline-secondary btn-sm me-2" onClick={handleClipboardClick}>
                <FaClipboard />
              </button>
              <button className="btn btn-outline-secondary btn-sm me-2" onClick={handleShareClick}>
                <FaShareAlt />
              </button>
              <button className="btn btn-outline-danger btn-sm" onClick={handleFlagClick}>
                <FaFlag />
          </button>
        </div>
      </div>
    </div>
      </div>
    </div>
  );
};

export default QuoteCard;
