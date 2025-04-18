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
  const [sharedQuotes, setSharedQuotes] = useState([]); 
  const [filteredQuotes, setFilteredQuotes] = useState([]);
  const [showUsed, setShowUsed] = useState(false);
  const [usedQuotes, setUsedQuotes] = useState([]);
  const [selectedTags, setSelectedTags] = useState([]); 
  const [user] = useContext(UserContext);

  useEffect(() => {
    if (!user?._id) return;

    const fetchData = async () => {
      try {
        const [userQuotes, bookmarkedQuotes, sharedQuotes] = await Promise.all([
          fetchUserQuotes(user._id.$oid),
          Promise.all(user.BookmarkedQuotes.map(fetchQuoteById)).then((quotes) =>
            quotes.filter(Boolean)
          ),
          Promise.all(
            user.SharedQuotes.map((shared) => fetchQuoteById(shared.quoteId))
          ).then((quotes) => quotes.filter(Boolean)),
        ]);
        setUserQuotes(userQuotes);
        setBookmarkedQuotes(bookmarkedQuotes);
        setSharedQuotes(sharedQuotes);
        setFilteredQuotes([...userQuotes, ...bookmarkedQuotes, ...sharedQuotes]);
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

    const matchesTags =
      selectedTags.length === 0 || selectedTags.every((tag) => tags.includes(tag));

    return matchesSearch && matchesTags && (showUsed ? usedQuotes.includes(_id) : !usedQuotes.includes(_id));
  });

  return (
    <div className="container">
      <div className="row">
        <div className="col-12 text-center mb-4">
          <Input
            type="text"
            placeholder="Search by tag, author, or keyword..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value.toLowerCase())}
            className="form-control"
          />
          <div className="mt-3">
            <ToggleButton
              isActive={!showUsed}
              onToggle={(isActive) => setShowUsed(!isActive)}
              labels={["Unused", "Used"]}
            />
          </div>
        </div>
      </div>
      <div className="row">
        <Sidebar
          userQuotes={userQuotes}
          bookmarkedQuotes={bookmarkedQuotes}
          sharedQuotes={sharedQuotes} 
          onFilterChange={handleFilterChange}
          onTagSelect={setSelectedTags}
        />
        <div
          className="col"
          style={{
            paddingLeft: "70px", 
          }}
        >
          <div
            className="row w-100"
            style={{
              gap: "32px",
            }}
          >
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
    </div>
  );
};

export default MyCollection;
