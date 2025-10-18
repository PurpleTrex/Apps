import type { HTMLAttributes, CSSProperties } from 'react';
import { RiskClassification } from '@/types/api';
import {
  getRiskClassificationLabel,
  getRiskClassificationColor,
  getRiskClassificationBgColor,
} from '@/lib/utils';

interface BadgeProps extends HTMLAttributes<HTMLDivElement> {
  variant?: 'default' | 'risk';
  riskClassification?: RiskClassification;
}

export default function Badge({
  className,
  variant = 'default',
  riskClassification,
  children,
  style,
  ...props
}: BadgeProps) {
  const baseStyles: CSSProperties = {
    display: 'inline-flex',
    alignItems: 'center',
    borderRadius: '9999px',
    padding: '0.25rem 0.75rem',
    fontSize: '0.6rem',
    fontWeight: '600',
    textTransform: 'uppercase',
    letterSpacing: '0.3em',
    backdropFilter: 'blur(12px)',
  };

  if (variant === 'risk' && riskClassification !== undefined) {
    const color = getRiskClassificationColor(riskClassification);
    const bgColor = getRiskClassificationBgColor(riskClassification);
    const label = getRiskClassificationLabel(riskClassification);

    return (
      <div
        className={className}
        style={{
          ...baseStyles,
          color,
          backgroundColor: bgColor,
          border: 'none',
          ...style,
        }}
        {...props}
      >
        {label}
      </div>
    );
  }

  return (
    <div
      className={className}
      style={{
        ...baseStyles,
        border: '1px solid rgba(71, 85, 105, 0.5)',
        backgroundColor: 'rgba(15, 23, 42, 0.7)',
        color: '#e2e8f0',
        ...style,
      }}
      {...props}
    >
      {children}
    </div>
  );
}