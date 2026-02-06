# URL Shortener

A REST API service that shortens long URLs. Built with Spring Boot.

## Features

- Shorten long URLs
- Redirect short URLs to original URLs  
- Track click statistics
- View top 3 most shortened domains
- Web interface included

## Running with Docker

Prerequisites: Docker installed

```bash
docker pull yashkorekar/url-shortener:latest
docker run -p 8082:8082 yashkorekar/url-shortener:latest
```

Access at http://localhost:8082

## Running Locally

Prerequisites: Java 17

```bash
./mvnw spring-boot:run
```

Application runs on http://localhost:8082

## Usage

Open http://localhost:8082 in browser. Paste a long URL and click Shorten.

## API Endpoints

### Shorten URL
```
POST /api/shorten
Content-Type: application/json

{
  "longUrl": "https://www.example.com/very/long/url"
}
```

Response:
```json
{
  "longUrl": "https://www.example.com/very/long/url",
  "shortCode": "abc123",
  "shortUrl": "http://localhost:8082/abc123"
}
```

### Access Short URL
```
GET /{shortCode}
```

Redirects to original URL.

### Get Statistics
```
GET /api/stats/{shortCode}
```

Returns click count and URL details.

### Top Domains
```
GET /api/metrics/top-domains
```

Returns top 3 most shortened domains.

## How It Works

The service uses SHA-256 hashing to generate short codes. Long URLs are hashed, converted to Base64, and truncated to 7 characters. Data is stored in memory using ConcurrentHashMap.

## Tech Stack

- Java 17
- Spring Boot 4.0.2
- Maven
- Docker

## Project Structure

```
src/main/java/com/yk/url_shortener/
  - controller/    # REST endpoints
  - service/       # Business logic  
  - repository/    # Data storage
  - model/         # URL entity
  - dto/           # Request/Response objects
```

## Notes

- Data is stored in memory and cleared on restart

