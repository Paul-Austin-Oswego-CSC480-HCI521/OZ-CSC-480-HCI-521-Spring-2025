import Tag from "./Tag";
import { useState, useEffect, useContext } from "react";
import QuoteActions from "../buttons/QuoteActions";
import QuoteUseButton from "../buttons/QuoteUseButton";
import { UserContext } from "../lib/Contexts";
import AlertMessage from "./AlertMessage";

export default function QuoteViewModal({ quote, close, onBookmarkToggle, onQuoteUsed }) {
  const [showLogin, setShowLogin] = useState(false);
  const [usedDate, setUsedDate] = useState(null);
  const [user] = useContext(UserContext);
  const [editable, setEditable] = useState(false);

    useEffect(() => {
      setEditable(user?.MyQuotes.includes(quote._id) || user?.admin || false);
  
      const usedQuotes = JSON.parse(localStorage.getItem("usedQuotes")) || [];
      const usedQuote = usedQuotes.find((q) => q.id === quote._id);
      setUsedDate(usedQuote?.usedDate || null);
    }, [user, quote]);


  return (
    <div className="modal show" style={{ display: "block" }}>
      <div className="modal-dialog modal-dialog-centered">
        <div
          className="modal-content"
          style={{ backgroundColor: "#F8FDF1", borderRadius: "10px", padding: "10px 10px 25px 25px" }}
        >
          {alert && (
            <div className="position-fixed top-0 start-50 translate-middle-x mt-3 px-4" style={{ zIndex: 9999 }}>
              <AlertMessage type={alert.type} message={alert.message} />
            </div>
          )}

          {showLogin && <LoginOverlay setShowLogin={setShowLogin} />}

          <div className="modal-header" style={{borderBottom:"none"}}>
            <div>
              {quote.tags.map((tag, i) => (
                <Tag key={i} text={tag} />
              ))}
            </div>
            <button type="button" className="btn-close" onClick={close}></button>
          </div>
          <div className="modal-body">
            <p style={{ color: "#1E1E1E", fontSize: "24px", textAlign: "left" }}>{'"' + quote.quote + '"'}</p>
            <p style={{ textAlign: "left", marginBottom: "40px" }}>— {quote.author}</p>
            <QuoteActions
              quote={quote}
              onBookmarkToggle={onBookmarkToggle}
              setShowLogin={setShowLogin}
            />
            {usedDate && (
              <div style={{ marginBottom: "40px", fontSize: "14px", fontStyle: "italic", color: "#5A5A5A" }}>
                Used on: {new Date(usedDate).toLocaleDateString()}
              </div>
            )}
            <div style={{ display: "flex", gap: "12px", justifyContent: "space-between", marginTop: "20px" }}>
              <QuoteUseButton
                quote={quote}
                setShowLogin={setShowLogin}
                onQuoteUsed={onQuoteUsed}
              />
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
