const QUOTE_SERVICE_URL =
  import.meta.env.VITE_QUOTE_SERVICE_URL || "http://localhost:9082";
const USER_SERVICE_URL =
  import.meta.env.VITE_USER_SERVICE_URL || "http://localhost:9081";

export const createQuote = async ({ quote, author, tags }) => {
  try {
    const quoteData = {
      quote,
      author: author || "Unknown",
      date: Math.floor(new Date().getTime() / 1000), //convert to Unix timestamp for int
      tags: tags || [],
    };

    console.log("Sending API Payload:", JSON.stringify(quoteData));

    const response = await fetch(`${QUOTE_SERVICE_URL}/quotes/create`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(quoteData),
    });

    if (!response.ok) {
      const errorMessage = await response.text();
      console.error("Backend Error:", errorMessage);
      throw new Error("Failed to create quote");
    }
    return await response.json();
  } catch (error) {
    console.error("Error creating quote:", error);
    // Re-throw or return a fallback value
    throw error;
  }
};

export const deleteQuote = async (quoteId) => {
  try {
    const response = await fetch(
      `${QUOTE_SERVICE_URL}/quotes/delete/${quoteId}`,
      {
        method: "DELETE",
      }
    );

    const contentType = response.headers.get("Content-Type");
    if (contentType && contentType.includes("application/json")) {
      return await response.json();
    }
    return { message: "Quote deleted successfully" }; // Fallback for non-JSON response
  } catch (error) {
    console.error("Error deleting quote:", error);
    throw error; // Re-throw to handle in UI
  }
};

export const reportQuote = async (reportData) => {
  try {
    const response = await fetch(`${QUOTE_SERVICE_URL}/quotes/report/id`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ quoteID: reportData.quoteID }),
      dy,
    });
    if (!response.ok) throw new Error("Failed to report quote");
    return await response.json();
  } catch (error) {
    console.error("Error reporting quote:", error);
  }
};

export const searchQuotes = async (query, isQuoteID = false) => {
  try {
    const endpoint = isQuoteID
      ? `${QUOTE_SERVICE_URL}/quotes/search/id/${query}`
      : `${QUOTE_SERVICE_URL}/quotes/search/query/${query}`; //Search by text

    const response = await fetch(endpoint);
    if (!response.ok) throw new Error("Failed to search quotes");
    return await response.json();
  } catch (error) {
    console.error("Error searching quotes:", error);
  }
};

export const updateQuote = async (quoteData) => {
  try {
    console.log("Sending update request:", JSON.stringify(quoteData));

    const response = await fetch(`${QUOTE_SERVICE_URL}/quotes/update`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(quoteData),
    });

    const text = await response.text(); 
    console.log("Raw API Response:", text); 

    if (!response.ok) {
      console.error("Backend returned an error:", text);
      throw new Error(`Failed to update quote: ${text}`);
    }

    try {
      return JSON.parse(text);
    } catch (error) {
      console.error("Error parsing JSON:", text);
      throw new Error("Invalid JSON response from server.");
    }
  } catch (error) {
    console.error("Error updating quote:", error);
    throw error;
  }
};


export const fetchTopBookmarkedQuotes = async () => {
  try {
    const response = await fetch(
      `${QUOTE_SERVICE_URL}/quotes/search/topBookmarked`
    );

    if (!response.ok) throw new Error("Failed to fetch top bookmarked quotes");

    const data = await response.json();

    if (!data || data.length === 0) {
      return [];
    }

    return data;
  } catch (error) {
    console.error("Error fetching top bookmarked quotes:", error);
    return []; //return an empty array in case of an error
  }
};

//user profile (From User Service)
export const fetchUserProfile = async (userId) => {
  try {
    const response = await fetch(
      `${USER_SERVICE_URL}/users/search/id/${userId}`
    );
    if (!response.ok) throw new Error("Failed to fetch user profile");
    return await response.json();
  } catch (error) {
    console.error("Error fetching user profile:", error);
  }
};

export const fetchTopSharedQuotes = async () => {
  try {
    const response = await fetch(
      `${QUOTE_SERVICE_URL}/quotes/search/topShared`
    );
    if (!response.ok) throw new Error("Failed to fetch top shared quotes");

    const data = await response.json();
    return data.length ? data : []; // empty array if no data
  } catch (error) {
    console.error("Error fetching top shared quotes:", error);
    return [];
  }
};
