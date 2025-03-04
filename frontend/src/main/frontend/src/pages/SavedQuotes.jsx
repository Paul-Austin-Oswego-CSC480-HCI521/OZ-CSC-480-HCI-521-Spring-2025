import React, { useState } from "react"; 
import QuoteCard from "../components/QuoteCard"; //import QuoteCard component for displaying individual quotes
import Input from "../components/Input"; //Input component for search functionality

const SavedQuotes = ({ userQuotes, bookmarkedQuotes }) => {
  const [searchTerm, setSearchTerm] = useState(""); //store the search term entered by the user
  const [quotes, setQuotes] = useState([...userQuotes, ...bookmarkedQuotes]); //combine user and bookmarked quotes into a single list

  const handleSearch = (event) => {
    //update search term when user types in the input field
    setSearchTerm(event.target.value.toLowerCase());
  };

  const filteredQuotes = quotes.filter(({ author, text, tags }) => {
    //filter quotes based on search term matching author, text, or tags
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
            //display filtered quotes if available, otherwise show a 'No quotes found' message
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

export default SavedQuotes; //export the component for use in other parts of the app
