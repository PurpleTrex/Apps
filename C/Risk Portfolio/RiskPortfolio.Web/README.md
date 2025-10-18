# Risk Portfolio - Frontend Application

A modern React frontend for the Risk Portfolio Management System, built with TypeScript, Vite, and TailwindCSS.

## Features

- **Portfolio Management**: Create, view, edit, and delete investment portfolios
- **Position Tracking**: Add, update, and remove asset positions within portfolios
- **Risk Analytics Dashboard**: Visual risk metrics with interactive charts
- **Real-time Risk Calculation**: Manual and automatic risk assessment
- **Responsive Design**: Mobile-friendly UI with TailwindCSS
- **Type-Safe**: Full TypeScript implementation with API type definitions

## Tech Stack

- **React 19** - UI framework
- **TypeScript** - Type safety
- **Vite** - Build tool and dev server
- **TailwindCSS** - Utility-first CSS framework
- **TanStack Query (React Query)** - Data fetching and caching
- **React Router** - Client-side routing
- **Axios** - HTTP client
- **Recharts** - Data visualization
- **date-fns** - Date formatting

## Project Structure

```
src/
├── components/          # Reusable UI components
│   ├── ui/             # Base UI components (Button, Card, Input, etc.)
│   ├── Layout.tsx      # Main layout wrapper
│   ├── RiskDashboard.tsx      # Risk analytics dashboard
│   ├── PositionsTable.tsx     # Position list table
│   └── AddPositionForm.tsx    # Add position form
├── pages/              # Page components
│   ├── PortfolioList.tsx      # Portfolio list view
│   ├── PortfolioDetails.tsx   # Portfolio details view
│   └── CreatePortfolio.tsx    # Create portfolio form
├── hooks/              # Custom React hooks
│   └── usePortfolios.ts       # Portfolio data hooks
├── lib/                # Utilities
│   ├── api-client.ts          # API client configuration
│   └── utils.ts               # Helper functions
├── types/              # TypeScript type definitions
│   └── api.ts                 # API types
├── App.tsx             # Main app component
└── main.tsx            # Entry point
```

## Getting Started

### Prerequisites

- Node.js 18+ and npm
- Running Risk Portfolio API backend (default: http://localhost:5000)

### Installation

1. Install dependencies:
```bash
npm install
```

### Development

Start the development server:

```bash
npm run dev
```

The application will be available at http://localhost:3000

The dev server includes:
- Hot Module Replacement (HMR)
- Proxy to backend API at `/api` → `http://localhost:5000`

### Build for Production

```bash
npm run build
```

Built files will be in the `dist/` directory.

### Preview Production Build

```bash
npm run preview
```

## Configuration

### API Backend URL

The frontend is configured to proxy API requests to `http://localhost:5000`. To change this:

1. Edit `vite.config.ts`:
```typescript
server: {
  proxy: {
    '/api': {
      target: 'http://your-backend-url:port',
      changeOrigin: true,
    },
  },
}
```

### Port

To change the frontend port, edit `vite.config.ts`:
```typescript
server: {
  port: 3000, // Change this
}
```

## API Integration

The frontend integrates with the following backend endpoints:

- `GET /api/portfolios` - Get all portfolios
- `GET /api/portfolios/{id}` - Get portfolio by ID
- `POST /api/portfolios` - Create new portfolio
- `PUT /api/portfolios/{id}/owner` - Update portfolio owner
- `PUT /api/portfolios/{id}/positions` - Add/update position
- `DELETE /api/portfolios/{id}/positions/{positionId}` - Remove position
- `DELETE /api/portfolios/{id}` - Delete portfolio
- `POST /api/portfolios/{id}/recalculate-risk` - Recalculate risk

## Key Features Explained

### Portfolio List
- View all portfolios in a card grid
- See key metrics: total value, risk score, value at risk
- Risk classification badges with color coding
- Quick actions: view details, delete

### Portfolio Details
- Comprehensive portfolio overview
- Interactive risk analytics dashboard with charts
- Detailed positions table with gain/loss tracking
- Risk metrics summary
- Manual risk recalculation

### Create Portfolio
- Form with validation
- Add multiple positions during creation
- Inline position management
- Real-time form validation

### Risk Dashboard
- Portfolio allocation pie chart
- Risk contribution bar chart
- Key risk metrics display
- Color-coded risk classifications

## Risk Classifications

- **Low**: Risk score < 15 (Green)
- **Moderate**: Risk score 15-34 (Yellow)
- **Elevated**: Risk score 35-59 (Orange)
- **Critical**: Risk score ≥ 60 (Red)

## Development Notes

### Path Aliases

The project uses TypeScript path aliases:
- `@/*` maps to `./src/*`

Example: `import Button from '@/components/ui/Button'`

### Type Safety

All API types are defined in `src/types/api.ts` and match the backend API contracts.

### State Management

Uses TanStack Query for:
- Server state management
- Automatic caching
- Background refetching
- Optimistic updates
- Query invalidation

### Styling

TailwindCSS utility classes for all styling. Custom components in `components/ui/` follow a consistent design system.

## Troubleshooting

### CORS Issues

If you encounter CORS errors, ensure your backend API has CORS configured to allow requests from `http://localhost:3000`.

### API Connection Failed

1. Verify the backend is running on port 5000
2. Check the proxy configuration in `vite.config.ts`
3. Ensure no firewall is blocking the connection

### Build Errors

If TypeScript errors occur:
```bash
npm run lint
```

Clear cache and reinstall:
```bash
rm -rf node_modules package-lock.json
npm install
```

## License

This project is part of the Risk Portfolio Management System.
