import { Link } from 'react-router-dom';
import { useEffect } from 'react';
import 'bootstrap/dist/css/bootstrap.min.css';
import 'bootstrap/dist/js/bootstrap.bundle.min.js';


const TopNavigation = ({ user, notifications = [] }) => {
    useEffect(() => {
        const dropdownElementList = document.querySelectorAll('.dropdown-toggle');
        dropdownElementList.forEach((dropdownToggleEl) => {
            new window.bootstrap.Dropdown(dropdownToggleEl);
        });
    }, []);
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

            {user && (
                <li className="nav-item dropdown mx-2 position-relative">
                    <button className="btn btn-light dropdown-toggle position-relative" id="notificationDropdown" data-bs-toggle="dropdown" aria-expanded="false">
                        <i className="bi bi-bell" style={{ fontSize: "24px" }}></i>
                        {notifications.length > 0 && (
                            <span className="badge bg-danger position-absolute" style={{ top: "5px", right: "5px", fontSize: "12px" }}>
                                        {notifications.length}
                                    </span>
                        )}
                    </button>
                    {/*<ul className="dropdown-menu dropdown-menu-end" aria-labelledby="notificationDropdown">*/}
                    {/*    {notifications.length > 0 ? (*/}
                    {/*        notifications.map((notif, index) => (*/}
                    {/*            <li key={index}>*/}
                    {/*                <Link className="dropdown-item text-wrap" to={`/notifications/${notif.id}`}>*/}
                    {/*                    {notif.message}*/}
                    {/*                </Link>*/}
                    {/*            </li>*/}
                    {/*        ))*/}
                    {/*    ) : (*/}
                    {/*        <li>*/}
                    {/*            <span className="dropdown-item text-muted">No new notifications</span>*/}
                    {/*        </li>*/}
                    {/*    )}*/}
                    {/*</ul>*/}
                    <ul className="dropdown-menu dropdown-menu-end p-2" aria-labelledby="notificationDropdown" style={{ minWidth: "250px" }}>
                        {notifications.length > 0 ? (
                            notifications.map((notif, index) => (
                                <li key={index} className="dropdown-item border-bottom py-2">
                                    <Link to={`/notifications/${notif.id}`} className="text-decoration-none text-dark d-block">
                                        <strong>🔔 {notif.message}</strong>
                                        <p className="mb-0 text-muted" style={{ fontSize: "12px" }}>Click to view</p>
                                    </Link>
                                </li>
                            ))
                        ) : (
                            <li className="dropdown-item text-center text-muted">No new notifications</li>
                        )}
                    </ul>

                </li>
            )}

          {!user && (
            <li className="nav-item">
              <Link className="nav-link" to="/login">Login</Link>
            </li>
          )}
          {user && (
            <li className="nav-item">
              <div style={circleStyle}>
                <i className="bi bi-person"></i>
              </div>
              {user.Username}
            </li>
          )}
        </ul>
      </div>
    </nav>
  );
};
export default TopNavigation;
