# Changelog

All notable changes to the VS Code Tempo Tracker extension will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.0.1] - 2025-10-23

### Added
- Initial proof-of-concept implementation
- Automatic activity tracking (typing, saving, file switching)
- Idle detection with 5-minute threshold
- Git branch integration for Jira ticket ID extraction
- Status bar UI element showing current tracked ticket
- Review and log command placeholder
- Complete TypeScript project setup
- ESLint configuration for code quality
- VS Code debugging and build configurations
- Comprehensive documentation (README, QUICKSTART, PROJECT_STRUCTURE)

### Security
- Updated simple-git dependency to v3.16.0+ to fix RCE vulnerability
- Passed CodeQL security scan with 0 alerts

### Developer Experience
- Full TypeScript compilation setup
- Watch mode for development
- Linting with ESLint
- VS Code Extension Development Host integration
- Comprehensive inline code documentation

## [Unreleased]

### Planned Features
- Local storage implementation for time tracking data
- Interactive review and approve UI
- Tempo API integration for submitting worklogs
- Secure credential storage for Tempo API tokens
- Configuration options for idle timeout and ticket regex
- Manual time entry for meetings and non-coding activities
- Daily summary notifications
- Time tracking reports and analytics
- Multi-workspace support
- Automated testing suite
- CI/CD pipeline
- Extension marketplace publishing

### Future Enhancements
- Support for other project management tools (GitHub Issues, Azure DevOps)
- Integration with calendar apps for meeting tracking
- Team time tracking visibility (with privacy controls)
- Customizable activity categories
- Time tracking insights and productivity metrics
- Browser extension for tracking non-VS Code activities
