package ru.katakin.voicechess

import android.webkit.JavascriptInterface

open class WebViewJavaScriptApi(private val delegate: Delegate?) {

    interface Delegate {
        fun postMessage(paramsJsonObj: String)
    }

    @JavascriptInterface
    fun postMessage(paramsJsonObj: String) {
        delegate?.postMessage(paramsJsonObj)
    }
}