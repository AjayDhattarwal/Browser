package com.ar.webwiz.utils.youtube

import android.util.Log
import android.webkit.WebView

fun WebView.toggleYTControls(boolean: Boolean = true){
    if(url?.contains("youtube.com") == true && boolean){
        Log.d("YTControls", "Toggling YT Controls")
        val script = """
            (function() {
                var styleElement = document.getElementById("stylehidecontrols");

                if (styleElement) {
                    styleElement.remove();
                } else {
                
                    styleElement = document.createElement("style");
                    styleElement.id = "stylehidecontrols";
                    
                    var cssRules = `
                        #movie_player .ytp-gradient-top,
                        #movie_player .ytp-gradient-bottom,
                        #movie_player .ytp-chrome-top,
                        #movie_player .ytp-chrome-bottom {
                            display: none !important;
                        }
                    `;
                    
                    styleElement.appendChild(document.createTextNode(cssRules));
                    document.body.appendChild(styleElement);
                }
            })();

        """

        evaluateJavascript(script, null)
    }
}