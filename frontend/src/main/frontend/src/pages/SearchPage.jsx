import React, { useState, useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import QuoteCard from '../components/QuoteCard';
import { searchQuotes } from '../lib/api';
import FilteredSearch from '../components/FilteredSearch';
import { Funnel } from 'react-bootstrap-icons';

const SearchPage = () => {
  const [searchResults, setSearchResults] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [isFilterModalVisible, setIsFilterModalVisible] = useState(false);
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
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h1>Search Results</h1>
        <Funnel
          size={24}
          style={{ cursor: 'pointer', color: '#146C43' }}
          onClick={() => setIsFilterModalVisible(true)}
        />
      </div>
      {loading && <p>Loading...</p>}
      {error && <p>Error: {error}</p>}
      {searchResults.length > 0 ? (
        searchResults.map((quote) => (
          <QuoteCard key={quote.id} quote={quote} />
        ))
      ) : (
        !loading && <p>No quotes matched your search.</p>
      )}
      <FilteredSearch
        isVisible={isFilterModalVisible}
        onClose={() => setIsFilterModalVisible(false)}
      />
    </div>
  );
};

export default SearchPage;