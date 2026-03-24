package com.example.app_music

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class AdminSongAdapter(
    private var songList: ArrayList<Song>,
    private val onEditClick: (Song) -> Unit,
    private val onDeleteClick: (Song) -> Unit
) : RecyclerView.Adapter<AdminSongAdapter.AdminViewHolder>() {

    class AdminViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvAdminTitle)
        val tvArtist: TextView = itemView.findViewById(R.id.tvAdminArtist)
        val imgThumb: ImageView = itemView.findViewById(R.id.imgAdminThumb)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_song, parent, false)
        return AdminViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdminViewHolder, position: Int) {
        val song = songList[position]
        val context = holder.itemView.context

        holder.tvTitle.text = song.title
        holder.tvArtist.text = song.artist

        // Dùng Glide tải ảnh
        Glide.with(context)
            .load(song.image)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.ic_menu_gallery)
            .into(holder.imgThumb)

        holder.btnEdit.setOnClickListener { onEditClick(song) }
        holder.btnDelete.setOnClickListener { onDeleteClick(song) }
    }

    override fun getItemCount(): Int = songList.size

    fun updateData(newList: ArrayList<Song>) {
        songList = newList
        notifyDataSetChanged()
    }
}