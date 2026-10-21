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
        .dialog-upsell-renderer {
            display: none !important;
            visibility: hidden !important;
            height: 0 !important;
            max-height: 0 !important;
            opacity: 0 !important;
            pointer-events: none !important;
        }

        /* Ensure deep dark AMOLED background */
        html, body {
            background-color: #0F0F0F !important;
        }
    """.trimIndent()

    val AD_BLOCK_JS = """
        (function() {
            if (window._ft_adblock_initialized) return;
            window._ft_adblock_initialized = true;

            // 1. Auto-skip video ads
            function skipAds() {
                // If ad is playing, jump video to end & mute
                var video = document.querySelector('video');
                var adShowing = document.querySelector('.ad-showing, .ad-interrupting');
                if (adShowing && video) {
                    try {
                        video.muted = true;
                        if (!isNaN(video.duration) && video.duration > 0) {
                            video.currentTime = video.duration;
                        }
                    } catch(e) {}
                }

                // Click all known skip ad button variations
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

            // 2. Remove annoying banners & promo popups
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

            // Run regularly
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
        webView.evaluateJavascript(AD_BLOCK_JS, null)
    }
}
