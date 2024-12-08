package com.ar.webwiz.utils.webview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

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


fun WebView.saveHtmlToCache(){
    evaluateJavascript(
        "(function() { return document.documentElement.outerHTML; })();"
    ) { html ->


        val cacheDir = this.context.cacheDir
        val file = File(cacheDir, "webview_content.html")

        try {
            // Delete the file if it already exists to ensure a clean write
            if (file.exists()) {
                file.delete()
            }

            // Write the new HTML content to the file
            file.writeText(html)

            println("HTML content successfully saved to: ${file.absolutePath}")
        } catch (e: IOException) {
            e.printStackTrace() // Handle the exception
            println("Error saving HTML content: ${e.message}")
        }
    }

}


fun WebView.implementDistractionControl(){

    val js = """
        (function injectCSS() {
            const css = `
                .disintegrating {
                    position: relative;
                    transition: opacity 1s ease-out, transform 1s ease-out;
                }
                .disintegrating.fade-out {
                    opacity: 0;
                    transform: scale(0.5);
                }
                .selectedByWibWiz {
                    position: relative;
                    border-radius: inherit; 
                    box-shadow: 0 0 5px 1px red; 
                    filter: blur(2px);
                }
            `;
            const style = document.createElement('style');
            style.textContent = css;
            document.head.appendChild(style);
        })();

        (function addSelectionEffect() {
            var selectedElements = [];
            var isSelectionLocked = true; 

            
            document.addEventListener('click', function (event) {
               
                if (isSelectionLocked) return;

                event.preventDefault();
                event.stopImmediatePropagation();

                const clickedElement = event.target;

                
                if (selectedElements.includes(clickedElement)) {
                    clickedElement.classList.remove('selectedByWibWiz');
                    selectedElements = selectedElements.filter(el => el !== clickedElement);
                } else {
                    selectedElements.push(clickedElement);
                    clickedElement.classList.add('selectedByWibWiz');
                }
            }, true);

            
            function hideSelected() {
                isSelectionLocked = true; 
                const hiddenElements = [];

                selectedElements.forEach(function (element) {
                    if (element) {
                        createDisintegrationEffect(element);
                        hiddenElements.push(element);

                        setTimeout(() => {
                            element.style.display = 'none';
                        }, 1500);
                    }
                });

                selectedElements = [];
                return hiddenElements;
            }

            
            function createDisintegrationEffect(element) {
                element.classList.add('disintegrating');
                setTimeout(() => {
                    element.classList.add('fade-out');
                }, 0);
            }

            
            function toggleSelection() {
                const currentState = isSelectionLocked; 
                isSelectionLocked = !isSelectionLocked; 
                return currentState; 
            }

            
            window.hideSelected = hideSelected;
            window.toggleSelection = toggleSelection;
        })();

    """.trimIndent()

    evaluateJavascript(js, null)
}


fun WebView.toggleDistractionSelection(callback: (Boolean) -> Unit){
    evaluateJavascript("window.toggleSelection();"){ result ->
        val isSelectionLocked = result?.toBoolean() ?: false
        callback(isSelectionLocked)
    }
}

fun WebView.hideDistraction(){
    evaluateJavascript("window.hideSelected();", null)
}

fun WebView.initializeReaderMode(context: Context, onInitialized: () -> Unit){
    val jsScript =  context.assets.open("reader.js").bufferedReader().use { it.readText() }
    val jsScript2 = """
        (function () {
            // Define a variable to store the original content
            if (!window.originalContent) {
                window.originalContent = document.body.innerHTML;
            }

            // Define a variable to track the reader mode state
            window.readerModeActive = window.readerModeActive || false;

            // Define the toggleReaderMode function
            window.toggleReaderMode = function () {
                if (window.readerModeActive) {
                    // Deactivate Reader Mode: Restore the original content
                    document.body.innerHTML = window.originalContent;
                    window.readerModeActive = false;
                    console.log('Reader Mode Deactivated');
                    return false; // Return false to indicate Reader Mode is inactive
                } else {
                    // Activate Reader Mode
                    const documentClone = document.cloneNode(true);
                    const article = new Readability(documentClone).parse();

                    if (article && article.content) {
                        // Save the current content in case it's not already saved
                        if (!window.originalContent) {
                            window.originalContent = document.body.innerHTML;
                        }

                        const readerContainer = document.createElement('div');
                        readerContainer.style.fontFamily = `'Georgia', serif`;
                        readerContainer.style.fontSize = '20px';
                        readerContainer.style.lineHeight = '1.8';
                        readerContainer.style.color = '#C6C4C4';
                        readerContainer.style.backgroundColor = '#2B2A2A';
                        readerContainer.style.padding = '40px';
                        readerContainer.style.maxWidth = '800px';
                        readerContainer.style.margin = '50px auto';
                        readerContainer.style.boxShadow = '0 4px 15px rgba(0, 0, 0, 0.3)';
                        readerContainer.id = 'reader-container';

                        const title = document.createElement('h1');
                        title.textContent = article.title || 'Untitled Article';
                        title.style.fontSize = '28px';
                        title.style.color = '#eee';
                        readerContainer.appendChild(title);

                        const content = document.createElement('div');
                        content.innerHTML = article.content;
                        content.querySelectorAll('img').forEach(img => img.remove());
                        readerContainer.appendChild(content);

                        const themeButton = document.createElement('button');
                        themeButton.innerHTML = '🌙'; // Start in dark mode
                        themeButton.style.position = 'fixed';
                        themeButton.style.top = '20px';
                        themeButton.style.right = '20px';
                        themeButton.style.padding = '10px';
                        themeButton.style.fontSize = '24px';
                        themeButton.style.cursor = 'pointer';
                        themeButton.style.border = 'none';
                        themeButton.style.borderRadius = '50%';
                        themeButton.style.boxShadow = '0 2px 5px rgba(0, 0, 0, 0.2)';
                        themeButton.style.backgroundColor = '#333';
                        themeButton.style.color = '#fff';
                        themeButton.id = 'theme-switch';

                        let darkMode = true; // Start in dark mode by default

                        themeButton.addEventListener('click', () => {
                            darkMode = !darkMode;

                            if (darkMode) {
                                readerContainer.style.color = '#C6C4C4';
                                readerContainer.style.backgroundColor = '#2B2A2A';
                                document.body.style.backgroundColor = '#151515';
                                title.style.color = '#eee';
                                content.querySelectorAll('p, a, h1, h2, h3, h4, h5, h6').forEach(el => {
                                    el.style.color = el.tagName === 'A' ? '#2955DE' : '#DDDDDD';
                                });
                                themeButton.innerHTML = '🌙';
                            } else {
                                readerContainer.style.color = '#333';
                                readerContainer.style.backgroundColor = '#f9f9f9';
                                document.body.style.backgroundColor = '#ffff';
                                title.style.color = '#222';
                                content.querySelectorAll('p, a, h1, h2, h3, h4, h5, h6').forEach(el => {
                                    el.style.color = el.tagName === 'A' ? '#1a73e8' : '#333';
                                });
                                themeButton.innerHTML = '☀️';
                            }
                        });

                        document.body.innerHTML = '';
                        document.body.style.backgroundColor = '#000';
                        document.body.appendChild(themeButton);
                        document.body.appendChild(readerContainer);

                        window.readerModeActive = true;
                        console.log('Reader Mode Activated');
                        return true; // Return true to indicate Reader Mode is active
                    } else {
                        console.error('Failed to parse the article content.');
                        return false; // Return false if activation failed
                    }
                }
            };
        })();

    """.trimIndent()
    evaluateJavascript(jsScript, null)
    evaluateJavascript(jsScript2){
        onInitialized()
    }
}

fun WebView.toggleReadMode(context: Context, callback: (Boolean) -> Unit){

    val checkScript = "typeof window.toggleReaderMode !== 'undefined';"
    evaluateJavascript(checkScript) { isInitialized ->
        if (isInitialized.toBoolean()) {
            evaluateJavascript("window.toggleReaderMode();"){ result ->
                val isReadModeEnabled = result?.toBoolean() ?: false
                callback(isReadModeEnabled)
            }
        } else {
            initializeReaderMode(context){
                evaluateJavascript("window.toggleReaderMode();") { result ->
                    val isReadModeEnabled = result?.toBoolean() ?: false
                    callback(isReadModeEnabled)
                }
            }

        }
    }

}
