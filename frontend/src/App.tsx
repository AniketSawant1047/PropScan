import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import Navbar from './components/layout/Navbar';
import HomePage from './pages/HomePage';
import SearchResultsPage from './pages/SearchResultsPage';
import PropertyDetailsPage from './pages/PropertyDetailsPage';
import SavedPropertiesPage from './pages/SavedPropertiesPage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import InstallPwaPopup from './components/InstallPwaPopup';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: { retry: 1, staleTime: 5 * 60 * 1000 }
  }
});

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <div className="min-h-screen flex flex-col">
          <Navbar />
          <InstallPwaPopup />
          <main className="flex-1">

            <Routes>
              <Route path="/" element={<HomePage />} />
              <Route path="/results" element={<SearchResultsPage />} />
              <Route path="/property/:groupId" element={<PropertyDetailsPage />} />
              <Route path="/saved" element={<SavedPropertiesPage />} />
              <Route path="/login" element={<LoginPage />} />
              <Route path="/register" element={<RegisterPage />} />
            </Routes>
          </main>
        </div>
      </BrowserRouter>
    </QueryClientProvider>
  );
}

export default App;
