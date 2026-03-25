# 🌐 JavaServ — Custom HTTP/1.1 Web Server

> A lightweight, crash-proof, non-blocking HTTP/1.1 server built from scratch in Java — no frameworks, no shortcuts.

---

## 📖 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [CGI Support](#cgi-support)
- [Testing](#testing)
- [Authors](#authors)

---

## Overview

JavaServ is a custom HTTP/1.1-compliant web server implemented in pure Java using non-blocking I/O (`java.nio`). It handles static content, file uploads, CGI execution, sessions, cookies, and multiple virtual servers — all within a single process and a single thread using an event-driven architecture.

This project was built as part of a systems programming curriculum to deeply understand how web servers operate at the protocol level.

---

## ✨ Features

### Core Server
- ✅ HTTP/1.1 compliant request/response handling
- ✅ Single-process, single-thread event-driven architecture via Java NIO `Selector`
- ✅ Non-blocking I/O for all connections
- ✅ Multi-port and multi-server support from one config file
- ✅ Long-request timeouts (crash-proof design)
- ✅ Chunked and unchunked transfer encoding support

### HTTP Methods
- ✅ `GET` — serve static files and directory listings
- ✅ `POST` — handle form data and file uploads
- ✅ `DELETE` — remove server-side resources

### Request Handling
- ✅ File upload support
- ✅ Cookie parsing and injection
- ✅ Session management
- ✅ Custom error pages: `400`, `401`, `403`, `404`, `405`, `409`, `413`, `500`
- ✅ Configurable client body size limit
- ✅ HTTP redirections

### CGI
- ✅ Execute CGI scripts (e.g., `.py`) via `ProcessBuilder`
- ✅ `PATH_INFO` environment variable support
- ✅ Correct relative path handling

---

## 📁 Project Structure

```
.
├── config.json                  # Server configuration file
├── run.sh                       # Build and run script
├── custom_error_pages/          # HTML error pages
│   ├── 400.html
│   ├── 401.html
│   ├── 403.html
│   ├── 404.html
│   ├── 405.html
│   ├── 409.html
│   ├── 413.html
│   └── 500.html
├── src/
│   ├── Main.java                # Entry point
│   ├── config/                  # Configuration parsing layer
│   │   ├── Parser.java
│   │   ├── SimpleConfigLexer.java
│   │   ├── model/
│   │   │   └── WebServerConfig.java
│   │   └── utils/
│   │       ├── CookieParser.java
│   │       ├── Session.java
│   │       ├── SessionManager.java
│   │       └── Validators.java
│   ├── customError/
│   │   └── FormatException.java
│   ├── DTO/                     # Data Transfer Objects
│   │   ├── Route.java
│   │   └── Server.java
│   ├── handlers/                # Request handlers
│   │   ├── CgiHandler.java
│   │   ├── ChunkedHundler.java
│   │   ├── DeleteHandler.java
│   │   ├── ErrorHandler.java
│   │   ├── RedirectHandler.java
│   │   ├── StaticFileHandler.java
│   │   └── UploadsHandler.java
│   ├── http/                    # HTTP protocol layer
│   │   ├── HttpHandler.java
│   │   ├── HttpHeader.java
│   │   ├── HttpParser.java
│   │   ├── HttpRequest.java
│   │   └── HttpResponse.java
│   └── Nio/                     # Non-blocking I/O core
│       ├── ClientHandler.java
│       └── NioServer.java
└── www/
    ├── html/
    │   └── primary/
    │       └── index.html
    └── scripts/
        └── test.py              # Sample CGI script
```

---

## 🚀 Getting Started

### Prerequisites

- Java 17 or higher
- Python 3 (for CGI script execution)

### Build & Run

```bash
# Clone the repository
git clone <repository-url>
cd java-localserver

# Make the run script executable
chmod +x run.sh

# Start the server
./run.sh
```

Or compile and run manually:

```bash
# Compile
find src -name "*.java" | xargs javac -d out/

# Run
java -cp out/ Main config.json
```

The server will start listening on the ports defined in `config.json`.

---

## ⚙️ Configuration

The server is configured via `config.json`. Below is a reference of supported fields:

```json
[
  {
    "name": "primary.local",
    "host": "127.0.0.1",
    "port": [
      8081,
      8023,
      8082,
      8084,
      8088,
      8092
    ],
    "defaultServer": true,
    "limitSize": 1024,
    "errorPages": {
      "400": "./custom_error_pages/400.html",
      "401": "./custom_error_pages/401.html",
      "403": "./custom_error_pages/403.html",
      "404": "./custom_error_pages/404.html",
      "405": "./custom_error_pages/405.html",
      "409": "./custom_error_pages/409.html",
      "413": "./custom_error_pages/413.html",
      "500": "./custom_error_pages/500.html"
    },
    "routes": [
      {
        "path": "/",
        "root": "./www/html/primary",
        "methods": [
          "GET"
        ],
        "index": "index.html"
      },
      {
        "path": "/uploads",
        "root": "./www/uploads",
        "methods": [
          "GET",
          "POST",
          "DELETE"
        ]
      },
      {
        "path": "/cgi-bin",
        "root": "./www/scripts",
        "methods": [
          "GET",
          "POST"
        ],
        "cgiExtension": ".py"
      },
      {
        "path": "/redirect",
        "methods": [
          "GET"
        ],
        "redirectTo": "/www",
        "redirectStatusCode": 302
      },
      {
        "path": "/www",
        "root": "./www/",
        "methods": [
          "GET",
          "DELETE"
        ],
        "directoryListing": true
      }
    ]
  }
]
```

### Configuration Options

| Field | Description |
|---|---|
| `host` | IP address to bind |
| `port` | Port to listen on |
| `default` | Mark as default server for unmatched hosts |
| `client_max_body_size` | Maximum allowed request body size |
| `error_pages` | Map of HTTP status codes to custom HTML files |
| `routes[].path` | URL prefix to match |
| `routes[].methods` | Allowed HTTP methods |
| `routes[].root` | Filesystem root for this route |
| `routes[].default_file` | Default file served for directory requests |
| `routes[].directory_listing` | Enable/disable directory browsing |
| `routes[].cgi_extension` | File extension to route through CGI |
| `routes[].redirect` | Redirect target URL |

---

## 🐍 CGI Support

The server supports CGI script execution via `ProcessBuilder`. Scripts are invoked with the request file path as the first argument, and the `PATH_INFO` environment variable is set to the full resource path.

**Example CGI script** (`www/scripts/test.py`):
```python
#!/usr/bin/env python3
printf("Hello World !")
```

To invoke it, map a route with `"cgi_extension": ".py"` in `config.json` and request `/cgi-bin/test.py`.

---

## 🧪 Testing

### Functional Testing

Test the server manually with `curl`:

```bash
# GET request
curl http://localhost:<port>/

# POST with body
curl -X POST http://localhost:<port>/upload -F "file=@myfile.txt"

# DELETE request
curl -X DELETE http://localhost:<port>/upload//<file_name> 

# Test error handling
curl http://localhost:<port>/<wrong_path/ data>
```

### Stress Testing

Use [siege](https://www.joedog.org/siege-home/) to benchmark availability:

```bash
# Install siege (Ubuntu/Debian)
sudo apt install siege

# Run stress test (target: ≥ 99.5% availability)
siege -b http://127.0.0.1:<port>/
```

Expected output should show an availability of **99.5% or higher**.

### Memory & Leak Testing

```bash
# Monitor file descriptors
lsof -p <PID> | wc -l

# Monitor memory usage over time
watch -n 1 "ps -o pid,rss,vsz -p <PID>"
```

> ⚠️ **Disclaimer:** Only run stress tests against servers you own or have explicit permission to test. Using `siege` or similar tools against third-party servers without permission is illegal and unethical.

---

## 👥 Authors
 
This project was built collaboratively by a team of three.
 
---
 
### Author 1
 
| | |
|---|---|
| **Name** | Hamza Elkhawlani |
| **GitHub** | [heeemzaaa](https://github.com/heeemzaaa) |
| **Email** | hamzaelkhawlani00@gmail.com |
 
---
 
### Author 2
 
| | |
|---|---|
| **Name** | Ismail Sayen |
| **GitHub** | [ismailsayen](https://github.com/ismailsayen) |
| **Email** | ismailsvn02@gmail.com |
 
---
 
### Author 3
 
| | |
|---|---|
| **Name** | Youssef El Asri |
| **GitHub** | [yelasri07](https://github.com/yelasri07) |
| **Email** | elasriyoussef604@gmail.com |
 
---

## 📚 References

- [RFC 2616 — HTTP/1.1 Specification](https://www.rfc-editor.org/rfc/rfc2616)
- [Java NIO Documentation](https://docs.oracle.com/javase/8/docs/api/java/nio/package-summary.html)
- [CGI Protocol Overview](https://www.ietf.org/rfc/rfc3875)
- [Siege Load Testing Tool](https://www.joedog.org/siege-home/)

---

*Built for educational purposes as part of a systems programming curriculum.*