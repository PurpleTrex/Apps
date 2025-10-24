# PurpleTalk (Next.js)

A modern, secure web app for PurpleTalk using Next.js 16, Tailwind CSS, shadcn/ui, and Matrix.

## Getting Started

1) Copy `.env.local.example` to `.env.local` and fill values.

2) Install deps:

```bash
npm install
```

3) Run the development server:

```bash
npm run dev
```

Open http://localhost:3000

## Environment

- MATRIX_SERVER, MATRIX_DOMAIN: Your Matrix homeserver
- PAYPAL_*: PayPal REST credentials
- CRYPTO_*: Donation addresses for QR codes
- SESSION_*: Cookie name and max age
- JWT_SECRET: Secret to sign the session JWT cookie
- RESEND_API_KEY: API key for Resend (transactional email)
- SYNAPSE_ADMIN_TOKEN: Token for Synapse Admin API (password resets)
- INACTIVITY_TIMEOUT_MINUTES or NEXT_PUBLIC_INACTIVITY_TIMEOUT_MINUTES: Minutes of inactivity before auto-logout (client)

## Data storage

- SQLite DB at `data/purpletalk.db` for 2FA, donations log, email signups

## Features implemented

- Register and login via Matrix with phone->hashed Matrix ID
- Optional 2FA (TOTP) with SQLite storage
- 2FA end-to-end: server-issued TOTP secret + QR, verify and enable/disable via UI (`/api/twofactor`)
- Change password UI on Account (calls Synapse password change)
- Inactivity auto-logout (configurable via env)
- Secure httpOnly cookie session signed as JWT (HMAC via `jose`)
- Dashboard, Account settings (update display name, delete account)
- Basic Chat (rooms list, send/receive text)
- Donate page with PayPal order/capture endpoints + Crypto QR
- Email signup API route
- Password recovery with email (Resend) and Synapse Admin API

### API routes

- Auth: `/api/auth` (ping, register, login, logout, 2FA save/verify)
- Matrix proxy: `/api/matrix` (profile, change password, delete account, rooms, send)
- Donations: `/api/donate` (create/capture PayPal, log crypto)
- Email signup: `/api/email`
- QR: `/api/qr`
- Session: `/api/session`
- Recovery: `/api/recovery/{initiate,confirm,status}`
- Password reset: `/api/password/{initiate,complete}`
- Two-factor: `/api/twofactor`

## Roadmap / Remaining features

These are the next items to reach full parity with the requested feature set.

1) End-to-end encryption (E2EE)
- Initialize `matrix-js-sdk` crypto, key backup, device verification/cross-signing
- Secure client-side key storage (IndexedDB) and recovery

2) Chat enrichments
- Typing indicators, read receipts, presence UI
- Message reactions, replies/threads, edits, deletes, forwards
- Message search, per-room mute, pinned messages, bookmarks, drafts

3) Media and files
- Image/video/file uploads to Matrix media repo, drag & drop, progress UI
- Thumbnails/previews, lightbox, downloads

4) Calls (voice/video)
- 1:1 calling UI with TURN integration (env: `TURN_*`)
- Group calls (follow-up)

5) Settings & privacy
- Toggles for read receipts, typing, last seen, avatar visibility
- Theme toggle control in UI; notification preferences
- Avatar upload; devices/sessions list and revoke

6) PWA & notifications
- Manifest and Service Worker for installability
- Web push notifications for new messages

7) Donations polish
- PayPal Smart Buttons integration and capture flow
- PayPal webhooks for server-side verification
- Email receipts and admin dashboard with charts/exports

8) Downloads
- Add Android APK to `public/downloads/` and a Downloads page

9) Analytics & observability
- Basic analytics (privacy-respecting) and server metrics/logging

10) Lint/type cleanup & DX
- Remove remaining `any`, fix hook warnings, enable clean lint CI

11) Security hardening
- CSP/HSTS/security headers, rate limiting on APIs
- Session rotation/invalidations, cookie `Secure`/`SameSite` review
