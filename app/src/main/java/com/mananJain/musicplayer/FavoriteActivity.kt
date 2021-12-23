package com.mananJain.musicplayer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.mananJain.musicplayer.databinding.ActivityFavoriteBinding

class FavoriteActivity : AppCompatActivity() {

    private lateinit var binding : ActivityFavoriteBinding
    private lateinit var adapter: FavoriteAdapter

    companion object {
        var favoriteSongs : ArrayList<Music> = ArrayList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(MainActivity.currentTheme[MainActivity.themeIndex])
        binding = ActivityFavoriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Checking whether song is present in mobile or not
        favoriteSongs = checkPlaylist(favoriteSongs)

        binding.backBtnFA.setOnClickListener {
            finish()
        }

        binding.favoriteRV.setHasFixedSize(true)
        binding.favoriteRV.setItemViewCacheSize(13)
        binding.favoriteRV.layoutManager = GridLayoutManager(this, 4)

        adapter = FavoriteAdapter(this, favoriteSongs)
        binding.favoriteRV.adapter = adapter

        if (favoriteSongs.size < 1) {
            binding.shuffleBtnFA.visibility = View.INVISIBLE
        }

        binding.shuffleBtnFA.setOnClickListener {
            val intent = Intent(this, PlayerActivity::class.java)

            intent.putExtra("index", 0)
            intent.putExtra("class", "FavoriteShuffle")

            startActivity(intent)
        }
    }
}