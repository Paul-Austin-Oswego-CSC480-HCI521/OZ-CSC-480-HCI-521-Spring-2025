import "./App.css";
import { BrowserRouter as Router, Route, Routes } from "react-router-dom";
import LandingPage from "./pages/LandingPage";
import LoginPage from "./pages/LoginPage";
import AccountPage from "./pages/AccountPage";
import Layout from "./components/Layout";
import MyCollection from "./pages/MyCollection";
import QuoteForm from "./components/QuoteForm";
import DebugPage from "./pages/DebugPage";
import { userQuotes, bookmarkedQuotes } from "./placeholderdata";
import SearchPage from './pages/SearchPage';
import { AlertProvider, UserProvider } from "./lib/Contexts";
import CommunityGuidelinesPage from "./pages/CommunityGuidelinesPage";
import AdminPanel from "./pages/AdminPanel";

const App = () => {

  return (
    <AlertProvider>
      <UserProvider>
        <Router>
          <Routes>
            <Route path="/" element={<Layout />}>
              <Route path="/login" element={<LoginPage />} />
              <Route
                path="/my-collection"
                element={
                  <MyCollection 
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
              <Route path="/community-guidelines" element={<CommunityGuidelinesPage/>} />
              <Route path="/admin" element={<AdminPanel />} />
            </Route>
          </Routes>
        </Router>
      </UserProvider>
    </AlertProvider>
  );
};

export default App;
