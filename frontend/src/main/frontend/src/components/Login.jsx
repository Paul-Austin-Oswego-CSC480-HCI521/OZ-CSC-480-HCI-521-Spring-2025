import React from 'react';

const LoginBox = ({ handleGoogleLogin, handleGuestLogin }) => {
  return (
    <div className="d-flex flex-column align-items-center justify-content-center bg-light p-4 rounded shadow" style={{ maxWidth: '400px' }}>
      <h2 className="mb-3">Sign in</h2>
      <button onClick={handleGoogleLogin} className="btn btn-outline-primary w-100 d-flex align-items-center justify-content-center mb-2">
        <img src="/GoogleLogo.png" alt="Google logo" className="me-2" width="20" />
        Sign in with Google
      </button>
      <button className="btn btn-secondary w-100" onClick={handleGuestLogin}>
        Guest Mode
      </button>
    </div>
  );
};

export default LoginBox;
