# 🏕️ CampingTN — Smart Camping Platform

> AI-powered camping platform for Tunisia: budget prediction, site explorer, gear shop, and admin panel — built with Spring Boot + MongoDB + Python ML.

![Java](https://img.shields.io/badge/Java-17-orange) ![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2-green) ![MongoDB](https://img.shields.io/badge/MongoDB-7.0-green) ![Python](https://img.shields.io/badge/Python-3.11-blue) ![Docker](https://img.shields.io/badge/Docker-Compose-blue)

---

## 📋 Features

| Module | Description |
|--------|-------------|
| 🗺️ **Explore** | Browse 22+ official Tunisian camping centers with filtering by region, site type, capacity |
| 🤖 **Budget AI** | ML-powered budget prediction with weather data, seasonal multipliers, equipment lists |
| 🛒 **Shop** | Buy or rent camping gear — 16+ products with cart, checkout, and order tracking |
| 📅 **Bookings** | Book camping spots with availability checking and confirmation |
| ⚙️ **Admin Panel** | Full dashboard: users, bookings, orders, maintenance tasks, analytics |
| 📊 **ML Notebooks** | 3 Kaggle notebooks: budget model, recommender, weather risk classifier |

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Frontend (HTML/CSS/JS)                │
│         Thymeleaf + Vanilla JS + REST API calls         │
└───────────────────────┬─────────────────────────────────┘
                        │ HTTP
┌───────────────────────▼─────────────────────────────────┐
│              Spring Boot 3.2 (Port 8080)                │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐  │
│  │   Auth   │ │ Camping  │ │   Shop   │ │  Admin   │  │
│  │Controller│ │Controller│ │Controller│ │Controller│  │
│  └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘  │
│       │            │            │             │         │
│  ┌────▼────────────▼────────────▼─────────────▼──────┐ │
│  │              Service Layer (Business Logic)        │ │
│  └────────────────────────┬───────────────────────────┘ │
│                           │                             │
│  ┌────────────────────────▼───────────────────────────┐ │
│  │          MongoDB (Spring Data Repositories)        │ │
│  └────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────┘
                        │ HTTP REST
┌───────────────────────▼─────────────────────────────────┐
│              Python Flask ML API (Port 5000)             │
│  /predict/budget  /predict/equipment  /weather-risk      │
│  Models: RandomForest + GBT Ensemble (scikit-learn)      │
└─────────────────────────────────────────────────────────┘
```

---

## 🚀 Quick Start

### Option A — Docker Compose (Recommended)

```bash
git clone https://github.com/yourname/camping-tunisia.git
cd camping-tunisia
docker compose up --build
```

| Service | URL |
|---------|-----|
| 🌐 Web App | http://localhost:8080 |
| 🤖 ML API | http://localhost:5000/health |
| 🍃 Mongo Express | http://localhost:8081 |

### Option B — Manual Setup

**Prerequisites:** Java 17, Maven 3.9+, MongoDB 7, Python 3.11+

#### 1. Start MongoDB
```bash
mongod --dbpath /data/db --port 27017
```

#### 2. Start ML API
```bash
cd ml-notebooks/ml-api
pip install -r requirements.txt

# Optional: run notebooks first to generate model .pkl files
# Then:
python app.py
# → running on http://localhost:5000
```

#### 3. Start Spring Boot
```bash
mvn clean spring-boot:run
# → running on http://localhost:8080
```

---

## 🔑 Default Credentials

| Role | Email | Password |
|------|-------|----------|
| Admin | admin@camping.tn | Admin@2024 |
| User | register via UI | your choice |

---

## 📂 Project Structure

```
camping-tunisia/
├── src/main/java/com/camping/
│   ├── config/
│   │   ├── MongoConfig.java          # DB config + data seeder
│   │   └── SecurityConfig.java       # JWT + Spring Security
│   ├── controller/
│   │   ├── AuthController.java       # POST /api/auth/login|register
│   │   ├── CampingController.java    # GET /api/camping-centers
│   │   ├── BudgetController.java     # POST /api/budget/predict
│   │   ├── ShopController.java       # GET/POST /api/products
│   │   ├── OrderController.java      # POST /api/orders
│   │   ├── BookingController.java    # POST /api/bookings
│   │   ├── AdminController.java      # /api/admin/** (ADMIN only)
│   │   └── WebController.java        # Serves frontend HTML
│   ├── model/
│   │   ├── User.java
│   │   ├── CampingCenter.java
│   │   ├── Product.java
│   │   ├── Order.java
│   │   ├── Booking.java
│   │   ├── Review.java
│   │   ├── BudgetPrediction.java
│   │   └── MaintenanceTask.java
│   ├── repository/                   # Spring Data MongoDB repos
│   ├── service/
│   │   ├── AuthService.java
│   │   ├── BudgetPredictionService.java
│   │   ├── CampingService.java
│   │   ├── ShopService.java
│   │   └── AdminService.java
│   └── security/
│       ├── JwtUtil.java
│       ├── JwtAuthFilter.java
│       └── CustomUserDetailsService.java
├── src/main/resources/
│   ├── templates/index.html          # Full SPA frontend
│   └── application.properties
├── ml-notebooks/
│   ├── 01_budget_prediction_model.ipynb   # Kaggle notebook 1
│   ├── 02_recommender_equipment.ipynb     # Kaggle notebook 2
│   ├── 03_weather_risk_model.ipynb        # Kaggle notebook 3
│   └── ml-api/
│       ├── app.py                    # Flask REST API
│       ├── requirements.txt
│       └── Dockerfile
├── scripts/
│   └── mongo-init.js                 # MongoDB init script
├── docker-compose.yml
├── Dockerfile
└── pom.xml
```

---

## 🤖 ML Models

### Notebook 1 — Budget Prediction
- **Algorithm:** Random Forest + Gradient Boosting Ensemble
- **Features:** 15 (governorate, site type, season, persons, days, distance, weather...)
- **Training data:** 5,000 synthetic samples based on official cost data
- **Accuracy:** R² > 0.95, MAE ≈ 45 TND

### Notebook 2 — Recommender + Equipment Classifier
- **Recommender:** KNN cosine similarity on 13 camping centers
- **Equipment:** Multi-output Random Forest (21 gear items, 12 conditions)
- **Hamming Loss:** < 0.05

### Notebook 3 — Weather Risk Model
- **Algorithm:** Random Forest classifier (4 risk levels)
- **Covers:** 13 Tunisian governorates × 4 seasons × 3 site types
- **Output:** LOW / MEDIUM / HIGH / EXTREME risk + safety advice

---

## 📡 API Reference

### Auth
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | Login → JWT token |

### Camping
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/camping-centers` | All active centers |
| GET | `/api/camping-centers/{id}` | Center details |
| GET | `/api/camping-centers/governorate/{gov}` | By governorate |
| GET | `/api/camping-centers/type/{type}` | By site type |
| GET | `/api/camping-centers/stats` | Regional stats |

### Budget
| Method | Endpoint | Body |
|--------|----------|------|
| POST | `/api/budget/predict` | `{governorate, siteType, numberOfPersons, numberOfDays, startDate, accommodationType}` |

### Shop
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/products` | All products |
| GET | `/api/products/category/{cat}` | By category |
| GET | `/api/products/recommended/{siteType}` | Gear for site type |
| POST | `/api/orders` | Create order (auth required) |

### Admin (ADMIN role required)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/admin/dashboard` | Full stats |
| GET | `/api/admin/users` | All users |
| GET | `/api/admin/maintenance` | Maintenance tasks |
| POST | `/api/admin/maintenance` | Create task |

---

## 🗄️ MongoDB Collections

| Collection | Description |
|------------|-------------|
| `users` | User accounts with roles |
| `camping_centers` | 13+ official Tunisian camping centers |
| `products` | 16+ camping gear items |
| `orders` | Purchase & rental orders |
| `bookings` | Camping spot reservations |
| `reviews` | User reviews per center |
| `budget_predictions` | Saved AI predictions per user |
| `maintenance_tasks` | Admin maintenance tracking |

---

## 📊 Data Sources

- **Repartition régionale des centres de camping (2017)** — Official Ministry of Tourism data
- **Évolution des centres de camping 2002–2017** — Historical survey data
- Synthetic cost data calibrated to Tunisian market prices (TND)

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|------------|
| Backend | Spring Boot 3.2, Spring Security, Spring Data MongoDB |
| Auth | JWT (io.jsonwebtoken 0.11.5) |
| Database | MongoDB 7.0 |
| Frontend | HTML5, CSS3, Vanilla JavaScript (SPA) |
| ML | Python 3.11, scikit-learn, pandas, numpy |
| ML API | Flask 3.0, Gunicorn |
| DevOps | Docker, Docker Compose |

---

## 📝 License

MIT License — © 2024 CampingTN

---

*Built with ❤️ for Tunisian camping enthusiasts*
