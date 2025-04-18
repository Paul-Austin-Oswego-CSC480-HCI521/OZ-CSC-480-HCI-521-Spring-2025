import { Link, useNavigate, useLocation } from "react-router-dom";
import { useContext, useState } from "react";
import QuoteUploadModal from "./QuoteUploadModal";
import { BsPersonCircle } from "react-icons/bs";
import '../TopNav.css';
import { UserContext } from "../lib/Contexts";
import logo from "../assets/logo.png"; 
import NotificationDropdown from "./NotificationDropdown";


const TopNavigation = () => {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [quoteText, setQuoteText] = useState(""); // State to manage quote input
  const navigate = useNavigate();
  const location = useLocation();
  const [user] = useContext(UserContext);
  const [isNotificationOpen, setIsNotificationOpen] = useState(false);
  const [notifications, setNotifications] = useState([]);

  const handleFetchNotifications = async () => {
    if (user && user.id) {
      try {
        const fetchedNotifications = await fetchNotifications(user.id);
        setNotifications(fetchedNotifications);
      } catch (error) {
        console.error("Failed to fetch notifications:", error);
      }
    }
  };
  
  const handleRemoveNotification = async (index) => {
    try {
      const notificationId = notifications[index]._id;
      await deleteNotification(notificationId);
      setNotifications((prevNotifications) => prevNotifications.filter((_, i) => i !== index));
    } catch (error) {
      console.error("Failed to remove notification:", error);
    }
  };

  const handleClearAllNotifications = async () => {
    if (user && user.id) {
      try {
        await clearAllNotifications(user.id);
        setNotifications([]);
      } catch (error) {
        console.error("Failed to clear all notifications:", error);
      }
    }
  };

  const isActive = (path) => location.pathname === path;

  return (
    <>
      <nav className="navbar navbar-expand-lg navbar-light sticky-top custom-nav">
        <Link className="navbar-brand pl-2" to="/">
          <img src={logo} alt="Logo" style={{ height: "40px" }} /> 
        </Link>
        <button className="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarNav" aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
          <span className="navbar-toggler-icon"></span>
        </button>
        <div className="collapse navbar-collapse pr-2" id="navbarNav">
          <ul className="navbar-nav ml-auto">
            <li className={`nav-item ${isActive("/") ? "active" : ""}`}>
              <Link className="nav-link" to="/">Home</Link>
            </li>
            <li className={`nav-item ${isActive("/add-quote") ? "active" : ""}`}>
              <span 
                className="nav-link" 
                style={{ cursor: "pointer" }} 
                onClick={() => setIsModalOpen(true)}
              >
                Add Quote
              </span>
            </li>
            { user ? (
              <li className={`nav-item ${isActive("/my-collection") ? "active" : ""}`}>
                <Link className="nav-link" to="/my-collection">My Collection</Link> {/* Updated path */}
              </li>
            ) : (
              <></>
            )}
            { !user ? (
              <li className="nav-item">
                <button className="btn btn-dark" onClick={() => navigate("/login")}>Sign in</button>
              </li>
            ) : (
              <></>

            )}
            <li className="nav-item ml-3 mr-3" style={{ position: 'relative' }} title={user?.Username || "Click sign in to sign in"}>
              <div style={{ cursor: "pointer" }} 
              onClick={() => { 
                setIsNotificationOpen(!isNotificationOpen);
                if (!isNotificationOpen) handleFetchNotifications();
              }}>
              
                {user && <BsPersonCircle size={40} style={{ color: "#146C43" }} />}

               {/* Notification Dot */}
               {notifications.length > 0 && (
                <span style={{
                  position: 'absolute',
                  bottom: '5px', 
                  right: '-5px',
                  minWidth: '24px',
                  height: '24px',
                  backgroundColor: 'red',
                  color: 'white',
                  borderRadius: '50%',
                  border: '2px solid white',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  fontSize: '14px',
                  padding: '2px',
                  lineHeight: 1,
                }}>
                  {notifications.length > 9 ? '9+' : notifications.length} 
                </span>
              )}
              </div>

              {/* Notification Dropdown */}
            <NotificationDropdown
              isVisible={isNotificationOpen}
              onClose={() => setIsNotificationOpen(false)}
              notifications={notifications}
              onRemoveNotification={handleRemoveNotification}
              onClearAll={handleClearAllNotifications}
            />
            </li>
          </ul>
        </div>
      </nav>

      <QuoteUploadModal
        isVisible={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSubmit={() => setIsModalOpen(false)} // Close modal after submission
        quoteText={quoteText}
        setQuoteText={setQuoteText}
      />
    </>
  );
};

export default TopNavigation;