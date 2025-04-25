import { useState, useEffect, useContext } from "react";
import { UserContext } from "../lib/Contexts";
import AlertMessage from "./AlertMessage";
import LoginOverlay from "./LoginOverlay";
import QuoteTags from "./QuoteTags";
import QuoteContent from "./QuoteContent";
import QuoteActions from "../buttons/QuoteActions";
import QuoteUseButton from "../buttons/QuoteUseButton";

const QuoteCard = ({ quote, onBookmarkToggle, showViewModal, onQuoteUsed }) => {
  const [user] = useContext(UserContext);
  const [showLogin, setShowLogin] = useState(false);
  const [usedDate, setUsedDate] = useState(null);

  useEffect(() => {
    const usedQuotes = JSON.parse(localStorage.getItem("usedQuotes")) || [];
    const usedQuote = usedQuotes.find((q) => q.id === quote._id);
    setUsedDate(usedQuote?.usedDate || null);
  }, [quote])

  return (
    <div
      onClick={() => showViewModal(quote)}
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
        minHeight: "240px",
      }}
    >
      {alert && (
        <div className="position-fixed top-0 start-50 translate-middle-x mt-3 px-4" style={{ zIndex: 9999 }}>
          <AlertMessage type={alert.type} message={alert.message} />
        </div>
      )}

      {showLogin && <LoginOverlay setShowLogin={setShowLogin} />}

      <div style={{ display: "flex", gap: "10px" }}>
        <QuoteTags tags={(quote.tags || []).slice(0, 3)} />
        {quote.tags && quote.tags.length > 3 && (
          <span style={{ fontSize: "14px", fontWeight: "bold", color: "#5A5A5A" }}>
            +{quote.tags.length - 3}
          </span>
        )}
      </div>
      <QuoteContent quote={quote} />
      <QuoteActions
        quote={quote}
        onBookmarkToggle={onBookmarkToggle}
        setShowLogin={setShowLogin}
        user={user}
      />
      {usedDate && (
        <div style={{ marginTop: "0px", fontSize: "14px", fontStyle: "italic", color: "#5A5A5A" }}>
          Used on: {new Date(usedDate).toLocaleDateString()}
        </div>
      )}
      {user && <QuoteUseButton
        quote={quote}
        setShowLogin={setShowLogin}
        onQuoteUsed={onQuoteUsed}
      />}
    </div>
  );
};

export default QuoteCard;
