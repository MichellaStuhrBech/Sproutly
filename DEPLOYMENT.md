# Deployment guide (GitHub Actions)

This project is set up for CI/CD with GitHub Actions.

---

## DigitalOcean droplet (Nginx + Docker) — current production

The app is deployed on a **DigitalOcean droplet** with this architecture. Keep it in mind for any code or infrastructure changes.

### Architecture

| Component | Where it runs | Notes |
|-----------|----------------|--------|
| **Nginx** | Host (Linux) | Reverse proxy + static files. Ports 80, 443. HTTPS via Certbot. |
| **Frontend** | Nginx | Static files from `/var/www/sproutly`. **Not** in Docker. |
| **Backend** | Docker (`app` container) | Java JAR, port **7070**. |
| **PostgreSQL** | Docker (`db` container) | Persistence. |

- **Domain:** michellastuhrbech.dk  
- **Frontend:** Built to `frontend/dist/`, copied to `/var/www/sproutly`.  
- **API:** Nginx proxies `https://michellastuhrbech.dk/api` → `http://127.0.0.1:7070/api/`.  
- **docker-compose:** `/root/backend/docker-compose.yml` (on the server). Services: `app`, `db`.

### Constraints

- Ports 80 and 443 are used by Nginx; do not bind the app to them.  
- The frontend is **not** served by Docker; Nginx serves it from `/var/www/sproutly`.  
- The frontend uses relative `/api`; no `VITE_API_URL` is needed because Nginx proxies on the same origin.

### Backend env (Docker)

In production, set these on the server (e.g. in `/root/backend/.env`, not committed):

| Variable | Required | Description |
|----------|----------|-------------|
| `SECRET_KEY` | **Yes** | JWT signing secret (min 32 chars). Without it, login and create-account fail. |
| `ISSUER` | No | JWT issuer (default: Sproutly). |
| `TOKEN_EXPIRE_TIME` | No | Expiry in ms (default: 86400000). |
| `OPENAI_API_KEY` | No | Plant chat. |
| `PERENUAL_API_KEY` | No | Plant search. |

Example `/root/backend/.env` on the server:

```bash
SECRET_KEY=your-long-random-secret-at-least-32-characters
# ISSUER=Sproutly
# TOKEN_EXPIRE_TIME=86400000
```

Then run: `docker compose --env-file .env up -d --build`.

### CI/CD (GitHub Actions + SSH)

On push to `main`, the workflow (or a server-side script) typically:

1. SSH to the droplet  
2. `cd /root/backend`  
3. `git pull origin main`  
4. `docker compose down`  
5. `docker compose up -d --build --force-recreate`  
6. Build frontend (e.g. `cd frontend && npm ci && npm run build`)  
7. Copy `frontend/dist/*` to `/var/www/sproutly`  
8. `nginx -s reload` (or `systemctl reload nginx`)

The repo’s `deploy.yml` builds the JAR and frontend and uploads artifacts; the actual deploy to the droplet can be a separate job (e.g. SSH + run the steps above) or a script on the server triggered by webhook/pull.

### Nginx (reference)

- Static root: `root /var/www/sproutly;`  
- Proxy: `location /api { proxy_pass http://127.0.0.1:7070/api/; ... }`  
- SPA fallback: `try_files $uri $uri/ /index.html;` for client-side routing.

---

## Workflows

### CI (`ci.yml`)

Runs on every **push** and **pull request** to `main` or `master`:

- **Backend:** `mvn clean verify` (compile + run tests with Testcontainers)
- **Frontend:** install deps, lint (if script exists), `npm run build`

Ensure your default branch is `main` or `master`, or adjust the `on.push.branches` in `.github/workflows/ci.yml`.

### Build for deployment (`deploy.yml`)

Runs on **push to main/master** and on **manual trigger** (Actions → Build for deployment → Run workflow):

- Builds backend **fat JAR** (`backend/target/sproutly-backend.jar`)
- Builds frontend (`frontend/dist/`)
- **Uploads artifacts** so you can download them from the Actions run or use them in a later deploy step

To add a real deployment (e.g. Railway, Render, Fly.io):

1. Add a job that `needs: build` and downloads the artifacts with `actions/download-artifact@v4`.
2. Add the deployment steps for your platform (e.g. Railway CLI, Docker push, or FTP).

## Running the backend in production

1. Build the JAR locally or download the `backend-jar` artifact from a successful “Build for deployment” run.
2. Set **environment variables** (no `config.properties` in production when `DEPLOYED` is set):

   | Variable           | Description                          |
   |--------------------|--------------------------------------|
   | `DEPLOYED`         | Set to any value (e.g. `true`)       |
   | `DB_NAME`          | PostgreSQL database name             |
   | `CONNECTION_STR`   | JDBC prefix, e.g. `jdbc:postgresql://host:5432/` |
   | `DB_USERNAME`      | Database user                        |
   | `DB_PASSWORD`      | Database password                    |
   | `SECRET_KEY`       | JWT signing secret (min 32 chars)   |
   | `ISSUER`           | JWT issuer (e.g. `Sproutly`)        |
   | `TOKEN_EXPIRE_TIME`| JWT expiry in ms (e.g. `86400000`)  |
   | `OPENAI_API_KEY`    | For plant chat                       |
   | `PERENUAL_API_KEY` | For plant data (optional)           |
   | `TREFLE_TOKEN`     | For plant data (optional)           |

3. Run the JAR:

   ```bash
   java -jar sproutly-backend.jar
   ```

   The server listens on **port 7070** (see `dat.Main`). Use a reverse proxy (e.g. Nginx, Caddy) or your host’s port mapping to expose it and/or serve the frontend.

## Serving the frontend in production

- **Option A – Same host as API:**  
  Build the frontend (`npm run build` in `frontend/`). Serve the `frontend/dist/` directory with your web server and proxy `/api` to the backend (e.g. `http://localhost:7070`). The app uses relative `/api` requests, so no extra env is needed.

- **Option B – Static host (e.g. Vercel, Netlify):**  
  Deploy the contents of `frontend/dist/`. Set the backend API URL via your host’s env (e.g. Vite’s `VITE_API_URL`) and ensure the frontend uses it for API calls instead of `/api` if the backend is on another origin.

## Digital Ocean App Platform

To deploy the **backend** (JAR) on Digital Ocean:

### Build the JAR

From the project root:

```bash
cd backend
mvn clean package -DskipTests
```

Or run **`backend/build-jar.ps1`** (Windows) or **`backend/build-jar.sh`** (Linux/Mac). The runnable JAR is **`backend/target/sproutly-backend.jar`**.

### App Platform

1. Connect your GitHub repo.
2. **Build command:** `cd backend && mvn clean package -DskipTests`
3. **Run command:** `java -jar backend/target/sproutly-backend.jar`
4. **HTTP port:** **7070**
5. **Environment variables:** Add all production vars (see table above): `DEPLOYED`, `DB_NAME`, `CONNECTION_STR`, `DB_USERNAME`, `DB_PASSWORD`, `SECRET_KEY`, `ISSUER`, `TOKEN_EXPIRE_TIME`, and optionally `OPENAI_API_KEY`, `PERENUAL_API_KEY`, `TREFLE_TOKEN`.
6. Add a PostgreSQL database and use its connection details for the DB env vars.

### Droplet (manual)

1. Copy `sproutly-backend.jar` to the Droplet. Install Java 17: `sudo apt install openjdk-17-jre-headless -y`
2. Set env vars (e.g. in a `.env` or start script). Run: `java -jar sproutly-backend.jar`
3. Expose port 7070 (firewall or reverse proxy).

---

## Required GitHub setup

- **Secrets** (for deployment): add any API keys or DB URLs as repository **Secrets** and reference them in the workflow (e.g. `${{ secrets.OPENAI_API_KEY }}`). Do not commit them.
- **Branches:** workflows are configured for `main` and `master`; change in the YAML if you use another default branch.
