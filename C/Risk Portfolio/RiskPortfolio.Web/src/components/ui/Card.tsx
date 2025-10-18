import { forwardRef } from 'react';
import type { HTMLAttributes } from 'react';

const Card = forwardRef<HTMLDivElement, HTMLAttributes<HTMLDivElement>>(
  ({ className, style, ...props }, ref) => (
    <div
      ref={ref}
      className={className}
      style={{
        borderRadius: '1rem',
        border: '1px solid rgba(148, 163, 184, 0.6)',
        backgroundColor: 'rgba(15, 23, 42, 0.7)',
        boxShadow: '0 20px 60px -24px rgba(15, 23, 42, 0.9)',
        backdropFilter: 'blur(12px)',
        ...style
      }}
      {...props}
    />
  )
);
Card.displayName = 'Card';

const CardHeader = forwardRef<HTMLDivElement, HTMLAttributes<HTMLDivElement>>(
  ({ className, style, ...props }, ref) => (
    <div
      ref={ref}
      className={className}
      style={{
        display: 'flex',
        flexDirection: 'column',
        gap: '0.375rem',
        padding: '1.5rem',
        color: '#cbd5e1',
        ...style
      }}
      {...props}
    />
  )
);
CardHeader.displayName = 'CardHeader';

const CardTitle = forwardRef<HTMLHeadingElement, HTMLAttributes<HTMLHeadingElement>>(
  ({ className, style, ...props }, ref) => (
    <h3
      ref={ref}
      className={className}
      style={{
        fontSize: '1.125rem',
        fontWeight: '600',
        lineHeight: '1',
        letterSpacing: '-0.025em',
        color: '#f8fafc',
        ...style
      }}
      {...props}
    />
  )
);
CardTitle.displayName = 'CardTitle';

const CardDescription = forwardRef<HTMLParagraphElement, HTMLAttributes<HTMLParagraphElement>>(
  ({ className, style, ...props }, ref) => (
    <p 
      ref={ref} 
      className={className}
      style={{
        fontSize: '0.875rem',
        color: '#94a3b8',
        ...style
      }}
      {...props} 
    />
  )
);
CardDescription.displayName = 'CardDescription';

const CardContent = forwardRef<HTMLDivElement, HTMLAttributes<HTMLDivElement>>(
  ({ className, style, ...props }, ref) => (
    <div 
      ref={ref} 
      className={className}
      style={{
        padding: '0 1.5rem 1.5rem 1.5rem',
        color: '#e2e8f0',
        ...style
      }}
      {...props} 
    />
  )
);
CardContent.displayName = 'CardContent';

const CardFooter = forwardRef<HTMLDivElement, HTMLAttributes<HTMLDivElement>>(
  ({ className, style, ...props }, ref) => (
    <div 
      ref={ref} 
      className={className}
      style={{
        display: 'flex',
        alignItems: 'center',
        padding: '0 1.5rem 1.5rem 1.5rem',
        color: '#e2e8f0',
        ...style
      }}
      {...props} 
    />
  )
);
CardFooter.displayName = 'CardFooter';

export { Card, CardHeader, CardFooter, CardTitle, CardDescription, CardContent };