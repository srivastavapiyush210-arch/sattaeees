import React from 'react';
import { Link } from 'react-router-dom';
import './Signup.css';

const WorkerLogin = () => {
  return (
    <div className="signup-page">
      <div className="container signup-container animate-fade-in-up">
        <div className="signup-card glass-card" style={{textAlign: 'center'}}>
          <h2>Security Update Active</h2>
          <p className="subtitle" style={{marginBottom:'24px'}}>
             The legacy "Worker Dropdown Selection" has been permanently disabled in favor of our new cryptographically secure JWT authentication infrastructure. 
          </p>
          <p className="subtitle" style={{marginBottom:'32px'}}>
             Please use the standard Login form to authenticate using your designated encrypted Worker email and password!
          </p>
          <Link to="/login" className="btn-primary" style={{padding:'14px 32px', display:'inline-block', textDecoration:'none', borderRadius:'10px'}}>Proceed to Global Login</Link>
        </div>
      </div>
    </div>
  );
};
export default WorkerLogin;
