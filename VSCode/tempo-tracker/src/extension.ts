import * as vscode from 'vscode';
import simpleGit, { SimpleGit } from 'simple-git';

// --- Globals ---
let statusBarItem: vscode.StatusBarItem;
let activeTicketId: string | null = null;
let lastHeartbeat: number = Date.now();
let isTracking = true;

const IDLE_THRESHOLD_MS = 5 * 60 * 1000; // 5 minutes

// --- Core Functions ---

/**
 * This is the main entry point for the extension.
 */
export function activate(context: vscode.ExtensionContext) {
    console.log('Tempo Tracker is now active.');

    // 1. Create and configure the status bar item
    statusBarItem = vscode.window.createStatusBarItem(vscode.StatusBarAlignment.Left, 100);
    statusBarItem.command = 'tempo-tracker.reviewAndLog';
    context.subscriptions.push(statusBarItem);

    // 2. Register the command to review and log time
    context.subscriptions.push(
        vscode.commands.registerCommand('tempo-tracker.reviewAndLog', () => {
            // This is the trigger for the "Review and Approve" UI
            vscode.window.showInformationMessage(`Reviewing time for ticket: ${activeTicketId || 'None'}`);
            // In a real implementation, this would open a Webview or Quick Pick UI.
        })
    );

    // 3. Initialize the activity heartbeat listeners
    initializeHeartbeat(context);

    // 4. Set up the Git integration and initial status bar state
    updateGitInfo();

    // 5. Set up an interval to check for idle state and branch changes
    setInterval(() => {
        if (isTracking && Date.now() - lastHeartbeat > IDLE_THRESHOLD_MS) {
            isTracking = false;
            updateStatusBar();
            console.log('User is idle. Pausing tracking.');
        }
        updateGitInfo(); // Periodically check for branch changes
    }, 30 * 1000); // Check every 30 seconds
}

/**
 * Sets up listeners for user activity to generate "heartbeats".
 */
function initializeHeartbeat(context: vscode.ExtensionContext) {
    const onHeartbeat = () => {
        lastHeartbeat = Date.now();
        if (!isTracking) {
            isTracking = true;
            updateStatusBar();
            console.log('User is active again. Resuming tracking.');
        }
        // In a real implementation, you would log this activity segment to local storage.
    };

    context.subscriptions.push(vscode.workspace.onDidChangeTextDocument(onHeartbeat));
    context.subscriptions.push(vscode.workspace.onDidSaveTextDocument(onHeartbeat));
    context.subscriptions.push(vscode.window.onDidChangeActiveTextEditor(onHeartbeat));

    console.log('Heartbeat listeners initialized.');
}

/**
 * Reads the current Git branch and updates the active ticket ID.
 */
async function updateGitInfo() {
    const workspaceFolders = vscode.workspace.workspaceFolders;
    if (!workspaceFolders) {
        activeTicketId = null;
        updateStatusBar();
        return;
    }

    try {
        const git: SimpleGit = simpleGit(workspaceFolders[0].uri.fsPath);
        const branch = await git.branch();
        const currentBranch = branch.current;

        // Regex to extract a Jira-like ticket ID (e.g., PROJ-123)
        const ticketRegex = /([A-Z]+-\d+)/;
        const match = currentBranch.match(ticketRegex);

        const newTicketId = match ? match[0] : null;

        if (newTicketId !== activeTicketId) {
            activeTicketId = newTicketId;
            console.log(`Switched to ticket: ${activeTicketId}`);
        }
    } catch (error) {
        activeTicketId = null;
    } finally {
        updateStatusBar();
    }
}

/**
 * Updates the status bar item with the current tracking status and ticket ID.
 */
function updateStatusBar() {
    if (activeTicketId) {
        const icon = isTracking ? '$(clock)' : '$(debug-pause)';
        statusBarItem.text = `${icon} ${activeTicketId}`;
        statusBarItem.tooltip = `Tracking time for ${activeTicketId}. Click to review and log.`;
        statusBarItem.show();
    } else {
        statusBarItem.text = '$(stop-circle) No Ticket';
        statusBarItem.tooltip = 'Current branch does not contain a ticket ID.';
        statusBarItem.show();
    }
}

export function deactivate() {
    statusBarItem.dispose();
}
