import { useState } from "react"; //import useState to manage component state
import { useNavigate } from "react-router-dom"; //import useNavigate to navigate between pages
import searchSvg from "../assets/search.svg"
import Tag from "./Tag";

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
    padding: "24px 0px",
    justifyContent: "center",
    alignItems: "center",
    gap: "60px",
    alignSelf: "stretch",
    borderRadius: "22px",
    background: "linear-gradient(276deg, rgba(253, 247, 205, 0.90) 11.16%, rgba(90, 212, 120, 0.90) 148.05%)",
  }

  const splashContainerStyle = {
    //styling for the inner content container
    display: "flex",
    padding: "24px 0px",
    flexDirection: "column",
    justifyContent: "center",
    alignItems: "center",
    gap: "20px",
    flex: "1 0 0",
  }

  const splashH1Style = {
    //styling for the main heading
    color: "#1E1E1E",
    textAlign: "center",
    fontFamily: "Inter",
    fontSize: "40px",
    fontStyle: "normal",
    fontWeight: "700",
    lineHeight: "48px",
  }
  
  const splashH2Style = {
    //styling for the subheading
    color: "rgba(30, 30, 30, 0.45)",
    textAlign: "center",
    fontFamily: "Inter",
    maxWidth: "600px",
    fontSize: "20px",
    fontStyle: "normal",
    fontWeight: "500",
    lineHeight: "normal",
  }

  const splashH3Style = {
    color: "#000",
    textAlign: "center",
    fontFamily: "Inter",
    fontSize: "20px",
    fontStyle: "normal",
    fontWeight: "300",
    lineHeight: "22px", /* 110% */
  }


  const searchText = {
    color: "#FFF",
    fontFamily: "Roboto",
    fontSize: "20px",
    fontStyle: "normal",
    fontWeight: "500",
    lineHeight: "24px", /* 120% */
  }

  const searchButton = {
    display: "flex",
    width: "240px",
    padding: "12px",
    flexDirection: "column",
    justifyContent: "center",
    alignItems: "center",
    borderRadius: "8px",
    background: "#146C43"
  }

  return (
    <div style={splashStyle}>
      <div style={splashContainerStyle}>

      <h1 style={splashH1Style}>Find, Share & Save Quotes <span style={{color: "#146C43"}}>Effortlessly</span></h1>
      <h2 style={splashH2Style}>Find insightful quotes from various authors and themes. Copy and paste with just one click</h2>
      <div style={{width: "100%", position: "relative", maxWidth: "600px"}}>
        <img src={searchSvg} style={{position: "absolute", top: "0", left: "0", padding: "10px", aspectRatio: "1", height: "100%"}} alt="" />
        <input
            type="text"
            className="form-control"
            style={{
                fontFamily: "Inter",
                fontSize: "16px",
                fontWeight: "400",
                lineHeight: "20px",
                padding: "12px 20px 12px 40px",

            }}
            placeholder="Search quotes, authors, or themes..."
            onChange={handleSearchChange} //update the searchQuery state when input changes
        />
      </div>


      <button className="btn" style={searchButton} onClick={handleSearchRedirect}>
        <span style={searchText}>Search</span>
      </button>

      <h3 style={splashH3Style}>Explore the following popular tags to get started:</h3>

      <div style={{ display: "flex", gap: "8px", flexWrap: "wrap" }}>
        {["Inspiration", "Love"].map((tag, i) => <Tag text={tag} key={i} />)}
      </div>

      </div>
    </div>
  )
}
