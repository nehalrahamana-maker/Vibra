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
 * Verified live 2026-10-27: ANDROID player returns direct (no signatureCipher) URLs,
 * playabilityStatus OK, itags 139/140/249/251/599/600.
 */
const IT_CLIENTS = {
  web_remix: {
    name: 'WEB_REMIX',
    version: '1.20250101.01.00',
    host: 'https://music.youtube.com',
    key: YTM_CLIENT_KEY,
    ua: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36',
  },
  android: {
    name: 'ANDROID',
    version: '20.10.38',
    host: 'https://www.youtube.com',
    key: null,
    androidSdkVersion: 34,
    ua: 'com.google.android.youtube/20.10.38 (Linux; U; Android 14) gzip',
  },
  ios: {
    name: 'IOS',
    version: '20.10.4',
    host: 'https://www.youtube.com',
    key: null,
    deviceMake: 'Apple',
    deviceModel: 'iPhone16,2',
    ua: 'com.google.ios.youtube/20.10.4 (iPhone16,2; U; CPU iOS 18_1 like Mac OS X;)',
  },
};

// Search ladder: WEB_REMIX gives YT Music shelves, ANDROID gives YouTube-wide results
const SEARCH_LADDER = ['web_remix', 'android'];
// Stream ladder: ANDROID proven to return unciphered URLs; others as rungs
const PLAYER_LADDER = ['android', 'ios', 'web_remix'];

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
          version: '2.1.0',
          pipeline: 'riva-style',
          searchLadder: SEARCH_LADDER,
          playerLadder: PLAYER_LADDER,
          timestamp: Date.now()
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

        const streamData = await resolveYouTubeStream(videoId, quality);
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

        const fwdHeaders = {};
        const range = request.headers.get('Range');
        if (range) fwdHeaders['Range'] = range;

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

  const payload = { context: { client: clientCtx }, ...body };

  const res = await fetch(`${client.host}/youtubei/v1/${endpoint}${query}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'User-Agent': client.ua,
      'X-Goog-Api-Format-Version': '2',
    },
    body: JSON.stringify(payload),
  });

  if (!res.ok) throw new Error(`YouTubei ${endpoint} ${client.name} HTTP ${res.status}`);
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
    artist = subRuns[0]?.text || 'YouTube Artist';
    album = subRuns[2]?.text || subRuns[subRuns.length - 1]?.text || 'YouTube Stream';
    for (let i = subRuns.length - 1; i >= 0; i--) {
      const d = parseDurationText(subRuns[i]?.text);
      if (d !== null) { duration = d; break; }
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

  for (const clientKey of PLAYER_LADDER) {
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
        // Cipher-protected only (signatureCipher, no direct url) — try next client
        const hasCipher = (formats || []).some(f => f.signatureCipher || f.cipher);
        attempts.push(`${clientKey}: ${hasCipher ? 'cipher-only' : 'no-audio'}`);
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
        expiresAt: Date.now() + 3.8 * 3600 * 1000 // ~4 hours
      };
    } catch (err) {
      attempts.push(`${clientKey}: ${err.message}`);
    }
  }

  // Fallback to Invidious & Piped mirrors if direct InnerTube player was blocked
  const mirrors = [
    `https://inv.tux.pizza/api/v1/videos/${videoId}`,
    `https://vid.puffyan.us/api/v1/videos/${videoId}`,
    `https://invidious.nerdvpn.de/api/v1/videos/${videoId}`,
    `https://pipedapi.adminforge.de/streams/${videoId}`,
    `https://api.piped.private.coffee/streams/${videoId}`
  ];

  for (const mirror of mirrors) {
    try {
      const res = await fetch(mirror, { headers: { 'User-Agent': 'Mozilla/5.0' } });
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
