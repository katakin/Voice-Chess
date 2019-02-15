package ru.katakin.voicechess

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText

class SettingsDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(activity).inflate(R.layout.settings_dialog, null)
        val builder = AlertDialog.Builder(activity!!)

        val serverEditText = view.findViewById<EditText>(R.id.settings_edittext_server_url)
        serverEditText.setText(Global.SERVER_URL)
        val webviewEditText = view.findViewById<EditText>(R.id.settings_edittext_webview_url)
        webviewEditText.setText(Global.WEBVIEW_URL)

        view.findViewById<Button>(R.id.settings_button_cancel).setOnClickListener {
            dismiss()
        }

        view.findViewById<Button>(R.id.settings_button_ok).setOnClickListener {
            Global.SERVER_URL = serverEditText.text.toString()
            Global.WEBVIEW_URL = webviewEditText.text.toString()
            dismiss()
        }

        builder.setView(view)
        isCancelable = false

        return builder.create()
    }

    companion object {
        const val TAG = "SettingsDialog"
    }
}