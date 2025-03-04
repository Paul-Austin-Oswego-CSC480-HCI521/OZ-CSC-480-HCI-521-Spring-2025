import { useState, useEffect } from "react";
import { fetchTopBookmarkedQuotes } from "../lib/api";

export const FetchTopQuotes = () => {
  const [topQuotes, setTopQuotes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const loadQuotes = async () => {
      try {
        console.log("Fetching top bookmarked quotes...");
        const data = await fetchTopBookmarkedQuotes();
        console.log("Fetched Quotes:", data);
        if (!data || data.length === 0) {
          setError("No quotes yet! Try adding your own");
        } else {
          setTopQuotes(data);
        }
      } catch (err) {
        console.error("Error fetching quotes:", err);
        setError("Failed to load quotes");
      } finally {
        setLoading(false);
      }
    };

    loadQuotes();
  }, []);

  return { topQuotes, loading, error };
};