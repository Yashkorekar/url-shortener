# 🔗 URL Shortener

A production-ready REST API service that shortens long URLs. Built with Spring Boot and deployed on Render.

## ✨ Features

- ✅ Shorten long URLs into 7-character codes
- ✅ Redirect short URLs to original URLs  
- ✅ Track click statistics and access counts
- ✅ View top 3 most shortened domains
- ✅ Beautiful responsive web interface
- ✅ Interactive API documentation (Swagger/OpenAPI)
- ✅ Comprehensive URL validation (prevents SSRF attacks)
- ✅ Docker support with multi-stage builds
- ✅ Cloud deployment ready (Render, Heroku, etc.)

## 🌐 Live Demo

**Deployed on Render:** https://url-shortener-0l0j.onrender.com

Try it out - shorten any URL and share the short link!

---

## 🐳 Running with Docker (Recommended)

### Quick Start:
```bash
# Pull the latest image from Docker Hub
docker pull yashkorekar/url-shortener:latest

# Run the container
docker run -p 8081:8081 yashkorekar/url-shortener:latest
```

Access at: **http://localhost:8081**

### Available Docker Tags:
- `yashkorekar/url-shortener:latest` - Latest stable version
- `yashkorekar/url-shortener:v3.0` - Version 3.0 (with Swagger, validation, Render fixes)
- `yashkorekar/url-shortener:v2.0` - Version 2.0 (with Swagger and validation)

### Using Docker Compose:
```bash
docker-compose up -d
```

---

## 💻 Running Locally

### Prerequisites:
- Java 17 or higher
- Maven 3.6+ (or use included Maven Wrapper)

### Run:
```bash
# Using Maven Wrapper (recommended)
./mvnw spring-boot:run

# Or on Windows
.\mvnw.cmd spring-boot:run
```

Application runs on: **http://localhost:8081**

---

## 🎯 Usage

### Web Interface:
1. Open http://localhost:8081 in your browser
2. Paste a long URL (e.g., `https://github.com/Yashkorekar/url-shortener`)
3. Click **"Shorten URL"**
4. Copy and share your shortened URL!

### API Documentation:
Interactive Swagger UI: **http://localhost:8081/swagger-ui.html**

---

## 📚 API Endpoints

### 1. Shorten URL
```http
POST /api/shorten
Content-Type: application/json

{
  "url": "https://www.example.com/very/long/url"
}
```

**Response:**
```json
{
  "longUrl": "https://www.example.com/very/long/url",
  "shortCode": "abc123",
  "shortUrl": "http://localhost:8081/abc123"
```json
{
  "longUrl": "https://www.example.com/very/long/url",
  "shortCode": "xY7zK3m",
  "shortUrl": "http://localhost:8081/xY7zK3m",
  "createdAt": "2026-02-08T10:00:00"
}
```

### 2. Redirect to Original URL
```http
GET /{shortCode}
```
Redirects to the original long URL (302 redirect).

### 3. Get URL Statistics
```http
GET /api/stats/{shortCode}
```

**Response:**
```json
{
  "shortCode": "xY7zK3m",
  "longUrl": "https://www.example.com/very/long/url",
  "shortUrl": "http://localhost:8081/xY7zK3m",
  "createdAt": "2026-02-08T10:00:00",
  "accessCount": 42
}
```

### 4. Top Domains Metrics
```http
GET /api/metrics/domains
```

**Response:**
```json
[
  {"domain": "youtube.com", "count": 15},
  {"domain": "github.com", "count": 10},
  {"domain": "stackoverflow.com", "count": 7}
]
```

### 5. Health Check
```http
GET /health
```

---

## 🏗️ How It Works

1. **URL Shortening Algorithm:**
   - Input URL is hashed using SHA-256
   - Hash is Base64 encoded
   - First 7 characters form the short code
   - Collision handling ensures uniqueness

2. **Data Storage:**
   - In-memory storage using `ConcurrentHashMap`
   - Thread-safe for concurrent requests
   - Fast O(1) lookups

3. **Validation:**
   - Comprehensive URL validation (RFC 3986)
   - Protocol whitelisting (http/https only)
   - SSRF attack prevention (blocks localhost/private IPs)
   - Length limits (max 2048 characters)

---

## 🛠️ Tech Stack

### Backend:
- **Java 17** - Modern Java features
- **Spring Boot 4.0.2** - Web framework
- **Spring Validation** - Input validation
- **Lombok** - Boilerplate reduction

### API Documentation:
- **Swagger/OpenAPI 3.0** - Interactive API docs
- **SpringDoc** - OpenAPI integration

### DevOps:
- **Docker** - Containerization
- **Docker Compose** - Multi-container orchestration
- **Maven** - Build automation
- **Render** - Cloud deployment

---

## 📁 Project Structure

```
url-shortener/
├── src/main/java/com/yk/url_shortener/
│   ├── controller/       # REST API endpoints
│   │   ├── UrlShortenerController.java
│   │   └── HealthController.java
│   ├── service/          # Business logic
│   │   └── UrlShortenerService.java
│   ├── repository/       # Data access layer
│   │   └── UrlRepository.java
│   ├── model/            # Domain entities
│   │   └── Url.java
│   ├── dto/              # Data Transfer Objects
│   ├── validation/       # Custom validators
│   └── config/           # Configuration classes
├── src/main/resources/
│   ├── static/           # Web UI (HTML, CSS, JS)
│   └── application.properties
├── Dockerfile            # Docker image definition
├── docker-compose.yml    # Docker Compose configuration
└── pom.xml              # Maven dependencies
```

---

## 🚀 Deployment

### Deploy to Render:
1. Fork this repository
2. Connect to Render
3. Create a new Web Service
4. Select this repository
5. Build command: `mvn clean package -DskipTests`
6. Start command: `java -jar target/url-shortener-0.0.1-SNAPSHOT.jar`
7. Deploy!

**Environment Variables:**
- `PORT` - Auto-set by Render (app uses this automatically)

### Deploy with Docker:
```bash
docker pull yashkorekar/url-shortener:latest
docker run -d -p 8081:8081 --name url-shortener yashkorekar/url-shortener:latest
```

---

## 🔧 Development

### Build:
```bash
./mvnw clean package
```

### Run Tests:
```bash
./mvnw test
```

### Build Docker Image:
```bash
docker build -t url-shortener .
```

---

## 📝 Notes

- **Data Persistence:** Currently in-memory (data lost on restart). For production, consider PostgreSQL/Redis.
- **Scalability:** Stateless design allows horizontal scaling.
- **Security:** URL validation prevents SSRF attacks.
- **Rate Limiting:** Not implemented (consider for production).

