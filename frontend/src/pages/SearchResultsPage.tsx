import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Search, Filter, MapPin, SlidersHorizontal, Bookmark,
  BookmarkCheck, ChevronRight, TrendingDown,
  AlertCircle, CheckCircle2, Star, Building2, X, RefreshCw
} from 'lucide-react';
import { useStore } from '../store';
import { propertyApi } from '../services/api';
import type { PropertyGroup, SearchRequest } from '../types';
import ScoreBadge from '../components/property/ScoreBadge';
import SourceBadge from '../components/property/SourceBadge';

const PUNE_LOCALITIES = [
  'Wakad', 'Hinjewadi', 'Baner', 'Kharadi', 'Viman Nagar',
  'Kothrud', 'Aundh', 'Hadapsar', 'Balewadi', 'Pimple Saudagar'
];

const SORT_OPTIONS = [
  { value: 'score_desc', label: 'AI Recommended' },
  { value: 'price_asc', label: 'Price: Low to High' },
  { value: 'price_desc', label: 'Price: High to Low' },
  { value: 'area_asc', label: 'Area: Small to Large' },
];

function SkeletonCard() {
  return (
    <div className="card p-4 animate-pulse">
      <div className="flex gap-4">
        <div className="w-32 h-24 bg-gray-100 rounded-xl skeleton flex-shrink-0" />
        <div className="flex-1 space-y-2">
          <div className="h-4 bg-gray-100 rounded skeleton w-3/4" />
          <div className="h-3 bg-gray-100 rounded skeleton w-1/2" />
          <div className="h-6 bg-gray-100 rounded skeleton w-1/3" />
        </div>
      </div>
    </div>
  );
}

export default function SearchResultsPage() {
  const navigate = useNavigate();
  const { searchRequest, searchResponse, isSearching, searchError,
          setSearchRequest, setSearchResponse, setIsSearching, setSearchError,
          toggleSaved, isSaved } = useStore();

  const [filterLocality, setFilterLocality] = useState(searchRequest.locality || '');
  const [filterBhk, setFilterBhk] = useState(searchRequest.bhk?.toString() || '');
  const [filterMaxBudget, setFilterMaxBudget] = useState('');
  const [sortBy, setSortBy] = useState(searchRequest.sortBy || 'score_desc');
  const [showFilters, setShowFilters] = useState(false);
  const [nlQuery, setNlQuery] = useState(searchRequest.naturalLanguageQuery || '');

  // Initial search if no response yet
  useEffect(() => {
    if (!searchResponse && !isSearching) {
      doSearch(searchRequest);
    }
  }, []);

  const doSearch = async (req: SearchRequest) => {
    setIsSearching(true);
    setSearchError(null);
    try {
      const response = await propertyApi.search(req);
      setSearchResponse(response);
    } catch (err: any) {
      setSearchError(err.message || 'Search failed');
    } finally {
      setIsSearching(false);
    }
  };

  const handleApplyFilters = () => {
    const req: SearchRequest = {
      ...searchRequest,
      naturalLanguageQuery: undefined, // Clear NL query so pure filters take over
      locality: filterLocality || undefined,
      bhk: filterBhk ? parseInt(filterBhk) : undefined,
      maxBudget: filterMaxBudget ? parseFloat(filterMaxBudget) * 100000 : undefined,
      sortBy,
    };
    setNlQuery(''); // Also clear the text input to avoid confusion
    setSearchRequest(req);
    doSearch(req);
    setShowFilters(false);
  };

  const handleNlSearch = (e: React.FormEvent) => {
    e.preventDefault();
    const req: SearchRequest = { naturalLanguageQuery: nlQuery, city: 'Pune', page: 0, size: 20 };
    setSearchRequest(req);
    doSearch(req);
  };

  const handleViewProperty = (group: PropertyGroup) => {
    navigate(`/property/${group.groupId}`, { state: { group } });
  };

  const groups = searchResponse?.groups || [];
  const totalGroups = searchResponse?.totalGroups || 0;
  const totalListings = searchResponse?.totalListings || 0;

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Top search bar */}
      <div className="bg-white border-b border-gray-100 shadow-sm sticky top-16 z-40">
        <div className="max-w-7xl mx-auto px-4 py-3">
          <form onSubmit={handleNlSearch} className="flex gap-2">
            <div className="flex-1 relative">
              <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
              <input
                type="text"
                value={nlQuery}
                onChange={(e) => setNlQuery(e.target.value)}
                placeholder='Search: "2 BHK in Hinjewadi under ₹80L with parking"'
                className="w-full pl-9 pr-4 py-2.5 border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
            <button type="submit" className="btn-primary py-2.5 px-3 sm:px-4 text-sm hidden sm:flex">
              <Search size={15} /> Search
            </button>
            <button type="submit" className="btn-primary py-2.5 px-3 text-sm sm:hidden flex items-center justify-center">
              <Search size={15} />
            </button>
            <button
              type="button"
              onClick={() => setShowFilters(!showFilters)}
              className={`btn-secondary py-2.5 px-3 sm:px-4 text-sm ${showFilters ? 'bg-blue-50 border-blue-200 text-blue-600' : ''}`}
            >
              <SlidersHorizontal size={15} className="sm:mr-1" /> <span className="hidden sm:inline">Filters</span>
            </button>
          </form>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 py-6">
        <div className="flex gap-6">
          {/* Left Sidebar Filters */}
          <aside className={`${showFilters ? 'block' : 'hidden'} md:block w-64 flex-shrink-0`}>
            <div className="card p-4 sticky top-36">
              <div className="flex items-center justify-between mb-4">
                <h3 className="font-bold text-gray-900 flex items-center gap-2">
                  <Filter size={16} className="text-blue-600" /> Filters
                </h3>
                <button onClick={() => setShowFilters(false)} className="md:hidden p-1 hover:bg-gray-100 rounded">
                  <X size={16} />
                </button>
              </div>

              <div className="space-y-4">
                {/* Locality */}
                <div>
                  <label className="block text-xs font-semibold text-gray-500 mb-1.5 flex items-center gap-1">
                    <MapPin size={11} /> Locality
                  </label>
                  <select
                    value={filterLocality}
                    onChange={(e) => setFilterLocality(e.target.value)}
                    className="input-field text-sm py-2"
                  >
                    <option value="">All localities</option>
                    {PUNE_LOCALITIES.map(l => (
                      <option key={l} value={l}>{l}</option>
                    ))}
                  </select>
                </div>

                {/* BHK */}
                <div>
                  <label className="block text-xs font-semibold text-gray-500 mb-1.5">BHK</label>
                  <div className="flex gap-1.5 flex-wrap">
                    {['', '1', '2', '3', '4'].map((v) => (
                      <button
                        key={v}
                        onClick={() => setFilterBhk(v)}
                        className={`px-3 py-1.5 text-xs font-semibold rounded-lg border transition-all ${
                          filterBhk === v
                            ? 'bg-blue-600 text-white border-blue-600'
                            : 'bg-white text-gray-600 border-gray-200 hover:border-blue-300'
                        }`}
                      >
                        {v === '' ? 'Any' : `${v} BHK`}
                      </button>
                    ))}
                  </div>
                </div>

                {/* Budget */}
                <div>
                  <label className="block text-xs font-semibold text-gray-500 mb-1.5">Max Budget</label>
                  <select
                    value={filterMaxBudget}
                    onChange={(e) => setFilterMaxBudget(e.target.value)}
                    className="input-field text-sm py-2"
                  >
                    <option value="">Any</option>
                    <option value="40">₹40L</option>
                    <option value="60">₹60L</option>
                    <option value="80">₹80L</option>
                    <option value="100">₹1 Cr</option>
                    <option value="150">₹1.5 Cr</option>
                  </select>
                </div>

                {/* Sort */}
                <div>
                  <label className="block text-xs font-semibold text-gray-500 mb-1.5">Sort By</label>
                  <select
                    value={sortBy}
                    onChange={(e) => setSortBy(e.target.value)}
                    className="input-field text-sm py-2"
                  >
                    {SORT_OPTIONS.map(o => (
                      <option key={o.value} value={o.value}>{o.label}</option>
                    ))}
                  </select>
                </div>

                <button onClick={handleApplyFilters} className="btn-primary w-full text-sm py-2.5 justify-center">
                  Apply Filters
                </button>
              </div>
            </div>
          </aside>

          {/* Main Content */}
          <div className="flex-1 min-w-0">
            {/* Results header */}
            <div className="flex items-center justify-between mb-4">
              <div>
                {isSearching ? (
                  <div className="flex items-center gap-2 text-blue-600">
                    <RefreshCw size={16} className="animate-spin" />
                    <span className="font-semibold">Searching all sources…</span>
                  </div>
                ) : searchResponse ? (
                  <div>
                    <h2 className="font-bold text-gray-900">
                      {totalGroups} properties found
                    </h2>
                    <p className="text-sm text-gray-500">
                      {totalListings} listings from {searchResponse.sourceSummaries?.filter(s => s.available).length} sources
                      {searchResponse.interpretedQuery && (
                        <> • <span className="text-blue-600">{searchResponse.interpretedQuery}</span></>
                      )}
                    </p>
                  </div>
                ) : null}
              </div>

              <select
                value={sortBy}
                onChange={(e) => { setSortBy(e.target.value); handleApplyFilters(); }}
                className="text-sm border border-gray-200 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                {SORT_OPTIONS.map(o => (
                  <option key={o.value} value={o.value}>{o.label}</option>
                ))}
              </select>
            </div>

            {/* Source status bar */}
            {searchResponse?.sourceSummaries && (
              <div className="flex flex-wrap gap-2 mb-4">
                {searchResponse.sourceSummaries.map(s => (
                  <span
                    key={s.source}
                    className={`inline-flex items-center gap-1 px-2 py-1 rounded-lg text-xs font-medium ${
                      s.available
                        ? 'bg-green-50 text-green-700 border border-green-100'
                        : 'bg-red-50 text-red-600 border border-red-100'
                    }`}
                  >
                    {s.available ? <CheckCircle2 size={11} /> : <AlertCircle size={11} />}
                    {s.sourceDisplayName}
                    {s.available && <span className="text-gray-400">({s.listingsFound})</span>}
                  </span>
                ))}
              </div>
            )}

            {/* Error */}
            {searchError && (
              <div className="bg-red-50 border border-red-200 rounded-xl p-4 mb-4 flex items-start gap-3">
                <AlertCircle size={18} className="text-red-500 flex-shrink-0 mt-0.5" />
                <div>
                  <p className="font-semibold text-red-700">Search failed</p>
                  <p className="text-sm text-red-600">{searchError}</p>
                </div>
              </div>
            )}

            {/* Loading skeletons */}
            {isSearching && (
              <div className="space-y-3">
                {Array.from({ length: 6 }).map((_, i) => <SkeletonCard key={i} />)}
              </div>
            )}

            {/* Results */}
            {!isSearching && groups.length > 0 && (
              <div className="space-y-3">
                {groups.map((group, idx) => (
                  <PropertyCard
                    key={group.groupId}
                    group={group}
                    isTopPick={idx === 0}
                    onView={() => handleViewProperty(group)}
                    onToggleSave={() => toggleSaved(group.groupId)}
                    isSaved={isSaved(group.groupId)}
                  />
                ))}
              </div>
            )}

            {/* Empty state */}
            {!isSearching && !searchError && groups.length === 0 && searchResponse && (
              <div className="text-center py-16">
                <Building2 size={48} className="text-gray-200 mx-auto mb-4" />
                <h3 className="font-bold text-gray-700 mb-2">No properties found</h3>
                <p className="text-gray-400 text-sm">Try adjusting your filters or search area</p>
                <button
                  onClick={() => { setFilterLocality(''); setFilterBhk(''); doSearch({ city: 'Pune' }); }}
                  className="mt-4 btn-secondary text-sm"
                >
                  Clear Filters
                </button>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

// Property Card Component
interface PropertyCardProps {
  group: PropertyGroup;
  isTopPick: boolean;
  onView: () => void;
  onToggleSave: () => void;
  isSaved: boolean;
}

function PropertyCard({ group, isTopPick, onView, onToggleSave, isSaved }: PropertyCardProps) {
  const hasSaving = group.potentialSavingInr && group.potentialSavingInr > 0 && group.sourceCount > 1;

  return (
    <div className={`card-hover group ${isTopPick ? 'ring-2 ring-blue-200' : ''}`} onClick={onView}>
      <div className="flex flex-col sm:flex-row">
        {/* Image */}
        <div className="relative w-full sm:w-48 flex-shrink-0">
          <img
            src={group.primaryImage || `https://images.unsplash.com/photo-1545324418-cc1a3fa10c00?w=400`}
            alt={group.canonicalTitle}
            className="w-full h-48 sm:h-36 object-cover"
            onError={(e) => {
              (e.target as HTMLImageElement).src = 'https://images.unsplash.com/photo-1545324418-cc1a3fa10c00?w=400';
            }}
          />
          {isTopPick && (
            <div className="absolute top-2 left-2 flex items-center gap-1 bg-blue-600 text-white text-xs font-bold px-2 py-1 rounded-full shadow">
              <Star size={10} fill="white" /> AI Recommended
            </div>
          )}
          {hasSaving && (
            <div className="absolute bottom-2 left-2">
              <span className="bg-green-500 text-white text-xs font-bold px-2 py-0.5 rounded-full">
                Save {group.potentialSavingFormatted}
              </span>
            </div>
          )}
        </div>

        {/* Content */}
        <div className="flex-1 p-4 min-w-0">
          <div className="flex items-start justify-between gap-2">
            <div className="min-w-0">
              <h3 className="font-bold text-gray-900 text-sm leading-tight truncate group-hover:text-blue-700 transition-colors">
                {group.canonicalTitle}
              </h3>
              <p className="text-xs text-gray-500 mt-0.5 flex items-center gap-1">
                <MapPin size={11} /> {group.canonicalLocality}, {group.canonicalCity}
              </p>
            </div>
            <button
              onClick={(e) => { e.stopPropagation(); onToggleSave(); }}
              className="flex-shrink-0 p-1.5 hover:bg-gray-100 rounded-lg transition-colors"
            >
              {isSaved
                ? <BookmarkCheck size={16} className="text-blue-600" />
                : <Bookmark size={16} className="text-gray-400" />
              }
            </button>
          </div>

          {/* Specs row */}
          <div className="flex items-center gap-3 mt-1.5 text-xs text-gray-500">
            {group.canonicalBhk && <span className="font-semibold text-gray-700">{group.canonicalBhk} BHK</span>}
            {group.areaFormatted && <span>{group.areaFormatted}</span>}
            {group.bestListing?.furnishing && <span>{group.bestListing.furnishing}</span>}
          </div>

          {/* Price & Score row */}
          <div className="flex items-center justify-between mt-2">
            <div>
              <div className="text-lg font-black text-gray-900">{group.bestPriceFormatted || 'N/A'}</div>
              {group.pricePerSqftFormatted && (
                <div className="text-xs text-gray-400">{group.pricePerSqftFormatted}</div>
              )}
            </div>
            {group.aiScore && (
              <ScoreBadge score={group.aiScore.overallScore} grade={group.aiScore.grade} />
            )}
          </div>

          {/* Sources & savings */}
          <div className="flex items-center gap-2 mt-2 flex-wrap">
            {group.sources.slice(0, 4).map(s => (
              <SourceBadge key={s} source={s} />
            ))}
            {hasSaving && (
              <span className="badge-green text-xs">
                <TrendingDown size={10} /> Best deal available
              </span>
            )}
            {group.matchConfidence && group.sourceCount > 1 && (
              <span className="text-xs text-gray-400">
                {Math.round(group.matchConfidence)}% match confidence
              </span>
            )}
          </div>
        </div>

        {/* Right actions */}
        <div className="hidden sm:flex flex-col items-center justify-center gap-3 px-4 border-l border-gray-50 flex-shrink-0">
          {group.locationScore && (
            <div className="text-center">
              <div className="text-lg font-black text-indigo-600">{group.locationScore}</div>
              <div className="text-xs text-gray-400">Location</div>
            </div>
          )}
          <ChevronRight size={20} className="text-gray-300 group-hover:text-blue-600 group-hover:translate-x-1 transition-all" />
        </div>
      </div>
    </div>
  );
}
