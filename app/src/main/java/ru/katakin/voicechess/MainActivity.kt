package ru.katakin.voicechess

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        main_toggleswitch_user.setCheckedPosition(0)

        main_button_settings.setOnClickListener {
            val dialogFragment =
                supportFragmentManager.findFragmentByTag(SettingsDialogFragment.TAG) as? SettingsDialogFragment
                    ?: SettingsDialogFragment()
            dialogFragment.show(supportFragmentManager, SettingsDialogFragment.TAG)
        }

        main_button_start_game.setOnClickListener {
            val intent = Intent(this, ChessActivity::class.java)
            intent.putExtra(
                "user",
                if (main_toggleswitch_user.getCheckedPosition() == 0) "white" else "black"
            )
            startActivity(intent)
        }
    }
}