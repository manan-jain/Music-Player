package com.mananJain.musicplayer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.audiofx.AudioEffect
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mananJain.musicplayer.databinding.ActivityPlayerBinding

class PlayerActivity : AppCompatActivity(), ServiceConnection, MediaPlayer.OnCompletionListener {

    companion object {
        lateinit var musicListPA : ArrayList<Music>
        var songPosition : Int = 0
        var isPlaying : Boolean = false
        var musicService : MusicService ?= null
        lateinit var binding : ActivityPlayerBinding
        var repeat : Boolean = false
        var min_15 : Boolean = false
        var min_30 : Boolean = false
        var min_60 : Boolean = false
        var nowPlayingId : String = ""
        var isFavorite : Boolean = false
        var fIndex : Int = -1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.coolPink)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)


        initializeLayout()

        /**
         * Accessing buttons from activity_player.xml
         */
        binding.backBtnPA.setOnClickListener {
            finish()
        }

        binding.playPauseBtnPA.setOnClickListener {
            if (isPlaying)
                pauseMusic()
            else
                playMusic()
        }

        binding.previousBtnPA.setOnClickListener { prevNextSong(increment = false) }

        binding.nextBtnPA.setOnClickListener { prevNextSong(increment = true) }

        binding.seekBarPA.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekbar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    musicService!!.mediaPlayer!!.seekTo(progress)
                }
            }

            // Here Unit means to do nothing when function is called
            override fun onStartTrackingTouch(p0: SeekBar?) = Unit
            override fun onStopTrackingTouch(p0: SeekBar?) = Unit

        })

        binding.repeatButtonPA.setOnClickListener {
            if (!repeat) {
                repeat = true
                binding.repeatButtonPA.setColorFilter(ContextCompat.getColor(this, R.color.purple_500))
            }
            else {
                repeat = false
                binding.repeatButtonPA.setColorFilter(ContextCompat.getColor(this, R.color.cool_pink))
            }
        }

        binding.equalizerBtnPA.setOnClickListener {
            try {
                val eqIntent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
                eqIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, musicService!!.mediaPlayer!!.audioSessionId)
                eqIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, baseContext.packageName)
                eqIntent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
                startActivityForResult(eqIntent, 13)
            }
            catch (e: Exception) {
                Toast.makeText(this, "Equalizer feature not supported", Toast.LENGTH_SHORT).show()
            }
        }

        binding.timerBtnPA.setOnClickListener {
            val timer = min_15 || min_30 || min_60
            if (!timer)
                showBottomSheetDialog()
            else {
                val builder = MaterialAlertDialogBuilder(this)
                builder.setTitle("Stop Timer")
                    .setMessage("Do you want to stop the timer?")
                    .setPositiveButton("Yes") {_ , _ ->
                        min_15 = false
                        min_30 = false
                        min_60 = false
                        binding.timerBtnPA.setColorFilter(ContextCompat.getColor(this, R.color.cool_pink))
                    }
                    .setNegativeButton("No") {dialog, _ ->
                        dialog.dismiss()
                    }

                val customDialog = builder.create()
                customDialog.show()
                customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
                customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
            }
        }

        binding.shareBtnPA.setOnClickListener {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.type = "audio/*"
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(musicListPA[songPosition].path))
            startActivity(Intent.createChooser(shareIntent, "Sharing Music File!!!"))
        }

        binding.favoriteBtnPA.setOnClickListener {
            if (isFavorite) {
                isFavorite = false
                binding.favoriteBtnPA.setImageResource(R.drawable.favourite_empty_icon)
                FavoriteActivity.favoriteSongs.removeAt(fIndex)
            } else {
                isFavorite = true
                binding.favoriteBtnPA.setImageResource(R.drawable.favourite_icon)
                FavoriteActivity.favoriteSongs.add(musicListPA[songPosition])
            }
        }
    }

    private fun createMediaPlayer() {
        try {
            if (musicService!!.mediaPlayer == null) musicService!!.mediaPlayer = MediaPlayer()
            musicService!!.mediaPlayer!!.reset()
            musicService!!.mediaPlayer!!.setDataSource(musicListPA[songPosition].path)
            musicService!!.mediaPlayer!!.prepare()
            musicService!!.mediaPlayer!!.start()
            isPlaying = true
            binding.playPauseBtnPA.setIconResource(R.drawable.pause_icon)
            musicService!!.showNotification(R.drawable.pause_icon)

            binding.tvSeekBarStart.text = formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
            binding.tvSeekBarEnd.text = formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
            binding.seekBarPA.progress = 0
            binding.seekBarPA.max = musicService!!.mediaPlayer!!.duration

            musicService!!.mediaPlayer!!.setOnCompletionListener(this)

            nowPlayingId = musicListPA[songPosition].id
        } catch (e: Exception) {
            return
        }
    }

    private fun setLayout() {
        fIndex = favoriteChecker(musicListPA[songPosition].id)

        Glide.with(this)
            .load(musicListPA[songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.music_player_icon_splash_screen).centerCrop())
            .into(binding.songImagePA)
        binding.songNamePA.text = musicListPA[songPosition].title

        if (repeat) {
            binding.repeatButtonPA.setColorFilter(ContextCompat.getColor(this, R.color.purple_500))
        }

        if (min_15 || min_30 || min_60) {
            binding.timerBtnPA.setColorFilter(ContextCompat.getColor(this, R.color.purple_500))
        }

        if (isFavorite) {
            binding.favoriteBtnPA.setImageResource(R.drawable.favourite_icon)
        } else {
            binding.favoriteBtnPA.setImageResource(R.drawable.favourite_empty_icon)
        }
    }

    // Important Function
    private fun initializeLayout() {
        songPosition = intent.getIntExtra("index", 0)
        when (intent.getStringExtra("class")) {

            "FavoriteAdapter" -> {
                // For starting service
                val intent = Intent(this, MusicService::class.java)
                bindService(intent, this, BIND_AUTO_CREATE)
                startService(intent)

                musicListPA = ArrayList()
                musicListPA.addAll(FavoriteActivity.favoriteSongs)
                setLayout()
            }

            "NowPlaying" -> {
                setLayout()
                binding.tvSeekBarStart.text = formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
                binding.tvSeekBarEnd.text = formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
                binding.seekBarPA.progress = musicService!!.mediaPlayer!!.currentPosition
                binding.seekBarPA.max = musicService!!.mediaPlayer!!.duration

                if (isPlaying) {
                    binding.playPauseBtnPA.setIconResource(R.drawable.pause_icon)
                } else {
                    binding.playPauseBtnPA.setIconResource(R.drawable.play_icon)
                }
            }

            "MusicAdapterSearch" -> {
                // For starting service
                val intent = Intent(this, MusicService::class.java)
                bindService(intent, this, BIND_AUTO_CREATE)
                startService(intent)

                musicListPA = ArrayList()
                musicListPA.addAll(MainActivity.musicListSearch)
                setLayout()
            }

            "MusicAdapter" -> {
                // For starting service
                val intent = Intent(this, MusicService::class.java)
                bindService(intent, this, BIND_AUTO_CREATE)
                startService(intent)

                musicListPA = ArrayList()
                musicListPA.addAll(MainActivity.MusicListMA)
                setLayout()
            }

            "MainActivity" -> {
                // For starting service
                val intent = Intent(this, MusicService::class.java)
                bindService(intent, this, BIND_AUTO_CREATE)
                startService(intent)

                musicListPA = ArrayList()
                musicListPA.addAll(MainActivity.MusicListMA)
                musicListPA.shuffle()
                setLayout()
            }

            "FavoriteShuffle" -> {
                // For starting service
                val intent = Intent(this, MusicService::class.java)
                bindService(intent, this, BIND_AUTO_CREATE)
                startService(intent)

                musicListPA = ArrayList()
                musicListPA.addAll(FavoriteActivity.favoriteSongs)
                musicListPA.shuffle()
                setLayout()
            }

            "PlaylistDetailsAdapter" -> {
                // For starting service
                val intent = Intent(this, MusicService::class.java)
                bindService(intent, this, BIND_AUTO_CREATE)
                startService(intent)

                musicListPA = ArrayList()
                musicListPA.addAll(PlaylistActivity.musicPlaylist.ref[PlaylistDetailsActivity.currentPlaylistPosition].playlist)
                setLayout()
            }

            "PlaylistDetailsShuffle" -> {
                // For starting service
                val intent = Intent(this, MusicService::class.java)
                bindService(intent, this, BIND_AUTO_CREATE)
                startService(intent)

                musicListPA = ArrayList()
                musicListPA.addAll(FavoriteActivity.favoriteSongs)
                musicListPA.shuffle()
                setLayout()
            }
        }
    }

    private fun playMusic() {
        binding.playPauseBtnPA.setIconResource(R.drawable.pause_icon)
        musicService!!.showNotification(R.drawable.pause_icon)
        isPlaying = true
        musicService!!.mediaPlayer!!.start()
    }

    private fun pauseMusic() {
        binding.playPauseBtnPA.setIconResource(R.drawable.play_icon)
        musicService!!.showNotification(R.drawable.play_icon)
        isPlaying = false
        musicService!!.mediaPlayer!!.pause()
    }

    private fun prevNextSong(increment : Boolean) {
        if (increment) {
            setSongPosition(increment = true)
            setLayout()
            createMediaPlayer()
        }
        else {
            setSongPosition(increment = false)
            setLayout()
            createMediaPlayer()
        }
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as MusicService.MyBinder
        musicService = binder.currentService()
        createMediaPlayer()
        musicService!!.seekBarSetup()
        musicService!!.audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        musicService!!.audioManager.requestAudioFocus(musicService, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        musicService = null
    }

    // After song is completed and play next song
    override fun onCompletion(mp: MediaPlayer?) {
        setSongPosition(increment = true)
        createMediaPlayer()
        try {
            setLayout()
        } catch (e : Exception) {
            return
        }
    }

    // For accessing Equalizer
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 13 || resultCode == RESULT_OK) {
            return
        }
    }

    // For showing dialog box of timer button
    private fun showBottomSheetDialog() {
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(R.layout.bottom_sheet_dialog)
        dialog.show()

        dialog.findViewById<LinearLayout>(R.id.min_15)?.setOnClickListener {
            Toast.makeText(this, "Music will stop after 15 minutes", Toast.LENGTH_SHORT).show()
            binding.timerBtnPA.setColorFilter(ContextCompat.getColor(this, R.color.purple_500))
            min_15 = true

            // For exiting app after timer
            Thread {
                Thread.sleep((15 * 60000).toLong())
                if (min_15)
                    exitApplication()
            }.start()
            dialog.dismiss()
        }

        dialog.findViewById<LinearLayout>(R.id.min_30)?.setOnClickListener {
            Toast.makeText(this, "Music will stop after 30 minutes", Toast.LENGTH_SHORT).show()
            binding.timerBtnPA.setColorFilter(ContextCompat.getColor(this, R.color.purple_500))
            min_30 = true

            // For exiting app after timer
            Thread {
                Thread.sleep((30 * 60000).toLong())
                if (min_30)
                    exitApplication()
            }.start()
            dialog.dismiss()
        }

        dialog.findViewById<LinearLayout>(R.id.min_60)?.setOnClickListener {
            Toast.makeText(this, "Music will stop after 60 minutes", Toast.LENGTH_SHORT).show()
            binding.timerBtnPA.setColorFilter(ContextCompat.getColor(this, R.color.purple_500))
            min_60 = true

            // For exiting app after timer
            Thread {
                Thread.sleep((60 * 60000).toLong())
                if (min_60)
                    exitApplication()
            }.start()
            dialog.dismiss()
        }
    }
}