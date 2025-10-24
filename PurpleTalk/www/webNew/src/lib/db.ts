import Database from 'better-sqlite3'
import path from 'path'
import fs from 'fs'

const DB_PATH = path.join(process.cwd(), 'data', 'purpletalk.db')

// Ensure data directory exists
const dataDir = path.dirname(DB_PATH)
if (!fs.existsSync(dataDir)) fs.mkdirSync(dataDir, { recursive: true })

const db = new Database(DB_PATH)

db.pragma('journal_mode = WAL')

db.exec(`
CREATE TABLE IF NOT EXISTS user_mappings (
  phone_number TEXT PRIMARY KEY,
  matrix_username TEXT NOT NULL,
  matrix_id TEXT NOT NULL,
  last_login TEXT
);

CREATE TABLE IF NOT EXISTS two_factor (
  user_id TEXT PRIMARY KEY,
  secret TEXT NOT NULL,
  enabled INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS donations (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  created_at TEXT DEFAULT (datetime('now')),
  type TEXT NOT NULL,
  amount REAL,
  currency TEXT,
  transaction_id TEXT,
  payer_email TEXT,
  meta TEXT
);

CREATE TABLE IF NOT EXISTS email_signups (
  email TEXT PRIMARY KEY,
  created_at TEXT DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS recovery_emails (
  user_id TEXT PRIMARY KEY,
  email TEXT NOT NULL,
  verified INTEGER NOT NULL DEFAULT 0,
  verification_token_hash TEXT,
  verification_expires_at TEXT
);

CREATE TABLE IF NOT EXISTS password_resets (
  user_id TEXT NOT NULL,
  token_hash TEXT NOT NULL,
  created_at TEXT DEFAULT (datetime('now')),
  expires_at TEXT NOT NULL,
  consumed_at TEXT,
  ip TEXT,
  ua TEXT
);
`)

export default db
