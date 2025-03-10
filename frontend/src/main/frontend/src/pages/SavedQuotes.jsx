import React, { useState, useEffect } from "react";
import QuoteCard from "../components/QuoteCard";
import Input from "../components/Input";
import { fetchMe, fetchUserQuotes } from "../lib/api";

const SavedQuotes = () => {
  const [searchTerm, setSearchTerm] = useState("");
  const [quotes, setQuotes] = useState([]); //store all quotes for saved (uploaded and bookmarked)
  const [userId, setUserId] = useState(null); //store the current user's ID

  useEffect(() => {
    const fetchData = async () => {
      try {
        const user = await fetchMe();
        const userIdString = user._id.$oid || user._id;
        setUserId(userIdString);
        const userQuotes = await fetchUserQuotes(userIdString);
        setQuotes(userQuotes); 
      } catch (error) {
        console.error("Error fetching data:", error);
      }
    };

    if (!userId) {
      fetchData();
    }
  }, [userId]);

  const handleSearch = (event) => {
    //update search term when user types in the input field
    setSearchTerm(event.target.value.toLowerCase());
  };

  const handleBookmarkToggle = (updatedQuote, isBookmarked) => {
    setQuotes((prevQuotes) => {
      if (isBookmarked) {
        return [...prevQuotes, updatedQuote];
      } else {
        return prevQuotes.filter((quote) => quote._id !== updatedQuote._id);
      }
    });
  };


  return (
    <div className="p-6 max-w-3xl mx-auto">
      <Input
        type="text"
        placeholder="Search by tag, author, or keyword..."
        value={searchTerm}
        onChange={handleSearch}
        className="mb-4"
      />
      <div className="row w-100 gap-4">
        {quotes.length > 0 ? (
          //display quotes if available, otherwise show a 'No quotes found' message
          quotes.map((quote) => (
            <QuoteCard key={quote._id} quote={quote} onBookmarkToggle={handleBookmarkToggle} />
          ))
        ) : (
          <p className="text-center w-100">No quotes found.</p>
        )}
      </div>
    </div>
  );
};

export default SavedQuotes; 