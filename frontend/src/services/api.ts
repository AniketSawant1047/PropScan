import axios from 'axios';
import type { SearchRequest, SearchResponse, LocationIntelligence, PriceAnalysis } from '../types';

const api = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
  timeout: 30000,
});

// We inject the token dynamically from the store in the components or we can add an interceptor here
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('propscan_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export const authApi = {
  login: (data: any) => api.post('/auth/login', data).then(r => r.data),
  register: (data: any) => api.post('/auth/register', data).then(r => r.data),
};

export const propertyApi = {
  search: (request: SearchRequest): Promise<SearchResponse> =>
    api.post<SearchResponse>('/search', request).then(r => r.data),

  aiSearch: (query: string): Promise<SearchResponse> =>
    api.post<SearchResponse>('/ai/search', { query }).then(r => r.data),

  getLocationIntelligence: (
    groupId: string,
    lat?: number,
    lon?: number,
    locality?: string
  ): Promise<LocationIntelligence> =>
    api.get<LocationIntelligence>(`/properties/${groupId}/location`, {
      params: { lat, lon, locality }
    }).then(r => r.data),

  getPriceAnalysis: (
    groupId: string,
    priceInr?: number,
    areaSqft?: number,
    locality?: string
  ): Promise<PriceAnalysis> =>
    api.get<PriceAnalysis>(`/properties/${groupId}/price-analysis`, {
      params: { priceInr, areaSqft, locality }
    }).then(r => r.data),

  getSources: () =>
    api.get('/sources').then(r => r.data),

  createAlert: (alertData: object) =>
    api.post('/alerts', alertData).then(r => r.data),

  getSavedProperties: () =>
    api.get<string[]>('/user/saved').then(r => r.data),

  saveProperty: (groupId: string) =>
    api.post(`/user/saved/${groupId}`).then(r => r.data),

  unsaveProperty: (groupId: string) =>
    api.delete(`/user/saved/${groupId}`).then(r => r.data),
};

export default api;
