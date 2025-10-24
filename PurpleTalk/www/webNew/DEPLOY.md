# PurpleTalk Web Deployment (Same-Origin Matrix Proxy)

This project assumes the web app and Matrix homeserver share the same public domain (e.g., `https://purpletalk.devit.dev`).

## 1) Environment

Create `web/.env.local` with:

```
MATRIX_SERVER=https://purpletalk.devit.dev
MATRIX_DOMAIN=purpletalk.devit.dev
NEXT_PUBLIC_MATRIX_BASE=https://purpletalk.devit.dev
SESSION_COOKIE_NAME=purpletalk_session
SESSION_MAX_AGE=2592000
```

Fill remaining keys for PayPal/SMTP/crypto/TURN as needed.

## 2) Build and run

```
cd web
npm install
npm run build
npm start # serves on 0.0.0.0:3000 by default
```

Run as a systemd service or behind a process manager (pm2) in production.

## 3) Nginx (recommended)

Proxy all application traffic to the Next.js server and forward `/_matrix` to Synapse. Replace upstreams to match your setup.

```
upstream purpletalk_web {
  server 127.0.0.1:3000;
}

upstream purpletalk_synapse_http {
  # If Synapse listens on 8008 (HTTP):
  server 127.0.0.1:8008;
}

server {
  listen 80;
  listen 443 ssl http2;
  server_name purpletalk.devit.dev;

  # SSL config here (...)

  # App (Next.js)
  location / {
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
    proxy_pass http://purpletalk_web;
  }

  # Static asset hints (optional but helpful)
  location /_next/ {
    proxy_pass http://purpletalk_web;
  }

  # Matrix proxy (same-origin)
  location /_matrix/ {
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;

    # If Synapse is HTTP on 8008
    proxy_pass http://purpletalk_synapse_http/_matrix/;

    # If Synapse is HTTPS on 8448 instead, use:
    # proxy_pass https://127.0.0.1:8448/_matrix/;

    # Handle CORS preflight gracefully if ever reached here (usually not needed when same-origin)
    if ($request_method = OPTIONS) { return 204; }
  }
}
```

After reloading Nginx, the browser and the matrix-js-sdk will call `https://purpletalk.devit.dev/_matrix/...` which Nginx forwards to Synapse. No browser CORS required.

## 4) Smoke tests

- POST `/api/auth` with `{ "action": "ping" }` should return versions from Synapse.
- Register/Login via `/register` and `/login` should create a session.
- `/chat` should load joined rooms and send text messages.

## Notes

- Keep `.env.local` out of version control.
- SQLite database is created under `web/data/purpletalk.db` with WAL mode. Ensure the process user can write there.
- For voice/video calls, configure TURN and add credentials to `.env.local`, then we’ll enable the call UI.
