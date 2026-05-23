import React, { useState } from 'react';
import { Search, MapPin, Wrench, Zap, Sparkles, Hammer } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import './Hero.css';

const Hero = () => {
  const navigate = useNavigate();
  const [skill, setSkill] = useState('');
  const [city, setCity] = useState('');

  const handleSearch = () => {
    navigate(`/dashboard?skill=${encodeURIComponent(skill)}&city=${encodeURIComponent(city)}`);
  };

  return (
    <div className="hero-section">
      <div className="container hero-layout">
         
         {/* Left Side: Copy and Search */}
         <div className="hero-left">
            <h1 className="hero-title">
               Find Trusted <br />
               <span style={{color: 'var(--primary)'}}>Profe</span><span style={{color: 'var(--secondary)'}}>ssionals</span> for <br />
               Every Home Service
            </h1>
            <p className="hero-subtitle">
               Book verified experts for plumbing, electrical, cleaning, and more. Quality service at your doorstep.
            </p>
            
            <div className="hero-search-box">
               <div className="search-field">
                  <Search size={18} color="var(--text-muted)"/>
                  <input type="text" placeholder="Search for a service..." value={skill} onChange={(e)=>setSkill(e.target.value)} />
               </div>
               <div className="search-field">
                  <MapPin size={18} color="var(--text-muted)"/>
                  <input type="text" placeholder="Your location..." value={city} onChange={(e)=>setCity(e.target.value)} />
               </div>
               <button className="btn-search-solid" onClick={handleSearch}>Search Services</button>
            </div>
         </div>

         {/* Right Side: Grid Graphics */}
         <div className="hero-right">
            <div className="services-grid-card">
               <div className="service-square">
                  <Wrench className="sv-icon" size={32}/>
                  <span className="sv-text">Plumbing</span>
               </div>
               <div className="service-square">
                  <Zap className="sv-icon" size={32}/>
                  <span className="sv-text">Electrical</span>
               </div>
               <div className="service-square">
                  <Sparkles className="sv-icon" size={32}/>
                  <span className="sv-text">Cleaning</span>
               </div>
               <div className="service-square">
                  <Hammer className="sv-icon" size={32}/>
                  <span className="sv-text">Carpentry</span>
               </div>
            </div>
         </div>

      </div>
    </div>
  );
};

export default Hero;
