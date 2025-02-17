import React from "react";

const QuoteCard = ({ quote }) => {
  return (
    <div className="col-md-4 mb-4">
      <div className="card shadow p-3">
        <div className="card-body">
          <p className="card-text">"{quote.text}"</p>
          <h6 className="text-muted">- {quote.author}</h6>
        </div>
      </div>
    </div>
  );
};

export default QuoteCard;
