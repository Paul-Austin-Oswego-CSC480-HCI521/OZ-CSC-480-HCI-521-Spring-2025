import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { FaBookmark, FaRegBookmark, FaShareAlt, FaFlag, FaClipboard } from 'react-icons/fa';

const QuoteCard = ({ quote, onBookmarkToggle }) => {
  const navigate = useNavigate();
  const [isBookmarked, setIsBookmarked] = useState(false);
  const [bookmarkCount, setBookmarkCount] = useState(0);
  const [shareUser, setShareUser] = useState("");

  const handleClick = () => {
    navigate(`/quote/${quote.id}`, { state: { quote } });
  };

  const handleBookmarkClick = (e) => {
    e.stopPropagation();
    const newBookmarkState = !isBookmarked;
    setIsBookmarked(newBookmarkState);
    setBookmarkCount((prevCount) => newBookmarkState ? prevCount + 1 : prevCount - 1);
    onBookmarkToggle(quote.quoteId, newBookmarkState);
  };

  const handleClipboardClick = (e) => {
    e.stopPropagation();
    const textToCopy = `"${quote.text}" - ${quote.author}`;
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
          <p className="card-text">"{quote.text}"</p>
          <h6 className="text-muted">- {quote.author}</h6>
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
