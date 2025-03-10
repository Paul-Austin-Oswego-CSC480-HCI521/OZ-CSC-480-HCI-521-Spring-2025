import QuoteCard from "../components/QuoteCard";

const QuoteList = ({ topQuotes, loading, error }) => {

  const buttonStyle = {
    display: "flex",
    border: "none",
    padding: "11px 18px",
    justifyContent: "center",
    alignItems: "center",
    gap: "8px",
    borderRadius: "999px",
    background: "#146C43",

    color: "#FFF",
    fontFamily: "Lato",
    fontSize: "14px",
    fontStyle: "normal",
    fontWeight: "600",
    lineHeight: "100%", /* 14px */
  }

  return (
    <div style={{ padding: "40px", display: "flex", flexDirection: "column", gap: "24px", justifyContent: "center", alignItems: "center", width: "100%" }}>
      <div style={{display: "flex", justifyContent: "space-between", width: "100%"}}>
        <h1>Top Quotes</h1>
        <button style={buttonStyle}>View More</button>
      </div>
      <div className="d-flex w-100" style={{ gap: "40px", flexWrap: "wrap", justifyContent: "center" }}>
        {loading ? (
          <p className="text-center w-100">Loading quotes...</p>
        ) : error ? (
          <p className="text-center w-100">{error}</p>
        ) : topQuotes.length > 0 ? (
          topQuotes.map((quote) => <QuoteCard key={quote._id} quote={quote} />)
        ) : (
          <p className="text-center w-100">No quotes found.</p>
        )}
      </div>
    </div>
  );
};

export default QuoteList;
