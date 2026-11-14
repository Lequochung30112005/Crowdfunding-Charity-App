package com.example.charityapp.ui.adapter

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.charityapp.databinding.ItemCampaignBinding
import com.example.charityapp.models.Campaign

class CampaignAdapter(
    private var campaignList: List<Campaign>,
    private val onItemClick: (Campaign) -> Unit
) : RecyclerView.Adapter<CampaignAdapter.CampaignViewHolder>() {

    inner class CampaignViewHolder(private val binding: ItemCampaignBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(campaign: Campaign) {
            binding.tvCampaignTitle.text = campaign.title

            // Hiển thị tiến độ quyên góp và thanh progress
            val target = if (campaign.targetAmount > 0) campaign.targetAmount else 1.0
            val percent = ((campaign.currentAmount / target) * 100).toInt()
            binding.progressDonation.progress = percent.coerceIn(0, 100)

            binding.tvProgressText.text = "Đã quyên góp: ${String.format("%,.0f", campaign.currentAmount)}đ / ${String.format("%,.0f", campaign.targetAmount)}đ"

            val imageUrl = campaign.imageUrl

            // Xóa sạch ảnh cũ trước khi gán mới để tránh nháy ảnh khi cuộn danh sách
            binding.ivCampaignImage.setImageDrawable(null)

            if (!imageUrl.isNullOrEmpty()) {
                // 🛑 KIỂM TRA AN TOÀN TUYỆT ĐỐI:
                // Nếu là URL mạng chuẩn bắt đầu bằng http/https thì mới dùng Glide.
                if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
                    Glide.with(binding.ivCampaignImage.context)
                        .load(imageUrl)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_gallery)
                        .into(binding.ivCampaignImage)
                }
                else if (imageUrl.startsWith("content://") || imageUrl.startsWith("file://")) {
                    // Xử lý Uri nội bộ bằng InputStream an toàn
                    try {
                        val uri = Uri.parse(imageUrl)
                        val inputStream = binding.ivCampaignImage.context.contentResolver.openInputStream(uri)
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        inputStream?.close()

                        if (bitmap != null) {
                            binding.ivCampaignImage.setImageBitmap(bitmap)
                        } else {
                            binding.ivCampaignImage.setImageResource(android.R.drawable.ic_menu_gallery)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        binding.ivCampaignImage.setImageResource(android.R.drawable.ic_menu_gallery)
                    }
                }
                else {
                    // 🖼️ TOÀN BỘ CHUỖI BASE64 TRONG FIRESTORE (Như trong ảnh của bạn) SẼ CHẠY VÀO ĐÂY.
                    // Tuyệt đối KHÔNG QUA GLIDE, nên sẽ không bao giờ có lỗi ENAMETOOLONG nữa!
                    try {
                        val cleanStr = if (imageUrl.contains(",")) {
                            imageUrl.substringAfter(",")
                        } else {
                            imageUrl
                        }
                        val finalStr = cleanStr.replace("\\s".toRegex(), "")
                        val decodedBytes = Base64.decode(finalStr, Base64.DEFAULT)
                        val decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

                        if (decodedBitmap != null) {
                            binding.ivCampaignImage.setImageBitmap(decodedBitmap)
                        } else {
                            binding.ivCampaignImage.setImageResource(android.R.drawable.ic_menu_gallery)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        binding.ivCampaignImage.setImageResource(android.R.drawable.ic_menu_gallery)
                    }
                }
            } else {
                binding.ivCampaignImage.setImageResource(android.R.drawable.ic_menu_gallery)
            }

            // Xử lý sự kiện click vào item
            binding.root.setOnClickListener {
                onItemClick(campaign)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CampaignViewHolder {
        val binding = ItemCampaignBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CampaignViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CampaignViewHolder, position: Int) {
        holder.bind(campaignList[position])
    }

    override fun getItemCount(): Int = campaignList.size

    fun updateData(newList: List<Campaign>) {
        campaignList = newList
        notifyDataSetChanged()
    }
}