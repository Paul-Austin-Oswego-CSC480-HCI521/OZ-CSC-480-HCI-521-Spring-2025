import React, { useState } from "react";
import QuoteCard from "../components/QuoteCard";
import Input from "../components/Input";

const SavedQuotes = ({ userQuotes, bookmarkedQuotes }) => {
  const [searchTerm, setSearchTerm] = useState("");
  const [quotes, setQuotes] = useState([...userQuotes, ...bookmarkedQuotes]);

  const handleSearch = (event) => {
    setSearchTerm(event.target.value.toLowerCase());
  };

  const filteredQuotes = quotes.filter(({ author, text, tags }) => {
    return (
      author.toLowerCase().includes(searchTerm) ||
      text.toLowerCase().includes(searchTerm) ||
      tags.some(tag => tag.toLowerCase().includes(searchTerm))
    );
  });

  return (
    <div className="p-6 max-w-3xl mx-auto">
      <Input
        type="text"
        placeholder="Search by tag, author, or keyword..."
        value={searchTerm}
        onChange={handleSearch}
        className="mb-4"
      />
      <div className="row w-100">
          {filteredQuotes.length > 0 ? (
            filteredQuotes.map((quote) => (
              <QuoteCard key={quote.id} quote={quote} />
            ))
          ) : (
            <p className="text-center w-100">No quotes found.</p>
          )}
        </div>
    </div>
  );
};

export default SavedQuotes;
