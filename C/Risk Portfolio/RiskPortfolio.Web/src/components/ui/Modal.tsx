import { useEffect } from 'react';
import type { ReactNode, CSSProperties } from 'react';

interface ModalProps {
  isOpen: boolean;
  onClose: () => void;
  title: string;
  children: ReactNode;
  footer?: ReactNode;
  size?: 'sm' | 'md' | 'lg' | 'xl';
}

export default function Modal({
  isOpen,
  onClose,
  title,
  children,
  footer,
  size = 'md',
}: ModalProps) {
  useEffect(() => {
    const handleEscape = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose();
    };

    if (isOpen) {
      document.addEventListener('keydown', handleEscape);
      document.body.style.overflow = 'hidden';
    }

    return () => {
      document.removeEventListener('keydown', handleEscape);
      document.body.style.overflow = 'unset';
    };
  }, [isOpen, onClose]);

  if (!isOpen) return null;

  const sizes: Record<string, string> = {
    sm: '28rem',
    md: '32rem',
    lg: '42rem',
    xl: '56rem',
  };

  const containerStyles: CSSProperties = {
    position: 'fixed',
    inset: 0,
    zIndex: 50,
    overflowY: 'auto',
  };

  const flexContainerStyles: CSSProperties = {
    display: 'flex',
    minHeight: '100%',
    alignItems: 'center',
    justifyContent: 'center',
    padding: '1rem',
  };

  const backdropStyles: CSSProperties = {
    position: 'fixed',
    inset: 0,
    backgroundColor: 'rgba(2, 6, 23, 0.7)',
    backdropFilter: 'blur(4px)',
    transition: 'opacity 200ms',
  };

  const modalStyles: CSSProperties = {
    position: 'relative',
    width: '100%',
    maxWidth: sizes[size],
    borderRadius: '1rem',
    border: '1px solid rgba(148, 163, 184, 0.7)',
    backgroundColor: 'rgba(2, 6, 23, 0.9)',
    boxShadow: '0 40px 120px -32px rgba(15, 23, 42, 0.95)',
    backdropFilter: 'blur(12px)',
    transition: 'all 200ms',
  };

  const headerStyles: CSSProperties = {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    borderBottom: '1px solid rgba(148, 163, 184, 0.7)',
    padding: '1rem 1.5rem',
  };

  const titleStyles: CSSProperties = {
    fontSize: '1rem',
    fontWeight: '600',
    textTransform: 'uppercase',
    letterSpacing: '0.3em',
    color: '#e2e8f0',
  };

  const closeButtonStyles: CSSProperties = {
    color: '#64748b',
    transition: 'color 200ms',
    cursor: 'pointer',
    background: 'none',
    border: 'none',
    padding: 0,
  };

  const contentStyles: CSSProperties = {
    padding: '1rem 1.5rem',
    color: '#e2e8f0',
  };

  const footerStyles: CSSProperties = {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'flex-end',
    gap: '0.75rem',
    borderTop: '1px solid rgba(148, 163, 184, 0.7)',
    padding: '1rem 1.5rem',
  };

  return (
    <div style={containerStyles}>
      <div style={flexContainerStyles}>
        {/* Backdrop */}
        <div
          style={backdropStyles}
          onClick={onClose}
        />

        {/* Modal */}
        <div style={modalStyles}>
          {/* Header */}
          <div style={headerStyles}>
            <h2 style={titleStyles}>
              {title}
            </h2>
            <button
              onClick={onClose}
              style={closeButtonStyles}
              onMouseEnter={(e) => e.currentTarget.style.color = '#e2e8f0'}
              onMouseLeave={(e) => e.currentTarget.style.color = '#64748b'}
            >
              <svg
                style={{ height: '1.5rem', width: '1.5rem' }}
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M6 18L18 6M6 6l12 12"
                />
              </svg>
            </button>
          </div>

          {/* Content */}
          <div style={contentStyles}>{children}</div>

          {/* Footer */}
          {footer && (
            <div style={footerStyles}>
              {footer}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}