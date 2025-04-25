import React, { useContext, useState } from "react";
import { createQuote } from "../lib/api";
import { AlertContext } from "../lib/Contexts";
const QuoteUploadModal = ({ isVisible, onClose, onSubmit, quoteText, setQuoteText }) => {
    if (!isVisible) return null; //if the modal is not visible, do not render anything

    const [_, setAlert] = useContext(AlertContext);
    const [author, setAuthor] = useState("");
    const [tags, setTags] = useState([]);
    const [customTag, setCustomTag] = useState("");
    const suggestedTags = ["Love", "Willpower", "Life", "Success"];
    const [isPrivate, setIsPrivate] = useState(false);

    const toggleTag = (tag) => {
        setTags((prevTags) =>
            prevTags.includes(tag) ? prevTags.filter((t) => t !== tag) : [...prevTags, tag]
        );
    };

    const addCustomTag = () => {
        if (customTag.trim() && !tags.includes(customTag)) {
            setTags([...tags, customTag.trim()]);
        }
        setCustomTag("");
    };

    const handleSubmit = async () => {
        if (!quoteText.trim()) return;

        const quoteData = {
            quote: quoteText.trim(),
            author: author.trim() || "Unknown",
            tags,
            ["private"]: isPrivate || false
        };

        try {
            await createQuote(quoteData);
            setQuoteText("");
            setAuthor("");
            setTags([]);
            setIsPrivate(false);
            onClose();
            window.location.href = "/";
        } catch (err) {
            setAlert(err);
            console.error("Error submitting quote:", err);
        }
    };

    return (
        <div className="modal show" style={{ display: "block" }}>
            <div className="modal-dialog modal-dialog-centered">
                <div className="modal-content" style={{ backgroundColor: "#F8FDF1", borderRadius: "10px" }}>
                    <div
                        className="modal-header"
                        style={{
                            borderTopLeftRadius: "10px",
                            borderTopRightRadius: "10px",
                            position: "relative",
                            display: "flex",
                            justifyContent: "center",
                            alignItems: "center"
                        }}
                    >
                        <h5 className="modal-title" style={{ margin: "0 auto" }}>
                            Upload Quote
                        </h5>
                        <button
                            type="button"
                            onClick={onClose}
                            style={{
                                position: "absolute",
                                right: "10px",
                                top: "50%",
                                transform: "translateY(-50%)",
                                backgroundColor: "#fff",
                                border: "1px solid rgba(180, 35, 24, 1)",
                                borderRadius: "50%",
                                width: "30px",
                                height: "30px",
                                color: "rgba(180, 35, 24, 1)",
                                display: "flex",
                                alignItems: "center",
                                justifyContent: "center",
                                fontSize: "16px",
                                lineHeight: "16px",
                                padding: 0,
                                cursor: "pointer"
                            }}
                        >
                            ×
                        </button>
                    </div>
                    <div className="modal-body">
                        <div className="mb-3">
                            <label className="form-label" style={{textAlign: "left", display: "block"}}>Quote</label>
                            <textarea
                                className="form-control"
                                rows="3"
                                value={quoteText}
                                onChange={(e) => setQuoteText(e.target.value)}
                                placeholder="Enter your quote here"
                                style={{ fontFamily: "Inter", fontSize: "16px", lineHeight: "24px" }}
                            />
                        </div>

                        <div className="d-flex justify-content-between" style={{gap:"2rem"}}>
                            {/*<div className={{"flex-1"}} >*/}
                            <div style={{ flex:1}}>
                                <label className="form-label" style={{textAlign: "left", display: "block"}}>
                                    Author
                                </label>
                                <input
                                    type="text"
                                    className="form-control"
                                    value={author}
                                    onChange={(e) => setAuthor(e.target.value)}
                                    placeholder="Unknown"
                                    // style={{ fontFamily: "Inter", fontSize: "14px", lineHeight: "20px" }}
                                />
                                {/*</div>*/}
                            </div>

                            {/* Tags Section */}
                            <div style={{ flex: 2 }}>
                                <label className="form-label" style={{textAlign: "left", display: "block"}}>Tags</label>

                                <div className="d-flex flex-wrap align-items-center" style={{ gap: "8px" }}>
                                    {/* + Add a new tag (input or button) */}
                                    {!customTag ? (
                                        <button
                                            onClick={() => setCustomTag(" ")}
                                            className="border border-success rounded-pill px-3 py-1 text-success bg-white"
                                            style={{ fontSize: "14px" }}
                                        >
                                            + Add a new tag
                                        </button>
                                    ) : (
                                        <input
                                            type="text"
                                            className="form-control"
                                            value={customTag}
                                            autoFocus
                                            onChange={(e) => setCustomTag(e.target.value)}
                                            onKeyDown={(e) => {
                                                if (e.key === "Enter") {
                                                    addCustomTag();
                                                } else if (e.key === "Escape") {
                                                    setCustomTag("");
                                                }
                                            }}
                                            onBlur={() => setCustomTag("")}
                                            placeholder="Enter tag"
                                            style={{
                                                maxWidth: "200px",
                                                borderRadius: "20px",
                                                padding: "6px 15px",
                                                fontSize: "14px",
                                            }}
                                        />
                                    )}

                                    {/* Display selected tags (custom or suggested) with ✕ */}
                                    {tags.map((tag) => (
                                        <span
                                            key={tag}
                                            className="border bg-success text-white rounded-pill px-3 py-1 d-flex align-items-center"
                                            style={{ fontSize: "14px", cursor: "pointer", gap: "5px" }}
                                            onClick={() => toggleTag(tag)}
                                        >
        #{tag} <span style={{ fontWeight: "bold" }}>✕</span>
      </span>
                                    ))}

                                    {/* Suggested tags that are not selected */}
                                    {suggestedTags
                                        .filter((tag) => !tags.includes(tag))
                                        .map((tag) => (
                                            <button
                                                key={tag}
                                                onClick={() => toggleTag(tag)}
                                                className="border rounded-pill px-3 py-1 border-success text-success bg-white d-flex align-items-center"
                                                style={{ fontSize: "14px", borderWidth: "1.5px", gap: "5px" }}
                                            >
                                                #{tag}
                                            </button>
                                        ))}
                                </div>
                            </div>
                        </div>

                        <div className="mb-3">
                            <label className="form-label" style={{textAlign: "left", display: "block"}}>Visibility</label>
                            <div
                                className="d-flex align-items-center"
                                style={{
                                    border: "1px solid #000",
                                    borderRadius: "20px",
                                    overflow: "hidden",
                                    display: "inline-flex",
                                    width: "fit-content",
                                    fontSize: "14px",
                                }}
                            >
                                {/* Public Button */}
                                <button
                                    type="button"
                                    className={`btn ${!isPrivate ? "bg-success text-white" : "bg-white text-dark"}`}
                                    onClick={() => setIsPrivate(false)}
                                    style={{
                                        border: "none",
                                        borderRadius: "0",
                                        padding: "7px 10px",
                                        flex: 1,
                                        fontWeight: !isPrivate ? "bold" : "normal",
                                        display: "flex",
                                        alignItems: "center",
                                        justifyContent: "center",
                                        fontSize: "14px",
                                    }}
                                >
                                    {!isPrivate && (
                                        <span
                                            style={{
                                                marginRight: "5px",
                                                fontSize: "10px",
                                            }}
                                        >
                    ✔
                </span>
                                    )}
                                    Public
                                </button>

                                {/* Private Button */}
                                <button
                                    type="button"
                                    className={`btn ${isPrivate ? "bg-success text-white" : "bg-white text-dark"}`}
                                    onClick={() => setIsPrivate(true)}
                                    style={{
                                        border: "none",
                                        borderRadius: "0",
                                        padding: "7px 10px",
                                        flex: 1,
                                        fontWeight: isPrivate ? "bold" : "normal",
                                        display: "flex",
                                        alignItems: "center",
                                        justifyContent: "center",
                                        fontSize: "14px",
                                    }}
                                >
                                    {isPrivate && (
                                        <span
                                            style={{
                                                marginRight: "5px",
                                                fontSize: "10px",
                                            }}
                                        >
                    ✔
                </span>
                                    )}
                                    Private
                                </button>
                            </div>
                        </div>



                    </div>
                    <div className="modal-footer">
                        {/*<button type="button" className="btn btn-secondary" onClick={onClose}>*/}
                        {/*  Close*/}
                        {/*</button>*/}
                        <button
                            type="button"
                            className="btn btn-primary"
                            style={{borderRadius:'35px',backgroundColor: "rgba(20, 108, 67, 1)", borderColor: "rgba(20, 108, 67, 1)" }}
                            onClick={handleSubmit}
                        >
                            Upload
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default QuoteUploadModal;