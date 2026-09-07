# PROPScan 🏠

> **"Scan properties. Compare deals. Decide smarter."**

PROPScan is an AI-powered real estate metasearch platform — like Skyscanner/Google Flights, but for Indian property. Search once across multiple property sources, find duplicate listings, compare prices, and get AI-powered property recommendations.

---

## 🚀 Quick Start

### Prerequisites
- Java 21+ and Maven 3.8+
- Node.js 18+ and npm 9+
- (Optional) PostgreSQL for production — H2 in-memory is used by default for demo

### 1. Start the Backend

```bash
cd backend
mvn spring-boot:run
```

Backend starts at: **http://localhost:8080**
H2 Console: **http://localhost:8080/h2-console** (JDBC URL: `jdbc:h2:mem:propscandb`)

### 2. Start the Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend starts at: **http://localhost:5173**

---

## 🎯 Demo Flow (Hackathon)

1. Open **http://localhost:5173**
2. In the AI search box, type:
   ```
   2 BHK in Pune under ₹80 lakh near Hinjewadi with parking and good connectivity
   ```
3. Click **"Find My Property"**
4. Watch PropScan:
   - Search all 6 mock sources simultaneously
   - Group duplicate listings (same property on multiple platforms)
   - Show AI match score (94/100)
   - Highlight best price (Housing.com at ₹76.5L)
   - Show potential saving (₹2.5L)
5. Click any property card → **Property Details** page
6. Click **"Sources"** tab → see all prices from all sources
7. Click **"Location IQ"** tab → see nearby places, commute times
8. Click **"Price Analysis"** tab → see vs market average
9. Click **"AI Analysis"** tab → see score breakdown and recommendations
10. Click **"View Original Listing"** → opens source website

---

## 🏗️ Architecture

```
propscan/
├── backend/                    Java 21 + Spring Boot 3.x
│   ├── src/main/java/com/propscan/
│   │   ├── config/             Security, CORS config
│   │   ├── controller/         REST API endpoints
│   │   ├── service/
│   │   │   ├── ai/             AiService interface + RuleBasedAiService
│   │   │   ├── location/       LocationService interface + MockLocationService
│   │   │   ├── provider/       PropertySource interface + 6 adapters
│   │   │   └── search/         PropertySearchService (orchestration)
│   │   ├── model/              JPA entities
│   │   ├── repository/         Spring Data repositories
│   │   ├── dto/                Data Transfer Objects
│   │   ├── exception/          GlobalExceptionHandler
│   │   └── util/               PriceNormalizer, AreaNormalizer, BhkNormalizer
│   └── src/main/resources/
│       ├── mock-data/          JSON feeds (99acres, MagicBricks, Housing, NoBroker, Builders, Local)
│       └── application.yml
│
└── frontend/                   React + TypeScript + Vite + Tailwind CSS
    └── src/
        ├── pages/
        │   ├── HomePage.tsx
        │   ├── SearchResultsPage.tsx
        │   ├── PropertyDetailsPage.tsx
        │   └── SavedPropertiesPage.tsx
        ├── components/
        │   ├── layout/Navbar.tsx
        │   └── property/       ScoreBadge, SourceBadge, PriceSavingBadge
        ├── services/api.ts     Axios API layer
        ├── store/index.ts      Zustand global state
        └── types/index.ts      TypeScript interfaces
```

---

## 🔌 REST APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/search` | Multi-source property search |
| POST | `/api/ai/search` | Natural language search |
| GET | `/api/properties/{groupId}/location` | Location intelligence |
| GET | `/api/properties/{groupId}/price-analysis` | Price analysis |
| GET | `/api/sources` | Available property sources |
| POST | `/api/alerts` | Create price alert |
| GET | `/api/user/saved` | Get saved properties |

### Example: Search Request
```json
POST /api/search
{
  "naturalLanguageQuery": "2 BHK in Pune under ₹80 lakh near Hinjewadi with parking",
  "city": "Pune",
  "page": 0,
  "size": 20
}
```

### Example: Search Response (abbreviated)
```json
{
  "totalListings": 52,
  "totalGroups": 12,
  "interpretedQuery": "2 BHK in Hinjewadi Pune under ₹80L with parking",
  "groups": [
    {
      "groupId": "PG001",
      "canonicalTitle": "Premium 2 BHK Apartment in Wakad",
      "bestPriceFormatted": "₹76.5L",
      "bestPriceSource": "Housing.com",
      "highestPriceFormatted": "₹79L",
      "potentialSavingFormatted": "₹2.5L",
      "matchConfidence": 96.0,
      "sourceCount": 4,
      "sources": ["99acres", "magicbricks", "housing", "nobroker"],
      "aiScore": {
        "overallScore": 94,
        "grade": "Excellent",
        "positives": ["Price is below locality average", "Parking included", ...]
      }
    }
  ]
}
```

---

## 🧠 AI Architecture

### AiService (Interface)
```java
public interface AiService {
    SearchRequestDto parseNaturalLanguageQuery(String query);
    AiScoreDto calculateScore(PropertyGroup group, List<PropertyListing> listings, SearchRequestDto request);
    String generateRecommendation(...);
}
```

### Implementations
| Implementation | Availability | Config |
|---|---|---|
| `RuleBasedAiService` | Always available | `propscan.ai.provider=rule-based` |
| LLM (future) | When API key present | `propscan.ai.provider=openai` |

The rule-based implementation:
- Parses NL queries using regex patterns
- Scores properties deterministically (price vs locality average, area vs BHK ideal, amenities)
- Generates text recommendations from templates
- Never fabricates data

---

## 🗂️ Mock Data Structure

Each source has a JSON file at `backend/src/main/resources/mock-data/`:

| File | Source | Listings |
|---|---|---|
| `99acres.json` | 99acres | 12 listings |
| `magicbricks.json` | MagicBricks | 10 listings |
| `housing.json` | Housing.com | 10 listings |
| `nobroker.json` | NoBroker | 10 listings |
| `builders.json` | Builder Direct | 8 listings |
| `local.json` | Local Sources | 8 listings |

**Intentional duplicates**: The same physical property (e.g., "Palm Heights, Wakad") appears across all sources with:
- Slightly different titles
- Different prices (₹76.5L to ₹79L)
- Different descriptions
- Nearly identical coordinates

PropScan's grouping algorithm detects these as the same property with ~96% match confidence.

---

## 📊 Duplicate Grouping Algorithm

Properties are grouped using a weighted similarity score:

| Factor | Weight |
|---|---|
| Project name similarity | 35% |
| Developer name similarity | 15% |
| BHK match | 15% |
| Area within 5% | 15% |
| Coordinates within 200m | 20% |

Threshold: **≥ 75% similarity** → grouped as same property

---

## 🌍 Location Mock Data

Supported Pune localities with mock data:
- Wakad, Hinjewadi, Baner, Kharadi, Viman Nagar
- Kothrud, Aundh, Hadapsar, Balewadi, Pimple Saudagar

Each locality has mock:
- Metro stations
- Hospitals
- Schools
- Shopping centers
- IT offices
- Restaurants
- Commute time estimates

---

## 🔒 Security

| Feature | Status |
|---|---|
| JWT structure | Scaffolded, not enforced for demo |
| CORS | Configured (all origins for demo) |
| Input validation | Spring Validation |
| Exception handling | GlobalExceptionHandler |
| SQL injection | JPA parameter binding |
| H2 console | Enabled for demo only |

---

## 🔧 Configuration

Key settings in `application.yml`:

```yaml
propscan:
  ai:
    provider: rule-based   # Change to 'openai' with API key
  location:
    provider: mock         # Change to 'google-maps' with API key
```

---

## 🔄 Extending to Real APIs

### Add a Real Property Source
1. Implement `PropertySource` interface
2. Add `@Component` annotation
3. Inject real API client instead of JSON reader
4. Replace `AbstractMockPropertySource` with real HTTP calls

### Add Real AI
1. Implement `AiService` interface
2. Set `propscan.ai.provider=openai`
3. The `RuleBasedAiService` acts as automatic fallback

### Add Real Location
1. Implement `LocationService` interface
2. Call Google Maps API / HERE Maps
3. Set `propscan.location.provider=google-maps`

---

## 📦 Tech Stack

| Layer | Technology |
|---|---|
| Frontend | React 18, TypeScript, Vite, Tailwind CSS v4 |
| State | Zustand, React Query |
| HTTP | Axios |
| Charts | Recharts |
| Icons | Lucide React |
| Backend | Java 21, Spring Boot 3.2 |
| Database | H2 (demo), PostgreSQL (production) |
| Security | Spring Security, JWT-ready |
| Build | Maven, npm |

---

## 📋 Hackathon Checklist

- ✅ Natural language property search (rule-based NLP)
- ✅ 6 property source adapters (all mock JSON)
- ✅ Price normalization (Lakh/Crore → INR)
- ✅ Area normalization (sq.ft / sq.m)
- ✅ BHK normalization
- ✅ Duplicate property grouping with confidence score
- ✅ Best price comparison across sources
- ✅ AI match score (rule-based)
- ✅ Location intelligence (mock data)
- ✅ Nearby places (metro, hospitals, schools, etc.)
- ✅ Commute time estimates
- ✅ Price analysis vs locality average
- ✅ Investment indicators
- ✅ Property save/shortlist
- ✅ View original listing URL
- ✅ Responsive design
- ✅ Source error handling (one failing source doesn't break search)
- ✅ AI fallback (rule-based always works)

---

*Made for the real-estate tech hackathon. All property data is mock — no unauthorized scraping.*
