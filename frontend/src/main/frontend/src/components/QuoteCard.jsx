import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { FaBookmark, FaRegBookmark, FaShareAlt, FaFlag, FaClipboard } from 'react-icons/fa';
import { bookmarkQuote, deleteBookmark } from "../lib/api";
import Tag from "./Tag";

const QuoteCard = ({ quote, onBookmarkToggle }) => {
  const navigate = useNavigate();
  const [isBookmarked, setIsBookmarked] = useState(quote.isBookmarked || false);
  const [bookmarkCount, setBookmarkCount] = useState(quote.bookmarks || 0);

  const handleBookmarkClick = async (e) => {
    e.stopPropagation();

    if (isBookmarked) {
      console.log("Quote is already bookmarked by the user.");
      return; // Prevent bookmarking the quote twice
    }

    const newBookmarkState = !isBookmarked;
    setIsBookmarked(newBookmarkState);
    setBookmarkCount((prevCount) => newBookmarkState ? prevCount + 1 : prevCount - 1);

    try {
      let updatedQuote;
      if (newBookmarkState) {
        updatedQuote = await bookmarkQuote(quote._id);
      } else {
        await deleteBookmark(quote._id);
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
      fontSize: "24px",
      fontStyle: "normal",
      fontWeight: "500",
      lineHeight: "normal",
    }

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
            {'"' +quote.quote + '"'}
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
            {/*<Copy size={18} color="#5A5A5A" />*/}
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path id="Vector" d="M24 3.69231V16.6154C24 17.5946 23.6839 18.5338 23.1213 19.2262C22.5587 19.9187 21.7956 20.3077 21 20.3077C21 21.287 20.6839 22.2261 20.1213 22.9185C19.5587 23.611 18.7956 24 18 24H3C2.20435 24 1.44129 23.611 0.878681 22.9185C0.316071 22.2261 0 21.287 0 20.3077V7.38461C0 6.40535 0.316071 5.4662 0.878681 4.77376C1.44129 4.08132 2.20435 3.69231 3 3.69231C3 2.71305 3.31607 1.7739 3.87868 1.08145C4.44129 0.389011 5.20435 0 6 0H21C21.7956 0 22.5587 0.389011 23.1213 1.08145C23.6839 1.7739 24 2.71305 24 3.69231ZM4.5 3.69231H18C18.7956 3.69231 19.5587 4.08132 20.1213 4.77376C20.6839 5.4662 21 6.40535 21 7.38461V18.4615C21.3978 18.4615 21.7794 18.267 22.0607 17.9208C22.342 17.5746 22.5 17.105 22.5 16.6154V3.69231C22.5 3.20268 22.342 2.7331 22.0607 2.38688C21.7794 2.04066 21.3978 1.84615 21 1.84615H6C5.60217 1.84615 5.22064 2.04066 4.93934 2.38688C4.65804 2.7331 4.5 3.20268 4.5 3.69231ZM18 22.1538C18.3978 22.1538 18.7794 21.9593 19.0607 21.6131C19.342 21.2669 19.5 20.7973 19.5 20.3077V7.38461C19.5 6.89499 19.342 6.42541 19.0607 6.07919C18.7794 5.73297 18.3978 5.53846 18 5.53846H3C2.60217 5.53846 2.22064 5.73297 1.93934 6.07919C1.65804 6.42541 1.5 6.89499 1.5 7.38461V20.3077C1.5 20.7973 1.65804 21.2669 1.93934 21.6131C2.22064 21.9593 2.60217 22.1538 3 22.1538H18Z" fill="black"/>
            </svg>
          </button>

          <button onClick={handleBookmarkClick} style={{ background: "none", border: "none", cursor: "pointer", display: "flex", alignItems: "center", gap: "4px" }}>
            {/*{isBookmarked ? <FaBookmark size={18} color="#2E7D32" /> : <FaRegBookmark size={18} color="#5A5A5A" />}*/}
            <svg width="23" height="24" viewBox="0 0 23 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <g id="Bookmark">
                <path id="Vector" d="M2.875 3C2.875 2.20435 3.1779 1.44129 3.71707 0.87868C4.25623 0.316071 4.9875 0 5.75 0L17.25 0C18.0125 0 18.7438 0.316071 19.2829 0.87868C19.8221 1.44129 20.125 2.20435 20.125 3V23.25C20.1249 23.3857 20.0896 23.5188 20.0228 23.6351C19.9559 23.7515 19.8601 23.8468 19.7455 23.9108C19.6309 23.9748 19.5018 24.0052 19.3719 23.9988C19.242 23.9923 19.1163 23.9492 19.0081 23.874L11.5 19.6515L3.99194 23.874C3.8837 23.9492 3.75796 23.9923 3.62809 23.9988C3.49823 24.0052 3.36912 23.9748 3.2545 23.9108C3.13988 23.8468 3.04406 23.7515 2.97723 23.6351C2.9104 23.5188 2.87507 23.3857 2.875 23.25V3ZM5.75 1.5C5.36875 1.5 5.00312 1.65804 4.73353 1.93934C4.46395 2.22064 4.3125 2.60218 4.3125 3V21.849L11.1018 18.126C11.2198 18.0441 11.3583 18.0004 11.5 18.0004C11.6417 18.0004 11.7802 18.0441 11.8982 18.126L18.6875 21.849V3C18.6875 2.60218 18.536 2.22064 18.2665 1.93934C17.9969 1.65804 17.6312 1.5 17.25 1.5H5.75Z" fill="#1E1E1E"/>
              </g>
            </svg>
            <span style={{ fontSize: "14px", fontWeight: "500", color: "#5A5A5A" }}>{bookmarkCount}</span>
          </button>

          <button onClick={handleShareClick} style={{ background: "none", border: "none", cursor: "pointer" }}>
            {/*<FaShareAlt size={18} color="#5A5A5A" />*/}
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path id="Vector" d="M16.5001 3.74995C16.5 2.87022 16.8093 2.01846 17.3738 1.3437C17.9383 0.668937 18.7222 0.214136 19.5882 0.0588685C20.4542 -0.0963994 21.3473 0.057753 22.1111 0.494355C22.875 0.930958 23.4609 1.62221 23.7665 2.44717C24.0721 3.27212 24.0779 4.17826 23.7828 5.00703C23.4877 5.8358 22.9106 6.53444 22.1523 6.98071C21.3941 7.42698 20.5031 7.59246 19.6352 7.4482C18.7672 7.30395 17.9777 6.85914 17.4046 6.1916L7.32778 10.8709C7.55934 11.605 7.55934 12.3926 7.32778 13.1266L17.4046 17.806C18.0104 17.1016 18.8561 16.6471 19.7779 16.5306C20.6996 16.4142 21.6318 16.644 22.3938 17.1756C23.1558 17.7072 23.6932 18.5027 23.902 19.4079C24.1108 20.3132 23.9761 21.2637 23.5239 22.0752C23.0717 22.8868 22.3344 23.5016 21.4547 23.8006C20.575 24.0995 19.6156 24.0613 18.7625 23.6933C17.9094 23.3253 17.2233 22.6537 16.8372 21.8087C16.4511 20.9637 16.3924 20.0055 16.6726 19.1198L6.59579 14.4404C6.09695 15.0217 5.43203 15.4363 4.69049 15.6284C3.94894 15.8205 3.16634 15.7809 2.44796 15.515C1.72959 15.2491 1.10989 14.7695 0.672248 14.1409C0.234602 13.5123 0 12.7647 0 11.9988C0 11.2329 0.234602 10.4853 0.672248 9.85666C1.10989 9.22803 1.72959 8.7485 2.44796 8.48256C3.16634 8.21663 3.94894 8.17706 4.69049 8.36917C5.43203 8.56128 6.09695 8.97586 6.59579 9.55713L16.6726 4.87779C16.5579 4.51284 16.4997 4.13249 16.5001 3.74995Z" fill="#1E1E1E"/>
            </svg>

          </button>

          <button onClick={handleFlagClick} style={{ background: "none", border: "none", cursor: "pointer" }}>
            {/*<FaFlag size={18} color="#B42318" />*/}
            <svg width="25" height="24" viewBox="0 0 25 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <g id="Flag">
                <path id="Vector" d="M23.0906 0.1275C23.1972 0.195956 23.2846 0.288554 23.3451 0.397119C23.4056 0.505684 23.4373 0.626877 23.4375 0.75V12C23.4375 12.1498 23.3907 12.2961 23.3033 12.4202C23.2158 12.5442 23.0917 12.6403 22.9469 12.696L22.9422 12.6975L22.9328 12.702L22.8969 12.7155C22.6915 12.794 22.4847 12.869 22.2766 12.9405C21.8641 13.083 21.2906 13.275 20.6406 13.4655C19.3656 13.8435 17.7047 14.25 16.4062 14.25C15.0828 14.25 13.9875 13.83 13.0344 13.4625L12.9906 13.4475C12 13.065 11.1562 12.75 10.1562 12.75C9.0625 12.75 7.59688 13.095 6.34844 13.4655C5.78956 13.633 5.23569 13.8156 4.6875 14.013V23.25C4.6875 23.4489 4.60519 23.6397 4.45868 23.7803C4.31216 23.921 4.11345 24 3.90625 24C3.69905 24 3.50034 23.921 3.35382 23.7803C3.20731 23.6397 3.125 23.4489 3.125 23.25V0.75C3.125 0.551088 3.20731 0.360322 3.35382 0.21967C3.50034 0.0790176 3.69905 0 3.90625 0C4.11345 0 4.31216 0.0790176 4.45868 0.21967C4.60519 0.360322 4.6875 0.551088 4.6875 0.75V1.173C5.04062 1.0545 5.4625 0.918 5.92188 0.783C7.19688 0.408 8.85938 0 10.1562 0C11.4688 0 12.5375 0.4155 13.4703 0.7785L13.5375 0.8055C14.5094 1.182 15.3562 1.5 16.4062 1.5C17.5 1.5 18.9656 1.155 20.2141 0.7845C20.9253 0.570706 21.6284 0.332554 22.3219 0.0705L22.3516 0.06L22.3578 0.057H22.3594M21.875 1.8315C21.5312 1.9485 21.125 2.082 20.6781 2.214C19.4125 2.592 17.7531 2.9985 16.4062 2.9985C15.0219 2.9985 13.9187 2.5695 12.9641 2.1975L12.9516 2.193C11.9719 1.815 11.1547 1.5 10.1562 1.5C9.11094 1.5 7.64687 1.8435 6.38281 2.217C5.8126 2.38567 5.24729 2.56924 4.6875 2.7675V12.417C5.03125 12.3 5.4375 12.1665 5.88438 12.0345C7.15 11.655 8.80937 11.25 10.1562 11.25C11.4797 11.25 12.575 11.67 13.5281 12.0375L13.5719 12.0525C14.5625 12.435 15.4062 12.75 16.4062 12.75C17.45 12.75 18.9156 12.4065 20.1797 12.033C20.7499 11.8643 21.3152 11.6808 21.875 11.4825V1.833V1.8315Z" fill="#B42318"/>
              </g>
            </svg>
          </button>
        </div>
      </div>
  );
};

export default QuoteCard;
