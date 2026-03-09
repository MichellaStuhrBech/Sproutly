# Deployment guide (GitHub Actions)

This project is set up for CI/CD with GitHub Actions.

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

## Required GitHub setup

- **Secrets** (for deployment): add any API keys or DB URLs as repository **Secrets** and reference them in the workflow (e.g. `${{ secrets.OPENAI_API_KEY }}`). Do not commit them.
- **Branches:** workflows are configured for `main` and `master`; change in the YAML if you use another default branch.
