package com.mananJain.musicplayer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.mananJain.musicplayer.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {

    lateinit var binding : ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(MainActivity.currentThemeNav[MainActivity.themeIndex])
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "About"

        binding.aboutText.text = aboutText()
    }

    private fun aboutText() : String {
        return "Developed by: Manan Jain" +
                "\n\nIf you want to provide any feedback, I'll love to hear that."
    }
}