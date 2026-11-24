# LinkVault &mdash; Cloud Computing URL Shortener

LinkVault is a lightweight, cloud-native URL shortening application built with **Java 21**, **Spring Boot 3.4**, and **MongoDB Atlas**, featuring a modern, responsive SaaS dashboard served directly from Spring Boot static resources.

Designed for simple containerized deployment on **Render** using Docker.

---

## Tech Stack

- **Backend**: Java 21, Spring Boot 3.4.3, Spring Web, Spring Data MongoDB
- **Database**: MongoDB Atlas (Cloud NoSQL Document Store)
- **Frontend**: Vanilla HTML5, Modern CSS (Charcoal/Violet SaaS Theme), JavaScript
- **Build & Dependency Management**: Apache Maven (via Maven Wrapper `mvnw`)
- **Containerization & Deployment**: Docker (Multi-stage build), Render Web Services
- **Testing**: JUnit 5, Mockito, Spring Boot Test

---

## Features

1. **Shorten URL**: Generates a unique 6-character alphanumeric short code (`[a-zA-Z0-9]`) with collision detection.
2. **Instant Redirection**: Redirects clients via `HTTP 302 Found` from `/{shortCode}` to the original destination.
3. **Click Analytics**: Tracks total visits and increments click counters in real-time.
4. **URL Management**: List all shortened URLs with timestamps and delete links (`DELETE /api/urls/{id}`).
5. **Modern Dashboard**: Responsive SaaS interface with KPI metric cards, one-click clipboard copying, and error toasts.

---

## Project Structure

```plaintext
LinkVault/
├── .mvn/wrapper/              # Maven Wrapper configuration
├── src/
│   ├── main/
│   │   ├── java/com/linkvault/
│   │   │   ├── controller/    # REST & Redirection Controllers
│   │   │   ├── dto/           # Request/Response DTOs
│   │   │   ├── model/         # MongoDB @Document Entity (ShortUrl)
│   │   │   ├── repository/    # Spring Data MongoRepository
│   │   │   ├── service/       # Shortening, collision handling, click tracking
│   │   │   └── LinkvaultApplication.java
│   │   └── resources/
│   │       ├── static/        # Frontend Dashboard (HTML, CSS, JS, Favicon)
│   │       └── application.properties # Environment-driven configuration
│   └── test/                  # Unit and integration test suite
├── Dockerfile                 # Multi-stage production container for Render
├── .dockerignore              # Docker build exclusions
├── .gitignore                 # Git ignore rules (strictly excludes .env)
├── mvnw & mvnw.cmd            # Cross-platform Maven Wrapper
└── pom.xml                    # Project dependencies
```

---

## REST API Specification

| Method | Endpoint | Description | Status Code |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/urls/shorten` | Shortens destination URL (body: `{"url": "https://..."}`) | `201 Created` / `400 Bad Request` |
| `GET` | `/{shortCode}` | Increments click count and redirects to original destination | `302 Found` / `404 Not Found` |
| `GET` | `/api/urls` | Retrieves all shortened URL records and click counts | `200 OK` |
| `DELETE`| `/api/urls/{id}` | Deletes a shortened URL record by its MongoDB ID | `204 No Content` / `404 Not Found` |

---

## Local Development Setup

### 1. Clone the repository
```bash
git clone https://github.com/your-username/LinkVault.git
cd LinkVault
```

### 2. Configure Environment Variables
Create a local `.env` file in the root directory (this file is excluded from Git):
```env
MONGODB_URI=mongodb+srv://<username>:<password>@<cluster-host>.mongodb.net/linkvault?retryWrites=true&w=majority
PORT=8080
```

### 3. Run the Application
Using the Maven Wrapper:
- **Windows**:
  ```powershell
  .\mvnw.cmd spring-boot:run
  ```
- **macOS / Linux**:
  ```bash
  ./mvnw spring-boot:run
  ```

### 4. Access the Dashboard
Open your browser and navigate to:
```
http://localhost:8080
```

---

## Running Automated Tests

Run the test suite across controllers, services, and MongoDB Atlas ping validation:
```bash
# Windows
.\mvnw.cmd test

# Linux/macOS
./mvnw test
```

---

## Cloud Deployment (Render via Docker)

1. Push your repository to **GitHub**.
2. Create a new **Web Service** on [Render](https://render.com/).
3. Connect your GitHub repository.
4. Set the Environment to **Docker** (Render will automatically detect the multi-stage `Dockerfile`).
5. Under **Environment Variables**, add:
   - `MONGODB_URI`: Your MongoDB Atlas connection string.
6. Click **Deploy Web Service**.

Render will automatically inject the `$PORT` environment variable, bind the container, and serve LinkVault with HTTPS.
