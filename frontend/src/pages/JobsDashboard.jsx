import React, { useState, useEffect } from 'react';
import { jobRequestService } from '../services/api';
import './JobsDashboard.css';

const JobsDashboard = () => {
  const [jobs, setJobs] = useState([]);
  const [loading, setLoading] = useState(true);
  const customerId = localStorage.getItem('customerId');
  const customerName = localStorage.getItem('customerName');

  useEffect(() => {
    const fetchJobs = async () => {
      try {
        const data = await jobRequestService.getAllJobRequests();
        const myJobs = data.filter(job => job.customer && String(job.customer.id) === String(customerId));
        setJobs(myJobs);
      } catch (err) {
        console.error("Failed to load jobs", err);
      } finally {
        setLoading(false);
      }
    };
    if(customerId) {
        fetchJobs();
    } else {
        setLoading(false);
    }
  }, [customerId]);

  if(!customerId) {
      return (
          <div className="dashboard-page">
              <div className="container" style={{textAlign:'center', marginTop:'100px'}}>
                  <h2>Please Log In or Sign Up to view your jobs.</h2>
              </div>
          </div>
      );
  }

  return (
    <div className="dashboard-page">
      <div className="container">
        <div className="page-header animate-fade-in-up">
          <h1 className="text-gradient">Welcome, {customerName}</h1>
          <p>Here are your job requests.</p>
        </div>
        
        {loading ? (
            <div className="loading-state">Loading jobs...</div>
        ) : jobs.length === 0 ? (
            <div className="empty-state glass-card">
              <h3>No Jobs Yet</h3>
              <p>You haven't booked any professionals. Go to "Find Talent" to get started!</p>
            </div>
        ) : (
            <div className="jobs-list animate-fade-in-up" style={{animationDelay: '0.1s'}}>
               {jobs.map(job => (
                   <div key={job.id} className="job-card glass-card">
                       <div className="job-header">
                           <h3>{job.serviceType}</h3>
                           <span className={`status-badge status-${job.status ? job.status.toLowerCase() : 'requested'}`}>
                               {job.status || 'REQUESTED'}
                           </span>
                       </div>
                       <div className="job-details">
                           <p><strong>Worker:</strong> {job.worker ? job.worker.name : 'Unassigned'}</p>
                           <p><strong>Location:</strong> {job.location}</p>
                       </div>
                   </div>
               ))}
            </div>
        )}
      </div>
    </div>
  );
};

export default JobsDashboard;
