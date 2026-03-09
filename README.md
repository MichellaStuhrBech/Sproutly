# Sproutly

A full-stack application with Java backend and React + Vite frontend.

A garden planning app that helps users design and organize their garden beds, plan sowing schedules, and gain insights — with potential business value for garden centers like Plantorama. 🌿

## Project Structure

```
Sproutly/
├── backend/          # Java backend (Maven)
└── frontend/         # React + Vite frontend
```

## Backend Setup

The backend is a Java project using Maven.

### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher

### Running the Backend

```bash
cd backend
mvn clean install
mvn exec:java -Dexec.mainClass="dat.Main"
```

Or run the packaged JAR: `java -jar backend/target/sproutly-backend.jar` (server listens on port **7070**).

## Frontend Setup

The frontend is a React application using Vite.

### Prerequisites
- Node.js 18 or higher
- npm or yarn

### Running the Frontend

```bash
cd frontend
npm install
npm run dev
```

The frontend will be available at `http://localhost:3000`

## Development

- Backend runs on port **7070**
- Frontend runs on port **3000** with proxy to backend API at `/api`

## CI/CD and deployment

- **GitHub Actions:** See [DEPLOYMENT.md](DEPLOYMENT.md) for CI workflow, build artifacts, and production env vars.
