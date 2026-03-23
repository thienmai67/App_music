package com.example.app_music

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AdminSongAdapter(
    private var songList: ArrayList<Song>,
    // Hai callback (hàm ẩn danh) để truyền sự kiện click ra bên ngoài (về AdminActivity)
    private val onEditClick: (Song) -> Unit,
    private val onDeleteClick: (Song) -> Unit
) : RecyclerView.Adapter<AdminSongAdapter.AdminViewHolder>() {

    // Lớp ViewHolder giúp ánh xạ các thành phần giao diện của từng dòng (item_admin_song)
    class AdminViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvAdminTitle)
        val tvArtist: TextView = itemView.findViewById(R.id.tvAdminArtist)
        val imgThumb: ImageView = itemView.findViewById(R.id.imgAdminThumb)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminViewHolder {
        // Nạp giao diện item_admin_song.xml cho mỗi hàng
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_song, parent, false)
        return AdminViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdminViewHolder, position: Int) {
        val song = songList[position]
        val context = holder.itemView.context

        // Gắn dữ liệu Text
        holder.tvTitle.text = song.title
        holder.tvArtist.text = song.artist

        // Xử lý load ảnh động theo tên file lưu trong biến song.image
        val imageResId = context.resources.getIdentifier(song.image, "drawable", context.packageName)
        if (imageResId != 0) {
            holder.imgThumb.setImageResource(imageResId)
        } else {
            // Ảnh mặc định nếu không tìm thấy file
            holder.imgThumb.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        // Bắt sự kiện click cho 2 nút Sửa và Xóa, truyền đối tượng Song hiện tại ra ngoài
        holder.btnEdit.setOnClickListener { onEditClick(song) }
        holder.btnDelete.setOnClickListener { onDeleteClick(song) }
    }

    override fun getItemCount(): Int = songList.size

    // Hàm hỗ trợ cập nhật lại danh sách (được gọi từ AdminActivity khi tìm kiếm hoặc thêm/sửa/xóa)
    fun updateData(newList: ArrayList<Song>) {
        songList = newList
        notifyDataSetChanged()
    }
}