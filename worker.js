/**
 * VIBRA AUDIO PROXY & SEARCH GATEWAY (Cloudflare Worker)
 * $0 Free Tier — 100,000 requests/day permanent free tier
 * Provides CORS relay for YouTube Music InnerTube, Deezer, and caching.
 */

const YTM_CLIENT_KEY = 'AIzaSyC9XL3ZjWddXya6X74dJoCTL-WEYFDNX30';
const CORS_HEADERS = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
  'Access-Control-Allow-Headers': 'Content-Type, Authorization',
  'Access-Control-Max-Age': '86400',
};

export default {
  async fetch(request, env, ctx) {
    if (request.method === 'OPTIONS') {
      return new Response(null, { headers: CORS_HEADERS });
    }

    const url = new URL(request.url);
    const path = url.pathname;

    try {
      if (path === '/api/health') {
        return jsonResponse({
          ok: true,
          service: 'Vibra Gateway',
          version: '2.0.0',
          timestamp: Date.now()
        });
      }

      // 1. YouTube Music Search
      if (path === '/api/search') {
        const query = url.searchParams.get('q');
        const limit = parseInt(url.searchParams.get('limit') || '25', 10);
        if (!query) {
          return jsonResponse({ error: 'Missing query param q' }, 400);
        }

        // Check Cloudflare Cache
        const cacheKey = new Request(url.toString(), request);
        const cache = caches.default;
        const cachedRes = await cache.match(cacheKey);
        if (cachedRes) return cachedRes;

        const results = await searchYouTubeMusic(query, limit);
        const response = jsonResponse(results, 200, {
          'Cache-Control': 'public, max-age=600' // 10 min cache
        });
        ctx.waitUntil(cache.put(cacheKey, response.clone()));
        return response;
      }

      // 2. YouTube Music Stream Resolver
      if (path === '/api/stream') {
        const videoId = url.searchParams.get('id');
        const quality = url.searchParams.get('q') || 'bestaudio';
        if (!videoId) {
          return jsonResponse({ error: 'Missing id param' }, 400);
        }

        const streamData = await resolveYouTubeStream(videoId, quality);
        return jsonResponse(streamData, 200, {
          'Cache-Control': 'public, max-age=13800' // ~3.8 hours
        });
      }

      // 3. Deezer Proxy (Bypasses Deezer CORS restriction)
      if (path === '/api/deezer') {
        const query = url.searchParams.get('q');
        const limit = url.searchParams.get('limit') || '25';
        if (!query) return jsonResponse({ error: 'Missing query' }, 400);

        const deezerRes = await fetch(`https://api.deezer.com/search?q=${encodeURIComponent(query)}&limit=${limit}`);
        const data = await deezerRes.json();
        return jsonResponse(data, 200, {
          'Cache-Control': 'public, max-age=1800'
        });
      }

      // 4. Thumbnail Proxy (CORS & Service Worker offline cache friendly)
      if (path === '/api/thumb') {
        const id = url.searchParams.get('id');
        if (!id) return jsonResponse({ error: 'Missing id param' }, 400);

        const thumbUrl = `https://i.ytimg.com/vi/${encodeURIComponent(id)}/hqdefault.jpg`;
        const thumbRes = await fetch(thumbUrl);
        if (!thumbRes.ok) return jsonResponse({ error: 'Thumbnail fetch failed' }, 404);

        return new Response(thumbRes.body, {
          headers: {
            'Content-Type': thumbRes.headers.get('Content-Type') || 'image/jpeg',
            'Cache-Control': 'public, max-age=86400',
            ...CORS_HEADERS
          }
        });
      }

      return jsonResponse({ error: 'Not found' }, 404);
    } catch (err) {
      return jsonResponse({ error: err.message || 'Gateway Internal Error' }, 500);
    }
  }
};

function jsonResponse(data, status = 200, extraHeaders = {}) {
  return new Response(JSON.stringify(data), {
    status,
    headers: {
      'Content-Type': 'application/json',
      ...CORS_HEADERS,
      ...extraHeaders
    }
  });
}

async function searchYouTubeMusic(query, limit = 25) {
  const payload = {
    context: {
      client: {
        clientName: 'WEB_REMIX',
        clientVersion: '1.20240101.01.00',
        hl: 'en'
      }
    },
    query
  };

  const res = await fetch(`https://music.youtube.com/youtubei/v1/search?key=${YTM_CLIENT_KEY}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  });

  if (!res.ok) {
    throw new Error(`YouTubei search HTTP ${res.status}`);
  }

  const json = await res.json();
  const tracks = [];

  // Parse InnerTube music shelves
  try {
    const tabs = json.contents?.tabbedSearchResultsRenderer?.tabs || [];
    const contents = tabs[0]?.tabRenderer?.content?.sectionListRenderer?.contents || [];

    const candidateRenderers = [];
    for (const section of contents) {
      if (section.musicCardShelfRenderer?.contents) {
        for (const it of section.musicCardShelfRenderer.contents) {
          if (it.musicResponsiveListItemRenderer) candidateRenderers.push(it.musicResponsiveListItemRenderer);
        }
      }
      if (section.musicShelfRenderer?.contents) {
        for (const it of section.musicShelfRenderer.contents) {
          if (it.musicResponsiveListItemRenderer) candidateRenderers.push(it.musicResponsiveListItemRenderer);
        }
      }
      if (section.itemSectionRenderer?.contents) {
        for (const it of section.itemSectionRenderer.contents) {
          if (it.musicResponsiveListItemRenderer) {
            candidateRenderers.push(it.musicResponsiveListItemRenderer);
          } else if (it.musicShelfRenderer?.contents) {
            for (const nested of it.musicShelfRenderer.contents) {
              if (nested.musicResponsiveListItemRenderer) candidateRenderers.push(nested.musicResponsiveListItemRenderer);
            }
          }
        }
      }
    }

    const seenVideoIds = new Set();
    for (const renderer of candidateRenderers) {
      const flexCols = renderer.flexColumns || [];
      const col0 = flexCols[0]?.musicResponsiveListItemFlexColumnRenderer;
      const titleRun = col0?.text?.runs?.[0] || col0?.title?.runs?.[0];
      const title = titleRun?.text;

      const videoId = renderer.playlistItemData?.videoId ||
                      renderer.overlay?.musicItemThumbnailOverlayRenderer?.content?.musicPlayButtonRenderer?.playNavigationEndpoint?.watchEndpoint?.videoId ||
                      titleRun?.navigationEndpoint?.watchEndpoint?.videoId;

      if (!title || !videoId || seenVideoIds.has(videoId)) continue;
      seenVideoIds.add(videoId);

      const col1 = flexCols[1]?.musicResponsiveListItemFlexColumnRenderer;
      const subRuns = col1?.text?.runs || col1?.title?.runs || [];
      const artist = subRuns[0]?.text || 'YouTube Artist';
      const album = subRuns[2]?.text || subRuns[subRuns.length - 1]?.text || 'YouTube Stream';

      // Thumbnails
      const thumbs = renderer.thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails || [];
      const art = thumbs[thumbs.length - 1]?.url || `https://i.ytimg.com/vi/${videoId}/hqdefault.jpg`;

      // Duration string parser
      let duration = 210;
      for (let i = subRuns.length - 1; i >= 0; i--) {
        const txt = subRuns[i]?.text;
        if (txt && txt.includes(':')) {
          const parts = txt.split(':').map(Number);
          if (parts.length === 2 && !isNaN(parts[0]) && !isNaN(parts[1])) {
            duration = parts[0] * 60 + parts[1];
            break;
          }
          if (parts.length === 3 && !isNaN(parts[0]) && !isNaN(parts[1]) && !isNaN(parts[2])) {
            duration = parts[0] * 3600 + parts[1] * 60 + parts[2];
            break;
          }
        }
      }

      tracks.push({
        id: `yt-${videoId}`,
        source: 'ytmusic',
        videoId,
        title,
        artist,
        album,
        genre: 'Online',
        duration,
        art,
        audioUrl: null,
        isPreview: false,
        lyricsOk: true
      });

      if (tracks.length >= limit) break;
    }
  } catch (parseErr) {
    console.warn('InnerTube parser warning:', parseErr);
  }

  return tracks;
}

async function resolveYouTubeStream(videoId, quality = 'bestaudio') {
  const payload = {
    context: {
      client: {
        clientName: 'WEB_REMIX',
        clientVersion: '1.20240101.01.00',
        hl: 'en'
      }
    },
    videoId
  };

  const res = await fetch(`https://music.youtube.com/youtubei/v1/player?key=${YTM_CLIENT_KEY}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  });

  if (!res.ok) {
    throw new Error(`YouTubei player HTTP ${res.status}`);
  }

  const json = await res.json();
  const adaptiveFormats = json.streamingData?.adaptiveFormats || [];

  // Filter for audio streams
  const audioFormats = adaptiveFormats.filter(f => f.mimeType && f.mimeType.startsWith('audio/'));
  if (audioFormats.length > 0) {
    let selected = audioFormats[0];
    if (quality === '140') {
      selected = audioFormats.find(f => f.itag === 140) || selected;
    } else if (quality === '251') {
      selected = audioFormats.find(f => f.itag === 251) || selected;
    } else {
      selected = audioFormats.reduce((prev, curr) => 
        ((curr.bitrate || 0) > (prev.bitrate || 0)) ? curr : prev, audioFormats[0]
      );
    }

    if (selected.url) {
      return {
        url: selected.url,
        expiresAt: Date.now() + 4 * 3600 * 1000 // 4 hours
      };
    }
  }

  // If cipher protected, fallback to piped audio relay
  return {
    url: `https://pipedapi.kavin.rocks/streams/${videoId}`,
    expiresAt: Date.now() + 2 * 3600 * 1000
  };
}
