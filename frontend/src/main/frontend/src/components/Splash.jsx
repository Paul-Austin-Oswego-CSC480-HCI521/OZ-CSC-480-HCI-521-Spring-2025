import { useState } from "react"; //import useState to manage component state
import { useNavigate } from "react-router-dom"; //import useNavigate to navigate between pages

export default function Splash() {

  const [searchQuery, setSearchQuery] = useState(""); //store the user's search input
  const navigate = useNavigate(); //initialize navigation function for page redirection

  const handleSearchRedirect = () => {
    //redirect to the search page with the query as a URL parameter
    navigate("/search?q=" + searchQuery);
  };

  const handleSearchChange = (e) => {
    //update the searchQuery state whenever the user types in the input field
    setSearchQuery(e.target.value);
  };

  const splashStyle = {
    //styling for the main splash section
    display: "flex",
    padding: "80px 120px",
    justifyContent: "center",
    alignItems: "center",
    gap: "60px",
    alignSelf: "stretch",
    background: "rgba(217, 217, 217, 0.80)"
  }

  const splashContainerStyle = {
    //styling for the inner content container
    display: "flex",
    padding: "24px 0px",
    flexDirection: "column",
    justifyContent: "center",
    alignItems: "center",
    gap: "24px",
    flex: "1 0 0",
  }

  const splashH1Style = {
    //styling for the main heading
    color: "#1E1E1E",
    textAlign: "center",
    fontFamily: "Inter",
    fontSize: "48px",
    fontStyle: "normal",
    fontWeight: "800",
    lineHeight: "48px",
  }
  
  const splashH2Style = {
    //styling for the subheading
    color: "#1E1E1E",
    textAlign: "center",
    fontFamily: "Inter",
    fontSize: "20px",
    fontStyle: "normal",
    fontWeight: "400",
    lineHeight: "24px",
  }

  return (
      <div style={splashStyle}>
        <div style={splashContainerStyle}>

        <h1 style={splashH1Style}>Find, Share & Save Quotes Effortlessly</h1>
        <h2 style={splashH2Style}>Find insightful quotes from various authors and themes</h2>

        <input
          type="text" 
          className="form-control"
          style={{ fontFamily: "Inter", fontSize: "16px", fontWeight: "400", lineHeight:"20px", width: "708px", padding: "14px 16px" }}
          placeholder="Search quotes, authors, or themes..."
          onChange={handleSearchChange} //update the searchQuery state when input changes
        />

        <button className="btn btn-dark" style={{ width: "240px", padding: "12px"}} onClick={handleSearchRedirect}>
          Search
        </button>
        </div>
      </div>
  )
}
