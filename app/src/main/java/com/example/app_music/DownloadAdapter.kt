package com.example.app_music

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class DownloadAdapter(private val fileList: ArrayList<File>) : RecyclerView.Adapter<DownloadAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Ánh xạ các thành phần từ item_song.xml
        val tvTitle: TextView = itemView.findViewById(R.id.tvItemTitle)
        val tvArtist: TextView = itemView.findViewById(R.id.tvItemArtist)
        val imgItemThumb: ImageView = itemView.findViewById(R.id.imgItemThumb)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_song, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val file = fileList[position]
        val context = holder.itemView.context

        // Tên file của bạn đang lưu có dạng "Tên bài hát - Ca sĩ.mp3"
        val fileName = file.nameWithoutExtension
        val parts = fileName.split(" - ")

        if (parts.size >= 2) {
            holder.tvTitle.text = parts[0]
            holder.tvArtist.text = parts[1]
        } else {
            holder.tvTitle.text = fileName
            holder.tvArtist.text = "Unknown"
        }

        // Đổi icon ảnh đại diện thành icon Download
        holder.imgItemThumb.setImageResource(android.R.drawable.stat_sys_download_done)

        holder.itemView.setOnClickListener {
            // Tạm thời hiển thị đường dẫn file.
            // Nếu bạn muốn phát nhạc trực tiếp từ đây, PlayerActivity sẽ cần nâng cấp để đọc đường dẫn cục bộ (thay vì R.raw).
            Toast.makeText(context, "File đã lưu tại: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        }
    }

    override fun getItemCount(): Int = fileList.size
}