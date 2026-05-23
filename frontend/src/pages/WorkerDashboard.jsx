import React, { useState, useEffect } from 'react';
import { jobRequestService } from '../services/api';
import { Briefcase, Clock, TrendingUp, CheckCircle, User, MapPin, Calendar } from 'lucide-react';
import './CustomerDashboard.css'; /* inherits universal dash classes */
import './WorkerDashboard.css';

const WorkerDashboard = () => {
  const [jobs, setJobs] = useState([]);
  const [loading, setLoading] = useState(true);
  const workerId = localStorage.getItem('workerId');
  const workerName = localStorage.getItem('workerName');

  const fetchJobs = async () => {
    try {
      const data = await jobRequestService.getJobsForWorker(workerId);
      setJobs(data ? data.reverse() : []);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if(workerId) fetchJobs();
    else setLoading(false);
  }, [workerId]);

  const handleStatusChange = async (jobId, status) => {
    try {
      await jobRequestService.updateJobStatus(jobId, status);
      setJobs(jobs.map(job => job.id === jobId ? { ...job, status } : job).reverse());
    } catch(err) {
      alert("Failed to update status.");
    }
  };

  if(!workerId) {
      return (
          <div className="dashboard-wrapper">
              <div className="container" style={{textAlign:'center', marginTop:'100px'}}>
                  <h2>Please Log In to the Worker Portal.</h2>
              </div>
          </div>
      );
  }

  const statTotal = jobs.length;
  const statPending = jobs.filter(j => !j.status || j.status === 'REQUESTED').length;
  const statInProgress = jobs.filter(j => j.status === 'ACCEPTED' || j.status === 'IN_PROGRESS').length;
  const statCompleted = jobs.filter(j => j.status === 'COMPLETED').length;

  return (
    <div className="dashboard-wrapper">
      <div className="container">
        
        <div className="dash-header">
           <h1>Worker Dashboard</h1>
           <p>Manage your job requests and track your progress</p>
        </div>
        
        {/* Statistics Grid */}
        <div className="w-stats-grid">
           <div className="w-stat-card">
              <div className="icon-wrap bg-blue"><Briefcase size={22} color="#3b82f6"/></div>
              <div className="w-stat-body">
                 <h2>{statTotal}</h2>
                 <p>Total Jobs</p>
              </div>
           </div>
           <div className="w-stat-card">
              <div className="icon-wrap bg-orange"><Clock size={22} color="#f59e0b"/></div>
              <div className="w-stat-body">
                 <h2>{statPending}</h2>
                 <p>Pending</p>
              </div>
           </div>
           <div className="w-stat-card">
              <div className="icon-wrap bg-blue-light"><TrendingUp size={22} color="#3b82f6"/></div>
              <div className="w-stat-body">
                 <h2>{statInProgress}</h2>
                 <p>In Progress</p>
              </div>
           </div>
           <div className="w-stat-card">
              <div className="icon-wrap bg-green"><CheckCircle size={22} color="#10b981"/></div>
              <div className="w-stat-body">
                 <h2>{statCompleted}</h2>
                 <p>Completed</p>
              </div>
           </div>
        </div>

        {/* Job Status Flow */}
        <div className="w-status-flow-card">
           <h3>Job Status Flow</h3>
           <div className="flow-track">
              <div className="flow-step">
                 <div className="f-circle f-orange">1</div>
                 <span>REQUESTED</span>
              </div>
              <div className="f-line"></div>
              
              <div className="flow-step">
                 <div className="f-circle f-green">2</div>
                 <span>ACCEPTED</span>
              </div>
              <div className="f-line"></div>
              
              <div className="flow-step">
                 <div className="f-circle f-blue">3</div>
                 <span>IN PROGRESS</span>
              </div>
              <div className="f-line"></div>
              
              <div className="flow-step">
                 <div className="f-circle f-dark">4</div>
                 <span>COMPLETED</span>
              </div>
           </div>
        </div>

        <h3 className="section-title" style={{marginBottom:'24px', fontSize:'1.4rem'}}>All Job Requests</h3>
        
        {loading ? (
            <p>Loading your jobs...</p>
        ) : jobs.length === 0 ? (
            <p style={{color:'var(--text-muted)'}}>No incoming job requests yet.</p>
        ) : (
            <div className="w-jobs-grid">
               {jobs.map(job => (
                   <div key={job.id} className="job-card-figma flex-between-col">
                       
                       <div className="job-card-head">
                          <h4>{job.serviceType}</h4>
                          <span className={`badge-pill badge-${job.status ? job.status.toLowerCase() : 'requested'}`}>
                             <span className="dot"></span> {job.status || 'Requested'}
                          </span>
                       </div>
                       
                       <div className="job-card-worker">
                          <User size={16} color="var(--text-muted)"/> {job.customer ? job.customer.name : 'Unknown Customer'}
                       </div>

                       <div className="job-card-meta">
                          <span><MapPin size={14}/> {job.location || 'Mumbai'}</span>
                          <span><Calendar size={14}/> 2026-04-19</span>
                          <span><Clock size={14}/> 3:00 PM</span>
                       </div>

                       {(!job.status || job.status === 'REQUESTED') && (
                          <div className="w-action-buttons">
                             <button className="btn-w-accept" onClick={() => handleStatusChange(job.id, 'ACCEPTED')}>Accept</button>
                             <button className="btn-w-reject" onClick={() => handleStatusChange(job.id, 'CANCELLED')}>Reject</button>
                          </div>
                       )}
                       {job.status === 'ACCEPTED' && (
                          <div className="w-action-buttons">
                             <button className="btn-w-inprogress" onClick={() => handleStatusChange(job.id, 'IN_PROGRESS')}>Start Job In Progress</button>
                          </div>
                       )}
                       {job.status === 'IN_PROGRESS' && (
                          <div className="w-action-buttons">
                             <button className="btn-w-complete" onClick={() => handleStatusChange(job.id, 'COMPLETED')}>Complete Job</button>
                          </div>
                       )}
                   </div>
               ))}
            </div>
        )}
      </div>
    </div>
  );
};

export default WorkerDashboard;
