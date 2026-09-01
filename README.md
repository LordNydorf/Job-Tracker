# 🚀 Job Application Tracker

A modern, production-grade **Kotlin Multiplatform (KMP)** application designed to track, manage, and follow up on job applications with zero friction. Built with **Jetpack Compose (Material 3)** on Android, a high-performance **Ktor Server** backend with **Exposed ORM** & **SQLite**, and background reminder workers via **AndroidX WorkManager**.

---

## 📱 Features

- **Multi-Step Application Wizard**: Fast, structured 3-step logging process (`Role & Company` → `Source & Stage` → `Timeline & Nudges`) with a persistent sticky action bar and inline date picker.
- **1-Tap Pipeline Progression**: Seamless status transitions (`Applied` ➔ `Screening` ➔ `Interview` ➔ `Offer` ➔ `Rejected` / `Ghosted`) with instant optimistic updates and server synchronization.
- **Activity & Interview Log**: Real-time note recording for interview questions, feedback, and take-home tasks with 1-tap quick suggestions and edge-to-edge chat-style composer.
- **Automated Follow-Up Nudges**: Background `WorkManager` worker that scans for stagnant applications and delivers notification alerts when follow-ups are due.
- **Rich Material 3 Aesthetics**: Curated dark & light modes, dynamic company avatar gradients, adaptive launcher icons, and native Android 12+ Splash Screen.
- **Dynamic Server Switching**: In-app backend switcher with quick presets for Wi-Fi IP, USB reverse tethering, Android Emulator, and Cloud Production.

---

## 🏗️ Architecture & Tech Stack

```mermaid
graph TD
    subgraph "Android Client (:androidApp)"
        UI[Jetpack Compose Material 3]
        VM[Lifecycle ViewModels]
        DI[Koin Dependency Injection]
        WM[WorkManager Reminder Worker]
        Client[Ktor HTTP Client + OkHttp]
    end

    subgraph "Shared Module (:shared)"
        Models[Data Models: Application, Note, Status, Source]
        Serialization[KotlinX Serialization & DateTime]
        ApiInterface[JobTrackerApi Interface]
    end

    subgraph "Backend Service (:backend)"
        Server[Ktor 3.0 Server + Netty]
        Auth[API Key Authentication Plugin]
        ORM[Exposed SQL ORM]
        DB[(SQLite Database)]
    end

    UI --> VM
    VM --> Client
    Client --> ApiInterface
    Client --> Models
    WM --> Client
    Server --> Models
    Server --> ORM
    ORM --> DB
```

### Technology Highlights
| Layer | Technologies |
|---|---|
| **Client** | Jetpack Compose, Material 3, Navigation Compose, Koin, AndroidX WorkManager, Core Splash Screen |
| **Shared** | Kotlin Multiplatform (KMP), `kotlinx.serialization`, `kotlinx-datetime`, `kotlinx.coroutines` |
| **Backend** | Ktor 3.0 Server, Netty Engine, Exposed SQL ORM, SQLite JDBC, Logback |
| **Deployment** | Docker (Multi-stage JRE 21 Alpine), Railway, Render |

---

## 📡 REST API Reference

All protected endpoints require the `X-API-Key` header with your configured API key.

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/health` | Server health check and status |
| `GET` | `/applications` | List all job applications (sorted by last updated) |
| `POST` | `/applications` | Create a new job application |
| `GET` | `/applications/{id}` | Get application details by ID |
| `PATCH` | `/applications/{id}` | Update application fields (role, company, status, etc.) |
| `DELETE` | `/applications/{id}` | Delete application and cascade its notes |
| `GET` | `/applications/{id}/notes` | Retrieve all notes/timeline events for an application |
| `POST` | `/applications/{id}/notes` | Add a new note or interview log entry |

---

## 🐳 Running with Docker

You can build and run the backend container locally with persistent SQLite storage:

```bash
# 1. Build Docker image
docker build -t job-tracker-backend .

# 2. Run container with persistent volume and your custom API key
docker run -d \
  -p 8080:8080 \
  -e PORT=8080 \
  -e API_KEY=your-secure-secret-key \
  -v jobtracker-data:/data \
  --name job-tracker \
  job-tracker-backend

# 3. Test health endpoint
curl http://localhost:8080/health
```

---

## ☁️ 1-Click Cloud Deployment

### Deploy to Railway
1. Fork or push this repository to GitHub.
2. Link the repository in [Railway](https://railway.app).
3. Railway automatically detects the `Dockerfile` and `railway.json`.
4. Attach a persistent volume mounted at `/data`.

### Deploy to Render
1. Create a **Web Service** from your GitHub repo in [Render](https://render.com).
2. Choose **Docker** runtime.
3. Attach a Persistent Disk mounted at `/data` with 1 GB size.

---

## 💻 Local Development Setup

### Prerequisites
- JDK 21
- Android Studio Ladybug / Meerkat or Android SDK 35
- Gradle 8.11+ (handled by `./gradlew`)

### 1. Start the Ktor Backend
```bash
./gradlew :backend:run
```
The server will start listening on `http://127.0.0.1:8080`.

### 2. Connect Your Phone via USB
```bash
# Reverse port forwarding for seamless USB testing
adb reverse tcp:8080 tcp:8080
```

### 3. Build & Install Android App
```bash
./gradlew :androidApp:installDebug
```

---

## ☁️ Free Cloud Deployment (Render + Neon PostgreSQL)

The backend is configured with **Dual-Engine Persistence** (Cloud PostgreSQL + SQLite) and the Android app features **0ms Offline-First Caching**:

1. **Create Free Database (100% Free Forever)**:
   - Create a free project at [neon.tech](https://neon.tech) (0.5 GiB serverless Postgres, never deleted).
   - Copy your PostgreSQL connection string (`postgresql://user:pass@ep-xyz.neon.tech/neondb?sslmode=require`).
2. **Deploy on Render**:
   - Create a **Web Service** on [render.com](https://render.com) using this repo.
   - In **Environment Variables**, set:
     - `DATABASE_URL` = `postgresql://...` (your Neon connection string)
     - `API_KEY` = `your-secret-api-key`
3. **Configure Android App**:
   - In the Android app, tap the **Cloud (Server Config)** icon in the top-right corner.
   - Enter your Render server URL (`https://your-service.onrender.com`) and your API Key.
   - Even during cold starts on Render's free tier, the Android app will load instantly (0ms latency) from on-device local storage!

---

## 🧪 Testing

```bash
# Run shared module unit tests
./gradlew :shared:allTests

# Run backend route and integration tests
./gradlew :backend:test

# Run Android unit tests
./gradlew :androidApp:testDebugUnitTest
```

---

## 📄 License
This project is open-source and available under the [MIT License](LICENSE).
