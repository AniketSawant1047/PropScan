// TypeScript types for PROPScan

export interface PropertyListing {
  id?: number;
  externalId: string;
  source: string;
  sourceDisplayName: string;
  title: string;
  description?: string;
  priceInr?: number;
  priceFormatted?: string;
  rawPrice?: string;
  pricePerSqft?: number;
  areaSqft?: number;
  areaFormatted?: string;
  bhk?: number;
  propertyType?: string;
  locality?: string;
  city?: string;
  address?: string;
  project?: string;
  developer?: string;
  floor?: string;
  totalFloors?: number;
  facing?: string;
  furnishing?: string;
  parking?: boolean;
  lift?: boolean;
  gym?: boolean;
  swimmingPool?: boolean;
  security?: boolean;
  amenities?: string[];
  images?: string[];
  primaryImage?: string;
  listingUrl?: string;
  postedDate?: string;
  readyToMove?: boolean;
  possession?: string;
  agentName?: string;
  agentPhone?: string;
  latitude?: number;
  longitude?: number;
  groupId?: string;
}

export interface AiScore {
  overallScore: number;
  grade: 'Excellent' | 'Good' | 'Fair' | 'Poor';
  priceValue: number;
  location: number;
  connectivity: number;
  propertySize: number;
  amenities: number;
  userMatch: number;
  priceVerdict?: string;
  priceVsLocalAverage?: number;
  parkingAvailable?: boolean;
  gymAvailable?: boolean;
  swimmingPoolAvailable?: boolean;
  summary?: string;
  positives?: string[];
  verifyPoints?: string[];
  aiGenerated: boolean;
}

export interface PropertyGroup {
  id?: number;
  groupId: string;
  canonicalTitle: string;
  canonicalAddress?: string;
  canonicalLocality?: string;
  canonicalCity?: string;
  canonicalProject?: string;
  canonicalDeveloper?: string;
  canonicalBhk?: number;
  canonicalAreaSqft?: number;
  areaFormatted?: string;
  canonicalLatitude?: number;
  canonicalLongitude?: number;
  bestPriceInr?: number;
  bestPriceFormatted?: string;
  bestPriceSource?: string;
  highestPriceInr?: number;
  highestPriceFormatted?: string;
  highestPriceSource?: string;
  potentialSavingInr?: number;
  potentialSavingFormatted?: string;
  savingPercentage?: number;
  bestPricePerSqft?: number;
  pricePerSqftFormatted?: string;
  matchConfidence?: number;
  sourceCount: number;
  sources: string[];
  listings: PropertyListing[];
  bestListing?: PropertyListing;
  images?: string[];
  primaryImage?: string;
  aiScore?: AiScore;
  locationScore?: number;
}

export interface SourceSummary {
  source: string;
  sourceDisplayName: string;
  listingsFound: number;
  available: boolean;
  error?: string;
}

export interface SearchRequest {
  naturalLanguageQuery?: string;
  city?: string;
  locality?: string;
  propertyType?: string;
  bhk?: number;
  maxBudget?: number;
  minBudget?: number;
  parkingRequired?: boolean;
  gymRequired?: boolean;
  swimmingPoolRequired?: boolean;
  maxCommuteMinutes?: number;
  commuteDestination?: string;
  furnishing?: string;
  readyToMove?: boolean;
  sortBy?: string;
  page?: number;
  size?: number;
}

export interface SearchResponse {
  totalListings: number;
  totalGroups: number;
  page: number;
  size: number;
  totalPages: number;
  groups: PropertyGroup[];
  sourceSummaries: SourceSummary[];
  searchTimeMs: number;
  interpretedQuery?: string;
  appliedFilters?: SearchRequest;
}

export interface NearbyPlace {
  name: string;
  category: string;
  subcategory?: string;
  latitude?: number;
  longitude?: number;
  distanceKm?: number;
  walkMinutes?: number;
  driveMinutes?: number;
  icon?: string;
}

export interface Commute {
  destination: string;
  destinationType: string;
  driveMinutes?: number;
  transitMinutes?: number;
  distanceKm?: number;
  route?: string;
}

export interface LocationIntelligence {
  groupId: string;
  locality?: string;
  city?: string;
  latitude?: number;
  longitude?: number;
  locationScore: number;
  connectivityScore: number;
  publicTransportScore: number;
  schoolsScore: number;
  hospitalsScore: number;
  shoppingScore: number;
  officeAccessScore: number;
  lifestyleScore: number;
  nearbyPlaces: NearbyPlace[];
  commutes: Commute[];
  locationSummary?: string;
}

export interface PriceAnalysis {
  groupId: string;
  propertyPrice?: number;
  propertyPriceFormatted?: string;
  localityAveragePricePerSqft?: number;
  localityAveragePricePerSqftFormatted?: string;
  propertyPricePerSqft?: number;
  propertyPricePerSqftFormatted?: string;
  comparableAverage?: number;
  comparableAverageFormatted?: string;
  differencePercentage?: number;
  priceTrend?: string;
  investmentScore?: number;
  estimatedRentalYield?: string;
  estimatedMonthlyRent?: number;
  demandIndicator?: string;
  disclaimer?: string;
  aiEnhanced: boolean;
}

export type SortOption = 'score_desc' | 'price_asc' | 'price_desc' | 'area_asc';
