import React from 'react';
import LoginBox from '../components/Login';

const LoginPage = ({ setIsAuthenticated, setIsGuest }) => {
  const handleGoogleLogin = () => {
    window.location.href = 'blank for now, replace with endpoint';
  };

  const handleGuestLogin = () => {
    setIsGuest(true);
  };

  return (
    <div className="d-flex justify-content-center align-items-center vh-100">
      <LoginBox handleGoogleLogin={handleGoogleLogin} handleGuestLogin={handleGuestLogin} />
    </div>
  );
};

export default LoginPage;
