self.addEventListener('install', (event) => {
  console.log('[Service Worker] Installed');
  self.skipWaiting();
});

self.addEventListener('activate', (event) => {
  console.log('[Service Worker] Activated');
  return self.clients.claim();
});

self.addEventListener('fetch', (event) => {
  // Pass-through fetch for basic PWA compliance
  event.respondWith(fetch(event.request));
});
