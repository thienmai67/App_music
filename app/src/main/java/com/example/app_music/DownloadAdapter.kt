package com.example.app_music

import android.app.AlertDialog
import android.content.Intent
import android.graphics.BitmapFactory
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

        // Tách tên bài hát và ca sĩ từ tên file
        val fileName = file.nameWithoutExtension
        val parts = fileName.split(" - ")

        if (parts.size >= 2) {
            holder.tvTitle.text = parts[0].trim()
            holder.tvArtist.text = parts[1].trim()
        } else {
            holder.tvTitle.text = fileName
            holder.tvArtist.text = "Unknown"
        }

        // --- TÌM VÀ HIỂN THỊ ẢNH BÌA TỪ BỘ NHỚ ---
        val imageFile = File(file.parentFile, "$fileName.jpg")
        if (imageFile.exists()) {
            val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
            holder.imgItemThumb.setImageBitmap(bitmap)
        } else {
            // Nếu không có ảnh tải kèm, dùng icon mặc định
            holder.imgItemThumb.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        // SỰ KIỆN 1: BẤM BÌNH THƯỜNG ĐỂ NGHE NHẠC
        holder.itemView.setOnClickListener {
            val intent = Intent(context, PlayerActivity::class.java)

            if (parts.size >= 2) {
                intent.putExtra("SONG_TITLE", parts[0].trim())
                intent.putExtra("SONG_ARTIST", parts[1].trim())
            } else {
                intent.putExtra("SONG_TITLE", fileName)
                intent.putExtra("SONG_ARTIST", "Unknown")
            }

            // Truyền đường dẫn nhạc và đường dẫn ảnh local
            intent.putExtra("SONG_LOCAL_PATH", file.absolutePath)
            if (imageFile.exists()) {
                intent.putExtra("SONG_LOCAL_IMAGE", imageFile.absolutePath)
            }

            context.startActivity(intent)
        }

        // SỰ KIỆN 2: NHẤN GIỮ ĐỂ XÓA BÀI HÁT
        holder.itemView.setOnLongClickListener {
            AlertDialog.Builder(context)
                .setTitle("Xóa bài hát")
                .setMessage("Bạn có chắc chắn muốn xóa bài hát '${holder.tvTitle.text}' khỏi máy không?")
                .setPositiveButton("Xóa") { _, _ ->
                    val currentPos = holder.adapterPosition
                    if (currentPos != RecyclerView.NO_POSITION) {

                        // 1. Thực hiện xóa file MP3 và KIỂM TRA KẾT QUẢ
                        val isDeleted = !file.exists() || file.delete()

                        if (isDeleted) {
                            // 2. Xóa file ẢNH JPG đi kèm trong máy (nếu có)
                            if (imageFile.exists()) imageFile.delete()

                            // 3. Xóa khỏi danh sách hiển thị và cập nhật lại giao diện
                            fileList.removeAt(currentPos)
                            notifyItemRemoved(currentPos)
                            notifyItemRangeChanged(currentPos, fileList.size)

                            Toast.makeText(context, "Đã xóa bài hát thành công!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Không thể xóa! Hệ điều hành từ chối quyền.", Toast.LENGTH_LONG).show()
                        }
                    }
                }
                .setNegativeButton("Hủy", null)
                .show()

            true // Trả về true để hệ thống biết sự kiện "nhấn giữ" đã được xử lý xong
        }
    }

    override fun getItemCount(): Int = fileList.size
}