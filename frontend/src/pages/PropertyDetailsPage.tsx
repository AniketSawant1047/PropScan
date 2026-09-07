import { useState, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import {
  ArrowLeft, MapPin, ExternalLink, Bookmark, BookmarkCheck, Share2,
  CheckCircle2, AlertTriangle, TrendingDown, Star, Building2,
  Car, Dumbbell, Waves, Shield, Zap, Home, BarChart3, MapIcon, Info
} from 'lucide-react';
import { useStore } from '../store';
import { propertyApi } from '../services/api';
import type { PropertyGroup, LocationIntelligence, PriceAnalysis } from '../types';
import ScoreBadge from '../components/property/ScoreBadge';
import SourceBadge from '../components/property/SourceBadge';

function ScoreBar({ label, score, color = 'bg-blue-500' }: { label: string; score: number; color?: string }) {
  return (
    <div className="flex items-center gap-3">
      <span className="text-sm text-gray-600 w-36 flex-shrink-0">{label}</span>
      <div className="flex-1 score-bar">
        <div
          className={`score-bar-fill ${color}`}
          style={{ width: `${score}%` }}
        />
      </div>
      <span className="text-sm font-bold text-gray-800 w-8 text-right">{score}</span>
    </div>
  );
}

export default function PropertyDetailsPage() {
  const navigate = useNavigate();
  const location = useLocation();

  const { toggleSaved, isSaved } = useStore();

  const group: PropertyGroup = location.state?.group;

  const [locationData, setLocationData] = useState<LocationIntelligence | null>(null);
  const [priceData, setPriceData] = useState<PriceAnalysis | null>(null);
  const [activeTab, setActiveTab] = useState<'overview' | 'sources' | 'location' | 'price' | 'ai'>('overview');
  const [imageIdx, setImageIdx] = useState(0);
  const [loadingLocation, setLoadingLocation] = useState(false);
  const [loadingPrice, setLoadingPrice] = useState(false);

  useEffect(() => {
    if (!group) return;

    // Load location data
    if (activeTab === 'location' && !locationData) {
      setLoadingLocation(true);
      propertyApi.getLocationIntelligence(
        group.groupId,
        group.canonicalLatitude,
        group.canonicalLongitude,
        group.canonicalLocality || ''
      ).then(data => {
        setLocationData(data);
        setLoadingLocation(false);
      }).catch(() => setLoadingLocation(false));
    }

    // Load price data
    if (activeTab === 'price' && !priceData) {
      setLoadingPrice(true);
      propertyApi.getPriceAnalysis(
        group.groupId,
        group.bestPriceInr,
        group.canonicalAreaSqft,
        group.canonicalLocality || ''
      ).then(data => {
        setPriceData(data);
        setLoadingPrice(false);
      }).catch(() => setLoadingPrice(false));
    }
  }, [activeTab, group]);

  if (!group) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <Building2 size={48} className="text-gray-200 mx-auto mb-4" />
          <h2 className="font-bold text-gray-700 mb-2">Property not found</h2>
          <button onClick={() => navigate('/results')} className="btn-primary text-sm">
            Back to Results
          </button>
        </div>
      </div>
    );
  }

  const images = group.images?.length ? group.images : ['https://images.unsplash.com/photo-1545324418-cc1a3fa10c00?w=800'];
  const bestListing = group.bestListing || group.listings?.[0];
  const hasSaving = group.potentialSavingInr && group.potentialSavingInr > 0 && group.sourceCount > 1;

  const tabs = [
    { id: 'overview', label: 'Overview', icon: <Home size={14} /> },
    { id: 'sources', label: `Sources (${group.sourceCount})`, icon: <BarChart3 size={14} /> },
    { id: 'location', label: 'Location IQ', icon: <MapIcon size={14} /> },
    { id: 'price', label: 'Price Analysis', icon: <TrendingDown size={14} /> },
    { id: 'ai', label: 'AI Analysis', icon: <Star size={14} /> },
  ];

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white border-b border-gray-100 shadow-sm sticky top-16 z-40">
        <div className="max-w-6xl mx-auto px-4 py-3 flex items-center gap-3">
          <button onClick={() => navigate(-1)} className="p-2 hover:bg-gray-100 rounded-xl transition-colors">
            <ArrowLeft size={18} />
          </button>
          <div className="flex-1 min-w-0">
            <h1 className="font-bold text-gray-900 text-sm truncate">{group.canonicalTitle}</h1>
            <p className="text-xs text-gray-500 flex items-center gap-1">
              <MapPin size={11} /> {group.canonicalLocality}, {group.canonicalCity}
            </p>
          </div>
          <div className="flex items-center gap-2">
            <button
              onClick={() => toggleSaved(group.groupId)}
              className="p-2 hover:bg-gray-100 rounded-xl"
            >
              {isSaved(group.groupId)
                ? <BookmarkCheck size={18} className="text-blue-600" />
                : <Bookmark size={18} className="text-gray-400" />}
            </button>
            <button className="p-2 hover:bg-gray-100 rounded-xl">
              <Share2 size={18} className="text-gray-400" />
            </button>
          </div>
        </div>
      </div>

      <div className="max-w-6xl mx-auto px-4 py-6">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Left: Main Content */}
          <div className="lg:col-span-2 space-y-5">
            {/* Image Gallery */}
            <div className="card overflow-hidden">
              <div className="relative h-72 sm:h-96 bg-gray-100">
                <img
                  src={images[imageIdx]}
                  alt={group.canonicalTitle}
                  className="w-full h-full object-cover"
                  onError={(e) => {
                    (e.target as HTMLImageElement).src = 'https://images.unsplash.com/photo-1545324418-cc1a3fa10c00?w=800';
                  }}
                />
                {images.length > 1 && (
                  <div className="absolute bottom-3 left-0 right-0 flex justify-center gap-1.5">
                    {images.map((_, i) => (
                      <button
                        key={i}
                        onClick={() => setImageIdx(i)}
                        className={`w-2 h-2 rounded-full transition-colors ${i === imageIdx ? 'bg-white' : 'bg-white/40'}`}
                      />
                    ))}
                  </div>
                )}
                {/* AI Score overlay */}
                {group.aiScore && (
                  <div className="absolute top-3 right-3 bg-white/95 backdrop-blur-sm rounded-2xl px-3 py-2 shadow-lg">
                    <div className="text-center">
                      <div className="text-2xl font-black text-blue-700">{group.aiScore.overallScore}</div>
                      <div className="text-xs font-bold text-gray-500">AI Score</div>
                      <div className={`text-xs font-semibold ${
                        group.aiScore.grade === 'Excellent' ? 'text-green-600' :
                        group.aiScore.grade === 'Good' ? 'text-blue-600' : 'text-amber-600'
                      }`}>{group.aiScore.grade}</div>
                    </div>
                  </div>
                )}
              </div>
              {images.length > 1 && (
                <div className="flex gap-2 p-3 overflow-x-auto scrollbar-hide">
                  {images.map((img, i) => (
                    <button
                      key={i}
                      onClick={() => setImageIdx(i)}
                      className={`flex-shrink-0 w-16 h-12 rounded-lg overflow-hidden transition-all ${i === imageIdx ? 'ring-2 ring-blue-500' : 'opacity-60 hover:opacity-100'}`}
                    >
                      <img src={img} alt="" className="w-full h-full object-cover"
                        onError={(e) => { (e.target as HTMLImageElement).src = 'https://images.unsplash.com/photo-1545324418-cc1a3fa10c00?w=200'; }} />
                    </button>
                  ))}
                </div>
              )}
            </div>

            {/* Tabs */}
            <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
              <div className="flex overflow-x-auto scrollbar-hide border-b border-gray-100">
                {tabs.map(tab => (
                  <button
                    key={tab.id}
                    onClick={() => setActiveTab(tab.id as any)}
                    className={`flex items-center gap-1.5 px-4 py-3.5 text-sm font-semibold whitespace-nowrap border-b-2 transition-colors ${
                      activeTab === tab.id
                        ? 'border-blue-600 text-blue-600 bg-blue-50/50'
                        : 'border-transparent text-gray-500 hover:text-gray-700 hover:bg-gray-50'
                    }`}
                  >
                    {tab.icon}
                    {tab.label}
                  </button>
                ))}
              </div>

              <div className="p-5">
                {/* OVERVIEW TAB */}
                {activeTab === 'overview' && (
                  <div className="space-y-5 animate-fade-in">
                    {/* Details grid */}
                    <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
                      {[
                        { label: 'BHK', value: `${group.canonicalBhk} BHK` },
                        { label: 'Area', value: group.areaFormatted || 'N/A' },
                        { label: 'Floor', value: bestListing?.floor || 'N/A' },
                        { label: 'Furnishing', value: bestListing?.furnishing || 'N/A' },
                        { label: 'Type', value: bestListing?.propertyType || 'Apartment' },
                        { label: 'Facing', value: bestListing?.facing || 'N/A' },
                        { label: 'Possession', value: bestListing?.possession || 'N/A' },
                        { label: 'Project', value: group.canonicalProject || 'N/A' },
                      ].map(d => (
                        <div key={d.label} className="bg-gray-50 rounded-xl p-3">
                          <div className="text-xs text-gray-500 font-medium mb-1">{d.label}</div>
                          <div className="text-sm font-bold text-gray-900">{d.value}</div>
                        </div>
                      ))}
                    </div>

                    {/* Amenities */}
                    <div>
                      <h3 className="section-title mb-3">Amenities</h3>
                      <div className="flex flex-wrap gap-2">
                        {bestListing?.parking && (
                          <span className="badge-green"><Car size={11} /> Parking</span>
                        )}
                        {bestListing?.gym && (
                          <span className="badge-blue"><Dumbbell size={11} /> Gym</span>
                        )}
                        {bestListing?.swimmingPool && (
                          <span className="badge-blue"><Waves size={11} /> Pool</span>
                        )}
                        {bestListing?.security && (
                          <span className="badge-blue"><Shield size={11} /> Security</span>
                        )}
                        {bestListing?.lift && (
                          <span className="badge-blue"><Zap size={11} /> Lift</span>
                        )}
                        {bestListing?.amenities?.map(a => (
                          <span key={a} className="badge-purple">{a}</span>
                        ))}
                      </div>
                    </div>

                    {/* Description */}
                    {bestListing?.description && (
                      <div>
                        <h3 className="section-title mb-2">About</h3>
                        <p className="text-sm text-gray-600 leading-relaxed">{bestListing.description}</p>
                      </div>
                    )}
                  </div>
                )}

                {/* SOURCES TAB */}
                {activeTab === 'sources' && (
                  <div className="animate-fade-in">
                    {hasSaving && (
                      <div className="bg-green-50 border border-green-200 rounded-xl p-4 mb-4 flex items-center gap-3">
                        <TrendingDown size={20} className="text-green-600 flex-shrink-0" />
                        <div>
                          <p className="font-bold text-green-800">Best Price Available!</p>
                          <p className="text-sm text-green-700">
                            Save {group.potentialSavingFormatted} by choosing {group.bestPriceSource}
                          </p>
                        </div>
                      </div>
                    )}

                    {group.sourceCount > 1 && (
                      <div className="bg-blue-50 border border-blue-100 rounded-xl p-3 mb-4">
                        <p className="text-sm text-blue-700">
                          <strong>Same Property Found Across {group.sourceCount} Sources</strong>
                          {group.matchConfidence && ` — ${Math.round(group.matchConfidence)}% match confidence`}
                        </p>
                      </div>
                    )}

                    <div className="space-y-3">
                      {group.listings.map((listing) => {
                        const isBest = listing.priceInr === group.bestPriceInr;
                        return (
                          <div
                            key={listing.externalId}
                            className={`flex items-center gap-4 p-4 rounded-xl border transition-all ${
                              isBest
                                ? 'border-green-300 bg-green-50 best-deal-ring'
                                : 'border-gray-100 hover:border-gray-200'
                            }`}
                          >
                            <SourceBadge source={listing.source} />
                            <div className="flex-1">
                              <div className="font-bold text-gray-900">
                                {listing.priceFormatted || 'N/A'}
                                {isBest && (
                                  <span className="ml-2 text-xs bg-green-500 text-white px-2 py-0.5 rounded-full font-bold">
                                    BEST PRICE
                                  </span>
                                )}
                              </div>
                              <div className="text-xs text-gray-500">
                                {listing.areaSqft && `${listing.areaSqft.toFixed(0)} sq.ft.`}
                                {listing.pricePerSqft && ` • ₹${listing.pricePerSqft.toFixed(0)}/sq.ft.`}
                              </div>
                            </div>
                            <a
                              href={listing.listingUrl || '#'}
                              target="_blank"
                              rel="noopener noreferrer"
                              onClick={(e) => e.stopPropagation()}
                              className="flex items-center gap-1.5 px-3 py-1.5 bg-blue-600 text-white text-xs font-semibold rounded-lg hover:bg-blue-700 transition-colors"
                            >
                              View <ExternalLink size={11} />
                            </a>
                          </div>
                        );
                      })}
                    </div>
                  </div>
                )}

                {/* LOCATION TAB */}
                {activeTab === 'location' && (
                  <div className="animate-fade-in">
                    {loadingLocation ? (
                      <div className="space-y-3">
                        {Array.from({ length: 4 }).map((_, i) => (
                          <div key={i} className="h-10 bg-gray-100 rounded-xl skeleton" />
                        ))}
                      </div>
                    ) : locationData ? (
                      <div className="space-y-5">
                        {/* Location score overview */}
                        <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
                          {[
                            { label: 'Overall', score: locationData.locationScore, color: 'bg-blue-500' },
                            { label: 'Connectivity', score: locationData.connectivityScore, color: 'bg-indigo-500' },
                            { label: 'Transport', score: locationData.publicTransportScore, color: 'bg-purple-500' },
                            { label: 'Offices', score: locationData.officeAccessScore, color: 'bg-green-500' },
                          ].map(item => (
                            <div key={item.label} className="bg-gray-50 rounded-xl p-3 text-center">
                              <div className={`text-2xl font-black ${item.label === 'Overall' ? 'text-blue-700' : 'text-gray-800'}`}>
                                {item.score}
                              </div>
                              <div className="text-xs text-gray-500 font-medium">{item.label}</div>
                              <div className="mt-1.5 score-bar">
                                <div className={`score-bar-fill ${item.color}`} style={{ width: `${item.score}%` }} />
                              </div>
                            </div>
                          ))}
                        </div>

                        {/* Score breakdown */}
                        <div>
                          <h3 className="section-title mb-3">Location Score Breakdown</h3>
                          <div className="space-y-2">
                            <ScoreBar label="Connectivity" score={locationData.connectivityScore} color="bg-blue-500" />
                            <ScoreBar label="Public Transport" score={locationData.publicTransportScore} color="bg-purple-500" />
                            <ScoreBar label="Schools" score={locationData.schoolsScore} color="bg-amber-500" />
                            <ScoreBar label="Hospitals" score={locationData.hospitalsScore} color="bg-red-400" />
                            <ScoreBar label="Shopping" score={locationData.shoppingScore} color="bg-pink-500" />
                            <ScoreBar label="Office Access" score={locationData.officeAccessScore} color="bg-green-500" />
                            <ScoreBar label="Lifestyle" score={locationData.lifestyleScore} color="bg-indigo-500" />
                          </div>
                        </div>

                        {/* Nearby Places */}
                        <div>
                          <h3 className="section-title mb-3">Nearby Places</h3>
                          <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
                            {locationData.nearbyPlaces.map((place, i) => (
                              <div key={i} className="flex items-center gap-3 p-3 bg-gray-50 rounded-xl">
                                <div className={`w-8 h-8 rounded-lg flex items-center justify-center text-white text-xs font-bold ${
                                  place.category === 'metro' ? 'bg-blue-500' :
                                  place.category === 'hospital' ? 'bg-red-500' :
                                  place.category === 'school' ? 'bg-amber-500' :
                                  place.category === 'shopping' ? 'bg-pink-500' :
                                  place.category === 'office' ? 'bg-green-500' : 'bg-purple-500'
                                }`}>
                                  {place.category.charAt(0).toUpperCase()}
                                </div>
                                <div className="flex-1 min-w-0">
                                  <div className="text-sm font-semibold text-gray-800 truncate">{place.name}</div>
                                  <div className="text-xs text-gray-400">
                                    {place.distanceKm && `${place.distanceKm.toFixed(1)} km`}
                                    {place.driveMinutes && ` • ${place.driveMinutes} min drive`}
                                  </div>
                                </div>
                              </div>
                            ))}
                          </div>
                        </div>

                        {/* Commute */}
                        {locationData.commutes.length > 0 && (
                          <div>
                            <h3 className="section-title mb-3">Commute Estimates</h3>
                            <div className="space-y-2">
                              {locationData.commutes.map((c, i) => (
                                <div key={i} className="flex items-center justify-between p-3 bg-gray-50 rounded-xl">
                                  <div className="flex items-center gap-2">
                                    <MapPin size={14} className="text-gray-400" />
                                    <div>
                                      <div className="text-sm font-semibold text-gray-800">{c.destination}</div>
                                      {c.route && <div className="text-xs text-gray-400">{c.route}</div>}
                                    </div>
                                  </div>
                                  <div className="text-right">
                                    <div className="text-lg font-black text-blue-700">{c.driveMinutes} min</div>
                                    <div className="text-xs text-gray-400">{c.distanceKm?.toFixed(1)} km</div>
                                  </div>
                                </div>
                              ))}
                            </div>
                          </div>
                        )}

                        {locationData.locationSummary && (
                          <div className="bg-blue-50 border border-blue-100 rounded-xl p-4">
                            <p className="text-sm text-blue-800">{locationData.locationSummary}</p>
                          </div>
                        )}
                      </div>
                    ) : (
                      <p className="text-gray-500 text-sm">Location data unavailable</p>
                    )}
                  </div>
                )}

                {/* PRICE ANALYSIS TAB */}
                {activeTab === 'price' && (
                  <div className="animate-fade-in">
                    {loadingPrice ? (
                      <div className="space-y-3">
                        {Array.from({ length: 4 }).map((_, i) => (
                          <div key={i} className="h-12 bg-gray-100 rounded-xl skeleton" />
                        ))}
                      </div>
                    ) : priceData ? (
                      <div className="space-y-5">
                        {/* Price comparison grid */}
                        <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
                          <div className="bg-blue-50 rounded-xl p-3 text-center">
                            <div className="text-xs text-gray-500 mb-1">Property Price</div>
                            <div className="text-lg font-black text-blue-700">{priceData.propertyPriceFormatted}</div>
                          </div>
                          <div className="bg-gray-50 rounded-xl p-3 text-center">
                            <div className="text-xs text-gray-500 mb-1">Comparable Avg</div>
                            <div className="text-lg font-black text-gray-700">{priceData.comparableAverageFormatted}</div>
                          </div>
                          <div className={`rounded-xl p-3 text-center ${
                            priceData.priceTrend === 'Below Average' ? 'bg-green-50' :
                            priceData.priceTrend === 'Above Average' ? 'bg-red-50' : 'bg-amber-50'
                          }`}>
                            <div className="text-xs text-gray-500 mb-1">vs Market</div>
                            <div className={`text-lg font-black ${
                              priceData.priceTrend === 'Below Average' ? 'text-green-700' :
                              priceData.priceTrend === 'Above Average' ? 'text-red-700' : 'text-amber-700'
                            }`}>
                              {priceData.differencePercentage !== undefined && priceData.differencePercentage !== null
                                ? `${priceData.differencePercentage > 0 ? '+' : ''}${priceData.differencePercentage?.toFixed(1)}%`
                                : 'N/A'}
                            </div>
                            <div className={`text-xs font-bold ${
                              priceData.priceTrend === 'Below Average' ? 'text-green-600' :
                              priceData.priceTrend === 'Above Average' ? 'text-red-600' : 'text-amber-600'
                            }`}>{priceData.priceTrend}</div>
                          </div>
                          <div className="bg-purple-50 rounded-xl p-3 text-center">
                            <div className="text-xs text-gray-500 mb-1">Price/sq.ft.</div>
                            <div className="text-sm font-black text-purple-700">{priceData.propertyPricePerSqftFormatted}</div>
                            <div className="text-xs text-gray-400">Locality: {priceData.localityAveragePricePerSqftFormatted}</div>
                          </div>
                        </div>

                        {/* Investment */}
                        <div>
                          <h3 className="section-title mb-3">Investment Indicators</h3>
                          <div className="grid grid-cols-2 gap-3">
                            <div className="bg-gray-50 rounded-xl p-3">
                              <div className="text-xs text-gray-500 mb-1">Investment Score</div>
                              <div className="text-2xl font-black text-blue-700">{priceData.investmentScore}/100</div>
                            </div>
                            <div className="bg-gray-50 rounded-xl p-3">
                              <div className="text-xs text-gray-500 mb-1">Rental Yield</div>
                              <div className="text-2xl font-black text-green-600">{priceData.estimatedRentalYield}</div>
                            </div>
                            <div className="bg-gray-50 rounded-xl p-3">
                              <div className="text-xs text-gray-500 mb-1">Demand</div>
                              <div className="text-base font-bold text-gray-800">{priceData.demandIndicator}</div>
                            </div>
                            {priceData.estimatedMonthlyRent && (
                              <div className="bg-gray-50 rounded-xl p-3">
                                <div className="text-xs text-gray-500 mb-1">Est. Monthly Rent</div>
                                <div className="text-base font-bold text-gray-800">
                                  ₹{priceData.estimatedMonthlyRent.toLocaleString()}
                                </div>
                              </div>
                            )}
                          </div>
                        </div>

                        {/* Disclaimer */}
                        <div className="flex items-start gap-2 bg-amber-50 border border-amber-100 rounded-xl p-3">
                          <Info size={14} className="text-amber-600 flex-shrink-0 mt-0.5" />
                          <p className="text-xs text-amber-800">{priceData.disclaimer}</p>
                        </div>
                      </div>
                    ) : (
                      <p className="text-gray-500 text-sm">Price analysis unavailable</p>
                    )}
                  </div>
                )}

                {/* AI ANALYSIS TAB */}
                {activeTab === 'ai' && group.aiScore && (
                  <div className="animate-fade-in space-y-5">
                    {/* Score breakdown */}
                    <div>
                      <h3 className="section-title mb-3">AI Score Breakdown</h3>
                      <div className="space-y-2.5">
                        <ScoreBar label="Price Value" score={group.aiScore.priceValue} color="bg-green-500" />
                        <ScoreBar label="Location" score={group.aiScore.location} color="bg-blue-500" />
                        <ScoreBar label="Connectivity" score={group.aiScore.connectivity} color="bg-indigo-500" />
                        <ScoreBar label="Property Size" score={group.aiScore.propertySize} color="bg-purple-500" />
                        <ScoreBar label="Amenities" score={group.aiScore.amenities} color="bg-amber-500" />
                        <ScoreBar label="Your Match" score={group.aiScore.userMatch} color="bg-green-600" />
                      </div>
                    </div>

                    {/* Positives */}
                    {group.aiScore.positives && group.aiScore.positives.length > 0 && (
                      <div>
                        <h3 className="section-title mb-3 text-green-700">✓ Why This Property</h3>
                        <div className="space-y-2">
                          {group.aiScore.positives.map((p, i) => (
                            <div key={i} className="flex items-start gap-2">
                              <CheckCircle2 size={15} className="text-green-500 flex-shrink-0 mt-0.5" />
                              <span className="text-sm text-gray-700">{p}</span>
                            </div>
                          ))}
                        </div>
                      </div>
                    )}

                    {/* Verify points */}
                    {group.aiScore.verifyPoints && group.aiScore.verifyPoints.length > 0 && (
                      <div>
                        <h3 className="section-title mb-3 text-amber-700">⚠ Verify Before Buying</h3>
                        <div className="space-y-2">
                          {group.aiScore.verifyPoints.map((v, i) => (
                            <div key={i} className="flex items-start gap-2">
                              <AlertTriangle size={15} className="text-amber-500 flex-shrink-0 mt-0.5" />
                              <span className="text-sm text-gray-700">{v}</span>
                            </div>
                          ))}
                        </div>
                      </div>
                    )}

                    <div className="bg-blue-50 border border-blue-100 rounded-xl p-3">
                      <p className="text-xs text-blue-700">
                        {group.aiScore.aiGenerated
                          ? '✦ AI-generated analysis using LLM'
                          : '⚙ Rule-based analysis (AI fallback mode) — scores are deterministic'}
                      </p>
                    </div>
                  </div>
                )}
              </div>
            </div>
          </div>

          {/* Right: Price Card */}
          <div className="space-y-4">
            {/* Best Price Card */}
            <div className="card p-5 sticky top-36">
              <div className="mb-4">
                <div className="text-xs font-bold text-green-700 uppercase tracking-wide mb-1">Best Available Price</div>
                <div className="text-4xl font-black text-gray-900">{group.bestPriceFormatted}</div>
                <div className="text-sm text-gray-500 mt-1">via {group.bestPriceSource}</div>
                {group.pricePerSqftFormatted && (
                  <div className="text-xs text-gray-400">{group.pricePerSqftFormatted}</div>
                )}
              </div>

              {hasSaving && (
                <div className="bg-green-50 border border-green-200 rounded-xl p-3 mb-4">
                  <div className="text-sm font-bold text-green-700">
                    Potential Saving: {group.potentialSavingFormatted}
                  </div>
                  <div className="text-xs text-green-600">
                    vs. highest listed price on {group.highestPriceSource}
                  </div>
                </div>
              )}

              {/* CTA */}
              <a
                href={bestListing?.listingUrl || '#'}
                target="_blank"
                rel="noopener noreferrer"
                className="w-full btn-primary justify-center text-sm py-3 mb-3 block text-center"
              >
                View Original Listing <ExternalLink size={14} />
              </a>

              <button
                onClick={() => toggleSaved(group.groupId)}
                className="w-full btn-secondary justify-center text-sm py-2.5"
              >
                {isSaved(group.groupId)
                  ? <><BookmarkCheck size={14} className="text-blue-600" /> Saved</>
                  : <><Bookmark size={14} /> Save Property</>}
              </button>

              {/* Score summary */}
              {group.aiScore && (
                <div className="mt-4 pt-4 border-t border-gray-100">
                  <div className="flex items-center justify-between mb-2">
                    <span className="text-xs font-semibold text-gray-500">AI Match Score</span>
                    <ScoreBadge score={group.aiScore.overallScore} grade={group.aiScore.grade} size="sm" />
                  </div>
                  {group.locationScore && (
                    <div className="flex items-center justify-between">
                      <span className="text-xs font-semibold text-gray-500">Location Score</span>
                      <span className="text-sm font-bold text-indigo-600">{group.locationScore}/100</span>
                    </div>
                  )}
                </div>
              )}

              {/* Source comparison mini */}
              {group.sourceCount > 1 && (
                <div className="mt-4 pt-4 border-t border-gray-100">
                  <div className="text-xs font-bold text-gray-500 mb-2">AVAILABLE ON {group.sourceCount} SOURCES</div>
                  <div className="space-y-1.5">
                    {group.listings.map(l => (
                      <div key={l.externalId} className="flex items-center justify-between">
                        <SourceBadge source={l.source} />
                        <span className={`text-sm font-bold ${l.priceInr === group.bestPriceInr ? 'text-green-600' : 'text-gray-700'}`}>
                          {l.priceFormatted}
                        </span>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
