const PROXY_URL = import.meta.env.VITE_PROXY_URL || "http://localhost:9083";

export const createQuote = async ({ quote, author, tags, private: isPrivate }) => {
  //send a request to create a new quote, including author, text, tags, and a timestamp
  try {
    const jwt = await getJWT();

    const quoteData = {
      quote,
      author: author || "Unknown",
      date: Math.floor(new Date().getTime() / 1000), //convert to Unix timestamp for int
      tags: tags || [],
      ["private"]: isPrivate || false,
    };

    console.log("Sending API Payload:", JSON.stringify(quoteData));

    const response = await fetch(`${PROXY_URL}/quotes/create`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${jwt}`,
      },
      body: JSON.stringify(quoteData),
      credentials: "include",
    });

    if (!response.ok) {
      const errorMessage = await response.text();
      console.error("Backend Error:", errorMessage);
      throw new Error("Failed to create quote");
    }
    return await response.json();
  } catch (error) {
    console.error("Error creating quote:", error);
    throw error;
  }
};

export const deleteQuote = async (quoteId) => {
  //send a request to delete a quote by its ID
  try {
    const jwt = await getJWT();

    const response = await fetch(
        `${PROXY_URL}/quotes/delete/${quoteId}`,
        {
          method: "DELETE",
          headers: {
            "Authorization": `Bearer ${jwt}`,
          },
          credentials: "include",
        }
    );

    const contentType = response.headers.get("Content-Type");
    if (contentType && contentType.includes("application/json")) {
      return await response.json();
    }
    return { message: "Quote deleted successfully" }; //fallback for non-JSON response
  } catch (error) {
    console.error("Error deleting quote:", error);
    throw error;
  }
};

export const reportQuote = async (reportData) => {
  //send a request to report a quote by ID
  try {
    const response = await fetch(`${PROXY_URL}/quotes/report/id`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ quoteID: reportData.quoteID }),
    });
    if (!response.ok) throw new Error("Failed to report quote");
    return await response.json();
  } catch (error) {
    console.error("Error reporting quote:", error);
  }
};

export const searchQuotes = async (query, isQuoteID = false) => {
  //search for quotes by text or by a specific quote ID
  try {
    const endpoint = isQuoteID
        ? `${PROXY_URL}/quotes/search/id/${query}`
        : `${PROXY_URL}/quotes/search/query/${query}`; //search by text

    const response = await fetch(endpoint);
    if (!response.ok) throw new Error("Failed to search quotes");
    return await response.json();
  } catch (error) {
    console.error("Error searching quotes:", error);
  }
};

export const updateQuote = async (quoteData) => {
  //send a request to update an existing quote with new data
  try {
    console.log("Sending update request:", JSON.stringify(quoteData));

    const jwt = await getJWT();

    const response = await fetch(`${PROXY_URL}/quotes/update`, {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${jwt}`,
      },
      body: JSON.stringify(quoteData),
      credentials: "include",
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
  //retrieve the most bookmarked quotes from the API
  try {
    const response = await fetch(
        `${PROXY_URL}/quotes/search/topBookmarked`
    );

    if (!response.ok) throw new Error("Failed to fetch top bookmarked quotes");

    const data = await response.json();

    if (!data || data.length === 0) {
      return [];
    }

    return data;
  } catch (error) {
    console.error("Error fetching top bookmarked quotes:", error);
    return [];
  }
};

export const fetchUserProfile = async (userId) => {
  //fetch user profile data using the user ID
  try {
    const response = await fetch(
        `${PROXY_URL}/users/search/id/${userId}`
    );
    if (!response.ok) throw new Error("Failed to fetch user profile");
    return await response.json();
  } catch (error) {
    console.error("Error fetching user profile:", error);
  }
};

export const fetchTopSharedQuotes = async () => {
  //retrieve the most shared quotes from the API
  try {
    const response = await fetch(
        `${PROXY_URL}/quotes/search/topShared`
    );
    if (!response.ok) throw new Error("Failed to fetch top shared quotes");

    const data = await response.json();
    return data.length ? data : []; //empty array if no data
  } catch (error) {
    console.error("Error fetching top shared quotes:", error);
    return [];
  }
};

export const fetchMe = async () => {
  //fetch the currently logged-in user's data
  try {
    const response = await fetch(
        `${PROXY_URL}/users/accounts/whoami`,
        {
          credentials: "include"
        }
    );

    if (!response.ok) return null;

    const data = await response.json();
    return data;
  } catch (error) {
    console.error("Error fetching user data:", error);
    return null;
  }
};

export const bookmarkQuote = async (quoteId) => {
  //send a request to bookmark a quote by its ID
  try {
    console.log("Sending bookmark request for quote ID:", quoteId);

    const jwt = await getJWT();

    const response = await fetch(`${PROXY_URL}/users/bookmarks/add/${quoteId}`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${jwt}`,
      },
      credentials: "include",
    });

    if (!response.ok) {
      const errorMessage = await response.text();
      console.error("Backend returned an error:", errorMessage);
      throw new Error(`Failed to bookmark quote: ${errorMessage}`);
    }

    const responseData = await response.json();
    console.log("Raw API Response:", responseData);
    return responseData;
  } catch (error) {
    console.error("Error bookmarking quote:", error);
    throw error;
  }
};

export const deleteBookmark = async (quoteId) => {
  //send a request to delete a bookmark by its ID
  try {
    console.log("Sending delete bookmark request for quote ID:", quoteId);

    const jwt = await getJWT();

    const response = await fetch(`${PROXY_URL}/users/bookmarks/delete/${quoteId}`, {
      method: "DELETE",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${jwt}`,
      },
      credentials: "include",
    });

    if (!response.ok) {
      const errorMessage = await response.text();
      console.error("Backend returned an error:", errorMessage);
      throw new Error(`Failed to delete bookmark: ${errorMessage}`);
    }

    const responseData = await response.json();
    console.log("Raw API Response:", responseData);
    return responseData;
  } catch (error) {
    console.error("Error deleting bookmark:", error);
    throw error;
  }
};

export const updateMe = async (updatedData) => {
  try {
    const jwt = await getJWT();

    const user = await fetchMe();
    console.log("User fetched for update:", user);

    const userId = user._id?.$oid;
    if (!userId) {
      throw new Error("User ID not found or invalid");
    }

    const response = await fetch(`${PROXY_URL}/users/accounts/update/${userId}`, {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${jwt}`,
      },
      body: JSON.stringify(updatedData),
      credentials: "include",
    });

    if (!response.ok) {
      const errorMessage = await response.text();
      console.error("Backend returned an error:", errorMessage);
      throw new Error(`Failed to update user: ${errorMessage}`);
    }

    return await response.json();
  } catch (error) {
    console.error("Error updating user:", error);
    throw error;
  }
};

export const fetchUserQuotes = async (userId) => {
  //fetch quotes created by a specific user
  try {
    const response = await fetch(`${PROXY_URL}/quotes/search/user/${userId}`);
    if (!response.ok) throw new Error("Failed to fetch user quotes");

    const data = await response.json();
    return data;
  } catch (error) {
    console.error("Error fetching user quotes:", error);
    return [];
  }
};

export const fetchQuoteById = async (quoteId) => {
  //fetch a quote by its ID
  try {
    const response = await fetch(`${PROXY_URL}/quotes/search/id/${quoteId}`);
    if (response.status === 404) {
      console.warn(`Quote with ID ${quoteId} not found.`);
      return null; 
    }
    if (!response.ok) throw new Error("Failed to fetch quote");

    const data = await response.json();
    return data;
  } catch (error) {
    console.error("Error fetching quote:", error);
    return null; 
  }
};

export const useQuote = async (quoteId) => {
  try {
    const jwt = await getJWT();

    const response = await fetch(`${PROXY_URL}/useQuote/use/${quoteId}`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${jwt}`,
      },
      credentials: "include",
    });

    if (!response.ok) {
      const errorMessage = await response.text();
      console.error("Error using quote:", errorMessage);
      throw new Error(`Failed to use quote: ${errorMessage}`);
    }

    return await response.json();
  } catch (error) {
    console.error("Error using quote:", error);
    throw error;
  }
};

const getJWT = async () => {
  try {
    const response = await fetch(`${PROXY_URL}/users/auth/jwt`, {
      method: "GET",
      credentials: "include",
    });

    console.log([...response.headers.entries()]);

    if (!response.ok) {
      const errorText = await response.text();
      console.error("Backend returned an error:", errorText);
      throw new Error(`Failed to fetch JWT: ${errorText}`);
    }

    const jwt = response.headers.get("Authorization")?.replace("Bearer ", "");
    if (!jwt) {
      throw new Error("JWT not found in response headers.");
    }

    return jwt;
  } catch (error) {
    console.error("Error fetching JWT:", error);
    throw error;
  }
};
