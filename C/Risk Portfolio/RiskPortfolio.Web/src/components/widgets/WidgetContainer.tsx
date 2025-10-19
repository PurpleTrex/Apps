import type { ReactNode } from 'react';

interface WidgetContainerProps {
  title: string;
  children: ReactNode;
  onEdit?: () => void;
  onDelete?: () => void;
  className?: string;
}

export default function WidgetContainer({
  title,
  children,
  onEdit,
  onDelete,
  className = ''
}: WidgetContainerProps) {
  return (
    <div
      className={`rounded-lg border border-slate-800/50 bg-[#12161F]/60 backdrop-blur-sm ${className}`}
      style={{ height: '100%', display: 'flex', flexDirection: 'column' }}
    >
      {/* Widget Header */}
      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          padding: '0.75rem 1rem',
          borderBottom: '1px solid rgba(148, 163, 184, 0.1)'
        }}
      >
        <h3 style={{
          fontSize: '0.875rem',
          fontWeight: '600',
          color: 'white',
          textTransform: 'uppercase',
          letterSpacing: '0.05em'
        }}>
          {title}
        </h3>

        <div style={{ display: 'flex', gap: '0.5rem' }}>
          {onEdit && (
            <button
              onClick={onEdit}
              style={{
                padding: '0.25rem',
                borderRadius: '0.25rem',
                backgroundColor: 'transparent',
                border: 'none',
                color: '#64748b',
                cursor: 'pointer',
                transition: 'color 200ms'
              }}
              onMouseEnter={(e) => e.currentTarget.style.color = '#38bdf8'}
              onMouseLeave={(e) => e.currentTarget.style.color = '#64748b'}
            >
              <svg style={{ width: '1rem', height: '1rem' }} fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
              </svg>
            </button>
          )}
          {onDelete && (
            <button
              onClick={onDelete}
              style={{
                padding: '0.25rem',
                borderRadius: '0.25rem',
                backgroundColor: 'transparent',
                border: 'none',
                color: '#64748b',
                cursor: 'pointer',
                transition: 'color 200ms'
              }}
              onMouseEnter={(e) => e.currentTarget.style.color = '#ef4444'}
              onMouseLeave={(e) => e.currentTarget.style.color = '#64748b'}
            >
              <svg style={{ width: '1rem', height: '1rem' }} fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          )}
        </div>
      </div>

      {/* Widget Content */}
      <div style={{
        flex: 1,
        padding: '1rem',
        overflow: 'auto'
      }}>
        {children}
      </div>
    </div>
  );
}
