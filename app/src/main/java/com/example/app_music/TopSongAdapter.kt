package com.example.app_music

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TopSongAdapter(private val songList: ArrayList<Song>) : RecyclerView.Adapter<TopSongAdapter.TopSongViewHolder>() {

    class TopSongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvRank: TextView = itemView.findViewById(R.id.tvRank)
        val tvTopTitle: TextView = itemView.findViewById(R.id.tvTopTitle)
        val tvTopArtist: TextView = itemView.findViewById(R.id.tvTopArtist)
        val tvLikeCount: TextView = itemView.findViewById(R.id.tvLikeCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopSongViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_top_song, parent, false)
        return TopSongViewHolder(view)
    }

    override fun onBindViewHolder(holder: TopSongViewHolder, position: Int) {
        val song = songList[position]
        val rank = position + 1

        holder.tvRank.text = rank.toString()
        holder.tvTopTitle.text = song.title
        holder.tvTopArtist.text = song.artist
        holder.tvLikeCount.text = "${song.likes} lượt thích"

        when (rank) {
            1 -> holder.tvRank.setTextColor(Color.parseColor("#FFD700"))
            2 -> holder.tvRank.setTextColor(Color.parseColor("#C0C0C0"))
            3 -> holder.tvRank.setTextColor(Color.parseColor("#CD7F32"))
            else -> holder.tvRank.setTextColor(Color.parseColor("#757575"))
        }

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, PlayerActivity::class.java)

            intent.putExtra("song_id", song.id)
            intent.putExtra("SONG_TITLE", song.title)
            intent.putExtra("SONG_ARTIST", song.artist)
            intent.putExtra("SONG_PATH", song.path)
            intent.putExtra("SONG_IMAGE", song.image) // ĐÃ THÊM
            intent.putExtra("SONG_LYRICS", song.lyrics)
            intent.putExtra("song_likes", song.likes)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = songList.size
}