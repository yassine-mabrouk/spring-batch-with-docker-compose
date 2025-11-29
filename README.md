
# 📘 Spring Batch – Customer Excel Export

A Spring Batch application that exports customer data from an H2 database into Excel files.  
It provides REST API endpoints to manually trigger the batch, list generated Excel files, and download them.  
It also supports automatic execution via scheduler and runs in Docker Compose.

---

##  Start Application

### Builds and runs the batch service inside Docker.

```bash

cd spring-batch-with-docker-compose
docker-compose up -d --build
docker-compose logs -f   # View real-time logs

```
### Check Running Container

```bash

docker ps

```

### Rebuild After Code Changes

```bash

docker-compose down
docker rm -f customer-export-batch # Remove the old container if exist 
docker-compose up -d --build

```

---

## 📊 API Endpoints (localhost)

### **1️⃣ Health Check**

```bash

curl http://localhost:8080/api/batch/health
```

---

### **2️⃣ Trigger Batch Job Manually**

```bash

curl -X POST http://localhost:8080/api/batch/trigger
```

---

### **3️⃣ List All Excel Files**

```bash

curl http://localhost:8080/api/batch/files
```

---

### **4️⃣ Download Today's File**

```bash

curl -O http://localhost:8080/api/batch/download/today
```

---

### **5️⃣ Download Specific File (replace date)**

```bash
curl -O http://localhost:8080/api/batch/download/customers_20251128.xlsx
```

---

## 📁 Output Directory

Excel files are stored inside:

```
/app/output
```

Mapped to your local machine when running Docker Compose.

---

## 🛠 Requirements

- Docker  
- Docker Compose  
- cURL (for API testing)

---

## 📦 Features

- Read customers from H2 database  
- Write Excel `.xlsx` files
- Scheduled execution every minute (for testing), configurable via Docker Compose for any period ( every 2 minutes, daily, etc.)
- Manual trigger via REST API  
- Download generated files

---

##  Embedded Postman Collection

```json
{
  "info": {
    "name": "Spring Batch Customer Export",
    "_postman_id": "cfa1c4d2-33fe-4a9d-94c2-73e21dc1c07e",
    "description": "API collection to trigger batch job, list Excel files, and download exports.",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Health Check",
      "request": {
        "method": "GET",
        "url": {
          "raw": "http://localhost:8080/api/batch/health",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["api","batch","health"]
        }
      }
    },
    {
      "name": "Trigger Batch Job",
      "request": {
        "method": "POST",
        "url": {
          "raw": "http://localhost:8080/api/batch/trigger",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["api","batch","trigger"]
        }
      }
    },
    {
      "name": "List Excel Files",
      "request": {
        "method": "GET",
        "url": {
          "raw": "http://localhost:8080/api/batch/files",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["api","batch","files"]
        }
      }
    },
    {
      "name": "Download Today's File",
      "request": {
        "method": "GET",
        "url": {
          "raw": "http://localhost:8080/api/batch/download/today",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["api","batch","download","today"]
        }
      }
    },
    {
      "name": "Download Specific File",
      "request": {
        "method": "GET",
        "url": {
          "raw": "http://localhost:8080/api/batch/download/customers_20251128.xlsx",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["api","batch","download","customers_20251128.xlsx"]
        }
      }
    }
  ]
}
```
