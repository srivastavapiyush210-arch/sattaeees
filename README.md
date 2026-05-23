# Sattaees (सत्ताईस) 🛠️
> **Daily Wage Labor Service Platform** — Connecting local daily wage workers and skilled laborers directly with customers in need of immediate services.

Sattaees is a modern web application designed to bridge the gap between daily wage workers (such as electricians, plumbers, carpenters, painters, and general labor) and customers. The system provides secure authentication, direct job request booking, and a reviews/ratings system to build trust within the community.

---

## 🌟 Key Features

### 👤 Customer Portal
- **Dashboard**: View list of active/available workers filtered by skills.
- **Job Booking**: Create new job requests specifying description, wage, date, and location.
- **Worker Reviews**: Rate and review workers after job completion.
- **History**: Track past and current job requests and bookings.

### 👷 Worker Portal
- **Dashboard**: Manage availability status and view assigned job requests.
- **Skill Profile**: Highlight primary skills, contact information, and hourly/daily rates.
- **Job Flow**: Accept, reject, or mark job requests as completed.
- **Reputation**: Build a profile with aggregate ratings and feedback.

### 🔒 Security & Backend
- **JWT Authentication**: Role-based access control (CUSTOMER / WORKER) for secure actions.
- **Swagger Documentation**: Interactive OpenAPI UI to inspect and test all backend endpoints.
- **Local DB**: File-based H2 database for zero-configuration testing.

---

## 🛠️ Technology Stack

### Backend
- **Framework**: Spring Boot 3.2.1 (Java 17)
- **Database**: H2 (File-based local DB)
- **JPA & ORM**: Spring Data JPA / Hibernate
- **Security**: Spring Security + JSON Web Tokens (JWT)
- **Documentation**: Springdoc OpenAPI (Swagger UI)

### Frontend
- **Framework**: React 19 (Vite 8)
- **Routing**: React Router DOM v7
- **Icons**: Lucide React
- **Styling**: Modern, responsive Custom CSS with CSS variables, gradients, and micro-interactions.

---

## 📁 Project Structure

```text
sattaees/
├── src/main/java/com/sattaees/sattaees/
│   ├── config/          # Spring Security, CORS & JWT filter config
│   ├── controller/      # REST API Controllers (Customer, Worker, Jobs, Reviews, Auth)
│   ├── dto/             # Data Transfer Objects (Auth, Login Requests)
│   ├── init/            # Sample Data Initializer (runs on startup)
│   ├── model/           # JPA Entities (Customer, Worker, JobRequest, Review)
│   ├── repository/      # Spring Data JPA Repositories
│   ├── service/         # Business logic layer
│   └── util/            # Helper utils (JWT creation/validation)
├── src/main/resources/
│   └── application.properties  # Database, H2, and server settings
├── frontend/
│   ├── src/
│   │   ├── components/  # Reusable UI components (Navbar, Footer, WorkerCard)
│   │   ├── pages/       # Dashboard & Auth views (Home, Login, Signup, Dashboards)
│   │   ├── services/    # API calling client (Axios/fetch wrappers)
│   │   ├── App.jsx      # Route management
│   │   └── main.jsx     # Frontend entrypoint
│   └── package.json
└── pom.xml              # Maven dependencies configuration
```

---

## 🚀 Getting Started

### Prerequisites
- **Java Development Kit (JDK)**: Version 17 or higher.
- **Node.js**: Version 18 or higher.
- **npm** or **yarn**.

---

### Step 1: Run the Spring Boot Backend

1. Navigate to the root directory.
2. Run the following command to start the Spring Boot application:

```bash
# Windows
./mvnw.cmd spring-boot:run

# macOS/Linux
./mvnw spring-boot:run
```

The server will start on **[http://localhost:8080](http://localhost:8080)**.

> [!NOTE]
> On startup, the application runs `DataInitializer.java` to pre-seed the H2 database with test customer and worker accounts, reviews, and job requests.

---

### Step 2: Run the React Frontend

1. Open a new terminal window.
2. Navigate to the `frontend` folder:
   ```bash
   cd frontend
   ```
3. Install the dependencies:
   ```bash
   npm install
   ```
4. Start the Vite development server:
   ```bash
   npm run dev
   ```

The frontend will run at **[http://localhost:5173](http://localhost:5173)**.

---

## 🔌 API Endpoints Summary

### Authentication APIs
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/auth/customer/login` | Login as Customer (returns JWT Token) |
| `POST` | `/api/auth/worker/login` | Login as Worker (returns JWT Token) |

### Workers APIs
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/workers` | Get list of all workers (supports skill filter) |
| `GET` | `/api/workers/{id}` | Get worker profile details |
| `POST` | `/api/workers` | Create a new worker profile (Sign up) |
| `PUT` | `/api/workers/{id}` | Update worker details (Requires Auth) |

### Customers APIs
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/customers` | Create a new customer profile (Sign up) |
| `GET` | `/api/customers/{id}` | Get customer profile details (Requires Auth) |

### Job Requests APIs
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/jobs` | Create a new Job Request (Requires Customer Auth) |
| `GET` | `/api/jobs/customer/{id}` | Get all jobs booked by a Customer |
| `GET` | `/api/jobs/worker/{id}` | Get all jobs assigned to a Worker |
| `PUT` | `/api/jobs/{id}/status` | Update job status (ACCEPTED, COMPLETED) |

### Reviews APIs
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/reviews` | Post a worker review (Requires Customer Auth) |
| `GET` | `/api/reviews/worker/{id}` | Get all reviews for a specific worker |

---

## 🗃️ Development Resources & Utilities

- **Interactive API Documentation (Swagger)**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **H2 Database Console**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
  - **JDBC URL**: `jdbc:h2:file:./sattaeesdb`
  - **Username**: `sa`
  - **Password**: *(leave blank)*
