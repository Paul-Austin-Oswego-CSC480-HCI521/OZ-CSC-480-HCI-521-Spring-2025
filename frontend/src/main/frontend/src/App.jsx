import './App.css';
import React, { useState } from 'react';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import LandingPage from './pages/LandingPage';
import LoginPage from './pages/LoginPage';
import Layout from './components/Layout';
import SavedQuotes from './pages/SavedQuotes';
import QuoteForm from './components/QuoteForm';
import { userQuotes, bookmarkedQuotes } from './placeholderdata';

const App = () => {
  const [isLoggedIn, setIsLoggedIn] = useState(false);

  return (
    <Router>
      <Routes>
        <Route path="/" element={<Layout isLoggedIn={isLoggedIn} />}>
          <Route path="/login" element={<LoginPage />} />
          <Route 
            path="/saved-quotes" 
            element={<SavedQuotes userQuotes={userQuotes} bookmarkedQuotes={bookmarkedQuotes} />} 
          />
          <Route path="/quote/:id" element={<QuoteForm />} />
          <Route path="/" element={<LandingPage />} />
        </Route>
      </Routes>
    </Router>
  );
};

export default App;
