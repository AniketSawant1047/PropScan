interface Props {
  source: string;
}

const SOURCE_CONFIG: Record<string, { name: string; color: string; textColor: string; bg: string }> = {
  '99acres': { name: '99acres', color: 'border-orange-200', textColor: 'text-orange-700', bg: 'bg-orange-50' },
  magicbricks: { name: 'MagicBricks', color: 'border-red-200', textColor: 'text-red-700', bg: 'bg-red-50' },
  housing: { name: 'Housing', color: 'border-blue-200', textColor: 'text-blue-700', bg: 'bg-blue-50' },
  nobroker: { name: 'NoBroker', color: 'border-green-200', textColor: 'text-green-700', bg: 'bg-green-50' },
  builders: { name: 'Builder', color: 'border-indigo-200', textColor: 'text-indigo-700', bg: 'bg-indigo-50' },
  local: { name: 'Local', color: 'border-purple-200', textColor: 'text-purple-700', bg: 'bg-purple-50' },
};

export default function SourceBadge({ source }: Props) {
  const config = SOURCE_CONFIG[source] || {
    name: source,
    color: 'border-gray-200',
    textColor: 'text-gray-700',
    bg: 'bg-gray-50'
  };

  return (
    <span className={`inline-flex items-center px-2 py-0.5 text-xs font-semibold rounded-md border ${config.color} ${config.textColor} ${config.bg}`}>
      {config.name}
    </span>
  );
}
