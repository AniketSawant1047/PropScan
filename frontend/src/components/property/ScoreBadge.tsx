interface Props {
  score: number;
  grade: string;
  size?: 'sm' | 'md' | 'lg';
}

const gradeColors: Record<string, string> = {
  Excellent: 'bg-green-500',
  Good: 'bg-blue-500',
  Fair: 'bg-amber-500',
  Poor: 'bg-red-500',
};

const gradeTextColors: Record<string, string> = {
  Excellent: 'text-green-700',
  Good: 'text-blue-700',
  Fair: 'text-amber-700',
  Poor: 'text-red-700',
};

const gradeBgColors: Record<string, string> = {
  Excellent: 'bg-green-50',
  Good: 'bg-blue-50',
  Fair: 'bg-amber-50',
  Poor: 'bg-red-50',
};

export default function ScoreBadge({ score, grade, size = 'md' }: Props) {
  const color = gradeColors[grade] || 'bg-blue-500';
  const textColor = gradeTextColors[grade] || 'text-blue-700';
  const bgColor = gradeBgColors[grade] || 'bg-blue-50';

  if (size === 'sm') {
    return (
      <div className={`inline-flex items-center gap-1 px-2 py-0.5 ${bgColor} rounded-lg`}>
        <div className={`w-1.5 h-1.5 rounded-full ${color}`} />
        <span className={`text-xs font-bold ${textColor}`}>{score}</span>
      </div>
    );
  }

  if (size === 'lg') {
    return (
      <div className={`flex flex-col items-center justify-center w-20 h-20 rounded-2xl ${bgColor} border-2 ${color.replace('bg-', 'border-')}`}>
        <div className={`text-3xl font-black ${textColor}`}>{score}</div>
        <div className={`text-xs font-bold ${textColor} opacity-70`}>{grade}</div>
      </div>
    );
  }

  return (
    <div className={`inline-flex items-center gap-1.5 px-2.5 py-1.5 ${bgColor} rounded-xl`}>
      <div className="flex flex-col items-center">
        <span className={`text-base font-black ${textColor} leading-none`}>{score}</span>
        <span className={`text-xs font-medium ${textColor} opacity-70`}>/100</span>
      </div>
    </div>
  );
}
