import React, { useEffect, useState } from 'react';
import Hero from '../components/Hero';
import WorkerCard from '../components/WorkerCard';
import { workerService } from '../services/api';
import Footer from '../components/Footer';
import { CheckCircle, Shield, Award, Star, Wrench, Zap, Sparkles, Hammer, PaintRoller, ThermometerSnowflake, Bug, WashingMachine } from 'lucide-react';
import './Home.css';

const Home = () => {
  const [featured, setFeatured] = useState([]);
  
  useEffect(() => {
    const fetchFeatured = async () => {
      try {
        const data = await workerService.getAllWorkers();
        // In the Figma screenshot we have exactly 3 top workers:
        setFeatured(data.slice(0, 3)); 
      } catch(err) {
         setFeatured([]);
      }
    };
    fetchFeatured();
  }, []);

  return (
    <div className="home-wrapper">
      <Hero />
      
      {/* 2. Stats Banner (Screenshot 2) */}
      <section className="container">
         <div className="stats-banner">
            <div className="stat-pill"><CheckCircle className="st-icon green"/> Verified Professionals</div>
            <div className="stat-pill"><Shield className="st-icon blue"/> Secure Payment</div>
            <div className="stat-pill"><Award className="st-icon orange"/> Quality Guaranteed</div>
            <div className="stat-pill"><Star className="st-icon red" fill="#ef4444"/> 10,000+ Happy Customers</div>
         </div>
      </section>

      {/* 3. Service Categories (Screenshot 2) */}
      <section className="container figma-section categories-section">
         <div className="section-header">
            <h2>Service Categories</h2>
            <p>Browse our wide range of professional home services</p>
         </div>
         <div className="categories-grid-8">
            <div className="cat-card"><Wrench size={36} className="cat-icon"/> <span className="cat-label">Plumbing</span></div>
            <div className="cat-card"><Zap size={36} className="cat-icon"/> <span className="cat-label">Electrical</span></div>
            <div className="cat-card"><Sparkles size={36} className="cat-icon"/> <span className="cat-label">Cleaning</span></div>
            <div className="cat-card"><Hammer size={36} className="cat-icon"/> <span className="cat-label">Carpentry</span></div>
            <div className="cat-card"><PaintRoller size={36} className="cat-icon"/> <span className="cat-label">Painting</span></div>
            <div className="cat-card"><ThermometerSnowflake size={36} className="cat-icon"/> <span className="cat-label">AC Repair</span></div>
            <div className="cat-card"><Bug size={36} className="cat-icon"/> <span className="cat-label">Pest Control</span></div>
            <div className="cat-card"><WashingMachine size={36} className="cat-icon"/> <span className="cat-label">Appliance</span></div>
         </div>
      </section>

      {/* 4. Service Packages (Screenshot 3) */}
      <section className="container figma-section packages-section">
         <div className="section-header">
            <h2>Service Packages</h2>
            <p>Choose the perfect plan for your needs</p>
         </div>
         <div className="packages-grid">
            {/* Basic Package */}
            <div className="package-card">
               <h3 className="pkg-title">Basic</h3>
               <p className="pkg-subtitle">Perfect for single tasks</p>
               <div className="pkg-price">₹499 <span>/ 1 hour</span></div>
               <ul className="pkg-features">
                  <li><CheckCircle size={16} className="st-icon green"/> Single service visit</li>
                  <li><CheckCircle size={16} className="st-icon green"/> Basic tools included</li>
                  <li><CheckCircle size={16} className="st-icon green"/> 7-day warranty</li>
                  <li><CheckCircle size={16} className="st-icon green"/> Standard response time</li>
               </ul>
               <button className="pkg-btn-secondary">Choose Plan</button>
            </div>

            {/* Standard Package (Most Popular) */}
            <div className="package-card popular-card">
               <div className="popular-badge">Most Popular</div>
               <h3 className="pkg-title">Standard</h3>
               <p className="pkg-subtitle">Most popular choice</p>
               <div className="pkg-price">₹1299 <span>/ 3 hours</span></div>
               <ul className="pkg-features">
                  <li><CheckCircle size={16} className="st-icon green"/> Up to 3 hours service</li>
                  <li><CheckCircle size={16} className="st-icon green"/> Advanced tools included</li>
                  <li><CheckCircle size={16} className="st-icon green"/> 30-day warranty</li>
                  <li><CheckCircle size={16} className="st-icon green"/> Priority support</li>
                  <li><CheckCircle size={16} className="st-icon green"/> Follow-up service</li>
               </ul>
               <button className="pkg-btn-primary">Choose Plan</button>
            </div>

            {/* Premium Package */}
            <div className="package-card">
               <h3 className="pkg-title">Premium</h3>
               <p className="pkg-subtitle">For complex projects</p>
               <div className="pkg-price">₹2499 <span>/ 1 day</span></div>
               <ul className="pkg-features">
                  <li><CheckCircle size={16} className="st-icon green"/> Full day service</li>
                  <li><CheckCircle size={16} className="st-icon green"/> Professional equipment</li>
                  <li><CheckCircle size={16} className="st-icon green"/> 90-day warranty</li>
                  <li><CheckCircle size={16} className="st-icon green"/> 24/7 priority support</li>
                  <li><CheckCircle size={16} className="st-icon green"/> Free follow-up visits</li>
                  <li><CheckCircle size={16} className="st-icon green"/> Satisfaction guarantee</li>
               </ul>
               <button className="pkg-btn-secondary">Choose Plan</button>
            </div>
         </div>
      </section>

      {/* 5. Featured Professionals (Screenshot 4) */}
      <section className="container figma-section featured-section">
         <div className="section-header">
            <h2>Featured Professionals</h2>
            <p>Top-rated experts ready to help you</p>
         </div>
         <div className="featured-workers-grid">
            {featured.map(w => (
               <WorkerCard key={w.id} worker={w} />
            ))}
         </div>
      </section>

      {/* 6. Testimonials (Screenshot 5) */}
      <section className="container figma-section testimonials-section" style={{marginBottom: '100px'}}>
         <div className="section-header">
            <h2>What Our Customers Say</h2>
            <p>Real experiences from real customers</p>
         </div>
         <div className="testimonials-grid">
            <div className="testimonial-card">
               <div className="stars">
                  <Star fill="#f59e0b" color="#f59e0b" size={18}/>
                  <Star fill="#f59e0b" color="#f59e0b" size={18}/>
                  <Star fill="#f59e0b" color="#f59e0b" size={18}/>
                  <Star fill="#f59e0b" color="#f59e0b" size={18}/>
                  <Star fill="#f59e0b" color="#f59e0b" size={18}/>
               </div>
               <p className="test-quote">"Sattaees made it so easy to find reliable professionals. The booking process is seamless!"</p>
               <div className="test-user">
                  <img src="https://api.dicebear.com/7.x/notionists/svg?seed=Arjun" alt="Arjun Kapoor" />
                  <div>
                     <h4>Arjun Kapoor</h4>
                     <span>Homeowner</span>
                  </div>
               </div>
            </div>
            
            <div className="testimonial-card">
               <div className="stars">
                  <Star fill="#f59e0b" color="#f59e0b" size={18}/>
                  <Star fill="#f59e0b" color="#f59e0b" size={18}/>
                  <Star fill="#f59e0b" color="#f59e0b" size={18}/>
                  <Star fill="#f59e0b" color="#f59e0b" size={18}/>
                  <Star fill="#f59e0b" color="#f59e0b" size={18}/>
               </div>
               <p className="test-quote">"I use Sattaees for all my office maintenance needs. Highly professional service providers."</p>
               <div className="test-user">
                  <img src="https://api.dicebear.com/7.x/notionists/svg?seed=Meera" alt="Meera Patel" />
                  <div>
                     <h4>Meera Patel</h4>
                     <span>Business Owner</span>
                  </div>
               </div>
            </div>
            
            <div className="testimonial-card">
               <div className="stars">
                  <Star fill="#f59e0b" color="#f59e0b" size={18}/>
                  <Star fill="#f59e0b" color="#f59e0b" size={18}/>
                  <Star fill="#f59e0b" color="#f59e0b" size={18}/>
                  <Star fill="#f59e0b" color="#f59e0b" size={18}/>
                  <Star fill="#f59e0b" color="#f59e0b" size={18}/>
               </div>
               <p className="test-quote">"Best platform for home services. Transparent pricing and verified professionals."</p>
               <div className="test-user">
                  <img src="https://api.dicebear.com/7.x/notionists/svg?seed=Karan" alt="Karan Verma" />
                  <div>
                     <h4>Karan Verma</h4>
                     <span>Property Manager</span>
                  </div>
               </div>
            </div>
         </div>
      </section>

      <Footer />
    </div>
  );
};
export default Home;
