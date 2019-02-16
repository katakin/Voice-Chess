package ru.katakin.voicechess

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_chess.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit
import android.webkit.WebViewClient
import com.google.gson.JsonParser

class ChessActivity : AppCompatActivity(), WebViewJavaScriptApi.Delegate {

    private lateinit var textToSpeech: TextToSpeech

    private val disposable: CompositeDisposable = CompositeDisposable()

    private val gson: Gson by lazy { GsonBuilder().setLenient().create() }

    private val httpLoggingInterceptor: HttpLoggingInterceptor by lazy { HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY } }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .readTimeout(15, TimeUnit.SECONDS)
            .connectTimeout(15, TimeUnit.SECONDS)
            .addNetworkInterceptor(httpLoggingInterceptor)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .addConverterFactory(GsonConverterFactory.create(gson))
            .baseUrl(Global.SERVER_URL)
            .client(okHttpClient)
            .build()
    }

    private lateinit var user: String

    private val serverApi: ServerApi by lazy { retrofit.create(ServerApi::class. java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chess)

        if (savedInstanceState == null) {
            user = intent.getStringExtra("user")
        } else {
            user = savedInstanceState.getString("user", "white")
        }

        main_toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        chess_webview_site.settings.javaScriptEnabled = true
        chess_webview_site.addJavascriptInterface(object : WebViewJavaScriptApi(this) {}, "Android")
        chess_webview_site.webViewClient = object : WebViewClient() {}
        chess_webview_site.loadUrl(Global.WEBVIEW_URL)

        textToSpeech = TextToSpeech(applicationContext, TextToSpeech.OnInitListener { status ->
            if (status != TextToSpeech.ERROR) {
                textToSpeech.language = Locale("ru", "RU")
            }
        })

        chess_imageview_speach_to_text.setOnClickListener {
            startVoiceInput()
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putString("user", user)
    }

    public override fun onDestroy() {
        super.onDestroy()
        textToSpeech.shutdown()
        disposable.clear()
    }

    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setMessage("Вы точно хотите выйти?")
            .setNegativeButton("Нет", null)
            .setPositiveButton("Да") { _: DialogInterface, _: Int ->
                super.onBackPressed()
            }.show()
    }

    override fun postMessage(paramsJsonObj: String) {
        val jsonObject = JsonParser().parse(paramsJsonObj).asJsonObject
        val text = jsonObject.get("text")?.asString
        if (!text.isNullOrBlank()) {
            startVoiceOutput(text)
        }
    }

    private fun startVoiceInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale("ru", "RU"))
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Куда будем ходить?")
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT)
        } catch (a: ActivityNotFoundException) {
        }
    }

    private fun sendRequest() {
        disposable.add(
            serverApi.move(
                user,
                mapOf("text" to "${chess_textview_text.text}")
            )
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { chess_container_progress.visibility = View.VISIBLE }
                .doAfterTerminate { chess_container_progress.visibility = View.GONE }
                .subscribe(
                    {
                        val message = it.string()
                        Toast.makeText(applicationContext, "Success: $message", Toast.LENGTH_SHORT).show()
                    },
                    {
                        Toast.makeText(applicationContext, "Error: $it", Toast.LENGTH_SHORT).show()
                    }
                )
        )
    }

    private fun startVoiceOutput(text: String) {
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQ_CODE_SPEECH_INPUT -> {
                if (resultCode == Activity.RESULT_OK && null != data) {
                    val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    chess_textview_text.text = result[0]
                    sendRequest()
                }
            }
        }
    }

    companion object {
        private const val REQ_CODE_SPEECH_INPUT = 100
    }
}