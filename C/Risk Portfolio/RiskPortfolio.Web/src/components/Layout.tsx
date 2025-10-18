import { type ReactNode } from 'react';
import TopHeader from './TopHeader';

interface LayoutProps {
  children: ReactNode;
  showDashboardLink?: boolean;
}

export default function Layout({ children }: LayoutProps) {
  return (
    <div style={{ minHeight: '100vh', backgroundColor: '#0A0E1A' }}>
      <TopHeader />
      <main style={{ 
        marginTop: '3.5rem',
        minHeight: 'calc(100vh - 3.5rem)'
      }}>
        {children}
      </main>
    </div>
  );
}