package com.mananJain.musicplayer

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.GsonBuilder
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

        // Checking whether song is present in mobile or not
        PlaylistActivity.musicPlaylist.ref[currentPlaylistPosition].playlist =
            checkPlaylist(playlist = PlaylistActivity.musicPlaylist.ref[currentPlaylistPosition].playlist)

        // Initializing Recycler View
        binding.playlistDetailsRV.setItemViewCacheSize(10)
        binding.playlistDetailsRV.setHasFixedSize(true)
        binding.playlistDetailsRV.layoutManager = LinearLayoutManager(this)

        adapter = MusicAdapter(this, PlaylistActivity.musicPlaylist.ref[currentPlaylistPosition].playlist, true)
        binding.playlistDetailsRV.adapter = adapter

        // Adding back button functionality
        binding.backBtnPD.setOnClickListener {
            finish()    // This is enough for going back
        }

        // For shuffle button
        binding.shuffleBtnPD.setOnClickListener {
            val intent = Intent(this, PlayerActivity::class.java)

            intent.putExtra("index", 0)
            intent.putExtra("class", "PlaylistDetailsShuffle")

            startActivity(intent)
        }

        // For adding songs
        binding.addBtnPD.setOnClickListener {
            startActivity(Intent(this, SelectionActivity::class.java))
        }

        // For removing songs
        binding.removeAllPD.setOnClickListener {
            val builder = MaterialAlertDialogBuilder(this)
            builder.setTitle("Remove")
                .setMessage("Do you want to remove all songs from playlist?")
                .setPositiveButton("Yes") { dialog , _ ->
                    PlaylistActivity.musicPlaylist.ref[currentPlaylistPosition].playlist.clear()
                    adapter.refreshPlaylist()
                    dialog.dismiss()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }

            val customDialog = builder.create()
            customDialog.show()
            customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
            customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
        }
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
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
        adapter.notifyDataSetChanged()

        // For storing favorites data using Shared Preferences
        val editor = getSharedPreferences("FAVORITES", MODE_PRIVATE).edit()

        val jsonStringPlaylist = GsonBuilder().create().toJson(PlaylistActivity.musicPlaylist)
        editor.putString("MusicPlaylist", jsonStringPlaylist)
        editor.apply()
    }
}