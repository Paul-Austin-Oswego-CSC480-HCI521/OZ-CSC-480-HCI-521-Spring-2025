import { useState } from 'react';
import { Outlet } from 'react-router-dom';
import TopNavigation from './TopNavigation';
import LoginBox from './Login';

const Layout = () => {
  const [showLogin, setShowLogin] = useState(null); //track whether to show the login modal

  const handleGoogleLogin = () => {
    //redirect user to the Google login page
    window.location.href = "http://localhost:9081/users/auth/login";
  };

  const handleGuestLogin = () => {
    //hide the login modal when user chooses to continue as a guest
    setShowLogin(false);
  };

  return (
    <div className="container">
      <TopNavigation /> {/* display the top navigation bar with user data */}
      <main>
        {showLogin && (
          //display the login modal if user needs to log in
          <div className="position-fixed top-0 start-0 w-100 h-100 d-flex justify-content-center align-items-center" style={{ backgroundColor: "rgba(0, 0, 0, 0.5)", zIndex: 1050 }}>
              <LoginBox handleGoogleLogin={handleGoogleLogin} handleGuestLogin={handleGuestLogin}  />   
          </div>
        )}

        <Outlet /> {/* render the appropriate page content based on the current route */}
      </main>
    </div>
  );
};

export default Layout; //export the Layout component for use in the app
