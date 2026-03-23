package com.example.app_music

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SongAdapter(private var songList: ArrayList<Song>) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvItemTitle)
        val tvArtist: TextView = itemView.findViewById(R.id.tvItemArtist)
        val imgItemThumb: ImageView = itemView.findViewById(R.id.imgItemThumb)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_song, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songList[position]
        val context = holder.itemView.context

        holder.tvTitle.text = song.title
        holder.tvArtist.text = song.artist

        // ĐÃ SỬA: Tìm ảnh theo song.image
        val imageResId = context.resources.getIdentifier(song.image, "drawable", context.packageName)
        if (imageResId != 0) {
            holder.imgItemThumb.setImageResource(imageResId)
        } else {
            holder.imgItemThumb.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, PlayerActivity::class.java)
            intent.putExtra("SONG_TITLE", song.title)
            intent.putExtra("SONG_ARTIST", song.artist)
            intent.putExtra("SONG_LYRICS", song.lyrics)
            intent.putExtra("SONG_PATH", song.path)
            intent.putExtra("SONG_IMAGE", song.image) // ĐÃ THÊM
            intent.putExtra("song_id", song.id)
            intent.putExtra("song_likes", song.likes)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = songList.size

    fun updateList(newList: ArrayList<Song>) {
        songList = newList
        notifyDataSetChanged()
    }
}