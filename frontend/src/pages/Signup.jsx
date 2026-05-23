import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { customerService, workerService } from '../services/api';
import './Auth.css';

const Signup = () => {
  const navigate = useNavigate();
  const [role, setRole] = useState('customer');
  const [formData, setFormData] = useState({
    name: '', email: '', password: '', phoneNumber: '', city: '', skill: '', address: '' 
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleChange = (e) => setFormData({...formData, [e.target.name]: e.target.value});

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true); setError('');
    try {
      if (role === 'customer') {
         const res = await customerService.createCustomer({
             name: formData.name, email: formData.email, password: formData.password,
             phoneNumber: formData.phoneNumber, address: formData.address
         });
         localStorage.clear();
         localStorage.setItem('customerId', res.user.id);
         localStorage.setItem('customerName', res.user.name);
         localStorage.setItem('token', res.token);
         navigate('/dashboard');
      } else {
         const res = await workerService.createWorker({
             name: formData.name, email: formData.email, password: formData.password,
             phoneNumber: formData.phoneNumber, city: formData.city, skill: formData.skill, available: true, experience: 1
         });
         localStorage.clear();
         localStorage.setItem('workerId', res.user.id);
         localStorage.setItem('workerName', res.user.name);
         localStorage.setItem('token', res.token);
         navigate('/worker-dashboard');
      }
    } catch(err) {
      setError('Registration failed.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="container auth-container animate-fade-in-up">
        <div className="auth-card card">
          <h2 style={{textAlign:'center', marginBottom: '8px'}}>Create an Account</h2>
          <p className="subtitle" style={{textAlign:'center', marginBottom: '24px', color:'var(--text-muted)'}}>Join our Home Services Platform</p>
          
          <div className="role-selector">
             <button type="button" className={`role-btn ${role === 'customer' ? 'active' : ''}`} onClick={() => setRole('customer')}>Customer</button>
             <button type="button" className={`role-btn ${role === 'worker' ? 'active' : ''}`} onClick={() => setRole('worker')}>Service Expert</button>
          </div>

          {error && <div className="error-badge" style={{background: 'var(--accent-danger)', color: 'white', padding: '12px', borderRadius: '8px', marginBottom: '16px'}}>{error}</div>}
          
          <form onSubmit={handleSubmit} className="auth-form">
            <div className="form-group">
              <label>Full Name</label>
              <input type="text" name="name" className="input-field" value={formData.name} onChange={handleChange} required />
            </div>
            <div className="form-group">
              <label>Email</label>
              <input type="email" name="email" className="input-field" value={formData.email} onChange={handleChange} required />
            </div>
            <div className="form-group">
              <label>Password</label>
              <input type="password" name="password" className="input-field" value={formData.password} onChange={handleChange} required />
            </div>
            <div className="form-group">
              <label>Phone Number</label>
              <input type="tel" name="phoneNumber" className="input-field" value={formData.phoneNumber} onChange={handleChange} required />
            </div>

            {role === 'customer' && (
               <div className="form-group">
                 <label>Address</label>
                 <input type="text" name="address" className="input-field" value={formData.address} onChange={handleChange} required />
               </div>
            )}

            {role === 'worker' && (
               <>
                 <div className="form-group">
                   <label>City</label>
                   <input type="text" name="city" className="input-field" value={formData.city} onChange={handleChange} required />
                 </div>
                 <div className="form-group">
                   <label>Primary Skill</label>
                   <input type="text" name="skill" className="input-field" placeholder="e.g. Plumber, Electrician" value={formData.skill} onChange={handleChange} required />
                 </div>
               </>
            )}
            
            <button type="submit" className="btn-primary" disabled={loading} style={{marginTop: '16px', width: '100%', padding: '12px'}}>
              {loading ? 'Creating...' : 'Sign Up'}
            </button>
            <p style={{textAlign: 'center', marginTop: '16px', fontSize: '0.9rem'}}>
               Already have an account? <Link to="/login" className="text-primary" style={{fontWeight: 600}}>Log In</Link>
            </p>
          </form>
        </div>
      </div>
    </div>
  );
};
export default Signup;
