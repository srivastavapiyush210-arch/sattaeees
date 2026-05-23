import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Navbar from './components/Navbar';
import Home from './pages/Home';
import Workers from './pages/Workers';
import Signup from './pages/Signup';
import CustomerDashboard from './pages/CustomerDashboard';
import Login from './pages/Login';
import WorkerLogin from './pages/WorkerLogin';
import WorkerDashboard from './pages/WorkerDashboard';

function App() {
  return (
    <Router>
      <div className="app-container">
        <Navbar />
        <main className="main-content">
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/workers" element={<Workers />} />
            <Route path="/signup" element={<Signup />} />
            <Route path="/login" element={<Login />} />
            <Route path="/dashboard" element={<CustomerDashboard />} />
            <Route path="/worker-login" element={<WorkerLogin />} />
            <Route path="/worker-dashboard" element={<WorkerDashboard />} />
          </Routes>
        </main>
      </div>
    </Router>
  );
}

export default App;
