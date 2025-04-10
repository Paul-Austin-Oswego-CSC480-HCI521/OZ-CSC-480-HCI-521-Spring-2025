import React from 'react';
import { FaTimes, FaUser } from 'react-icons/fa'; 
import { useNavigate } from 'react-router-dom';
import { FocusTrap } from 'focus-trap-react';

const LoginBox = ({ showLogin }) => {
  const navigate = useNavigate(); 

  const handleClose = () => {
    showLogin = false;
    navigate('/');
  };

  const handleGoogleLogin = () => {
    window.location.href = 'http://localhost:9081/users/auth/login';
  };

  const handleGuestLogin = () => {
    showLogin = false;
    navigate('/'); 
  };

  return (
    <div 
      className="d-flex flex-column align-items-center justify-content-center shadow position-relative"
      style={{
        width: '469px',
        height: '280px',
        padding: '40px',
        gap: '24px',
        background: '#FFFFFF',
        border: '1px solid #9B9B9B',
        borderRadius: '8px'
      }}
    >
      {/* Header Section */}
      <div 
        className="d-flex justify-content-between align-items-center" 
        style={{ width: '222px', height: '48px', gap: '82px' }}
      >
        <h2 
          className="mb-0"
          style={{
            fontFamily: 'Inter',
            fontWeight: 600,
            fontSize: '24px',
            lineHeight: '24px',
            textAlign: 'left',
            color: '#1E1E1E',
          }}
        >
          Welcome!
        </h2>
        <button 
          onClick={handleClose} 
          className="btn"
          aria-label="Exit login"
          style={{ 
            width: '50px',
            height: '40px',
            background: 'none',
            border: 'none',
            cursor: 'pointer'
          }}
        >
          <FaTimes size={20} color="#1E1E1E" />
        </button>
      </div>

      {/* Buttons Section */}
      <div className="d-flex flex-column align-items-center" style={{ width: '309px', height: '128px', gap: '16px' }}>
        
        {/* Continue with Google Button */}
        <button 
          onClick={handleGoogleLogin} 
          className="d-flex align-items-center justify-content-center w-100"
          style={{
            width: '309px',
            height: '56px',
            borderRadius: '6px',
            padding: '16px 24px',
            background: '#1E1E1E',
            border: '1px solid #1E1E1E',
            color: '#FFFFFF',
            fontFamily: 'Inter',
            fontWeight: 500,
            fontSize: '20px',
            lineHeight: '24.2px'
          }}
          aria-label="Login with a Google Account button"
        >
          <span 
            className="d-flex align-items-center justify-content-center" 
            style={{ width: '16.73px', height: '8.42px', marginRight: '8px' }}
          >
            G
          </span>
          Continue with Google
        </button>

        {/* Continue as Guest Button */}
        <button 
          onClick={handleGuestLogin} 
          className="d-flex align-items-center justify-content-center w-100"
          style={{
            width: '309px',
            height: '56px',
            borderRadius: '6px',
            padding: '16px 24px',
            background: '#FFFFFF',
            border: '1px solid #1E1E1E',
            color: '#1E1E1E',
            fontFamily: 'Inter',
            fontWeight: 500,
            fontSize: '20px',
            lineHeight: '24.2px'
          }}
          aria-label="Continue as Guest button"
        >
          <FaUser size={20} className="me-1" />
         <div style={{paddingLeft: '14px'}}>Continue as Guest</div>
        </button>

      </div>
    </div>
  );
};

export default LoginBox;
