package com.mananJain.musicplayer

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.mananJain.musicplayer.databinding.ActivityPlaylistDetailsBinding

class PlaylistDetailsActivity : AppCompatActivity() {

    lateinit var binding : ActivityPlaylistDetailsBinding
    lateinit var adapter : MusicAdapter

    companion object {
        var currentPlaylistPosition : Int = -1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTheme(R.style.coolPink)

        binding = ActivityPlaylistDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentPlaylistPosition = intent.extras?.get("index") as Int

        // Initializing Recycler View
        binding.playlistDetailsRV.setItemViewCacheSize(10)
        binding.playlistDetailsRV.setHasFixedSize(true)
        binding.playlistDetailsRV.layoutManager = LinearLayoutManager(this)

        PlaylistActivity.musicPlaylist.ref[currentPlaylistPosition].playlist.addAll(MainActivity.MusicListMA)
        PlaylistActivity.musicPlaylist.ref[currentPlaylistPosition].playlist.shuffle()
        adapter = MusicAdapter(this, PlaylistActivity.musicPlaylist.ref[currentPlaylistPosition].playlist, true)
        binding.playlistDetailsRV.adapter = adapter

        // Adding back button functionality
        binding.backBtnPD.setOnClickListener {
            finish()    // This is enough for going back
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()
        binding.playNamePD.text = PlaylistActivity.musicPlaylist.ref[currentPlaylistPosition].name
        binding.moreInfoPD.text = "Total ${adapter.itemCount} Songs.\n\n" +
                "Created On:\n${PlaylistActivity.musicPlaylist.ref[currentPlaylistPosition].createdOn}\n\n" +
                "  -- ${PlaylistActivity.musicPlaylist.ref[currentPlaylistPosition].createdBy}"

        if (adapter.itemCount > 0) {
            Glide.with(this)
                .load(PlaylistActivity.musicPlaylist.ref[currentPlaylistPosition].playlist[0].artUri)
                .apply(RequestOptions().placeholder(R.drawable.music_player_icon_splash_screen).centerCrop())
                .into(binding.playlistImgPD)

            binding.shuffleBtnPD.visibility = View.VISIBLE
        }
    }
}