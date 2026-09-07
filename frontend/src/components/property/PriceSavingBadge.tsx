import { TrendingDown } from 'lucide-react';

interface Props {
  saving?: string;
  percentage?: number;
}

export default function PriceSavingBadge({ saving, percentage }: Props) {
  if (!saving) return null;

  return (
    <div className="inline-flex items-center gap-1.5 px-3 py-1.5 bg-green-50 border border-green-200 rounded-xl">
      <TrendingDown size={14} className="text-green-600" />
      <span className="text-sm font-bold text-green-700">Save {saving}</span>
      {percentage && (
        <span className="text-xs text-green-600">({percentage.toFixed(1)}% off)</span>
      )}
    </div>
  );
}
