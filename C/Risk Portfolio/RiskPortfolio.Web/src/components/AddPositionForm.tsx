import { useState } from 'react';
import { useAddOrUpdatePosition } from '@/hooks/usePortfolios';
import Button from '@/components/ui/Button';
import Input from '@/components/ui/Input';
import type { AddOrUpdatePositionRequest } from '@/types/api';

interface AddPositionFormProps {
  portfolioId: string;
  onSuccess: () => void;
  onCancel: () => void;
}

export default function AddPositionForm({
  portfolioId,
  onSuccess,
  onCancel,
}: AddPositionFormProps) {
  const addOrUpdatePosition = useAddOrUpdatePosition();

  const [formData, setFormData] = useState<AddOrUpdatePositionRequest>({
    symbol: '',
    quantity: 0,
    averagePrice: 0,
    currentPrice: 0,
    volatility: 0,
  });

  const [errors, setErrors] = useState<Record<string, string>>({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  const validate = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (!formData.symbol.trim()) {
      newErrors.symbol = 'Symbol is required';
    } else if (formData.symbol.length > 20) {
      newErrors.symbol = 'Symbol must be 20 characters or less';
    }

    if (formData.quantity <= 0) {
      newErrors.quantity = 'Quantity must be greater than 0';
    }

    if (formData.averagePrice <= 0) {
      newErrors.averagePrice = 'Average price must be greater than 0';
    }

    if (formData.currentPrice <= 0) {
      newErrors.currentPrice = 'Current price must be greater than 0';
    }

    if (formData.volatility < 0 || formData.volatility > 1) {
      newErrors.volatility = 'Volatility must be between 0 and 1';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validate()) return;

    setIsSubmitting(true);
    try {
      await addOrUpdatePosition.mutateAsync({
        id: portfolioId,
        data: { ...formData, symbol: formData.symbol.toUpperCase() },
      });
      onSuccess();
    } catch (error: unknown) {
      console.error('Failed to add position:', error);
      if (error && typeof error === 'object' && 'errors' in error) {
        const apiErrors = error.errors as Record<string, string[]>;
        const newErrors: Record<string, string> = {};
        Object.entries(apiErrors).forEach(([key, messages]) => {
          newErrors[key.toLowerCase()] = messages[0];
        });
        setErrors(newErrors);
      } else {
        alert('Failed to add position. Please try again.');
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-5">
      <Input
        label="Symbol"
        value={formData.symbol}
        onChange={(e) => setFormData({ ...formData, symbol: e.target.value.toUpperCase() })}
        placeholder="e.g., AAPL"
        error={errors.symbol}
        required
        maxLength={20}
      />

      <Input
        type="number"
        label="Quantity"
        value={formData.quantity || ''}
        onChange={(e) =>
          setFormData({ ...formData, quantity: parseFloat(e.target.value) || 0 })
        }
        placeholder="e.g., 100"
        error={errors.quantity}
        required
        step="0.0001"
      />

      <Input
        type="number"
        label="Average Price"
        value={formData.averagePrice || ''}
        onChange={(e) =>
          setFormData({ ...formData, averagePrice: parseFloat(e.target.value) || 0 })
        }
        placeholder="e.g., 150.00"
        error={errors.averagePrice}
        required
        step="0.01"
      />

      <Input
        type="number"
        label="Current Price"
        value={formData.currentPrice || ''}
        onChange={(e) =>
          setFormData({ ...formData, currentPrice: parseFloat(e.target.value) || 0 })
        }
        placeholder="e.g., 155.00"
        error={errors.currentPrice}
        required
        step="0.01"
      />

      <Input
        type="number"
        label="Volatility (0-1)"
        value={formData.volatility || ''}
        onChange={(e) =>
          setFormData({ ...formData, volatility: parseFloat(e.target.value) || 0 })
        }
        placeholder="e.g., 0.25"
        error={errors.volatility}
        required
        step="0.001"
        min="0"
        max="1"
      />

      <div className="flex justify-end gap-3 border-t border-slate-800/60 pt-5">
        <Button type="button" variant="secondary" onClick={onCancel}>
          Cancel
        </Button>
        <Button type="submit" disabled={isSubmitting}>
          {isSubmitting ? 'Adding...' : 'Add Position'}
        </Button>
      </div>
    </form>
  );
}
