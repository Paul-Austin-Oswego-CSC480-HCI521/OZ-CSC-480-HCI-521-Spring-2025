import React from 'react';
import { Outlet } from 'react-router-dom';
import TopNavigation from './TopNavigation';

const Layout = ({ isLoggedIn }) => {

  return (
    <div>
      <TopNavigation isLoggedIn={isLoggedIn} />
      <main className="container mt-4">
        <Outlet />
      </main>
    </div>
  );
};

export default Layout;