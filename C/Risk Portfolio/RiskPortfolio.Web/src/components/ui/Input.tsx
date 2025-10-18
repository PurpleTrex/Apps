import { forwardRef, useState } from 'react';
import type { InputHTMLAttributes, CSSProperties } from 'react';

export interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  error?: string;
}

const Input = forwardRef<HTMLInputElement, InputProps>(
  ({ type, label, error, style, ...props }, ref) => {
    const [isFocused, setIsFocused] = useState(false);

    const inputStyles: CSSProperties = {
      display: 'flex',
      height: '2.75rem',
      width: '100%',
      borderRadius: '0.75rem',
      border: error 
        ? '1px solid rgba(244, 63, 94, 0.6)'
        : isFocused 
          ? '1px solid rgba(14, 165, 233, 0.6)'
          : '1px solid rgba(51, 65, 85, 0.7)',
      backgroundColor: 'rgba(2, 6, 23, 0.6)',
      padding: '0.5rem 1rem',
      fontSize: '0.875rem',
      color: '#f1f5f9',
      boxShadow: '0 12px 30px -20px rgba(15, 23, 42, 0.8)',
      outline: 'none',
      transition: 'all 200ms',
      ...style,
    };

    const focusRingStyles: CSSProperties = isFocused ? {
      boxShadow: error
        ? '0 0 0 2px rgba(244, 63, 94, 0.5)'
        : '0 0 0 2px rgba(14, 165, 233, 0.6)',
    } : {};

    return (
      <div style={{ width: '100%' }}>
        {label && (
          <label style={{
            marginBottom: '0.25rem',
            display: 'block',
            fontSize: '0.75rem',
            fontWeight: '600',
            textTransform: 'uppercase',
            letterSpacing: '0.28em',
            color: '#94a3b8',
          }}>
            {label}
            {props.required && <span style={{ color: '#ef4444', marginLeft: '0.25rem' }}>*</span>}
          </label>
        )}
        <input
          type={type}
          style={{
            ...inputStyles,
            ...focusRingStyles,
          }}
          onFocus={() => setIsFocused(true)}
          onBlur={() => setIsFocused(false)}
          ref={ref}
          {...props}
        />
        {error && (
          <p style={{
            marginTop: '0.25rem',
            fontSize: '0.75rem',
            fontWeight: '500',
            color: '#fda4af',
          }}>
            {error}
          </p>
        )}
      </div>
    );
  }
);

Input.displayName = 'Input';

export default Input;