import React, { useState, useEffect } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { Home, Briefcase, LayoutDashboard, Bell, Sun, Moon, User, LogOut } from 'lucide-react';
import './Navbar.css';

const Navbar = () => {
  const location = useLocation();
  const customerId = localStorage.getItem('customerId');
  const workerId = localStorage.getItem('workerId');
  
  let userRole = null;
  if(customerId) userRole = 'customer';
  if(workerId) userRole = 'worker';

  const [theme, setTheme] = useState(localStorage.getItem('theme') || 'light');
  useEffect(() => {
     document.documentElement.setAttribute('data-theme', theme);
     localStorage.setItem('theme', theme);
  }, [theme]);
  const toggleTheme = () => setTheme(prev => prev === 'dark' ? 'light' : 'dark');

  return (
    <nav className="navbar glass-card">
      <div className="container nav-container">
          
          <div className="nav-brand">
            <Link to="/">Sattaees</Link>
          </div>
          
          <div className="nav-links desktop-only">
            <Link to="/" className={`nav-link-pill ${location.pathname === '/' ? 'active' : ''}`}>
               <Home size={16} /> Home
            </Link>
            <Link to="/workers" className={`nav-link-pill ${location.pathname === '/workers' ? 'active' : ''}`}>
               <Briefcase size={16} /> Services
            </Link>
            <Link to={userRole === 'worker' ? "/worker-dashboard" : "/dashboard"} className={`nav-link-pill ${location.pathname.includes('dashboard') ? 'active' : ''}`}>
               <LayoutDashboard size={16} /> Dashboard
            </Link>
          </div>
          
          <div className="nav-actions desktop-only">
             <button onClick={toggleTheme} className="nav-icon-btn" title="Toggle Theme">
                {theme === 'dark' ? <Sun size={20} /> : <Moon size={20} />}
             </button>
             
             <button className="nav-icon-btn notification-btn" title="Notifications">
                <Bell size={20} />
                <span className="notification-dot"></span>
             </button>
             
             {userRole ? (
                <button onClick={() => { localStorage.clear(); window.location.href = '/login'; }} className="nav-icon-btn" title="Logout">
                   <LogOut size={20} />
                </button>
             ) : (
                <Link to="/login" className="nav-icon-btn" title="Login">
                   <User size={20} />
                </Link>
             )}
          </div>

      </div>
    </nav>
  );
};

export default Navbar;
