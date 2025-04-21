import { useState } from "react";
import QuoteCard from "../components/QuoteCard";
import QuoteViewModal from "./QuoteViewModal";

const QuoteList = ({ topQuotes, loading, error, setAlert, setShowLogin }) => {

  const [viewedQuote, setViewedQuote] = useState(null);

  const closeView = () => setViewedQuote(null);
  const viewQuote = (quote) => setViewedQuote(quote)

  const handleBookmarkToggle = (updatedQuote, bookmarkState) => {
    console.log("Updated Quote:", updatedQuote);
    console.log("Bookmark State:", bookmarkState);
  };

  const handleQuoteUsed = (quoteId) => {
    const updatedUsedQuotes = [...usedQuotes, quoteId];
    setUsedQuotes(updatedUsedQuotes);
    localStorage.setItem(
      "usedQuotes",
      JSON.stringify(updatedUsedQuotes.map((id) => ({ id, usedDate: new Date().toISOString() })))
    );
    setFilteredQuotes((prev) => prev.filter((quote) => quote._id !== quoteId));
  };


  return (
    <>
    {viewedQuote ? <QuoteViewModal 
                      quote={viewedQuote} 
                      close={closeView}   
                      onBookmarkToggle={handleBookmarkToggle}
                      setAlert={setAlert} 
                      setShowLogin={setShowLogin}
                      onQuoteUsed={handleQuoteUsed}
                      /> : (<></>)}
    <div style={{ padding: "40px", display: "flex", flexDirection: "column", gap: "24px", justifyContent: "center", alignItems: "center", width: "100%" }}>
      <div style={{display: "flex", justifyContent: "space-between", width: "100%"}}>
        <h1>Top Quotes</h1>
        <button className="rounded-button-style">View More</button>
      </div>
      <div className="d-flex w-100" style={{ gap: "40px", flexWrap: "wrap", justifyContent: "center" }}>
        {loading ? (
          <p className="text-center w-100">Loading quotes...</p>
        ) : error ? (
          <p className="text-center w-100">{error}</p>
        ) : topQuotes.length > 0 ? (
          topQuotes.map((quote) => <QuoteCard key={quote._id} quote={quote} showViewModal={viewQuote} />)
        ) : (
          <p className="text-center w-100">No quotes found...</p>
        )}
      </div>
    </div>
    </>
  );
};

export default QuoteList;
