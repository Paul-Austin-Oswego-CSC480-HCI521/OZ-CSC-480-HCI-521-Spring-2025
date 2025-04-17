import React, { useState } from "react";
import { BookmarkFill, Bookmark, Share, Flag, Clipboard } from "react-bootstrap-icons";
import { bookmarkQuote, deleteBookmark } from "../lib/api";
import ShareQuotePopup from "./ShareQuotePopup";
import { shareQuote } from "../lib/api"; // near top

const QuoteActions = ({ quote, onBookmarkToggle, setAlert, setShowLogin, user }) => {
    const [isBookmarked, setIsBookmarked] = useState(false);
    const [bookmarkCount, setBookmarkCount] = useState(quote.bookmarks || 0);
    const [showSharePopup, setShowSharePopup] = useState(false);

    const handleBookmarkClick = async (e) => {
        e.stopPropagation();

        if (!setAlert) {
            setAlert({ type: "danger", message: "Please sign in to bookmark!" });
            setShowLogin(true);
            return;
        }

        const newBookmarkState = !isBookmarked;
        setIsBookmarked(newBookmarkState);
        setBookmarkCount((prevCount) =>
            newBookmarkState ? prevCount + 1 : prevCount - 1
        );

        try {
            let updatedQuote;
            if (newBookmarkState) {
                updatedQuote = await bookmarkQuote(quote._id);
            } else {
                await deleteBookmark(quote._id);
            }
            if (typeof onBookmarkToggle === "function") {
                onBookmarkToggle(updatedQuote || quote, newBookmarkState);
            }
        } catch (error) {
            console.error("Error updating bookmark:", error);
        }
    };

    const handleShareClick = (e) => {
        e.stopPropagation();
        setShowSharePopup(prev => !prev);
    };


    const handleSendQuote = async (user) => {

        if ( !setAlert){
            setAlert({ type: "danger", message: "Please sign in to share!"})
            return;
        }

        try {
            await shareQuote(quote._id, user.email);
            setAlert({
                type: "success",
                message: `Quote successfully shared with ${user.name}!`,
            });
        } catch (error) {
            console.error("Error sharing quote:", error);
            setAlert({
                type: "danger",
                message: "Failed to share the quote. Please try again.",
            });
        }
    };


    const handleFlagClick = (e) => {
        e.stopPropagation();

        if (!setAlert){
            setAlert({ type: "danger", message: "To report this quote, Please sign in" });
            return;
        }

        alert("Quote has been reported. Our team will review it shortly.");
    };

    const handleCopyClick = (e) => {
        e.stopPropagation();
        navigator.clipboard.writeText(quote.quote).then(() => {
            setAlert({ type: "success", message: "Quote copied to clipboard!" });
        }).catch((error) => {
            console.error("Error copying quote:", error);
            setAlert({ type: "danger", message: "Failed to copy quote." });
        });
    };

    return (
        <div
            style={{
                position: "absolute",
                bottom: "10px",
                left: "10px",
                display: "flex",
                gap: "12px",
            }}
        >
            <button
                onClick={handleBookmarkClick}
                style={{
                    background: "none",
                    border: "none",
                    cursor: "pointer",
                    display: "flex",
                    alignItems: "center",
                    gap: "4px",
                    color: isBookmarked ? "green" : "inherit",
                    opacity: user ? "1" : "0.5",
                }}
            >
                {isBookmarked ? <BookmarkFill size={22} /> : <Bookmark size={22} />}
                <span
                    style={{ fontSize: "14px", fontWeight: "500", color: "#5A5A5A" }}
                >
                    {bookmarkCount}
                </span>
            </button>

            <div style={{ position: "relative" }}>
                <button
                    onClick={handleShareClick}
                    style={{ 
                        background: "none",
                        border: "none",
                        cursor: "pointer",
                        opacity: user ? "1" : "0.5",
                    }}
                >
                    <Share size={22} />
                </button>

                {showSharePopup && (
                    <div
                        style={{
                            position: "absolute",
                            top: "30px",
                            left: "0",
                            zIndex: 10,
                        }}
                    >
                        <ShareQuotePopup
                            quote={quote}
                            onClose={() => setShowSharePopup(false)}
                            onSend={handleSendQuote}
                        />
                    </div>
                )}
            </div>


            <button
                onClick={handleFlagClick}
                style={{
                    background: "none",
                    border: "none",
                    cursor: "pointer",
                    color: "#8B0000",
                    opacity: user ? "1" : "0.5",
                }}
            >
                <Flag size={22} />
            </button>

            <button
                onClick={handleCopyClick}
                style={{
                    background: "none",
                    border: "none",
                    cursor: "pointer",
                    color: "#146C43",
                }}
                title="Copy Quote"
            >
                <Clipboard size={22} />
            </button>
        </div>
    );
};

export default QuoteActions;