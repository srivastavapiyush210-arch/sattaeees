package com.sattaees.sattaees.init;

import com.sattaees.sattaees.model.Worker;
import com.sattaees.sattaees.model.Customer;
import com.sattaees.sattaees.repository.WorkerRepository;
import com.sattaees.sattaees.repository.CustomerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {

    private final WorkerRepository workerRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(WorkerRepository workerRepository, CustomerRepository customerRepository, PasswordEncoder passwordEncoder) {
        this.workerRepository = workerRepository;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if(customerRepository.count() == 0) {
            log.info("No customers found in database. Initializing demo customer data...");
            Customer c = new Customer();
            c.setName("Demo Customer");
            c.setEmail("demo@customer.com");
            c.setPassword(passwordEncoder.encode("password123"));
            c.setPhoneNumber("+91 9999999999");
            c.setAddress("Gateway of India, Mumbai");
            customerRepository.save(c);
            log.info("Demo customer registered: {}", c.getEmail());
        }
        
        if(workerRepository.count() == 0) {
            log.info("No workers found in database. Seeding demo worker records...");
            String pass = passwordEncoder.encode("pass123");
            workerRepository.save(new Worker(null, "Rahul Verma", "rahul@sattaees.com", pass, "+91 9876543210", "Electrician", 5, "Delhi", true, 45.0, 4.8, 120));
            workerRepository.save(new Worker(null, "Priya Sharma", "priya@sattaees.com", pass, "+91 8765432109", "House Cleaning", 3, "Mumbai", true, 25.0, 4.5, 45));
            workerRepository.save(new Worker(null, "Amit Kumar", "amit@sattaees.com", pass, "+91 7654321098", "Plumber", 4, "Bangalore", true, 35.0, 4.9, 210));
            workerRepository.save(new Worker(null, "Neha Singh", "neha@sattaees.com", pass, "+91 6543210987", "Interior Designer", 8, "Delhi", true, 85.0, 5.0, 89));
            workerRepository.save(new Worker(null, "Sanjay Patel", "sanjay@sattaees.com", pass, "+91 5432109876", "Electrician", 6, "Mumbai", true, 50.0, 4.6, 150));
            workerRepository.save(new Worker(null, "Vikram Rathore", "vikram@sattaees.com", pass, "+91 4432109876", "Carpenter", 8, "Delhi", true, 40.0, 4.2, 70));
            workerRepository.save(new Worker(null, "Sneha Reddy", "sneha@sattaees.com", pass, "+91 3432109876", "Plumber", 5, "Bangalore", true, 38.0, 4.8, 114));
            workerRepository.save(new Worker(null, "Arjun Kapoor", "arjun@sattaees.com", pass, "+91 2432109876", "House Cleaning", 1, "Mumbai", true, 20.0, 3.9, 15));
            log.info("Demo premium users and workers database seeding completed successfully!");
        }
    }
}

