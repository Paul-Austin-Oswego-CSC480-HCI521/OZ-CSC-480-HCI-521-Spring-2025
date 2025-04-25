import { IoIosArrowDown } from "react-icons/io";
import { Link } from "react-router-dom";
import { useContext } from "react";
import NotificationItem from "./Notification";
import { useState, useEffect } from "react";
import { fetchNotifications, deleteNotification, clearAllNotifications, logout } from "../lib/api";
import { AlertContext, UserContext } from "../lib/Contexts";
import { BsPersonCircle, BsBell, BsBoxArrowRight } from "react-icons/bs";

const NotificationDropdown = ({ isVisible }) => {
  const [notifications, setNotifications] = useState([]);
  const [_, setAlert] = useContext(AlertContext)
  const [isNotificationsVisible, setIsNotificationsVisible] = useState(false);
  const [user] = useContext(UserContext);
  const borderColor = "#0d5c05"; 

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

  const handleLogout = async () => {
    try {
      await logout(); 
      setAlert({ type: "success", message: "Successfully logged out." });
      setTimeout(() => window.location.reload(), 3000)
    } catch (error) {
      setAlert({ type: "danger", message: "An error occurred during logout." });
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

  return (
    <div
      style={{
        position: "absolute",
        top: "60px",
        left: "50%",
        transform: "translateX(-50%)",
        width: "250px",
        backgroundColor: "#f5e7c7",
        color: "black",
        borderRadius: "8px",
        boxShadow: "0 4px 12px rgba(0, 100, 0, 0.3)",
        zIndex: 1050,
        paddingTop: "10px",
      }}
    >
      
      <ul
        style={{
          listStyle: "none",
          margin: 0,
          padding: 0,
        }}
      >

        {/* User Profile Section */}
        <li
          style={{
            display: "flex",
            alignItems: "center",
            padding: "4px 10px",
            borderBottom: "1px solid #ccc",
            margin: "0 10px",
          }}
        >
          {user.profilePicture ? (
            <img
              src={user.profilePicture}
              alt="Profile"
              style={{
                width: "32px",
                height: "32px",
                borderRadius: "50%",
                marginRight: "8px",
              }}
            />
          ) : (
            <BsPersonCircle
              style={{
                fontSize: "32px",
                color: "#666",
                marginRight: "8px",
              }}
            />
          )}
          <Link
            to="/account"
            style={{
              textDecoration: "none",
              fontSize: "16px",
              color: "black",
              fontWeight: "bold",
            }}
          >
            {user.Username || "Guest"}
          </Link>
        </li>

        {/* Notification Section */}
        <li
          onClick={() => setIsNotificationsVisible(!isNotificationsVisible)}
          style={{
            padding: "4px 10px",
            cursor: "pointer",
            position: "relative",
            display: "flex",
            alignItems: "center",
            margin: "0 10px",
          }}
        >
          <BsBell
            style={{
              fontSize: "20px",
              color: "#0d5c05",
              marginRight: "8px",
            }}
          />
          <span
            style={{
              fontSize: "16px",
              color: "black",
              fontWeight: "bold",
              flex: 1,
              textAlign: "left",
            }}
          >
            Notification
          </span>

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
                fontSize: "12px",
                marginRight: "6px",
              }}
            >
              {notifications.length > 9 ? "9+" : notifications.length}
            </span>
          )}

          <IoIosArrowDown
            style={{
              fontSize: "16px",
              transition: "transform 0.3s ease",
              transform: isNotificationsVisible ? "rotate(180deg)" : "rotate(0deg)",
            }}
          />
        </li>

        {/* Notifications List */}
        {isNotificationsVisible && (
          <li
            style={{
              padding: "0 14px",
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
                {Array.isArray(notifications) &&
                  notifications.map((notification, index) => (
                    <NotificationItem
                      key={notification._id}
                      notification={notification}
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
          </li>
        )}

        {/* Logout Section */}
        <li
          style={{
            padding: "4px 10px",
            borderTop: "1px solid #ccc",
            margin: "0 10px",
            cursor: "pointer",
            textAlign: "left",
            display: "flex",
            alignItems: "center",
          }}
          onClick={() => {
            handleLogout(); // Taken from the AccountPage component, should work here as well.
          }}
        >
          <BsBoxArrowRight
            style={{
              fontSize: "20px",
              color: "#0d5c05",
              marginRight: "8px",
            }}
          />
          <span
            style={{
              fontSize: "16px",
              color: "black",
              fontWeight: "bold",
            }}
          >
            Log Out
          </span>
        </li>
      </ul>
    </div>
  );
};

export default NotificationDropdown;