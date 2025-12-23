# VS Code Tempo Tracker - Project Structure

## Directory Structure

```
VSCode/tempo-tracker/
├── .vscode/                    # VS Code workspace configuration
│   ├── launch.json            # Debug configuration
│   ├── settings.json          # Editor settings
│   └── tasks.json             # Build tasks
├── src/                       # Source code
│   └── extension.ts           # Main extension entry point
├── out/                       # Compiled JavaScript (generated)
│   ├── extension.js
│   └── extension.js.map
├── node_modules/              # Dependencies (generated, gitignored)
├── .eslintrc.json            # ESLint configuration
├── .gitignore                # Git ignore patterns
├── .vscodeignore             # VS Code extension packaging ignore
├── package.json              # Extension manifest and dependencies
├── package-lock.json         # Locked dependency versions
├── README.md                 # Project documentation
└── tsconfig.json             # TypeScript configuration
```

## Key Files

### Configuration Files

- **package.json**: Extension manifest with metadata, commands, activation events, and dependencies
- **tsconfig.json**: TypeScript compiler settings
- **.eslintrc.json**: Code quality and style rules
- **.gitignore**: Excludes build artifacts and dependencies from version control
- **.vscodeignore**: Excludes files from the extension package

### Source Code

- **src/extension.ts**: Main extension implementation with:
  - `activate()`: Extension activation entry point
  - `initializeHeartbeat()`: Sets up activity tracking listeners
  - `updateGitInfo()`: Extracts Jira ticket ID from Git branch
  - `updateStatusBar()`: Updates the status bar display
  - `deactivate()`: Cleanup when extension deactivates

### Development Configuration

- **.vscode/launch.json**: Debugging configurations for running and testing the extension
- **.vscode/settings.json**: Workspace-specific editor settings
- **.vscode/tasks.json**: Build task definitions

## Build Output

The TypeScript compiler generates:
- **out/extension.js**: Compiled JavaScript
- **out/extension.js.map**: Source maps for debugging

## Development Workflow

1. **Install dependencies**: `npm install`
2. **Compile**: `npm run compile`
3. **Watch mode**: `npm run watch`
4. **Lint**: `npm run lint`
5. **Debug**: Press F5 in VS Code to launch Extension Development Host

## Features Implemented

- ✅ Activity heartbeat tracking (typing, saving, file switching)
- ✅ Idle detection (5-minute threshold)
- ✅ Git branch integration for Jira ticket extraction
- ✅ Status bar UI element with ticket display
- ✅ Review and log command placeholder
- ✅ TypeScript compilation
- ✅ ESLint integration
- ✅ VS Code debugging configuration

## Next Steps (Future Enhancements)

- [ ] Implement local storage for time tracking data
- [ ] Create comprehensive review and approve UI
- [ ] Add Tempo API integration
- [ ] Implement secure token storage
- [ ] Add configuration options
- [ ] Create comprehensive tests
- [ ] Add CI/CD pipeline
