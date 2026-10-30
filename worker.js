/**
 * VIBRA AUDIO PROXY & SEARCH GATEWAY (Cloudflare Worker)
 * $0 Free Tier — 100,000 requests/day permanent free tier
 * Provides CORS relay for YouTube Music InnerTube, Deezer, and caching.
 *
 * Riva-style pipeline:
 *   1. Search: WEB_REMIX (YT Music shelves) → ANDROID client fallback + continuation paging
 *   2. Stream: ANDROID player (direct unciphered URLs) → IOS → WEB_REMIX ladder,
 *      with a 403 pre-check (YouTube URLs are session-bound and expire)
 *   3. /api/audio: byte relay with Range + CORS so Web Audio (EQ/visualizer) stays untainted
 */

const YTM_CLIENT_KEY = 'AIzaSyC9XL3ZjWddXya6X74dJoCTL-WEYFDNX30';
const CORS_HEADERS = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
  'Access-Control-Allow-Headers': 'Content-Type, Authorization, Range',
  'Access-Control-Expose-Headers': 'Content-Length, Content-Range, Accept-Ranges, Content-Type',
  'Access-Control-Max-Age': '86400',
};

/**
 * InnerTube client ladder.
 * Versions verified live against youtubei/v1/player on 2026-10-29 (yt-dlp current set):
 *   VISIONOS 1.02 + visitorData → status OK, DIRECT per-format URLs  ✅ (primary rung)
 *   ANDROID 21.26.364           → status OK, SABR-only (no per-format url)
 *   IOS     21.26.4             → status OK, SABR-only (no per-format url)
 *   TVHTML5 / VISIONOS w/o visitorData → LOGIN_REQUIRED (bot check)
 * 2026-10-29: YouTube force-migrated ANDROID/IOS/WEB to server-ABR (yt-dlp #12482),
 * which returns formats with NO url field (only serverAbrStreamingUrl — a UMP/SABR
 * endpoint, not playable as progressive HTTP). VISIONOS still hands back fetchable
 * URLs, but ONLY when the request carries a valid visitorData (client.visitorData +
 * X-Goog-Visitor-Id header) — verified: same request without it → LOGIN_REQUIRED.
 */
const IT_CLIENTS = {
  web_remix: {
    name: 'WEB_REMIX',
    version: '1.20260707.12.00',
    host: 'https://music.youtube.com',
    key: YTM_CLIENT_KEY,
    ua: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36',
  },
  android: {
    name: 'ANDROID',
    version: '21.26.364',
    host: 'https://www.youtube.com',
    key: null,
    androidSdkVersion: 30,
    osName: 'Android',
    osVersion: '11',
    ua: 'com.google.android.youtube/21.26.364 (Linux; U; Android 11) gzip',
  },
  ios: {
    name: 'IOS',
    version: '21.26.4',
    host: 'https://www.youtube.com',
    key: null,
    deviceMake: 'Apple',
    deviceModel: 'iPhone16,2',
    osName: 'iPhone',
    osVersion: '18.3.2.22D82',
    ua: 'com.google.ios.youtube/21.26.4 (iPhone16,2; U; CPU iOS 18_3_2 like Mac OS X;)',
  },
  // yt-dlp's default JS-less client — the one rung that still returns per-format
  // stream URLs (live-verified 2026-10-29: 5/5 videos → status OK + url + Range 206).
  visionos: {
    name: 'VISIONOS',
    version: '1.02',
    host: 'https://www.youtube.com',
    key: null,
    deviceMake: 'Apple',
    deviceModel: 'RealityDevice17,1',
    osName: 'visionOS',
    osVersion: '26.5.23O471',
    ua: 'Mozilla/5.0 (Macintosh; Intel Mac OS X 15_7_3) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/26.0 Safari/605.1.15',
  },
};

// Search ladder: WEB_REMIX gives YT Music shelves, ANDROID gives YouTube-wide results
const SEARCH_LADDER = ['web_remix', 'android'];
// Stream ladder: VISIONOS is the only client still returning fetchable URLs
// (ANDROID/IOS are SABR-only as of 2026-10-29) — keep them as rungs in case
// YouTube rotates the experiment back per-IP or per-session.
const PLAYER_LADDER = ['visionos', 'android', 'ios'];
const WEB_UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36';

// Hosts allowed through the audio relay (open-proxy protection)
const RELAY_HOST_SUFFIXES = ['.googlevideo.com', '.youtube.com', '.ytimg.com'];

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
          version: '2.6.0',
          pipeline: 'riva-style',
          searchLadder: SEARCH_LADDER,
          playerLadder: PLAYER_LADDER,
          timestamp: Date.now()
        });
      }

      // Friendly index so opening the bare worker URL in a browser isn't a 404
      if (path === '/') {
        return jsonResponse({
          ok: true,
          service: 'Vibra Gateway',
          note: 'API-only worker — use a path below',
          endpoints: [
            '/api/health',
            '/api/search?q=<query>',
            '/api/stream?id=<videoId>&q=bestaudio',
            '/api/audio?url=<googlevideo url>',
            '/api/thumb?id=<videoId>',
            '/api/deezer?q=<query>'
          ]
        });
      }

      // 1. YouTube Music Search (with continuation paging for "get all songs")
      if (path === '/api/search') {
        const query = url.searchParams.get('q');
        const ctoken = url.searchParams.get('ctoken');
        const cclient = url.searchParams.get('cc');
        const limit = parseInt(url.searchParams.get('limit') || '25', 10);
        if (!query && !ctoken) {
          return jsonResponse({ error: 'Missing query param q' }, 400);
        }

        // Check Cloudflare Cache
        const cacheKey = new Request(url.toString(), request);
        const cache = caches.default;
        const cachedRes = await cache.match(cacheKey);
        if (cachedRes) return cachedRes;

        const result = await searchYouTubeMusic(query, limit, ctoken, cclient);
        const response = jsonResponse(result, 200, {
          'Cache-Control': 'public, max-age=600' // 10 min cache
        });
        ctx.waitUntil(cache.put(cacheKey, response.clone()));
        return response;
      }

      // 2. YouTube Music Stream Resolver (multi-client ladder + 403 pre-check)
      if (path === '/api/stream') {
        const videoId = url.searchParams.get('id');
        const quality = url.searchParams.get('q') || 'bestaudio';
        if (!videoId) {
          return jsonResponse({ error: 'Missing id param' }, 400);
        }

        // Serve previously-resolved URLs straight from cache: a repeat play must
        // not depend on surviving YouTube's bot-check burst a second time, and
        // every extra player POST raises the 429 risk for the NEXT request too.
        const cacheKey = new Request(
          `https://vibra.internal/stream/${encodeURIComponent(videoId)}?q=${encodeURIComponent(quality)}`
        );
        try {
          const hit = await caches.default.match(cacheKey);
          if (hit) {
            const data = await hit.json();
            if (data && data.url && data.expiresAt > Date.now() + 120000) {
              return jsonResponse({ ...data, cached: true }, 200, {
                'Cache-Control': 'public, max-age=13800'
              });
            }
          }
        } catch { /* cache miss — resolve fresh */ }

        const streamData = await resolveYouTubeStream(videoId, quality);
        if (streamData && streamData.url) {
          const ttlSec = Math.max(60, Math.floor((streamData.expiresAt - Date.now()) / 1000) - 60);
          ctx.waitUntil(caches.default.put(
            cacheKey,
            new Response(JSON.stringify(streamData), {
              headers: { 'Content-Type': 'application/json', 'Cache-Control': `max-age=${ttlSec}` }
            })
          ).catch(() => {}));
        }
        return jsonResponse(streamData, 200, {
          'Cache-Control': 'public, max-age=13800' // ~3.8 hours
        });
      }

      // 2b. Audio byte relay (Range + CORS) — required for Web Audio API:
      //     a cross-origin <audio> without CORS headers taints MediaElementSource,
      //     silencing the EQ/visualizer/DSP chain.
      if (path === '/api/audio') {
        const target = url.searchParams.get('url');
        if (!target) return jsonResponse({ error: 'Missing url param' }, 400);

        let upstream;
        try {
          upstream = new URL(target);
        } catch {
          return jsonResponse({ error: 'Invalid url param' }, 400);
        }
        const hostOk = RELAY_HOST_SUFFIXES.some(
          s => upstream.hostname === s.slice(1) || upstream.hostname.endsWith(s)
        );
        if (upstream.protocol !== 'https:' || !hostOk) {
          return jsonResponse({ error: 'Host not allowed' }, 403);
        }

        // googlevideo REQUIRES a Range header from our egress IP: verified live
        // 2026-10-29 — same signed URL, no Range → 403, Range → 206. A browser
        // <audio> element often issues its first request without one, so default it.
        const fwdHeaders = {
          Range: request.headers.get('Range') || 'bytes=0-'
        };

        const upstreamRes = await fetch(upstream.toString(), { headers: fwdHeaders });
        const outHeaders = {
          ...CORS_HEADERS,
          'Content-Type': upstreamRes.headers.get('Content-Type') || 'application/octet-stream',
          'Accept-Ranges': 'bytes',
          'Cache-Control': 'private, max-age=3600',
        };
        const cr = upstreamRes.headers.get('Content-Range');
        if (cr) outHeaders['Content-Range'] = cr;
        const cl = upstreamRes.headers.get('Content-Length');
        if (cl) outHeaders['Content-Length'] = cl;

        return new Response(upstreamRes.body, {
          status: upstreamRes.status,
          headers: outHeaders
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

// ---------------------------------------------------------------------------
// InnerTube plumbing
// ---------------------------------------------------------------------------

// ---------------------------------------------------------------------------
// visitorData — mandatory for stream URLs
// ---------------------------------------------------------------------------

const VISITOR_CACHE_KEY = 'https://vibra.internal/visitor-data';
const VISITOR_TTL_MS = 30 * 60 * 1000;
let visitorMemory = null; // { value, fetchedAt }

/**
 * YouTube hands out a per-session visitorData from the youtube.com bootstrap.
 * Live-verified 2026-10-29: the SAME VISIONOS player request answers
 * LOGIN_REQUIRED without it and status OK + playable per-format URLs with it.
 * Cached in-memory (isolate lifetime) and in the Cache API (survives isolates)
 * so the ~800KB homepage fetch happens at most twice per 30 minutes.
 */
async function getVisitorData() {
  if (visitorMemory && Date.now() - visitorMemory.fetchedAt < VISITOR_TTL_MS) {
    return visitorMemory.value;
  }
  try {
    const cached = await caches.default.match(VISITOR_CACHE_KEY);
    if (cached) {
      const rec = await cached.json();
      if (rec && rec.value && Date.now() - rec.fetchedAt < VISITOR_TTL_MS) {
        visitorMemory = rec;
        return rec.value;
      }
    }
  } catch { /* Cache API unavailable — fall through to network */ }

  try {
    const res = await fetch('https://www.youtube.com/', { headers: { 'User-Agent': WEB_UA } });
    const html = await res.text();
    const m = html.match(/"VISITOR_DATA"\s*:\s*"([^"]+)"/);
    if (m && m[1]) {
      visitorMemory = { value: m[1], fetchedAt: Date.now() };
      try {
        await caches.default.put(
          VISITOR_CACHE_KEY,
          new Response(JSON.stringify(visitorMemory), {
            headers: { 'Content-Type': 'application/json', 'Cache-Control': 'max-age=1800' },
          })
        );
      } catch { /* cache write is best-effort */ }
      return m[1];
    }
  } catch { /* network failure — below */ }

  // Keep serving a stale value rather than none at all (stale visitorData still
  // unlocks URLs; a missing one guarantees LOGIN_REQUIRED).
  return visitorMemory ? visitorMemory.value : null;
}

async function innerTubePost(endpoint, clientKey, body) {
  const client = IT_CLIENTS[clientKey];
  if (!client) throw new Error(`Unknown InnerTube client: ${clientKey}`);

  const query = client.key ? `?key=${client.key}&prettyPrint=false` : '?prettyPrint=false';
  const clientCtx = {
    clientName: client.name,
    clientVersion: client.version,
    hl: 'en',
    gl: 'US',
  };
  if (client.androidSdkVersion) clientCtx.androidSdkVersion = client.androidSdkVersion;
  if (client.deviceMake) clientCtx.deviceMake = client.deviceMake;
  if (client.deviceModel) clientCtx.deviceModel = client.deviceModel;
  if (client.osName) clientCtx.osName = client.osName;
  if (client.osVersion) clientCtx.osVersion = client.osVersion;

  const visitor = await getVisitorData();
  if (visitor) clientCtx.visitorData = visitor;

  const payload = { context: { client: clientCtx }, ...body };

  const headers = {
    'Content-Type': 'application/json',
    'User-Agent': client.ua,
    'X-Goog-Api-Format-Version': '2',
  };
  if (visitor) headers['X-Goog-Visitor-Id'] = visitor;

  const res = await fetch(`${client.host}/youtubei/v1/${endpoint}${query}`, {
    method: 'POST',
    headers,
    body: JSON.stringify(payload),
  });

  if (!res.ok) {
    const err = new Error(`YouTubei ${endpoint} ${client.name} HTTP ${res.status}`);
    err.status = res.status; // lets the ladder distinguish "bad video" from "rate limited"
    throw err;
  }
  return res.json();
}

/** Recursively collect renderers of the given keys from an InnerTube response. */
function collectRenderers(node, keys, out = [], depth = 0) {
  if (!node || typeof node !== 'object' || depth > 60) return out;
  if (Array.isArray(node)) {
    for (const item of node) collectRenderers(item, keys, out, depth + 1);
    return out;
  }
  for (const k of keys) {
    if (node[k] && typeof node[k] === 'object') out.push(node[k]);
  }
  for (const v of Object.values(node)) {
    if (v && typeof v === 'object') collectRenderers(v, keys, out, depth + 1);
  }
  return out;
}

/** Find the next-page continuation token anywhere in the response. */
function findContinuationToken(node, depth = 0) {
  if (!node || typeof node !== 'object' || depth > 60) return null;
  if (Array.isArray(node)) {
    for (const item of node) {
      const t = findContinuationToken(item, depth + 1);
      if (t) return t;
    }
    return null;
  }
  if (node.nextContinuationData?.token) return node.nextContinuationData.token;
  if (node.continuationEndpoint?.continuationCommand?.token) return node.continuationEndpoint.continuationCommand.token;
  if (node.continuationCommand?.token) return node.continuationCommand.token;
  for (const v of Object.values(node)) {
    if (v && typeof v === 'object') {
      const t = findContinuationToken(v, depth + 1);
      if (t) return t;
    }
  }
  return null;
}

/** "3:32" / "1:02:03" → seconds. Returns null when not a time string. */
function parseDurationText(txt) {
  if (!txt || typeof txt !== 'string' || !txt.includes(':')) return null;
  const parts = txt.split(':').map(Number);
  if (parts.some(isNaN)) return null;
  if (parts.length === 2) return parts[0] * 60 + parts[1];
  if (parts.length === 3) return parts[0] * 3600 + parts[1] * 60 + parts[2];
  return null;
}

function runsText(node) {
  if (!node) return '';
  if (typeof node === 'string') return node;
  if (node.simpleText) return node.simpleText;
  if (Array.isArray(node.runs)) return node.runs.map(r => r?.text || '').join('');
  return '';
}

/** Normalize any song/video renderer into Vibra's Track shape. */
function rendererToTrack(renderer, kind) {
  let title = '', videoId = '', artist = '', album = '', duration = null, art = '';

  if (kind === 'music') {
    const flexCols = renderer.flexColumns || [];
    const col0 = flexCols[0]?.musicResponsiveListItemFlexColumnRenderer;
    const titleRun = col0?.text?.runs?.[0] || col0?.title?.runs?.[0];
    title = titleRun?.text || '';
    videoId = renderer.playlistItemData?.videoId ||
      renderer.overlay?.musicItemThumbnailOverlayRenderer?.content?.musicPlayButtonRenderer?.playNavigationEndpoint?.watchEndpoint?.videoId ||
      titleRun?.navigationEndpoint?.watchEndpoint?.videoId ||
      '';
    const col1 = flexCols[1]?.musicResponsiveListItemFlexColumnRenderer;
    const subRuns = col1?.text?.runs || col1?.title?.runs || [];

    // WEB_REMIX runs look like: ["Song", " • ", "Artist", " • ", "Album", " • ", "4:45"]
    // — strip the type label, separators and duration so artist/album come out right
    const TYPE_LABELS = ['Song', 'Video', 'Album', 'Single', 'EP', 'Episode', 'Playlist', 'Podcast', 'Artist', 'Community playlist', 'Uploaded'];
    const cleanRuns = subRuns.filter(r => {
      const t = (r?.text || '').trim();
      if (!t || t === '•' || t === '·' || t === '|') return false;
      if (TYPE_LABELS.includes(t)) return false;
      if (parseDurationText(t) !== null) return false;
      return true;
    });
    artist = cleanRuns[0]?.text || 'YouTube Artist';
    album = cleanRuns[1]?.text || 'YouTube Stream';

    // Duration can sit in this column or a later flex column — scan them all
    if (duration === null) {
      for (const col of flexCols) {
        const runs = col?.musicResponsiveListItemFlexColumnRenderer?.text?.runs || [];
        for (const r of runs) {
          const d = parseDurationText(r?.text);
          if (d !== null) { duration = d; break; }
        }
        if (duration !== null) break;
      }
    }
    const thumbs = renderer.thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails || [];
    art = thumbs[thumbs.length - 1]?.url || '';
  } else {
    // videoRenderer / compactVideoRenderer (ANDROID client shape)
    videoId = renderer.videoId || '';
    title = runsText(renderer.title);
    artist = runsText(renderer.longBylineText) || runsText(renderer.shortBylineText) || 'YouTube Artist';
    album = 'YouTube Stream';
    duration = parseDurationText(runsText(renderer.lengthText));
    const thumbs = renderer.thumbnail?.thumbnails || renderer.thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails || [];
    art = thumbs[thumbs.length - 1]?.url || '';
  }

  if (!title || !videoId) return null;
  if (!art) art = `https://i.ytimg.com/vi/${videoId}/hqdefault.jpg`;

  return {
    id: `yt-${videoId}`,
    source: 'ytmusic',
    videoId,
    title,
    artist,
    album,
    genre: 'Online',
    duration: duration || 210,
    art,
    audioUrl: null,
    isPreview: false,
    lyricsOk: true
  };
}

function parseSearchTracks(json, limit) {
  const candidates = collectRenderers(json, ['musicResponsiveListItemRenderer']);
  const videoCandidates = collectRenderers(json, ['videoRenderer', 'compactVideoRenderer']);

  const tracks = [];
  const seenVideoIds = new Set();

  for (const renderer of candidates) {
    const t = rendererToTrack(renderer, 'music');
    if (!t || seenVideoIds.has(t.videoId)) continue;
    seenVideoIds.add(t.videoId);
    tracks.push(t);
    if (tracks.length >= limit) return tracks;
  }
  // ANDROID-client fallback shape (only used when WEB_REMIX yielded nothing)
  if (tracks.length === 0) {
    for (const renderer of videoCandidates) {
      const t = rendererToTrack(renderer, 'video');
      if (!t || seenVideoIds.has(t.videoId)) continue;
      seenVideoIds.add(t.videoId);
      tracks.push(t);
      if (tracks.length >= limit) break;
    }
  }
  return tracks;
}

// ---------------------------------------------------------------------------
// Search (Riva-style: client ladder + continuation paging)
// ---------------------------------------------------------------------------

async function searchYouTubeMusic(query, limit = 25, continuation = null, continuationClient = null) {
  // When replaying a continuation token it must go back through the client that issued it
  const ladder = continuation && IT_CLIENTS[continuationClient]
    ? [continuationClient]
    : SEARCH_LADDER;

  let lastError = null;

  for (const clientKey of ladder) {
    try {
      const body = continuation
        ? { continuation }
        : { query };
      const json = await innerTubePost('search', clientKey, body);

      const tracks = parseSearchTracks(json, limit);
      const token = findContinuationToken(json);

      if (tracks.length > 0 || continuation) {
        return { tracks, continuation: token, client: clientKey };
      }
      lastError = new Error(`search ${clientKey}: 0 results`);
    } catch (err) {
      lastError = err;
    }
  }

  if (lastError) throw lastError;
  return { tracks: [], continuation: null, client: null };
}

// ---------------------------------------------------------------------------
// Stream resolution (Riva-style: client ladder + 403 pre-check)
// ---------------------------------------------------------------------------

function pickAudioFormat(formats, quality) {
  const audio = (formats || []).filter(f =>
    f.mimeType && f.mimeType.startsWith('audio/') && f.url
  );
  if (audio.length === 0) return null;

  if (quality === '140') {
    return audio.find(f => f.itag === 140) || pickBestBitrate(audio);
  }
  if (quality === '251') {
    return audio.find(f => f.itag === 251) || pickBestBitrate(audio);
  }
  // bestaudio: Opus 251 > AAC 140 > highest bitrate
  return audio.find(f => f.itag === 251)
    || audio.find(f => f.itag === 140)
    || pickBestBitrate(audio);
}

function pickBestBitrate(formats) {
  return formats.reduce((prev, curr) =>
    ((curr.bitrate || 0) > (prev.bitrate || 0)) ? curr : prev, formats[0]);
}

/** YouTube URLs are session-bound: verify with a 2-byte Range GET before trusting it. */
async function checkStreamUrl(url) {
  try {
    const res = await fetch(url, { headers: { Range: 'bytes=0-2' } });
    const ok = res.status === 200 || res.status === 206;
    if (res.body) {
      try { await res.body.cancel(); } catch { /* ignore */ }
    }
    return ok;
  } catch {
    return false;
  }
}

async function resolveYouTubeStream(videoId, quality = 'bestaudio') {
  const attempts = [];
  const sleep = (ms) => new Promise(r => setTimeout(r, ms));
  // Structural failures (SABR-only / cipher-only responses) are properties of the
  // client, not of the burst — re-asking the same client next round only burns
  // rate-limit budget (429 → 'HTTP ...' errors for everyone). Only burst-style
  // failures (LOGIN_REQUIRED, HTTP 4xx/5xx, network) get retried.
  const structural = new Set();
  let hardFail = false;

  // YouTube's LOGIN_REQUIRED bot-check hits our egress IP in correlated bursts —
  // live measurement 2026-10-29: single ladder round passed only 4/6 requests, and
  // when ANDROID missed, IOS/WEB_REMIX missed too (same burst). Retry the whole
  // ladder with growing (and jittered, so we don't re-sync with the burst)
  // spacing; a round that starts clean succeeds immediately. 4 rounds with
  // 0.7s/1.4s/2.1s gaps keeps the worst case ≈10s — inside the 16s client timeout.
  for (let round = 1; round <= 4; round++) {
    if (round > 1) {
      await sleep(700 * (round - 1) + Math.floor(Math.random() * 400));
    }
    for (const clientKey of PLAYER_LADDER) {
      if (structural.has(clientKey)) continue;
      try {
        const json = await innerTubePost('player', clientKey, {
          videoId,
          contentCheckOk: true,
          racyCheckOk: true,
        });

        const status = json.playabilityStatus?.status;
        if (status && status !== 'OK') {
          attempts.push(`${clientKey}: ${status}`);
          continue;
        }

        const formats = json.streamingData?.adaptiveFormats || [];
        const selected = pickAudioFormat(formats, quality);

        if (!selected) {
          // Cipher-protected only (signatureCipher, no direct url) → SABR/no-url
          // response: structural for this client, stop retrying it this round set.
          const hasCipher = (formats || []).some(f => f.signatureCipher || f.cipher);
          attempts.push(`${clientKey}: ${hasCipher ? 'cipher-only' : 'no-audio'}`);
          structural.add(clientKey);
          continue;
        }

        const alive = await checkStreamUrl(selected.url);
        if (!alive) {
          attempts.push(`${clientKey}: 403/expired itag ${selected.itag}`);
          continue;
        }

        return {
          url: selected.url,
          itag: selected.itag,
          mimeType: (selected.mimeType || '').split(';')[0],
          source: clientKey,
          attempts,
          round,
          expiresAt: Date.now() + 3.8 * 3600 * 1000 // ~4 hours
        };
      } catch (err) {
        attempts.push(`${clientKey}: ${err.message}`);
        // 400/404/410 = YouTube rejects this VIDEO (deleted, invalid id), not our
        // session — retrying every rung × every round only stalls the response
        // (measured: 12 identical ERRORs over 29s). Stop and let mirrors decide.
        if (err.status && err.status >= 400 && err.status < 500
            && err.status !== 403 && err.status !== 429) {
          hardFail = true;
          break;
        }
      }
    }
    if (hardFail) break;
  }

  // Fallback to Invidious & Piped mirrors if direct InnerTube player was blocked.
  // Strict budget: dead mirrors used to hang with no timeout, turning one failed
  // resolve into a 45s response (measured 2026-10-30) — far past the clients'
  // 16s stream timeout. 6s total, 3s per mirror.
  const deadline = Date.now() + 6000;
  const mirrors = [
    `https://inv.tux.pizza/api/v1/videos/${videoId}`,
    `https://vid.puffyan.us/api/v1/videos/${videoId}`,
    `https://invidious.nerdvpn.de/api/v1/videos/${videoId}`,
    `https://pipedapi.adminforge.de/streams/${videoId}`,
    `https://api.piped.private.coffee/streams/${videoId}`
  ];

  for (const mirror of mirrors) {
    if (Date.now() > deadline) {
      attempts.push('mirror: time budget exhausted');
      break;
    }
    try {
      const res = await fetch(mirror, {
        headers: { 'User-Agent': 'Mozilla/5.0' },
        signal: AbortSignal.timeout(Math.max(250, Math.min(3000, deadline - Date.now()))),
      });
      if (res.ok) {
        const data = await res.json();
        let streamUrl = null;
        if (data.adaptiveFormats) {
          const af = data.adaptiveFormats.find(f => (f.type || '').startsWith('audio/'));
          if (af && af.url) streamUrl = af.url;
        } else if (data.audioStreams && data.audioStreams.length > 0) {
          streamUrl = data.audioStreams[0].url;
        }
        if (streamUrl) {
          return {
            url: streamUrl,
            source: 'mirror',
            attempts,
            expiresAt: Date.now() + 3.8 * 3600 * 1000
          };
        }
      }
    } catch (e) {
      attempts.push(`mirror: ${e.message}`);
    }
  }

  // Every rung failed — client falls back to its next source
  return { url: null, error: 'All stream clients failed', attempts };
}
