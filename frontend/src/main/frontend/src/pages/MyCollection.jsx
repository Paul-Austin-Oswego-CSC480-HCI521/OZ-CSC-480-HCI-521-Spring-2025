import { useState, useEffect, useContext, useCallback } from "react";
import QuoteCard from "../components/QuoteCard";
import Input from "../components/Input";
import Sidebar from "../components/Sidebar";
import ToggleButton from "../buttons/ToggleButton";
import { fetchQuoteById, fetchUserQuotes } from "../lib/api";
import { UserContext } from "../lib/Contexts";

const MyCollection = () => { 
  const [searchTerm, setSearchTerm] = useState("");
  const [userQuotes, setUserQuotes] = useState([]);
  const [bookmarkedQuotes, setBookmarkedQuotes] = useState([]);
  const [filteredQuotes, setFilteredQuotes] = useState([]);
  const [showUsed, setShowUsed] = useState(false);
  const [usedQuotes, setUsedQuotes] = useState([]);
  const [user] = useContext(UserContext);

  useEffect(() => {
    if (!user?._id) return;

    const fetchData = async () => {
      try {
        const [userQuotes, bookmarkedQuotes] = await Promise.all([
          fetchUserQuotes(user._id.$oid),
          Promise.all(user.BookmarkedQuotes.map(fetchQuoteById)).then((quotes) =>
            quotes.filter(Boolean)
          ),
        ]);
        setUserQuotes(userQuotes);
        setBookmarkedQuotes(bookmarkedQuotes);
        setFilteredQuotes([...userQuotes, ...bookmarkedQuotes]);
      } catch (error) {
        console.error("Error fetching data:", error);
      }
    };

    fetchData();
  }, [user]);

  useEffect(() => {
    const storedUsedQuotes = JSON.parse(localStorage.getItem("usedQuotes")) || [];
    setUsedQuotes(storedUsedQuotes.map((quote) => quote.id));
  }, []);

  const handleFilterChange = useCallback((filtered) => {
    setFilteredQuotes((prev) => (JSON.stringify(prev) !== JSON.stringify(filtered) ? filtered : prev));
  }, []);

  const handleQuoteUsed = (quoteId) => {
    const updatedUsedQuotes = [...usedQuotes, quoteId];
    setUsedQuotes(updatedUsedQuotes);
    localStorage.setItem(
      "usedQuotes",
      JSON.stringify(updatedUsedQuotes.map((id) => ({ id, usedDate: new Date().toISOString() })))
    );
    setFilteredQuotes((prev) => prev.filter((quote) => quote._id !== quoteId));
  };

  const displayedQuotes = filteredQuotes.filter(({ _id, author, quote, tags }) => {
    const matchesSearch =
      author.toLowerCase().includes(searchTerm) ||
      quote.toLowerCase().includes(searchTerm) ||
      tags.some((tag) => tag.toLowerCase().includes(searchTerm));
    return matchesSearch && (showUsed ? usedQuotes.includes(_id) : !usedQuotes.includes(_id));
  });

  return (
    <div className="d-flex" style={{ marginLeft: "0" }}>
      <Sidebar
        userQuotes={userQuotes}
        bookmarkedQuotes={bookmarkedQuotes}
        onFilterChange={handleFilterChange}
      />
      <div className="p-6 max-w-3xl mx-auto" style={{ flex: 1 }}>
        <div className="mb-4" style={{ maxWidth: "500px", margin: "0 auto" }}>
          <Input
            type="text"
            placeholder="Search by tag, author, or keyword..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value.toLowerCase())}
            className="form-control"
          />
        </div>
        <div className="text-center mb-4">
          <ToggleButton
            isActive={!showUsed} 
            onToggle={(isActive) => setShowUsed(!isActive)}
            labels={["Unused", "Used"]}
          />
        </div>
        <div className="row w-100 gap-4">
          {displayedQuotes.length > 0 ? (
            displayedQuotes.map((quote) => (
              <QuoteCard
                key={quote._id}
                quote={quote}
                onQuoteUsed={handleQuoteUsed}
              />
            ))
          ) : (
            <p className="text-center w-100">No quotes found.</p>
          )}
        </div>
      </div>
    </div>
  );
};

export default MyCollection;
