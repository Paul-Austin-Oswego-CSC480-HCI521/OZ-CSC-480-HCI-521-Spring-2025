import { IoMdClose, IoIosArrowDown } from "react-icons/io";
import { Link } from "react-router-dom";
import { useContext } from "react";
import NotificationItem from "./Notification";
import { useState, useRef, useEffect } from "react";
import { fetchNotifications, deleteNotification, clearAllNotifications } from "../lib/api";
import { UserContext } from "../lib/Contexts";

const NotificationDropdown = ({ isVisible }) => {
  const [notifications, setNotifications] = useState([]);
  const [isNotificationsVisible, setIsNotificationsVisible] = useState(false);
  const contentRef = useRef(null);
  const [user] = useContext(UserContext); 
  const [dropdownStyles, setDropdownStyles] = useState({
    maxHeight: "0px",
    opacity: 0,
    overflow: "hidden",
    transition: "max-height 0.4s ease, opacity 0.4s ease",
  });

  useEffect(() => {
    if (isNotificationsVisible) {
      const scrollHeight = contentRef.current?.scrollHeight || 500;
      setDropdownStyles((prev) => ({
        ...prev,
        maxHeight: `${scrollHeight}px`,
        opacity: 1,
      }));
    } else {
      setDropdownStyles((prev) => ({
        ...prev,
        maxHeight: "0px",
        opacity: 0,
      }));
    }
  }, [isNotificationsVisible, notifications]);

  useEffect(() => {
    const loadNotifications = async () => {
      try {

        const userId = user._id.$oid;
        const fetchedNotifications = await fetchNotifications(userId); 
        setNotifications(fetchedNotifications);
      } catch (error) {
        console.error("Error fetching notifications:", error);
      }
    };

    if (isVisible) {
      loadNotifications();
    }
  }, [isVisible, user]);

  const handleRemoveNotification = async (index) => {
    try {
      const notificationId = notifications[index]._id;
      await deleteNotification(notificationId);
      setNotifications((prev) => prev.filter((_, i) => i !== index));
    } catch (error) {
      console.error("Error removing notification:", error);
    }
  };

  const handleClearAll = async () => {
    try {
      await clearAllNotifications(user._id);
      setNotifications([]);
    } catch (error) {
      console.error("Error clearing all notifications:", error);
    }
  };

  if (!isVisible) return null;

  // Color updates
  const backgroundColor = "#f5e7c7"; // darker cream
  const borderColor = "#1e7c3e"; // darker green

  return (
    <div
      style={{
        position: "absolute",
        top: "60px", // moved down for more space
        left: "50%",
        transform: "translateX(-50%)",
        width: "250px",
        backgroundColor,
        color: "black",
        border: `2px solid ${borderColor}`,
        borderRadius: "8px",
        boxShadow: "0 4px 12px rgba(0, 100, 0, 0.3)",
        zIndex: 1050,
        paddingTop: "10px", // space for the arrow
      }}
    >
      {/* Arrow */}
      <div
        style={{
          position: "absolute",
          top: "-10px",
          left: "50%",
          transform: "translateX(-50%)",
          width: "0",
          height: "0",
          borderLeft: "10px solid transparent",
          borderRight: "10px solid transparent",
          borderBottom: `10px solid ${backgroundColor}`,
          zIndex: 1051,
        }}
      />

      {/* Container */}
      <ul
        style={{
          listStyle: "none",
          margin: 0,
          padding: 0,
        }}
      >
        {/* Account Page Link */}
        <li
          style={{
            padding: "8px 14px", // reduced padding
            borderBottom: "1px solid #ccc",
            cursor: "pointer",
          }}
        >
          <Link
            to="/account"
            style={{
              textDecoration: "none",
              color: "black",
              fontSize: "13px",
            }}
          >
            Account Page
          </Link>
        </li>

        {/* Notifications Section */}
        <li
          onClick={() => setIsNotificationsVisible(!isNotificationsVisible)}
          style={{
            padding: "8px 14px", // reduced padding
            cursor: "pointer",
            position: "relative",
            display: "flex",
            alignItems: "center",
          }}
        >
          <span
            style={{
              fontSize: "13px",
              color: "black",
              flex: 1,
            }}
          >
            Notifications
          </span>

          {/* Notification Badge */}
          {Array.isArray(notifications) && notifications.length > 0 && (
            <span
              style={{
                backgroundColor: "red",
                color: "white",
                borderRadius: "50%",
                width: "18px",
                height: "18px",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                fontSize: "11px",
                marginRight: "6px",
              }}
            >
              {notifications.length > 9 ? "9+" : notifications.length}
            </span>
          )}

          {/* Rotating Arrow */}
          <IoIosArrowDown
            style={{
              fontSize: "16px",
              transition: "transform 0.3s ease",
              transform: isNotificationsVisible ? "rotate(180deg)" : "rotate(0deg)",
            }}
          />

          {/* Animated Dropdown */}
          <div
            style={{
              position: "absolute",
              top: "100%",
              left: "0",
              width: "250px",
              backgroundColor,
              color: "black",
              border: `2px solid ${borderColor}`,
              borderRadius: "8px",
              boxShadow: "0 4px 12px rgba(0, 100, 0, 0.3)",
              zIndex: 1051,
              marginTop: "0",
              ...dropdownStyles,
            }}
          >
            <div
              ref={contentRef}
              style={{
                maxHeight: "300px",
                overflowY: "auto",
              }}
            >
              {Array.isArray(notifications) && notifications.length === 0 ? (
                <div
                  style={{
                    padding: "6px",
                    textAlign: "center",
                    color: "#666",
                    fontSize: "12px",
                  }}
                >
                  No new notifications
                </div>
              ) : (
                <>
                  {Array.isArray(notifications) && notifications.map((notification, index) => (
                    <NotificationItem
                      key={notification._id}
                      notification={{
                        ...notification,
                        isLast: index === notifications.length - 1,
                      }}
                      onRemove={() => handleRemoveNotification(index)}
                    />
                  ))}
                  <div style={{ padding: "6px", textAlign: "center" }}>
                    <button
                      onClick={(e) => {
                        e.stopPropagation();
                        handleClearAll();
                      }}
                      style={{
                        background: borderColor,
                        border: "none",
                        color: "white",
                        cursor: "pointer",
                        fontSize: "11px",
                        borderRadius: "16px",
                        padding: "2px 20px",
                      }}
                    >
                      Clear All
                    </button>
                  </div>
                </>
              )}
            </div>
          </div>
        </li>
      </ul>
    </div>
  );
};

export default NotificationDropdown;
