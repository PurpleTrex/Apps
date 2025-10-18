import { forwardRef, useState } from 'react';
import type { ButtonHTMLAttributes, CSSProperties } from 'react';

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'danger' | 'ghost';
  size?: 'sm' | 'md' | 'lg';
}

const Button = forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, variant = 'primary', size = 'md', style, children, ...props }, ref) => {
    const [isHovered, setIsHovered] = useState(false);

    const baseStyles: CSSProperties = {
      display: 'inline-flex',
      alignItems: 'center',
      justifyContent: 'center',
      gap: '0.5rem',
      borderRadius: '0.75rem',
      fontWeight: '600',
      textTransform: 'uppercase',
      letterSpacing: '0.12em',
      transition: 'all 200ms',
      cursor: props.disabled ? 'not-allowed' : 'pointer',
      opacity: props.disabled ? 0.6 : 1,
      border: 'none',
      outline: 'none',
    };

    const variants: Record<string, CSSProperties> = {
      primary: {
        background: isHovered 
          ? 'linear-gradient(to right, #38bdf8, #3b82f6, #6366f1)'
          : 'linear-gradient(to right, #0ea5e9, #3b82f6, #6366f1)',
        color: '#ffffff',
        boxShadow: '0 18px 38px -20px rgba(59, 130, 246, 0.8)',
      },
      secondary: {
        border: '1px solid rgba(51, 65, 85, 0.7)',
        backgroundColor: isHovered ? 'rgba(30, 41, 59, 0.9)' : 'rgba(15, 23, 42, 0.8)',
        color: isHovered ? '#ffffff' : '#e2e8f0',
      },
      danger: {
        background: isHovered
          ? 'linear-gradient(to right, #fb7185, #ef4444, #f97316)'
          : 'linear-gradient(to right, #f43f5e, #ef4444, #f97316)',
        color: '#ffffff',
        boxShadow: '0 18px 38px -20px rgba(244, 63, 94, 0.8)',
      },
      ghost: {
        backgroundColor: isHovered ? 'rgba(30, 41, 59, 0.6)' : 'transparent',
        color: isHovered ? '#f1f5f9' : '#94a3b8',
      },
    };

    const sizes: Record<string, CSSProperties> = {
      sm: { fontSize: '0.65rem', padding: '0.375rem 0.75rem' },
      md: { fontSize: '0.7rem', padding: '0.625rem 1.25rem' },
      lg: { fontSize: '0.875rem', padding: '0.875rem 1.75rem' },
    };

    return (
      <button
        ref={ref}
        className={className}
        style={{
          ...baseStyles,
          ...variants[variant],
          ...sizes[size],
          ...style,
        }}
        onMouseEnter={() => setIsHovered(true)}
        onMouseLeave={() => setIsHovered(false)}
        {...props}
      >
        {children}
      </button>
    );
  }
);

Button.displayName = 'Button';

export default Button;