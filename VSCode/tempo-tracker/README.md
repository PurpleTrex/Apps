# Project: VS Code Tempo Time Tracker Assistant

## 1.1. Objective

To create a VS Code extension that automates the tracking of developer activity and provides a "human-in-the-loop" workflow for submitting accurate, pre-filled time logs to Tempo in Jira. This tool is designed to enhance, not replace, the existing Tempo ecosystem by filling the crucial gap of quantifying active coding time.

## 1.2. Core Principles

- **Empower, Don't Micromanage:** The developer has the final say. Time is never logged automatically without explicit, easy approval.
- **Minimize Friction:** The process from coding to logging time should require the fewest possible clicks and context switches.
- **Accuracy by Design:** Combine automated tracking with a simple verification step to ensure logs are both effortless and accurate.
- **Transparency:** The developer should always know what is being tracked and why.

## 1.3. High-Level Architecture & Features

This extension is built on the "Automate and Verify" model.

### 1.3.1. The Automation Engine (Background Tracking)

This is the core data-gathering component that runs silently in the background.

- **Activity Heartbeat:**
  - **Triggers:** Listens for `workspace.onDidChangeTextDocument` (typing), `workspace.onDidSaveTextDocument` (saving), `window.onDidChangeActiveTextEditor` (file switching), and `window.onDidWriteTerminalData` (terminal use).
  - **Logic:** Each trigger sends a "heartbeat" signal, confirming active work.

- **Idle Detection:**
  - **Logic:** An idle timer (e.g., 3-5 minutes) starts after the last heartbeat. If it expires, time tracking is paused. It resumes on the next heartbeat. This prevents tracking time when the developer is away.

- **Task Association:**
  - **Logic:** On startup and on branch change, the extension reads the current Git branch name. It uses a configurable regex (e.g., `/[A-Z]+-\d+/`) to extract the Jira ticket ID (e.g., `PROJ-123`).
  - **Data:** All tracked time is stored locally and bucketed against the detected ticket ID.

### 1.3.2. The Verification Workflow (The Human-in-the-Loop)

This is the user-facing part that ensures control and accuracy.

- **Local-First Storage:**
  - **Implementation:** All tracked time segments are stored in VS Code's `workspaceState`. This data is private to the user and their workspace.

- **The "Review and Approve" UI:**
  - **Trigger:** Can be activated on command, via a status bar click, or with an end-of-day reminder.
  - **Interface:** A VS Code Quick Pick or Webview will display a summary of the tracked activity.
  - **Example:**
    > **Ready to log time to Tempo for ticket PROJ-123?**
    >
    > **Time Tracked:** `[ 4.2 ]` hours
    > **Work Description:** `[ Automated log: Coding and debugging. Commits: 3. ]`
    >
    > `[Submit to Tempo]` `[Discard]`
  - **Functionality:** The user **can edit** the time and description before submission.

### 1.3.3. Tempo API Integration

- **Authentication:** The extension will prompt the user for their Jira domain and a Tempo API Token on first use. This token will be stored securely in VS Code's `SecretStorage`.
- **API Call:** On submission, the extension makes a `POST` request to the Tempo `worklogs` REST API endpoint, sending the developer-verified data.

### 1.3.4. UI and Transparency

- **Status Bar Element:**
  - **Display:** A subtle item in the status bar shows the currently tracked ticket and status (e.g., `🕒 PROJ-123 (Active)` or `⏸️ PROJ-123 (Idle)`).
  - **Interactivity:** Clicking the status bar item provides a menu with quick actions:
    - `Pause/Resume Tracking`
    - `Switch Tracked Ticket...`
    - `Review and Log Today's Time`
    - `Log Manual Time (Meeting, etc.)`

## 1.4. Proof-of-Concept Scope

The initial PoC code below implements the foundational pieces:
1.  A basic VS Code extension structure.
2.  Basic listeners for typing and saving to log activity to the console.
3.  A function to read and display the current Git branch name.
4.  A placeholder status bar item.
5.  A command to trigger a placeholder "Review and Log" workflow.

## Installation

1. Clone this repository
2. Navigate to the extension directory:
   ```bash
   cd VSCode/tempo-tracker
   ```
3. Install dependencies:
   ```bash
   npm install
   ```
4. Compile the TypeScript:
   ```bash
   npm run compile
   ```
5. Press F5 in VS Code to launch the Extension Development Host

## Usage

Once the extension is activated:
- The status bar will show the current ticket being tracked (extracted from your Git branch)
- Activity is tracked automatically while you code
- Click the status bar item or run the "Tempo Tracker: Review and Log Time" command to review tracked time

## Development

- `npm run compile` - Compile TypeScript to JavaScript
- `npm run watch` - Watch for changes and compile automatically
- `npm run lint` - Run ESLint on the source code

## License

MIT
