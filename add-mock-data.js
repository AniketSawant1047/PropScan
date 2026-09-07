const fs = require('fs');
const path = 'd:/Personal Projects/PROPERTY SEARCH/backend/src/main/resources/mock-data/';
const files = ['99acres.json', 'housing.json', 'magicbricks.json', 'nobroker.json', 'local.json', 'builders.json'];

files.forEach(file => {
  const data = JSON.parse(fs.readFileSync(path + file));
  
  const prefix = file.substring(0, 3).toUpperCase();
  
  // Property A: Blue Ridge
  const priceA = Math.floor(74 + Math.random() * 4); // 74 to 77 Lakh
  data.listings.push({
    "externalId": prefix + "-BR-001",
    "title": "2 BHK in Blue Ridge Hinjewadi",
    "description": "Luxurious 2 BHK apartment in Blue Ridge Township, Hinjewadi Phase 1. Walking distance to major IT companies.",
    "rawPrice": priceA + " Lakh",
    "rawArea": "1,050 sq.ft.",
    "rawBhk": "2 BHK",
    "propertyType": "Apartment",
    "locality": "Hinjewadi",
    "city": "Pune",
    "address": "Blue Ridge Township, Hinjewadi Phase 1, Pune 411057",
    "project": "Blue Ridge",
    "developer": "Paranjape Schemes",
    "floor": "12th Floor",
    "totalFloors": "25",
    "facing": "East",
    "furnishing": "Unfurnished",
    "parking": true,
    "lift": true,
    "gym": true,
    "swimmingPool": true,
    "security": true,
    "amenities": ["Gym", "Clubhouse", "Swimming Pool", "Security"],
    "latitude": 18.5833,
    "longitude": 73.7333,
    "listingUrl": "https://example.com/property/" + file + "/blueridge",
    "postedDate": "2024-02-10",
    "images": ["https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=800", "https://images.unsplash.com/photo-1484154218962-a197022b5858?w=800"],
    "readyToMove": true,
    "possession": "Ready to Move",
    "agentName": "Agent " + prefix,
    "agentPhone": "+91-9998887776"
  });

  // Property B: Megapolis
  const priceB = Math.floor(62 + Math.random() * 5); // 62 to 66 Lakh
  data.listings.push({
    "externalId": prefix + "-MP-001",
    "title": "2 BHK in Megapolis Mystic Hinjewadi",
    "description": "Spacious 2 BHK in Megapolis Mystic, Phase 3 Hinjewadi. Excellent views and amenities.",
    "rawPrice": priceB + " Lakh",
    "rawArea": "950 sq.ft.",
    "rawBhk": "2 BHK",
    "propertyType": "Apartment",
    "locality": "Hinjewadi",
    "city": "Pune",
    "address": "Megapolis Mystic, Hinjewadi Phase 3, Pune 411057",
    "project": "Megapolis Mystic",
    "developer": "Megapolis",
    "floor": "5th Floor",
    "totalFloors": "21",
    "facing": "West",
    "furnishing": "Semi-Furnished",
    "parking": true,
    "lift": true,
    "gym": true,
    "swimmingPool": true,
    "security": true,
    "amenities": ["Gym", "Clubhouse", "Security"],
    "latitude": 18.5777,
    "longitude": 73.6875,
    "listingUrl": "https://example.com/property/" + file + "/megapolis",
    "postedDate": "2024-03-01",
    "images": ["https://images.unsplash.com/photo-1493809842364-78817add7ffb?w=800"],
    "readyToMove": true,
    "possession": "Ready to Move",
    "agentName": "Agent " + prefix,
    "agentPhone": "+91-9998887776"
  });
  
  fs.writeFileSync(path + file, JSON.stringify(data, null, 2));
});
console.log('Mock data updated!');
