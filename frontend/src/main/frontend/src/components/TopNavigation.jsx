import React from 'react';
import { Link } from 'react-router-dom';

const TopNavigation = ({ isLoggedIn }) => {

  const circleStyle = {
    display: 'inline-flex',
    justifyContent: 'center',
    alignItems: 'center',
    width: '36px',
    height: '36px',
    backgroundColor: '#007bff',
    borderRadius: '50%',
    color: 'white',
    fontSize: '24px'
  };

  return (
    <nav className="navbar navbar-expand-lg navbar-light bg-light sticky-top">
        <Link className="navbar-brand pl-2" to="/">Home</Link>
      <button className="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarNav" aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
        <span className="navbar-toggler-icon"></span>
      </button>
      <div className="collapse navbar-collapse pr-2" id="navbarNav">
        <ul className="navbar-nav ml-auto">
          {!isLoggedIn && (
            <li className="nav-item">
              <Link className="nav-link" to="/login">Login</Link>
            </li>
          )}
          {isLoggedIn && (
            <li className="nav-item">
              <div style={circleStyle}>
                <i className="bi bi-person"></i>
              </div>
            </li>
          )}
        </ul>
      </div>
    </nav>
  );
};
export default TopNavigation;
