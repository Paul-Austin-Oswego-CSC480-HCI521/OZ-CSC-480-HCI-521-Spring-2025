import { useEffect, useState } from 'react';
import { Outlet } from 'react-router-dom';
import TopNavigation from './TopNavigation';
import LoginBox from './Login';
import { fetchMe } from '../lib/api';

const Layout = () => {
  const [user, setUser] = useState(null);
  const [showLogin, setShowLogin] = useState(null);
  const [notifications, setNotifications] = useState([]);
  const handleCloseLogin = () => {
    setShowLogin(false);
  };

  const handleGoogleLogin = () => {
    window.location.href = "http://localhost:9081/users/auth/login";
  };

  const handleGuestLogin = () => {
    setShowLogin(false);
  };

  useEffect(() => {
    (async () => {
      try {
        console.log("Fetching user..."); 
        // const data = await fetchMe();
        // console.log("Fetched user:", data);
        // if (data == null) throw "No user"
        const data = { Username: "JohnDoe" };
        console.log("Fetched user:", data);

        setUser(data);

        setNotifications([
          { id: 1, message: "New comment on your quote" },
          { id: 2, message: "New quote " }
        ]);

      } catch (err) {
        console.error("Error fetching user", err);

        const timer = setTimeout(() => {
          setShowLogin(true);
        }, 3000);
        return () => clearTimeout(timer);
          } 
        })();
  }, [])

  return (
    <div className="container">
      <TopNavigation user={user} notifications={notifications} />
      <main>
      {showLogin && (
        <div className="position-fixed top-0 start-0 w-100 h-100 d-flex justify-content-center align-items-center" style={{ backgroundColor: "rgba(0, 0, 0, 0.5)", zIndex: 1050 }}>
          <div className="bg-white p-4 rounded shadow-lg">
            <LoginBox handleGoogleLogin={handleGoogleLogin} handleGuestLogin={handleGuestLogin} handleCloseLogin={handleCloseLogin} />
          </div>
        </div>
      )}

        <Outlet />
      </main>
    </div>
  );
};

export default Layout;