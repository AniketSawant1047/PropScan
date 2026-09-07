import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Search, MapPin, Home, DollarSign, Sparkles, ChevronDown,
  ArrowRight, Shield, Zap, TrendingDown, Star, CheckCircle2
} from 'lucide-react';
import { useStore } from '../store';
import { propertyApi } from '../services/api';

const PUNE_LOCALITIES = [
  'Wakad', 'Hinjewadi', 'Baner', 'Kharadi', 'Viman Nagar',
  'Kothrud', 'Aundh', 'Hadapsar', 'Balewadi', 'Pimple Saudagar'
];

const SOURCE_LOGOS = [
  { name: '99acres', color: '#E65C2D', abbr: '99A' },
  { name: 'MagicBricks', color: '#E8272A', abbr: 'MB' },
  { name: 'Housing.com', color: '#FF4F00', abbr: 'H' },
  { name: 'NoBroker', color: '#2DB94D', abbr: 'NB' },
  { name: 'Builders', color: '#3B5BDB', abbr: 'BD' },
  { name: 'Local', color: '#7950F2', abbr: 'LC' },
];

const DEMO_QUERIES = [
  "2 BHK in Pune under ₹80 lakh near Hinjewadi with parking",
  "3 BHK apartment in Baner under ₹1.5 crore with gym and pool",
  "1 BHK in Hinjewadi under ₹45 lakh ready to move",
  "2 BHK in Wakad under ₹85 lakh with good connectivity",
];

export default function HomePage() {
  const navigate = useNavigate();
  const { setSearchRequest, setSearchResponse, setIsSearching, setSearchError } = useStore();

  const [nlQuery, setNlQuery] = useState('');
  const [locality, setLocality] = useState('');
  const [bhk, setBhk] = useState('');
  const [maxBudget, setMaxBudget] = useState('');
  const [propertyType, setPropertyType] = useState('Apartment');
  const [showAdvanced, setShowAdvanced] = useState(false);
  const [parking, setParking] = useState(false);

  const handleSearch = async () => {
    const request: any = { city: 'Pune', sortBy: 'score_desc', page: 0, size: 20 };

    if (nlQuery.trim()) {
      request.naturalLanguageQuery = nlQuery.trim();
    } else {
      if (locality) request.locality = locality;
      if (bhk) request.bhk = parseInt(bhk);
      if (maxBudget) request.maxBudget = parseFloat(maxBudget) * 100000;
      if (propertyType) request.propertyType = propertyType;
      if (parking) request.parkingRequired = true;
    }

    setSearchRequest(request);
    setIsSearching(true);
    setSearchError(null);

    navigate('/results');

    try {
      const response = await propertyApi.search(request);
      setSearchResponse(response);
    } catch (err: any) {
      setSearchError(err.message || 'Search failed. Please try again.');
    } finally {
      setIsSearching(false);
    }
  };

  const handleDemoQuery = (query: string) => {
    setNlQuery(query);
  };

  return (
    <div className="min-h-screen bg-gradient-to-b from-slate-50 via-blue-50/30 to-white">
      {/* Hero Section */}
      <section className="relative overflow-hidden">
        {/* Background decoration */}
        <div className="absolute inset-0 pointer-events-none">
          <div className="absolute top-20 left-10 w-72 h-72 bg-blue-100/50 rounded-full blur-3xl" />
          <div className="absolute top-40 right-10 w-96 h-96 bg-indigo-100/40 rounded-full blur-3xl" />
          <div className="absolute -bottom-20 left-1/2 w-80 h-80 bg-purple-100/30 rounded-full blur-3xl" />
        </div>

        <div className="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 pt-16 pb-8">
          {/* Badge */}
          <div className="flex justify-center mb-6">
            <span className="badge-blue text-sm gap-2 px-3 py-1.5">
              <Sparkles size={14} />
              AI-Powered Property Metasearch
            </span>
          </div>

          {/* Hero Text */}
          <div className="text-center mb-10">
            <h1 className="text-5xl sm:text-6xl lg:text-7xl font-black text-gray-900 mb-5 leading-tight">
              Find the property
              <br />
              <span className="gradient-text">actually worth buying.</span>
            </h1>
            <p className="text-xl text-gray-500 max-w-2xl mx-auto leading-relaxed">
              Search once. Compare <strong className="text-gray-700">6 property platforms</strong>.
              Let AI find the best match and the best price — for you.
            </p>
          </div>

          {/* Search Card */}
          <div className="max-w-4xl mx-auto">
            <div className="card p-6 shadow-xl border-gray-100">
              {/* Natural Language Search */}
              <div className="mb-4">
                <label className="block text-sm font-semibold text-gray-700 mb-2 flex items-center gap-1.5">
                  <Sparkles size={14} className="text-blue-500" />
                  Describe what you need (AI-powered)
                </label>
                <div className="relative">
                  <textarea
                    rows={2}
                    value={nlQuery}
                    onChange={(e) => setNlQuery(e.target.value)}
                    placeholder='e.g. "2 BHK in Pune under ₹80 lakh near Hinjewadi with parking and commute below 45 minutes"'
                    className="input-field resize-none pr-12 text-sm"
                  />
                  <Sparkles size={18} className="absolute right-3 top-3 text-blue-400 pointer-events-none" />
                </div>
                {/* Demo queries */}
                <div className="flex flex-wrap gap-1.5 mt-2">
                  {DEMO_QUERIES.slice(0, 2).map((q) => (
                    <button
                      key={q}
                      onClick={() => handleDemoQuery(q)}
                      className="text-xs px-2.5 py-1 bg-blue-50 text-blue-600 rounded-full hover:bg-blue-100 transition-colors border border-blue-100"
                    >
                      {q.length > 45 ? q.substring(0, 45) + '…' : q}
                    </button>
                  ))}
                </div>
              </div>

              {/* Divider */}
              <div className="flex items-center gap-3 my-4">
                <div className="flex-1 h-px bg-gray-100" />
                <span className="text-xs text-gray-400 font-medium">OR SEARCH WITH FILTERS</span>
                <div className="flex-1 h-px bg-gray-100" />
              </div>

              {/* Structured Filters */}
              <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 mb-4">
                {/* Location */}
                <div className="col-span-2 sm:col-span-1">
                  <label className="block text-xs font-semibold text-gray-500 mb-1.5 flex items-center gap-1">
                    <MapPin size={11} /> Location
                  </label>
                  <select
                    value={locality}
                    onChange={(e) => setLocality(e.target.value)}
                    className="input-field text-sm py-2.5"
                  >
                    <option value="">Any locality</option>
                    {PUNE_LOCALITIES.map(l => (
                      <option key={l} value={l}>{l}, Pune</option>
                    ))}
                  </select>
                </div>

                {/* Property Type */}
                <div>
                  <label className="block text-xs font-semibold text-gray-500 mb-1.5 flex items-center gap-1">
                    <Home size={11} /> Type
                  </label>
                  <select
                    value={propertyType}
                    onChange={(e) => setPropertyType(e.target.value)}
                    className="input-field text-sm py-2.5"
                  >
                    <option value="Apartment">Apartment</option>
                    <option value="Villa">Villa</option>
                    <option value="Plot">Plot</option>
                    <option value="">Any</option>
                  </select>
                </div>

                {/* BHK */}
                <div>
                  <label className="block text-xs font-semibold text-gray-500 mb-1.5">BHK</label>
                  <select
                    value={bhk}
                    onChange={(e) => setBhk(e.target.value)}
                    className="input-field text-sm py-2.5"
                  >
                    <option value="">Any</option>
                    <option value="1">1 BHK</option>
                    <option value="2">2 BHK</option>
                    <option value="3">3 BHK</option>
                    <option value="4">4 BHK+</option>
                  </select>
                </div>

                {/* Budget */}
                <div>
                  <label className="block text-xs font-semibold text-gray-500 mb-1.5 flex items-center gap-1">
                    <DollarSign size={11} /> Max Budget
                  </label>
                  <select
                    value={maxBudget}
                    onChange={(e) => setMaxBudget(e.target.value)}
                    className="input-field text-sm py-2.5"
                  >
                    <option value="">Any</option>
                    <option value="40">₹40L</option>
                    <option value="60">₹60L</option>
                    <option value="80">₹80L</option>
                    <option value="100">₹1 Cr</option>
                    <option value="150">₹1.5 Cr</option>
                    <option value="200">₹2 Cr</option>
                  </select>
                </div>
              </div>

              {/* Advanced toggle */}
              <button
                onClick={() => setShowAdvanced(!showAdvanced)}
                className="flex items-center gap-1 text-sm text-blue-600 hover:text-blue-700 mb-3 font-medium"
              >
                <ChevronDown size={14} className={`transition-transform ${showAdvanced ? 'rotate-180' : ''}`} />
                More preferences
              </button>

              {showAdvanced && (
                <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 mb-4 animate-slide-up">
                  <label className="flex items-center gap-2 cursor-pointer">
                    <input
                      type="checkbox"
                      checked={parking}
                      onChange={(e) => setParking(e.target.checked)}
                      className="w-4 h-4 rounded text-blue-600"
                    />
                    <span className="text-sm text-gray-700 font-medium">Parking</span>
                  </label>
                  <div className="col-span-3">
                    <input
                      type="text"
                      placeholder="Work location (for commute estimate)"
                      className="input-field text-sm py-2.5"
                    />
                  </div>
                </div>
              )}

              {/* CTA */}
              <button
                onClick={handleSearch}
                className="w-full btn-primary justify-center text-base py-3.5 rounded-xl"
              >
                <Search size={18} />
                Find My Property
                <ArrowRight size={16} />
              </button>
            </div>

            {/* Source logos */}
            <div className="mt-5 text-center">
              <p className="text-sm text-gray-400 mb-3">Searching across</p>
              <div className="flex flex-wrap items-center justify-center gap-2">
                {SOURCE_LOGOS.map(s => (
                  <span
                    key={s.name}
                    className="inline-flex items-center gap-1.5 px-3 py-1.5 bg-white rounded-lg border border-gray-100 shadow-sm text-sm font-medium text-gray-600 hover:shadow-md transition-shadow"
                  >
                    <span
                      className="w-5 h-5 rounded text-white text-xs font-black flex items-center justify-center"
                      style={{ backgroundColor: s.color }}
                    >
                      {s.abbr.charAt(0)}
                    </span>
                    {s.name}
                  </span>
                ))}
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
        <div className="text-center mb-12">
          <h2 className="text-3xl font-bold text-gray-900 mb-3">Why PropScan?</h2>
          <p className="text-gray-500 text-lg">The smarter way to find your perfect property</p>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
          {[
            {
              icon: <Search size={24} className="text-blue-600" />,
              bg: 'bg-blue-50',
              title: 'Search Everywhere',
              desc: 'Compare listings from 99acres, MagicBricks, Housing, NoBroker, Builders, and local sources — all at once.',
            },
            {
              icon: <Sparkles size={24} className="text-purple-600" />,
              bg: 'bg-purple-50',
              title: 'AI-Powered Scoring',
              desc: 'Get an AI match score for every property based on price, location, size, amenities, and your preferences.',
            },
            {
              icon: <TrendingDown size={24} className="text-green-600" />,
              bg: 'bg-green-50',
              title: 'Best Deal Finder',
              desc: 'Same property listed on 4 sources? We find the cheapest one and show you the potential savings.',
            },
            {
              icon: <MapPin size={24} className="text-amber-600" />,
              bg: 'bg-amber-50',
              title: 'Location Intelligence',
              desc: 'See nearby metros, hospitals, schools, and IT parks. Get commute estimates to your office.',
            },
            {
              icon: <Shield size={24} className="text-blue-600" />,
              bg: 'bg-blue-50',
              title: 'Transparent Analysis',
              desc: 'Honest price analysis comparing your property to locality averages. Clear buy/wait recommendations.',
            },
            {
              icon: <Zap size={24} className="text-indigo-600" />,
              bg: 'bg-indigo-50',
              title: 'AI Assistant',
              desc: '"Show me cheaper options", "Which is closest to my office?" — chat naturally to refine results.',
            },
          ].map((f) => (
            <div key={f.title} className="card p-6 hover:shadow-md transition-shadow group">
              <div className={`w-12 h-12 ${f.bg} rounded-xl flex items-center justify-center mb-4 group-hover:scale-110 transition-transform`}>
                {f.icon}
              </div>
              <h3 className="font-bold text-gray-900 mb-2">{f.title}</h3>
              <p className="text-sm text-gray-500 leading-relaxed">{f.desc}</p>
            </div>
          ))}
        </div>
      </section>

      {/* How it works */}
      <section className="bg-gradient-to-br from-blue-600 to-indigo-700 py-16">
        <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
          <h2 className="text-3xl font-bold text-white mb-3">How PropScan Works</h2>
          <p className="text-blue-200 mb-10 text-lg">From search to best deal in seconds</p>

          <div className="grid grid-cols-2 sm:grid-cols-4 gap-6">
            {[
              { step: '1', label: 'You Search Once', icon: <Search size={20} /> },
              { step: '2', label: 'AI Searches 6 Sources', icon: <Sparkles size={20} /> },
              { step: '3', label: 'Groups & Compares', icon: <TrendingDown size={20} /> },
              { step: '4', label: 'Finds Best Deal', icon: <Star size={20} /> },
            ].map((s) => (
              <div key={s.step} className="flex flex-col items-center gap-3">
                <div className="w-12 h-12 bg-white/20 rounded-2xl flex items-center justify-center text-white">
                  {s.icon}
                </div>
                <div className="text-xs font-bold text-blue-300">STEP {s.step}</div>
                <div className="text-sm font-semibold text-white">{s.label}</div>
              </div>
            ))}
          </div>

          <button
            onClick={handleSearch}
            className="mt-10 inline-flex items-center gap-2 px-8 py-4 bg-white text-blue-600 font-bold rounded-2xl hover:shadow-xl transition-all hover:-translate-y-0.5 text-lg"
          >
            <Search size={20} />
            Start Searching Free
            <ArrowRight size={18} />
          </button>
        </div>
      </section>

      {/* Footer */}
      <footer className="border-t border-gray-100 py-8">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex flex-col sm:flex-row justify-between items-center gap-4">
          <div className="flex items-center gap-2">
            <img src="/logo.png" alt="PropScan Logo" className="w-8 h-8 object-contain" />
            <span className="font-black text-gray-900 text-xl tracking-tight leading-none">Prop</span>
            <span className="font-black text-green-600 text-xl tracking-tight leading-none -ml-1">Scan</span>
          </div>
          <p className="text-sm text-gray-400 text-center sm:text-left">
            Scan properties. Compare deals. Decide smarter.
          </p>
          <div className="flex items-center gap-1 text-sm text-gray-400">
            <CheckCircle2 size={14} className="text-green-500" />
            No fake integrations — mock data only
          </div>
        </div>
      </footer>
    </div>
  );
}
