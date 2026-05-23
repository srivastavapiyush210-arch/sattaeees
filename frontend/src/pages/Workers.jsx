import React, { useState, useEffect } from 'react';
import { Search, Zap, Droplets, Sparkles, Hammer, Paintbrush, Wind } from 'lucide-react';
import { workerService } from '../services/api';
import WorkerCard from '../components/WorkerCard';
import './Workers.css';

const serviceCategories = [
  { name: 'Electrician', icon: <Zap size={28} /> },
  { name: 'Plumber', icon: <Droplets size={28} /> },
  { name: 'House Cleaning', icon: <Sparkles size={28} /> },
  { name: 'Carpenter', icon: <Hammer size={28} /> },
  { name: 'Painter', icon: <Paintbrush size={28} /> },
  { name: 'AC Repair', icon: <Wind size={28} /> }
];

const Workers = () => {
  const [workers, setWorkers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [skill, setSkill] = useState('');
  const [city, setCity] = useState('');

  const fetchWorkers = async (searchSkill, searchCity) => {
    setLoading(true);
    try {
      let data;
      // Use different API endpoints depending on search fields provided
      if (searchSkill || searchCity) {
        data = await workerService.searchWorkers(searchSkill, searchCity);
      } else {
        data = await workerService.getAllWorkers();
      }
      
      if (data && data.length > 0) {
         setWorkers(data);
      } else {
         setWorkers([]);
      }
    } catch (err) {
      console.warn("Backend down or no data, using fallback", err);
      setWorkers([
        { id: 1, name: "Rahul Verma", skill: "Electrician", city: "Delhi", phoneNumber: "+91 9876543210" },
        { id: 2, name: "Priya Sharma", skill: "House Cleaning", city: "Mumbai", phoneNumber: "+91 8765432109" },
        { id: 3, name: "Amit Kumar", skill: "Plumber", city: "Bangalore", phoneNumber: "+91 7654321098" }
      ]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchWorkers('', '');
  }, []);

  const handleSearch = () => {
    fetchWorkers(skill, city);
  };

  return (
    <div className="workers-page">
      <div className="container">
        <div className="page-header animate-fade-in-up">
          <h1 className="text-gradient">Our Services</h1>
          <p>Select a service category to find verified professionals, or search directly.</p>
        </div>

        <div className="services-grid animate-fade-in-up" style={{ animationDelay: '0.05s' }}>
           {serviceCategories.map(cat => (
              <div 
                 key={cat.name} 
                 className={`service-cat-card ${skill === cat.name ? 'active' : ''}`}
                 onClick={() => { setSkill(cat.name); fetchWorkers(cat.name, city); }}
              >
                 <div className="cat-card-icon">{cat.icon}</div>
                 <h3>{cat.name}</h3>
              </div>
           ))}
        </div>
        
        <div className="filter-bar glass-card animate-fade-in-up" style={{ animationDelay: '0.1s' }}>
          <div className="filter-inputs">
            <div className="input-group">
              <input 
                type="text" 
                className="input-field" 
                placeholder="Skill (e.g. Electrician)"
                value={skill}
                onChange={(e) => setSkill(e.target.value)}
              />
            </div>
            <div className="input-group">
              <input 
                type="text" 
                className="input-field" 
                placeholder="City (e.g. Delhi)"
                value={city}
                onChange={(e) => setCity(e.target.value)}
              />
            </div>
          </div>
          <button className="btn-primary search-btn-wide" onClick={handleSearch}>
            <Search size={18} /> Search
          </button>
        </div>

        {loading ? (
          <div className="loading-state">Loading workers...</div>
        ) : (
          <div>
            {workers.length === 0 ? (
              <div className="loading-state">No workers found matching your search.</div>
            ) : (
              <div className="workers-grid animate-fade-in-up" style={{ animationDelay: '0.2s' }}>
                {workers.map(w => (
                  <WorkerCard key={w.id} worker={w} />
                ))}
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
};

export default Workers;
