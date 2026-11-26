package com.freetube.app.util

import android.webkit.WebView

object AdBlockEngine {

    const val USER_AGENT = "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"

    val AD_BLOCK_CSS = """
        /* Video Ads & Overlays */
        .ytp-ad-module,
        .ytp-ad-overlay-container,
        .ad-showing .ytp-ad-text,
        .ad-container,
        #player-ads,
        .ytp-ad-action-interstitial,
        .ytp-ad-overlay-slot,
        .ytp-ad-image-overlay,
        
        /* Promoted & Sponsored Cards */
        ytm-promoted-sparkles-web-renderer,
        ytm-promoted-sparkles-text-search-renderer,
        ytm-compact-promoted-video-renderer,
        ytm-promoted-video-renderer,
        ad-slot-renderer,
        .ytm-promoted-sparkles-web-renderer,
        
        /* Upsells & App Download Nag Banners */
        ytm-app-promo-renderer,
        ytm-upsell-dialog-renderer,
        .mobile-topbar-header-sign-in-button,
        .mealbar-promo-renderer,
        .butterbar-container,
        ytm-pivot-bar-renderer ytm-app-promo-renderer,
        .dialog-upsell-renderer,
        ytm-open-app-banner-renderer,
        ytm-mealbar-promo-renderer {
            display: none !important;
            visibility: hidden !important;
            height: 0 !important;
            max-height: 0 !important;
            opacity: 0 !important;
            pointer-events: none !important;
        }

        /* Pure Dark AMOLED background */
        html, body {
            background-color: #0F0F0F !important;
        }
    """.trimIndent()

    val AD_BLOCK_AND_BACKGROUND_JS = """
        (function() {
            if (window._ft_initialized) return;
            window._ft_initialized = true;

            // ── 1. Enable Background & Off-Screen Playback ──────────────
            // Spoof Document Visibility so YouTube believes tab is always visible and screen is on
            try {
                Object.defineProperty(document, 'hidden', {
                    get: function() { return false; },
                    configurable: true
                });
                Object.defineProperty(document, 'visibilityState', {
                    get: function() { return 'visible'; },
                    configurable: true
                });
                Object.defineProperty(document, 'webkitVisibilityState', {
                    get: function() { return 'visible'; },
                    configurable: true
                });

                // Prevent visibility change listeners from pausing video
                var stopEvent = function(e) {
                    e.stopImmediatePropagation();
                };
                window.addEventListener('visibilitychange', stopEvent, true);
                document.addEventListener('visibilitychange', stopEvent, true);
                window.addEventListener('blur', stopEvent, true);
                window.addEventListener('pagehide', stopEvent, true);
            } catch(e) {}

            // ── 1b. Report playback to native + accept commands from native ──
            // Native side (MediaPlaybackService) shows the notification / lock-screen controls
            // from this data and drives the <video> through window.__ftControl.
            function getVideo() { return document.querySelector('video'); }

            function report() {
                if (!window.FreeTubeBridge) return;
                try {
                    var v = getVideo();
                    var hasMedia = !!(v && (v.currentSrc || v.src) && v.readyState > 0);
                    var md = navigator.mediaSession && navigator.mediaSession.metadata;
                    var title = (md && md.title) || document.title.replace(' - YouTube', '');
                    var artist = (md && md.artist) || '';
                    var art = '';
                    if (md && md.artwork && md.artwork.length) {
                        art = md.artwork[md.artwork.length - 1].src || '';
                    }
                    var dur = (v && isFinite(v.duration)) ? v.duration * 1000 : 0;
                    FreeTubeBridge.onMediaState(
                        hasMedia, title, artist, art,
                        !!(v && !v.paused && !v.ended),
                        v ? v.currentTime * 1000 : 0, dur
                    );
                } catch (e) {}
            }

            window.__ftControl = function(action, arg) {
                var v = getVideo();
                if (!v) return;
                try {
                    switch (action) {
                        case 'play':   v.play().catch(function() {}); break;
                        case 'pause':  v.pause(); break;
                        case 'toggle': if (v.paused) { v.play().catch(function() {}); } else { v.pause(); } break;
                        case 'seekBy': v.currentTime = Math.max(0, v.currentTime + arg); break;
                        case 'seekTo': v.currentTime = Math.max(0, arg); break;
                        case 'next':
                            var n = document.querySelector('.ytp-next-button, button[aria-label="Next video"]');
                            if (n) { n.click(); } else if (isFinite(v.duration)) { v.currentTime = v.duration; }
                            break;
                    }
                } catch (e) {}
                setTimeout(report, 150);
            };

            setInterval(report, 1000);

            // ── 2. Auto-Skip Video Ads ─────────────────────────────────
            var mutedByUs = false;
            var mutedBefore = false;
            function skipAds() {
                var video = document.querySelector('video');
                var adShowing = document.querySelector('.ad-showing, .ad-interrupting');
                if (adShowing && video) {
                    try {
                        if (!mutedByUs) { mutedBefore = video.muted; mutedByUs = true; }
                        video.muted = true;
                        if (!isNaN(video.duration) && video.duration > 0) {
                            video.currentTime = video.duration;
                        }
                    } catch(e) {}
                } else if (mutedByUs && video) {
                    // Ad is gone: give the user their original volume state back.
                    try { video.muted = mutedBefore; } catch(e) {}
                    mutedByUs = false;
                }

                var skipSelectors = [
                    '.ytp-ad-skip-button',
                    '.ytp-ad-skip-button-modern',
                    '.ytp-ad-skip-button-slot',
                    '.videoAdUiSkipButton',
                    'button.ytp-ad-skip-button-modern',
                    '.ytm-action-button[aria-label*="Skip"]',
                    '.ytp-ad-overlay-close-button',
                    '.ytp-skip-ad-button',
                    'button[aria-label*="Skip ad"]',
                    'button[aria-label*="Skip Ad"]'
                ];

                for (var i = 0; i < skipSelectors.length; i++) {
                    var btn = document.querySelector(skipSelectors[i]);
                    if (btn) {
                        try { btn.click(); } catch(e) {}
                    }
                }
            }

            // ── 3. Clean Nag Prompts & App Banners ─────────────────────
            function removeNagElements() {
                var badTags = [
                    'ytm-app-promo-renderer',
                    'ytm-upsell-dialog-renderer',
                    'ytm-mealbar-promo-renderer',
                    'ytm-pivot-bar-renderer ytm-app-promo-renderer'
                ];
                for (var i = 0; i < badTags.length; i++) {
                    var elements = document.querySelectorAll(badTags[i]);
                    for (var j = 0; j < elements.length; j++) {
                        try { elements[j].remove(); } catch(e) {}
                    }
                }
            }

            setInterval(skipAds, 200);
            setInterval(removeNagElements, 800);
        })();
    """.trimIndent()

    fun inject(webView: WebView) {
        val escapedCss = AD_BLOCK_CSS.replace("\n", " ").replace("'", "\\'")
        val cssInjectScript = """
            (function() {
                var parent = document.head || document.documentElement;
                if (!document.getElementById('freetube-css-injected')) {
                    var style = document.createElement('style');
                    style.id = 'freetube-css-injected';
                    style.type = 'text/css';
                    style.innerHTML = '$escapedCss';
                    parent.appendChild(style);
                }
            })();
        """.trimIndent()

        webView.evaluateJavascript(cssInjectScript, null)
        webView.evaluateJavascript(AD_BLOCK_AND_BACKGROUND_JS, null)
    }
}
