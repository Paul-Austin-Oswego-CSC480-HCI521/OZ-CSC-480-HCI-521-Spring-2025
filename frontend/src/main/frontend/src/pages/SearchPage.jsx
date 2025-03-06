import React, { useState, useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import QuoteCard from '../components/QuoteCard';
import { searchQuotes } from '../lib/api';

const SearchPage = () => {
  const [searchResults, setSearchResults] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const location = useLocation();

  useEffect(() => {
    const query = new URLSearchParams(location.search).get('q');
    if (query) {
      (async () => {
        try {
          const results = await searchQuotes(query);
          setSearchResults(results);
        } catch (err) {
          setError(err.message);
        } finally {
          setLoading(false);
        }
      })();
    }
  }, [location.search]);

  return (
    <div className="container">
      <h1>Search Results</h1>
      {loading && <p>Loading...</p>}
      {error && <p>Error: {error}</p>}
      {searchResults.length > 0 ? (
        searchResults.map((quote) => (
          <QuoteCard key={quote.id} quote={quote} />
        ))
      ) : (
        !loading && <p>No quotes matched your search.</p>
      )}
    </div>
  );
};

export default SearchPage;