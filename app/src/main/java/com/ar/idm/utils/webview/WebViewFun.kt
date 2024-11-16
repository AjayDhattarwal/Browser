package com.ar.idm.utils.webview

import android.graphics.Bitmap
import android.graphics.Canvas
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebView.WebViewTransport
import android.webkit.WebViewDatabase
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.webkit.CookieManagerCompat
import androidx.webkit.WebViewFeature
import androidx.webkit.internal.CookieManagerAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Headers.Companion.toHeaders
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

suspend fun WebView?.captureVisiblePartAndNavigate():ImageBitmap? {

    return withContext(Dispatchers.IO) {
        if (this@captureVisiblePartAndNavigate == null || width == 0 || height == 0) {
            return@withContext null
        }

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        val canvas = Canvas(bitmap).apply {
            translate(-scrollX.toFloat(), -scrollY.toFloat())
        }
        draw(canvas)

        bitmap.asImageBitmap()
    }

}

fun WebView.configureWebView(isIncognito: Boolean){
    settings.apply {
        javaScriptEnabled = true
        mediaPlaybackRequiresUserGesture = false
        domStorageEnabled = !isIncognito
        cacheMode = if (isIncognito) WebSettings.LOAD_NO_CACHE else WebSettings.LOAD_CACHE_ELSE_NETWORK
        setSupportZoom(true)
        builtInZoomControls = true
        displayZoomControls = false
        loadWithOverviewMode = true
        useWideViewPort = true
        allowContentAccess = !isIncognito
        allowFileAccess = !isIncognito
        javaScriptCanOpenWindowsAutomatically = true
        setSupportMultipleWindows(true)
        if (isIncognito) {
            clearFormData()
        }

    }
}


fun WebView.saveUserData() {
    val script = """
        (function() {
            const accountLink = document.querySelector('a.gb_A.gb_Xa.gb_Z[aria-label*="Google Account"]');
            if (accountLink) {
                console.log('Account link found');
                const ariaLabel = accountLink.getAttribute('aria-label');
                console.log('Aria Label:', ariaLabel);
                const nameEmailMatch = ariaLabel.match(/Google Account:\s([^(\n]+)\s\(([^)]+)\)/);
                if (nameEmailMatch) {
                    const userName = nameEmailMatch[1];
                    const userEmail = nameEmailMatch[2];
                    const userImage = accountLink.querySelector('img.gb_O').src;
                    console.log('User Info:', userName, userEmail, userImage);
                    
                    if (window.Android && window.Android.saveUserData) {
                        window.Android.saveUserData(userName, userEmail, userImage);
                    }
                } else {
                    console.log('No match found for name and email');
                    if (window.Android && window.Android.saveUserData) {
                        window.Android.saveUserData(null, null, null);
                    }
                }
            } else {
                console.log('Account link not found');
                if (window.Android && window.Android.saveUserData) {
                    window.Android.saveUserData(null, null, null);
                }
            }
        })();
    """
    evaluateJavascript(script, null)
}


fun WebView.youtubeAdBlockingScript() {
    val adBlockingScript = """
            (function()
             {
                // Enable The Undetected Adblocker
                const adblocker = true;

                // Enable The Popup remover (pointless if you have the Undetected Adblocker)
                const removePopup = false;

                // Checks for updates (Removes the popup)
                const updateCheck = true;

                // Enable debug messages into the console
                const debugMessages = true;

                // Enable custom modal
                // Uses SweetAlert2 library (https://cdn.jsdelivr.net/npm/sweetalert2@11) for the update version modal.
                // When set to false, the default window popup will be used. And the library will not be loaded.
                const updateModal = {
                    enable: true, // if true, replaces default window popup with a custom modal
                    timer: 5000, // timer: number | false
                };

                // Store the initial URL
                let currentUrl = window.location.href;

                // Used for if there is ad found
                let isAdFound = false;

                //used to see how meny times we have loopped with a ad active
                let adLoop = 0;

                let hasIgnoredUpdate = false;

                //Set everything up here
                log("Script started");

                if (adblocker) removeAds();
                if (removePopup) popupRemover();

                // Remove Them pesski popups
                function popupRemover() {
                    setInterval(() => {
                        const modalOverlay = document.querySelector("tp-yt-iron-overlay-backdrop");
                        const popup = document.querySelector(".style-scope ytd-enforcement-message-view-model");
                        const popupButton = document.getElementById("dismiss-button");

                        var video = document.querySelector('video');

                        const bodyStyle = document.body.style;
                        bodyStyle.setProperty('overflow-y', 'auto', 'important');

                        if (modalOverlay) {
                            modalOverlay.removeAttribute("opened");
                            modalOverlay.remove();
                        }

                        if (popup) {
                            log("Popup detected, removing...");

                            if(popupButton) popupButton.click();

                            popup.remove();
                            video.play();

                            setTimeout(() => {
                                video.play();
                            }, 500);

                            log("Popup removed");
                        }
                        // Check if the video is paused after removing the popup
                        if (!video.paused) return;
                        // UnPause The Video
                        video.play();

                    }, 1000);
                }
                // undetected adblocker method
                function removeAds()
                {
                    log("removeAds()");

                    var videoPlayback = 1;

                    setInterval(() =>{

                        var video = document.querySelector('video');
                        const ad = [...document.querySelectorAll('.ad-showing')][0];


                        //remove page ads
                        if (window.location.href !== currentUrl) {
                            currentUrl = window.location.href;
                            removePageAds();
                        }

                        if (ad)
                        {
                            isAdFound = true;
                            adLoop = adLoop + 1;

                            //
                            // ad center method
                            //

                            // If we tried 10 times we can assume it won't work this time (This stops the weird pause/freeze on the ads)

                            if(adLoop < 10){
                                const openAdCenterButton = document.querySelector('.ytp-ad-button-icon');
                                openAdCenterButton?.click();

                                const blockAdButton = document.querySelector('[label="Block ad"]');
                                blockAdButton?.click();

                                const blockAdButtonConfirm = document.querySelector('.Eddif [label="CONTINUE"] button');
                                blockAdButtonConfirm?.click();

                                const closeAdCenterButton = document.querySelector('.zBmRhe-Bz112c');
                                closeAdCenterButton?.click();
                            }
                            else{
                                if (video) video.play();
                            }

                          var popupContainer = document.querySelector('body > ytd-app > ytd-popup-container > tp-yt-paper-dialog');
                          if (popupContainer)
                            // popupContainer persists, lets not spam
                            if (popupContainer.style.display == "")
                              popupContainer.style.display = 'none';

                            //
                            // Speed Skip Method
                            //
                            log("Found Ad");


                            const skipButtons = ['ytp-ad-skip-button-container', 'ytp-ad-skip-button-modern', '.videoAdUiSkipButton', '.ytp-ad-skip-button', '.ytp-ad-skip-button-modern', '.ytp-ad-skip-button', '.ytp-ad-skip-button-slot' ];

                            // Add a little bit of obfuscation when skipping to the end of the video.
                            if (video){

                                video.playbackRate = 10;
                                video.volume = 0;

                                // Iterate through the array of selectors
                                skipButtons.forEach(selector => {
                                    // Select all elements matching the current selector
                                    const elements = document.querySelectorAll(selector);

                                    // Check if any elements were found
                                    if (elements && elements.length > 0) {
                                      // Iterate through the selected elements and click
                                      elements.forEach(element => {
                                        element?.click();
                                      });
                                    }
                                });
                                video.play();

                                let randomNumber = Math.random() * (0.5 - 0.1) + 0.1;
                                video.currentTime = video.duration + randomNumber || 0;
                            }

                            log("skipped Ad (✔️)");

                        } else {

                            //check for unreasonale playback speed
                            if(video && video?.playbackRate == 10){
                                video.playbackRate = videoPlayback;
                            }

                            if (isAdFound){
                                isAdFound = false;

                                // this is right after the ad is skipped
                                // fixes if you set the speed to 2x and an ad plays, it sets it back to the default 1x


                                //somthing bugged out default to 1x then
                                if (videoPlayback == 10) videoPlayback = 1;
                                if(video && isFinite(videoPlayback)) video.playbackRate = videoPlayback;

                                //set ad loop back to the defualt
                                adLoop = 0;
                            }
                            else{
                                if(video) videoPlayback = video.playbackRate;
                            }
                        }

                    }, 50)

                    removePageAds();
                }

                //removes ads on the page (not video player ads)
                function removePageAds(){

                    const sponsor = document.querySelectorAll("div#player-ads.style-scope.ytd-watch-flexy, div#panels.style-scope.ytd-watch-flexy");
                    const style = document.createElement('style');

                    style.textContent = `
                        ytd-action-companion-ad-renderer,
                        ytd-display-ad-renderer,
                        ytd-video-masthead-ad-advertiser-info-renderer,
                        ytd-video-masthead-ad-primary-video-renderer,
                        ytd-in-feed-ad-layout-renderer,
                        ytd-ad-slot-renderer,
                        yt-about-this-ad-renderer,
                        yt-mealbar-promo-renderer,
                        ytd-statement-banner-renderer,
                        ytd-ad-slot-renderer,
                        ytd-in-feed-ad-layout-renderer,
                        ytd-banner-promo-renderer-background
                        statement-banner-style-type-compact,
                        .ytd-video-masthead-ad-v3-renderer,
                        div#root.style-scope.ytd-display-ad-renderer.yt-simple-endpoint,
                        div#sparkles-container.style-scope.ytd-promoted-sparkles-web-renderer,
                        div#main-container.style-scope.ytd-promoted-video-renderer,
                        div#player-ads.style-scope.ytd-watch-flexy,
                        ad-slot-renderer,
                        ytm-promoted-sparkles-web-renderer,
                        masthead-ad,
                        tp-yt-iron-overlay-backdrop,

                        #masthead-ad {
                            display: none !important;
                        }
                    `;

                    document.head.appendChild(style);

                    sponsor?.forEach((element) => {
                         if (element.getAttribute("id") === "rendering-content") {
                            element.childNodes?.forEach((childElement) => {
                              if (childElement?.data.targetId && childElement?.data.targetId !=="engagement-panel-macro-markers-description-chapters"){
                                  //Skipping the Chapters section
                                    element.style.display = 'none';
                                }
                               });
                        }
                     });

                    log("Removed page ads (✔️)");
                }

                // Used for debug messages
                function log(log, level = 'l', ...args) {
                    if (!debugMessages) return;

                    const prefix = 'Remove Adblock Thing:'
                    const message = log;
                    switch (level) {
                        case 'e':
                        case 'err':
                        case 'error':
                            console.error(message, ...args);
                            break;
                        case 'l':
                        case 'log':
                            console.log(message, ...args);
                            break;
                        case 'w':
                        case 'warn':
                        case 'warning':
                            console.warn(message, ...args);
                            break;
                        case 'i':
                        case 'info':
                        default:
                    console.info(message, ...args);
                    break
                }
                }

            })();

        """.trimIndent()

    evaluateJavascript(adBlockingScript, null)
}