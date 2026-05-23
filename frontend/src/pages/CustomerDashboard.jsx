import React, { useState, useEffect } from 'react';
import { Search, MapPin, Filter, User, Calendar, Clock, Star, RotateCcw } from 'lucide-react';
import { workerService, jobRequestService, reviewService } from '../services/api';
import WorkerCard from '../components/WorkerCard';
import './CustomerDashboard.css';

const CustomerDashboard = () => {
  const customerId = localStorage.getItem('customerId');
  const [skill, setSkill] = useState('');
  const [city, setCity] = useState('');
  const [workers, setWorkers] = useState([]);
  const [loadingWorkers, setLoadingWorkers] = useState(false);
  const [myJobs, setMyJobs] = useState([]);

  useEffect(() => {
    if (!customerId) { window.location.href = '/login'; return; }
    fetchWorkerList('', '');
    fetchMyJobs();
  }, [customerId]);

  const fetchWorkerList = async (searchSkill, searchCity) => {
    setLoadingWorkers(true);
    try {
      let data;
      if (searchSkill || searchCity) {
        data = await workerService.searchWorkers(searchSkill, searchCity);
      } else {
        data = await workerService.getAllWorkers();
      }
      setWorkers(data || []);
    } catch (err) { setWorkers([]); } finally { setLoadingWorkers(false); }
  };

  const fetchMyJobs = async () => {
    try {
      const data = await jobRequestService.getAllJobRequests();
      const mapped = data.filter(job => job.customer && String(job.customer.id) === String(customerId));
      setMyJobs(mapped.reverse());
    } catch (err) { console.error(err); }
  };

  const handleReset = () => {
     setSkill(''); setCity(''); fetchWorkerList('','');
  };

  return (
    <div className="dashboard-wrapper">
      <div className="container">
        
        <div className="dash-header">
           <h1>Customer Dashboard</h1>
           <p>Find and hire the perfect professional for your needs</p>
        </div>

        <div className="dashboard-grid">
           
           {/* LEFT COLUMN */}
           <div className="dash-left">
              <div className="search-filter-card">
                 <h3 className="card-title-icon"><Filter size={18} color="var(--primary)"/> Search Workers</h3>
                 
                 <div className="search-row-flex">
                    <div className="search-input-field">
                       <Search size={18} color="var(--text-muted)"/>
                       <input type="text" placeholder="Search by skill..." value={skill} onChange={(e)=>setSkill(e.target.value)}/>
                    </div>
                    <div className="search-input-field">
                       <MapPin size={18} color="var(--text-muted)"/>
                       <input type="text" placeholder="Search by city..." value={city} onChange={(e)=>setCity(e.target.value)}/>
                    </div>
                 </div>

                 <div className="search-actions-flex">
                    <button className="btn-solid-blue full-w" onClick={() => fetchWorkerList(skill, city)}>Search</button>
                    <button className="btn-grey" onClick={handleReset}>Reset</button>
                 </div>
              </div>

              <div className="avail-section-head">
                 <h3>Available Workers</h3>
              </div>
              
              <div className="dash-workers-grid">
                 {loadingWorkers ? <p>Loading...</p> : (
                    workers.length > 0 ? workers.map(w => <WorkerCard key={w.id} worker={w} onHire={fetchMyJobs}/>) : <p>No professionals found.</p>
                 )}
              </div>
           </div>

           {/* RIGHT COLUMN */}
           <div className="dash-right">
              <h3>My Job Requests</h3>
              
              <div className="requests-feed">
                 {myJobs.map(job => (
                    <div key={job.id} className="job-card-figma">
                       <div className="job-card-head">
                          <h4>{job.serviceType}</h4>
                          <span className={`badge-pill badge-${job.status ? job.status.toLowerCase() : 'requested'}`}>
                             <span className="dot"></span> {job.status || 'Requested'}
                          </span>
                       </div>
                       
                       <div className="job-card-worker">
                          <User size={16} color="var(--text-muted)"/> {job.worker ? job.worker.name : 'Unknown Worker'}
                       </div>

                       <div className="job-card-meta">
                          <span><MapPin size={14}/> {job.location || 'Mumbai'}</span>
                          <span><Calendar size={14}/> 2026-04-18</span>
                          <span><Clock size={14}/> 10:00 AM</span>
                       </div>
                    </div>
                 ))}
                 {myJobs.length === 0 && <p style={{color:'var(--text-muted)', fontSize:'0.9rem'}}>No jobs requested yet.</p>}
              </div>
           </div>

        </div>
      </div>
    </div>
  );
};
export default CustomerDashboard;
