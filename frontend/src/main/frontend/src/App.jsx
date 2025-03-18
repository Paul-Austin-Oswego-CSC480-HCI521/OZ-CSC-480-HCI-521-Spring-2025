import "./App.css";
import { BrowserRouter as Router, Route, Routes } from "react-router-dom";
import LandingPage from "./pages/LandingPage";
import LoginPage from "./pages/LoginPage";
import AccountPage from "./pages/AccountPage";
import Layout from "./components/Layout";
import SavedQuotes from "./pages/SavedQuotes";
import QuoteForm from "./components/QuoteForm";
import DebugPage from "./pages/DebugPage";
import { userQuotes, bookmarkedQuotes } from "./placeholderdata";
import SearchPage from './pages/SearchPage';
import AccountSetup from './pages/AccountSetup';
import { UserProvider } from "./lib/Contexts";

const App = () => {

  return (
    <UserProvider>
      <Router>
        <Routes>
          <Route path="/" element={<Layout />}>
            <Route path="/login" element={<LoginPage />} />
            <Route
              path="/saved-quotes"
              element={
                <SavedQuotes
                  userQuotes={userQuotes}
                  bookmarkedQuotes={bookmarkedQuotes}
                />
              }
            />
            <Route path="/edit-quote/:id" element={<QuoteForm />} />
            <Route path="/debug" element={<DebugPage />} />
            <Route path="/" element={<LandingPage />} />
            <Route path="/account" element={<AccountPage />} />
            <Route path="/search" element={<SearchPage />} />
            <Route path="/setup" element={<AccountSetup />} />
          </Route>
        </Routes>
      </Router>
    </UserProvider>
  );
};

export default App;
