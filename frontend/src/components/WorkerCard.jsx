import React from 'react';
import { MapPin, Briefcase, Star, Eye } from 'lucide-react';
import './WorkerCard.css';

const WorkerCard = ({ worker, onHire }) => {
  return (
    <div className="worker-figma-card">
       <div className="worker-profile-row">
          <div className="worker-avatar">
             <img src={`https://api.dicebear.com/7.x/notionists/svg?seed=${worker.name}&backgroundColor=f1f5f9`} alt="avatar" />
          </div>
          <div className="worker-info">
             <h3>{worker.name}</h3>
             <p className="worker-skill">{worker.skill}</p>
             <div className="worker-stats-row">
                <span><MapPin size={12}/> {worker.city}</span>
                <span><Briefcase size={12}/> {worker.experienceYears || Math.floor(Math.random() * 10 + 2)} yrs</span>
                <span className="rating-text"><Star size={12} fill="#f59e0b" color="#f59e0b"/> {worker.averageRating || '4.8'}</span>
             </div>
          </div>
       </div>
       
       <div className="worker-status-price-row">
          <span className={`status-badge-figma ${worker.available ? 'available' : 'busy'}`}>
             <span className="dot"></span> {worker.available ? 'Available' : 'Busy'}
          </span>
          <span className="worker-price">₹ {worker.hourlyRate || 500}/hr</span>
       </div>

       <div className="worker-action-row">
          <button className="btn-view-profile"><Eye size={16}/> View Profile</button>
          {worker.available ? (
             <button className="btn-hire-now" onClick={() => onHire && onHire(worker.id)}>Hire Now</button>
          ) : (
             <button className="btn-unavailable" disabled>Unavailable</button>
          )}
       </div>
    </div>
  )
}
export default WorkerCard;
