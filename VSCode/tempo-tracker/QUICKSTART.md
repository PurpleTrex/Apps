# Quick Start Guide - VS Code Tempo Tracker

## Prerequisites

- Visual Studio Code (v1.75.0 or higher)
- Node.js (v16.x or higher)
- npm (comes with Node.js)
- Git repository with branches that contain Jira ticket IDs

## Installation & Setup

### 1. Navigate to the Extension Directory

```bash
cd VSCode/tempo-tracker
```

### 2. Install Dependencies

```bash
npm install
```

### 3. Compile the TypeScript

```bash
npm run compile
```

## Development

### Running the Extension

1. Open the `VSCode/tempo-tracker` folder in VS Code
2. Press `F5` to launch the Extension Development Host
3. A new VS Code window will open with the extension loaded

### Making Changes

1. Edit the source code in `src/extension.ts`
2. Run `npm run compile` to rebuild
3. Reload the Extension Development Host window (`Ctrl+R` or `Cmd+R`)

### Watch Mode (Recommended)

Run the compiler in watch mode to automatically recompile on changes:

```bash
npm run watch
```

## Testing the Extension

### In a Git Repository with Jira Ticket Branch

1. Open a Git repository in the Extension Development Host
2. Switch to a branch that contains a Jira ticket ID (e.g., `feature/PROJ-123-new-feature`)
3. Look at the status bar in the bottom left corner
4. You should see something like: `🕒 PROJ-123`

### Testing Activity Tracking

1. Start typing in a file - this generates a "heartbeat"
2. Save a file - this also generates a "heartbeat"
3. Switch between files - activity is tracked
4. Wait 5 minutes without activity - the status bar icon changes to `⏸️` (paused)

### Testing the Review Command

1. Click on the status bar item (e.g., `🕒 PROJ-123`)
2. Or run the command: `Ctrl+Shift+P` → "Tempo Tracker: Review and Log Time"
3. You should see an information message showing the current ticket

## Status Bar Icons

- `🕒 PROJ-123` - Actively tracking time for ticket PROJ-123
- `⏸️ PROJ-123` - Tracking paused (idle) for ticket PROJ-123
- `🛑 No Ticket` - No Jira ticket ID found in current branch

## Extension Features

### Automatic Tracking

The extension automatically tracks your activity when you:
- Type in a file (`workspace.onDidChangeTextDocument`)
- Save a file (`workspace.onDidSaveTextDocument`)
- Switch between files (`window.onDidChangeActiveTextEditor`)

### Idle Detection

- After 5 minutes of inactivity, tracking is automatically paused
- Resumes automatically when you start working again

### Jira Ticket Detection

- Extracts ticket IDs from branch names using regex: `/[A-Z]+-\d+/`
- Examples of valid branch names:
  - `feature/PROJ-123-new-feature` → PROJ-123
  - `bugfix/APP-456` → APP-456
  - `TASK-789-update-docs` → TASK-789

## Troubleshooting

### Extension Doesn't Load

- Make sure you compiled the TypeScript: `npm run compile`
- Check the Output panel in VS Code for errors
- Verify the `out/extension.js` file exists

### No Ticket Showing

- Ensure you're in a Git repository
- Check that your branch name contains a Jira ticket ID format (e.g., `PROJ-123`)
- The ticket ID must be uppercase letters followed by a hyphen and numbers

### Can't See Status Bar Item

- Look in the bottom left corner of VS Code
- The status bar item might be hidden if the window is too narrow

## Code Structure

```
src/extension.ts
├── activate()              # Extension entry point
├── initializeHeartbeat()   # Sets up activity listeners
├── updateGitInfo()         # Extracts Jira ticket from branch
├── updateStatusBar()       # Updates UI
└── deactivate()           # Cleanup
```

## npm Scripts

- `npm run compile` - Compile TypeScript to JavaScript
- `npm run watch` - Watch mode (auto-compile on changes)
- `npm run lint` - Run ESLint on source code
- `npm run pretest` - Prepare for testing (compile + lint)

## Next Steps

Once you're familiar with the extension:

1. Explore the code in `src/extension.ts`
2. Add new features (e.g., time logging to Tempo API)
3. Improve the UI (e.g., better review dialog)
4. Add configuration options
5. Write tests

## Resources

- [VS Code Extension API](https://code.visualstudio.com/api)
- [TypeScript Documentation](https://www.typescriptlang.org/docs/)
- [simple-git Library](https://github.com/steveukx/git-js)
- [Tempo API Documentation](https://tempo-io.github.io/tempo-api-docs/)

## Support

For issues or questions:
1. Check the console output in the Extension Development Host
2. Review the implementation in `src/extension.ts`
3. Consult the main `README.md` for architecture details
