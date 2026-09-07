import { create } from 'zustand';
import type { PropertyGroup, SearchRequest, SearchResponse } from '../types';
import { propertyApi } from '../services/api';

interface User {
  id: number;
  name: string;
  email: string;
}

interface AppStore {
  // Auth state
  user: User | null;
  token: string | null;
  setUser: (user: User | null) => void;
  setToken: (token: string | null) => void;
  logout: () => void;

  // Search state
  searchRequest: SearchRequest;
  searchResponse: SearchResponse | null;
  isSearching: boolean;
  searchError: string | null;

  // Selected property
  selectedGroup: PropertyGroup | null;

  // Saved properties
  savedGroupIds: Set<string>;
  syncSavedProperties: () => Promise<void>;

  // Actions
  setSearchRequest: (req: SearchRequest) => void;
  setSearchResponse: (res: SearchResponse | null) => void;
  setIsSearching: (v: boolean) => void;
  setSearchError: (e: string | null) => void;
  setSelectedGroup: (g: PropertyGroup | null) => void;
  toggleSaved: (groupId: string) => Promise<void>;
  isSaved: (groupId: string) => boolean;

  // AI assistant state
  aiMessages: { role: 'user' | 'ai'; content: string }[];
  addAiMessage: (msg: { role: 'user' | 'ai'; content: string }) => void;
  clearAiMessages: () => void;
}

export const useStore = create<AppStore>((set, get) => ({
  user: localStorage.getItem('propscan_user') ? JSON.parse(localStorage.getItem('propscan_user') as string) : null,
  token: localStorage.getItem('propscan_token'),

  setUser: (user) => {
    if (user) localStorage.setItem('propscan_user', JSON.stringify(user));
    else localStorage.removeItem('propscan_user');
    set({ user });
  },
  
  setToken: (token) => {
    if (token) localStorage.setItem('propscan_token', token);
    else localStorage.removeItem('propscan_token');
    set({ token });
  },

  logout: () => {
    localStorage.removeItem('propscan_user');
    localStorage.removeItem('propscan_token');
    set({ user: null, token: null, savedGroupIds: new Set() });
  },

  searchRequest: { city: 'Pune', sortBy: 'score_desc' },
  searchResponse: null,
  isSearching: false,
  searchError: null,
  selectedGroup: null,
  savedGroupIds: new Set(),
  aiMessages: [],

  syncSavedProperties: async () => {
    if (!get().token) return;
    try {
      const savedIds = await propertyApi.getSavedProperties();
      set({ savedGroupIds: new Set(savedIds) });
    } catch (e) {
      console.error("Failed to sync saved properties", e);
    }
  },

  setSearchRequest: (req) => set({ searchRequest: req }),
  setSearchResponse: (res) => set({ searchResponse: res }),
  setIsSearching: (v) => set({ isSearching: v }),
  setSearchError: (e) => set({ searchError: e }),
  setSelectedGroup: (g) => set({ selectedGroup: g }),

  toggleSaved: async (groupId) => {
    const { token, savedGroupIds } = get();
    if (!token) {
      // You could trigger a login modal here instead
      alert("Please sign in to save properties.");
      return;
    }

    const isCurrentlySaved = savedGroupIds.has(groupId);
    
    // Optimistic UI update
    const next = new Set(savedGroupIds);
    if (isCurrentlySaved) next.delete(groupId);
    else next.add(groupId);
    set({ savedGroupIds: next });

    // Background API call
    try {
      if (isCurrentlySaved) {
        await propertyApi.unsaveProperty(groupId);
      } else {
        await propertyApi.saveProperty(groupId);
      }
    } catch (e) {
      // Revert optimistic update on failure
      set({ savedGroupIds });
      console.error("Failed to toggle saved property", e);
    }
  },

  isSaved: (groupId) => get().savedGroupIds.has(groupId),

  addAiMessage: (msg) => set((state) => ({
    aiMessages: [...state.aiMessages, msg]
  })),

  clearAiMessages: () => set({ aiMessages: [] }),
}));
