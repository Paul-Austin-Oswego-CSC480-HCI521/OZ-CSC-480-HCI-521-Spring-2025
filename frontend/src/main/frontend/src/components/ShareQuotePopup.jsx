import React, { useEffect, useRef, useState } from "react";
import {handleSend} from "../lib/api.js";

const ShareQuotePopup = ({ quote, onClose }) => {
    const [input, setInput] = useState("");
    const [filteredUsers, setFilteredUsers] = useState([]);
    const [selectedUsers, setSelectedUsers] = useState([]);

    const [isTyping, setIsTyping] = useState(false);

    const [loading, setLoading] = useState(false);

    const popupRef = useRef(null);


    // close on outside click
    // useEffect(() => {
    //     const handleClickOutside = (e) => {
    //         if (popupRef.current && !popupRef.current.contains(e.target)) {
    //             onClose();
    //         }
    //     };
    //     document.addEventListener("mousedown", handleClickOutside);
    //     return () => document.removeEventListener("mousedown", handleClickOutside);
    // }, [onClose]);

    // Debounced user search
    // useEffect(() => {
    //     const timeout = setTimeout(async () => {
    //         if (!input.trim()) {
    //             setFilteredUsers([]);
    //             setIsTyping(false);
    //             return;
    //         }
    //
    //         try {
    //             const res = await fetch(`/api/users/search?q=${encodeURIComponent(input)}`);
    //             const data = await res.json();
    //             setFilteredUsers(data);
    //             setIsTyping(true);
    //         } catch (err) {
    //             console.error("User search failed:", err);
    //         }
    //     }, 300);
    //
    //     return () => clearTimeout(timeout);
    // }, [input]);

    // Add/remove from selected

    const toggleUser = (user) => {
        setSelectedUsers((prev) =>
            prev.some(u => u.email === user.email)
                ? prev.filter(u => u.email !== user.email)
                : [...prev, user]
        );
    };


    const copyLink = () => {
        const link = `${window.location.origin}/quote/${quote._id}`;
        navigator.clipboard.writeText(link);
        alert("Link copied to clipboard!");
    };


    return (
        <div
            ref={popupRef}
            onClick={(e) => e.stopPropagation()}
            style={{
                background: "#fff",
                borderRadius: "12px",
                padding: "12px",
                width: "280px",
                boxShadow: "0 2px 12px rgba(0,0,0,0.1)",
                zIndex: 1000,
            }}
        >


            <div
                aria-label="Share this quote."
                style={{
                    background: "#fff9d1",
                    padding: "8px",
                    fontWeight: "bold",
                    textAlign: "center",
                    borderRadius: "8px 8px 0 0",
                    marginBottom: "10px",
                }}
            >
                Share this quote
            </div>

            <input
                aria-label="Text Field"
                type="text"
                placeholder="Search by email..."
                value={input}
                onChange={(e) =>
                    setInput(e.target.value)}
                style={{
                    width: "100%",
                    padding: "8px",
                    borderRadius: "6px",
                    border: "1px solid #ccc",
                    marginBottom: "10px"
                }}
            />




            {/*{isTyping && filteredUsers.length > 0 && (*/}
            {/*    <ul style={{ maxHeight: "100px", overflowY: "auto", padding: 0, margin: 0 }}>*/}
            {/*        {filteredUsers.map((user) => (*/}
            {/*            <li*/}
            {/*                key={user.email}*/}
            {/*                onClick={() => toggleUser(user)}*/}
            {/*                style={{*/}
            {/*                    padding: "6px",*/}
            {/*                    cursor: "pointer",*/}
            {/*                    backgroundColor: selectedUsers.some(u => u.email === user.email)*/}
            {/*                        ? "#e0f7fa"*/}
            {/*                        : "#fff",*/}
            {/*                    borderBottom: "1px solid #eee"*/}
            {/*                }}*/}
            {/*            >*/}
            {/*                <strong>{user.name}</strong> – {user.email}*/}
            {/*            </li>*/}
            {/*        ))}*/}
            {/*    </ul>*/}
            {/*)}*/}

            {/*{selectedUsers.length > 0 && (*/}
            {/*    <div style={{ display: "flex", flexWrap: "wrap", gap: "6px", margin: "10px 0" }}>*/}
            {/*        {selectedUsers.map((user) => (*/}
            {/*            <div*/}
            {/*                key={user.email}*/}
            {/*                style={{*/}
            {/*                    background: "#d0f0ff",*/}
            {/*                    padding: "4px 8px",*/}
            {/*                    borderRadius: "12px",*/}
            {/*                    fontSize: "13px",*/}
            {/*                    display: "flex",*/}
            {/*                    alignItems: "center",*/}
            {/*                    gap: "4px"*/}
            {/*                }}*/}
            {/*            >*/}
            {/*                {user.name}*/}
            {/*                <span*/}
            {/*                    style={{ cursor: "pointer", fontWeight: "bold" }}*/}
            {/*                    onClick={() => toggleUser(user)}*/}
            {/*                >*/}
            {/*                    ×*/}
            {/*                </span>*/}
            {/*            </div>*/}
            {/*        ))}*/}
            {/*    </div>*/}
            {/*)}*/}

            <button
                // onClick={handleSend(input, quote._id)}

                onClick={() => handleSend(input, quote._id)}

                // disabled={selectedUsers.length === 0 || loading}
                aria-label="Send button"
                style={{
                    width: "100%",
                    padding: "8px",
                    background: loading ? "#ccc" : "#007bff",
                    color: "#fff",
                    border: "none",
                    borderRadius: "6px",
                    cursor: "pointer",
                    marginTop: "8px"
                }}
            >
                Send

                {/*{loading ? "Sharing..." : "Send"}*/}
            </button>

            <hr style={{ margin: "12px 0" }} />

            <div
                aria-label="Copy sharable link to your clipboard"
                onClick={copyLink}
                style={{
                    textAlign: "center",
                    color: "#007bff",
                    cursor: "pointer",
                    fontWeight: "bold"
                }}
            >
                🔗 Copy Shareable Link
            </div>

        </div>
    );
};

export default ShareQuotePopup;
