import { RiskClassification } from '@/types/api';
import clsx, { type ClassValue } from 'clsx';

export function cn(...inputs: ClassValue[]) {
  return clsx(inputs);
}

export function formatCurrency(value: number, currency: string = 'USD', decimals: number = 2): string {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: currency,
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals,
  }).format(value);
}

export function formatNumber(value: number, decimals: number = 2): string {
  return new Intl.NumberFormat('en-US', {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals,
  }).format(value);
}

export function formatPercentage(value: number, decimals: number = 2): string {
  return `${formatNumber(value, decimals)}%`;
}

export function getRiskClassificationLabel(classification: RiskClassification): string {
  switch (classification) {
    case RiskClassification.Low:
      return 'Low';
    case RiskClassification.Moderate:
      return 'Moderate';
    case RiskClassification.Elevated:
      return 'Elevated';
    case RiskClassification.Critical:
      return 'Critical';
    default:
      return 'Unspecified';
  }
}

export function getRiskClassificationColor(classification: RiskClassification): string {
  switch (classification) {
    case RiskClassification.Low:
      return 'rgb(34, 197, 94)'; // green-500
    case RiskClassification.Moderate:
      return 'rgb(234, 179, 8)'; // yellow-500
    case RiskClassification.Elevated:
      return 'rgb(249, 115, 22)'; // orange-500
    case RiskClassification.Critical:
      return 'rgb(239, 68, 68)'; // red-500
    default:
      return 'rgb(156, 163, 175)'; // gray-400
  }
}

export function getRiskClassificationBgColor(classification: RiskClassification): string {
  switch (classification) {
    case RiskClassification.Low:
      return 'rgba(34, 197, 94, 0.1)';
    case RiskClassification.Moderate:
      return 'rgba(234, 179, 8, 0.1)';
    case RiskClassification.Elevated:
      return 'rgba(249, 115, 22, 0.1)';
    case RiskClassification.Critical:
      return 'rgba(239, 68, 68, 0.1)';
    default:
      return 'rgba(156, 163, 175, 0.1)';
  }
}
