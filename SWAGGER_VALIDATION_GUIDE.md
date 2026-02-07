# 📚 Swagger & URL Validation Features

## 🎉 New Features Added

### 1. **Swagger/OpenAPI Documentation**

Interactive API documentation is now available! This provides:
- ✅ Complete API documentation with examples
- ✅ Try-it-out functionality to test APIs directly from the browser
- ✅ Automatic request/response schema generation
- ✅ Example values for all endpoints

#### Access Swagger UI:

```
http://localhost:8081/swagger-ui.html
```

#### Access OpenAPI JSON:

```
http://localhost:8081/v3/api-docs
```

### 2. **Enhanced URL Validation**

Comprehensive URL validation has been implemented with:
- ✅ **Format validation** - Checks for valid URL structure
- ✅ **Protocol validation** - Only allows http:// and https://
- ✅ **Host validation** - Ensures valid domain/host
- ✅ **Length validation** - Max 2048 characters
- ✅ **Security checks** - Blocks localhost and private IP addresses
- ✅ **Optional reachability check** - Can verify if URL is accessible (disabled by default for performance)

---

## 🚀 Using Swagger UI

### Step 1: Start Your Application

```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

### Step 2: Open Swagger UI

Navigate to: **http://localhost:8081/swagger-ui.html**

### Step 3: Explore APIs

You'll see all available endpoints:

1. **POST /api/shorten** - Shorten a URL
2. **GET /{shortCode}** - Redirect to original URL
3. **GET /api/stats/{shortCode}** - Get URL statistics
4. **GET /api/metrics/domains** - Get top 3 domains

### Step 4: Try It Out!

1. Click on any endpoint to expand it
2. Click **"Try it out"** button
3. Fill in the request body or parameters
4. Click **"Execute"**
5. See the response with status code, headers, and body

---

## 🔒 URL Validation Details

### What URLs are Accepted?

✅ **Valid:**
```
https://www.google.com
http://example.com/path/to/page
https://stackoverflow.com/questions/12345
https://www.youtube.com/watch?v=abc123
```

❌ **Invalid:**
```
localhost:8080                    # Missing protocol
http://localhost/path             # Localhost blocked
http://192.168.1.1               # Private IP blocked
http://10.0.0.1                  # Private IP blocked
ftp://example.com                # Invalid protocol
www.google.com                   # Missing protocol
http://                          # Missing host
```

### Validation Rules:

1. **Required Protocol:** Must start with `http://` or `https://`
2. **Valid Host:** Must have a proper domain or IP (no localhost/private IPs)
3. **Length Limit:** Maximum 2048 characters
4. **Well-formed:** Must be a syntactically valid URL

### Custom Validator Annotations:

The `@ValidUrl` annotation in `ShortenUrlRequest` provides:

```java
@ValidUrl(
    message = "Invalid URL format or unreachable domain",
    checkReachability = false  // Set to true to verify URL is accessible
)
```

**Options:**
- `checkReachability` - If true, makes HTTP HEAD request to verify URL exists (slower)
- `allowedProtocols` - Array of allowed protocols (default: `["http", "https"]`)

---

## 📝 Example API Usage via Swagger

### Example 1: Shorten a URL

**Request:**
```json
{
  "url": "https://www.example.com/very/long/url/path"
}
```

**Response (201 Created):**
```json
{
  "longUrl": "https://www.example.com/very/long/url/path",
  "shortCode": "xY7zK3m",
  "shortUrl": "http://localhost:8081/xY7zK3m",
  "createdAt": "2026-02-07T19:30:00"
}
```

### Example 2: Get Statistics

**Request:** `GET /api/stats/xY7zK3m`

**Response (200 OK):**
```json
{
  "shortCode": "xY7zK3m",
  "longUrl": "https://www.example.com/very/long/url/path",
  "shortUrl": "http://localhost:8081/xY7zK3m",
  "createdAt": "2026-02-07T19:30:00",
  "accessCount": 42
}
```

### Example 3: Get Domain Metrics

**Request:** `GET /api/metrics/domains`

**Response (200 OK):**
```json
[
  {
    "domain": "udemy.com",
    "count": 6
  },
  {
    "domain": "youtube.com",
    "count": 4
  },
  {
    "domain": "wikipedia.org",
    "count": 2
  }
]
```

---

## 🛠️ Configuration

### Swagger Configuration (application.properties)

```properties
# Swagger/OpenAPI paths
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.path=/swagger-ui.html

# UI sorting
springdoc.swagger-ui.tags-sorter=alpha
springdoc.swagger-ui.operations-sorter=alpha
```

### Custom Swagger Settings (SwaggerConfig.java)

Modify `src/main/java/com/yk/url_shortener/config/SwaggerConfig.java` to customize:
- API title and description
- Contact information
- License information
- Server URLs

---

## 🧪 Testing Validation

### Test 1: Invalid Protocol
```bash
curl -X POST http://localhost:8081/api/shorten \
  -H "Content-Type: application/json" \
  -d '{"url":"ftp://example.com"}'
```

**Expected:** 400 Bad Request with validation error

### Test 2: Localhost URL
```bash
curl -X POST http://localhost:8081/api/shorten \
  -H "Content-Type: application/json" \
  -d '{"url":"http://localhost:8080/test"}'
```

**Expected:** 400 Bad Request - "Cannot shorten localhost URLs"

### Test 3: Valid URL
```bash
curl -X POST http://localhost:8081/api/shorten \
  -H "Content-Type: application/json" \
  -d '{"url":"https://www.google.com"}'
```

**Expected:** 201 Created with shortened URL

---

## 📊 Benefits

### For Developers:
- ✅ **Interactive Testing:** Test all APIs without writing curl commands
- ✅ **Auto-generated Docs:** No need to manually maintain API documentation
- ✅ **Schema Validation:** See exact request/response structures
- ✅ **Examples:** Every field has example values

### For API Consumers:
- ✅ **Clear Documentation:** Understand all endpoints at a glance
- ✅ **Try Before Integration:** Test APIs before writing code
- ✅ **Error Messages:** Clear validation error messages
- ✅ **Security:** Localhost/private IP blocking prevents misuse

---

## 🔧 Technical Details

### Dependencies Added (pom.xml):

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>
```

### Files Created:

1. **SwaggerConfig.java** - Swagger/OpenAPI configuration
2. **ValidUrl.java** - Custom validation annotation
3. **ValidUrlValidator.java** - Validation logic implementation

### Files Modified:

1. **pom.xml** - Added Swagger dependency
2. **ShortenUrlRequest.java** - Added `@ValidUrl` annotation
3. **UrlShortenerController.java** - Added Swagger annotations
4. **ShortenUrlResponse.java** - Added schema descriptions
5. **UrlStatsResponse.java** - Added schema descriptions
6. **DomainMetrics.java** - Added schema descriptions
7. **application.properties** - Added Swagger configuration

---

## 🎯 Quick Links

When running locally:

- **Application:** http://localhost:8081
- **Swagger UI:** http://localhost:8081/swagger-ui.html
- **API Docs (JSON):** http://localhost:8081/v3/api-docs
- **Health Check:** http://localhost:8081/health

---

## 💡 Tips

1. **Swagger UI Shortcuts:**
   - Expand all endpoints: Click "List Operations"
   - Try an API: Click "Try it out" → Fill params → "Execute"
   - See models: Scroll to "Schemas" section at bottom

2. **Validation Customization:**
   - Enable reachability check: Set `checkReachability = true` in `@ValidUrl`
   - Allow other protocols: Modify `allowedProtocols` array
   - Disable localhost blocking: Comment out `isLocalOrPrivateAddress()` check

3. **Performance:**
   - Reachability checks add 3-5 seconds per request (disabled by default)
   - Validation runs before URL shortening
   - Invalid URLs are rejected early (saves processing)

---

## 📚 Additional Resources

- **SpringDoc OpenAPI:** https://springdoc.org/
- **OpenAPI Specification:** https://swagger.io/specification/
- **Bean Validation:** https://beanvalidation.org/

---

**Enjoy your enhanced URL Shortener with Swagger documentation and robust validation!** 🚀

