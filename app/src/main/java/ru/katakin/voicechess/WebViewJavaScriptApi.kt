package ru.katakin.voicechess

import android.webkit.JavascriptInterface

open class WebViewJavaScriptApi(private val delegate: Delegate?) {

    interface Delegate {
        fun getMessage(paramsJsonObj: String)
    }

    @JavascriptInterface
    fun getMessage(paramsJsonObj: String) {
        delegate?.getMessage(paramsJsonObj)
    }
}