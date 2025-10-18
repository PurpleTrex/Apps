import type { CSSProperties } from 'react';

interface LoadingSpinnerProps {
  size?: 'sm' | 'md' | 'lg';
  className?: string;
  style?: CSSProperties;
}

export default function LoadingSpinner({ size = 'md', className, style }: LoadingSpinnerProps) {
  const sizes: Record<string, CSSProperties> = {
    sm: { height: '1rem', width: '1rem', borderWidth: '2px' },
    md: { height: '2rem', width: '2rem', borderWidth: '3px' },
    lg: { height: '3rem', width: '3rem', borderWidth: '4px' },
  };

  const spinnerStyles: CSSProperties = {
    display: 'inline-block',
    borderRadius: '50%',
    borderStyle: 'solid',
    borderColor: 'rgba(14, 165, 233, 0.4)',
    borderTopColor: '#0ea5e9',
    animation: 'spin 1s linear infinite',
    ...sizes[size],
    ...style,
  };

  return (
    <>
      <style>
        {`
          @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
          }
        `}
      </style>
      <div
        className={className}
        style={spinnerStyles}
        role="status"
      >
        <span style={{
          position: 'absolute',
          width: '1px',
          height: '1px',
          padding: 0,
          margin: '-1px',
          overflow: 'hidden',
          clip: 'rect(0, 0, 0, 0)',
          whiteSpace: 'nowrap',
          borderWidth: 0,
        }}>
          Loading...
        </span>
      </div>
    </>
  );
}