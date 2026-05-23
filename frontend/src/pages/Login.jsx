import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { customerService, workerService } from '../services/api';
import './Auth.css';

const Login = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({ email: '', password: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [role, setRole] = useState('customer');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true); setError('');
    try {
        if(role === 'customer') {
            const res = await customerService.loginCustomer({ email: formData.email, password: formData.password });
            localStorage.clear();
            localStorage.setItem('customerId', res.user.id);
            localStorage.setItem('customerName', res.user.name);
            localStorage.setItem('token', res.token);
            window.location.href = '/dashboard';
        } else {
            const res = await workerService.loginWorker({ email: formData.email, password: formData.password });
            localStorage.clear();
            localStorage.setItem('workerId', res.user.id);
            localStorage.setItem('workerName', res.user.name);
            localStorage.setItem('token', res.token);
            window.location.href = '/worker-dashboard';
        }
    } catch(err) {
        setError("Invalid login credentials.");
    } finally {
        setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="container auth-container animate-fade-in-up">
        <div className="auth-card card">
          <h2 style={{textAlign:'center', marginBottom: '8px'}}>Welcome Back</h2>
          <p className="subtitle" style={{textAlign:'center', marginBottom: '24px', color:'var(--text-muted)'}}>Log in to your account</p>
          
          <div className="role-selector">
             <button type="button" className={`role-btn ${role === 'customer' ? 'active' : ''}`} onClick={() => setRole('customer')}>Customer</button>
             <button type="button" className={`role-btn ${role === 'worker' ? 'active' : ''}`} onClick={() => setRole('worker')}>Service Expert</button>
          </div>

          {error && <div className="error-badge" style={{background: 'var(--accent-danger)', color: 'white', padding: '12px', borderRadius: '8px', marginBottom: '16px'}}>{error}</div>}
          
          <form onSubmit={handleSubmit} className="auth-form">
            <div className="form-group">
              <label>Email / Username (or User ID)</label>
              <input type="text" className="input-field" value={formData.email} onChange={(e) => setFormData({...formData, email: e.target.value})} required />
            </div>
            <div className="form-group">
              <label>Password</label>
              <input type="password" className="input-field" value={formData.password} onChange={(e) => setFormData({...formData, password: e.target.value})} required />
            </div>
            <button type="submit" className="btn-primary" disabled={loading} style={{marginTop: '16px', width: '100%', padding: '12px'}}>
              {loading ? 'Logging in...' : 'Log In'}
            </button>
            <p style={{textAlign: 'center', marginTop: '16px', fontSize: '0.9rem'}}>
               Don't have an account? <Link to="/signup" className="text-primary" style={{fontWeight: 600}}>Sign Up</Link>
            </p>
          </form>
        </div>
      </div>
    </div>
  );
};
export default Login;
