import { useNavigate } from 'react-router-dom';
import { Bookmark, Building2, Search, Trash2, User } from 'lucide-react';
import { useStore } from '../store';

export default function SavedPropertiesPage() {
  const navigate = useNavigate();
  const { searchResponse, savedGroupIds, toggleSaved, user } = useStore();

  const savedGroups = searchResponse?.groups.filter(g => savedGroupIds.has(g.groupId)) || [];

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-4xl mx-auto px-4 py-8">
        <div className="flex items-center gap-3 mb-6">
          <div className="w-10 h-10 bg-blue-100 rounded-xl flex items-center justify-center">
            <Bookmark size={20} className="text-blue-600" />
          </div>
          <div>
            <h1 className="text-2xl font-bold text-gray-900">My Shortlist</h1>
            <p className="text-sm text-gray-500">{savedGroups.length} saved {savedGroups.length === 1 ? 'property' : 'properties'}</p>
          </div>
        </div>

        {!user ? (
          <div className="card p-12 text-center">
            <User size={48} className="text-gray-200 mx-auto mb-4" />
            <h2 className="font-bold text-gray-700 mb-2">Sign in to view saved properties</h2>
            <p className="text-gray-400 text-sm mb-6">
              Create an account or sign in to access your shortlist across devices.
            </p>
            <div className="flex justify-center gap-3">
              <button onClick={() => navigate('/login')} className="btn-secondary text-sm">
                Sign In
              </button>
              <button onClick={() => navigate('/register')} className="btn-primary text-sm">
                Create Account
              </button>
            </div>
          </div>
        ) : savedGroups.length === 0 ? (
          <div className="card p-12 text-center">
            <Building2 size={48} className="text-gray-200 mx-auto mb-4" />
            <h2 className="font-bold text-gray-700 mb-2">No saved properties</h2>
            <p className="text-gray-400 text-sm mb-6">
              Save properties while browsing to compare them here
            </p>
            <button onClick={() => navigate('/results')} className="btn-primary text-sm">
              <Search size={15} /> Browse Properties
            </button>
          </div>
        ) : (
          <div className="space-y-4">
            {savedGroups.map(group => (
              <div key={group.groupId} className="card p-4 flex items-center gap-4">
                <img
                  src={group.primaryImage || 'https://images.unsplash.com/photo-1545324418-cc1a3fa10c00?w=300'}
                  alt={group.canonicalTitle}
                  className="w-24 h-18 rounded-xl object-cover flex-shrink-0"
                  style={{ height: 72 }}
                />
                <div className="flex-1 min-w-0">
                  <h3 className="font-bold text-gray-900 truncate">{group.canonicalTitle}</h3>
                  <p className="text-sm text-gray-500">{group.canonicalLocality}, {group.canonicalCity}</p>
                  <div className="flex items-center gap-3 mt-1">
                    <span className="text-lg font-black text-gray-900">{group.bestPriceFormatted}</span>
                    {group.aiScore && (
                      <span className="text-sm font-bold text-blue-600">AI: {group.aiScore.overallScore}/100</span>
                    )}
                  </div>
                </div>
                <div className="flex gap-2">
                  <button
                    onClick={() => navigate(`/property/${group.groupId}`, { state: { group } })}
                    className="btn-primary text-sm py-2 px-3"
                  >
                    View
                  </button>
                  <button
                    onClick={() => toggleSaved(group.groupId)}
                    className="p-2 hover:bg-red-50 rounded-xl text-gray-400 hover:text-red-500 transition-colors"
                  >
                    <Trash2 size={16} />
                  </button>
                </div>
              </div>
            ))}

            {savedGroups.length >= 2 && (
              <button className="w-full btn-secondary py-3 text-sm">
                Compare All Saved Properties
              </button>
            )}
          </div>
        )}
      </div>
    </div>
  );
}
