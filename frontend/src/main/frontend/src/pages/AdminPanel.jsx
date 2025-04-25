import React, { useEffect, useState } from "react";
import { fetchReportedQuotes } from "../lib/api";
import QuoteCard from "../components/QuoteCard";

const AdminPanel = () => {
  const [reportedQuotes, setReportedQuotes] = useState([]);

  useEffect(() => {
    fetchReportedQuotes().then((reports) =>
      setReportedQuotes(reports.map((report) => report.quote).filter(Boolean))
    );
  }, []);

  return (
    <div style={{ padding: "20px" }}>
        <div style={{ display: "flex", flexWrap: "wrap", gap: "20px" }}>
          {reportedQuotes.map((quote) => (
            <QuoteCard key={quote._id} quote={quote} />
          ))}
        </div>
    </div>
  );
};

export default AdminPanel;
