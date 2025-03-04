import { useEffect, useState } from 'react';
import { Outlet } from 'react-router-dom';
import TopNavigation from './TopNavigation';
import LoginBox from './Login';
import { fetchMe } from '../lib/api';

const Layout = () => {
  const [user, setUser] = useState(null); //store user data after authentication
  const [showLogin, setShowLogin] = useState(null); //track whether to show the login modal

  const handleCloseLogin = () => {
    //hide the login modal when closed
    setShowLogin(false);
  };

  const handleGoogleLogin = () => {
    //redirect user to the Google login page
    window.location.href = "http://localhost:9081/users/auth/login";
  };

  const handleGuestLogin = () => {
    //hide the login modal when user chooses to continue as a guest
    setShowLogin(false);
  };

  useEffect(() => {
    //fetch user data when the component loads
    (async () => {
      try {
        console.log("Fetching user..."); 
        const data = await fetchMe();
        console.log("Fetched user:", data);
        if (data == null) throw "No user";
        setUser(data);
      } catch (err) {
        console.error("Error fetching user", err);

        const timer = setTimeout(() => {
          setShowLogin(true); //show login modal after 3 seconds if no user is found
        }, 3000);
        return () => clearTimeout(timer); //clear the timer when the component unmounts
      } 
    })();
  }, []);

  return (
    <div className="container">
      <TopNavigation user={user} /> {/* display the top navigation bar with user data */}
      <main>
        {showLogin && (
          //display the login modal if user needs to log in
          <div className="position-fixed top-0 start-0 w-100 h-100 d-flex justify-content-center align-items-center" style={{ backgroundColor: "rgba(0, 0, 0, 0.5)", zIndex: 1050 }}>
            <div className="bg-white p-4 rounded shadow-lg">
              <LoginBox handleGoogleLogin={handleGoogleLogin} handleGuestLogin={handleGuestLogin} handleCloseLogin={handleCloseLogin} />
            </div>
          </div>
        )}

        <Outlet /> {/* render the appropriate page content based on the current route */}
      </main>
    </div>
  );
};

export default Layout; //export the Layout component for use in the app
