import { Link, useNavigate } from 'react-router-dom';

const TopNavigation = ({ user }) => {

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

  const navigate = useNavigate();

  //  Upload Quote currently does nothing. 

  return (
    <nav className="navbar navbar-expand-lg navbar-light bg-light sticky-top">
        <Link className="navbar-brand pl-2" to="/">Logo</Link>
      <button className="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarNav" aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
        <span className="navbar-toggler-icon"></span>
      </button>
      <div className="collapse navbar-collapse pr-2" id="navbarNav">
        <ul className="navbar-nav ml-auto">
          <li className="nav-item">
            <Link className="nav-link" to="/">Home</Link>
          </li>
          {!user && (
            <>
              <li className="nav-item">
                <button className="btn btn-dark" onClick={ () => { navigate("/login") } }>Login</button>
              </li>
            </>
          )}
          {user && (
            <>
              <li className="nav-item">
                <Link className="nav-link">Upload Quote</Link> 
              </li>
              <li className="nav-item">
                <Link className="nav-link" to="/saved-quotes">Saved</Link>
              </li>
              <li className="nav-item">
                <div style={circleStyle}>
                  <i className="bi bi-person"></i>
                </div>
                {user.Username}
              </li>
            </>
          )}
        </ul>
      </div>
    </nav>
  );
};
export default TopNavigation;
