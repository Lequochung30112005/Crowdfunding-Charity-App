package com.example.charityapp.ui.adapter

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.charityapp.R
import com.example.charityapp.databinding.ItemCampaignBinding
import com.example.charityapp.models.Campaign

class CampaignAdapter(
    private val campaignList: List<Campaign>,
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

            // ⚡ XỬ LÝ HIỂN THỊ ẢNH (HỖ TRỢ CẢ URL WEB VÀ CHUỖI BASE64)
            val imageUrl = campaign.imageUrl
            if (!imageUrl.isNullOrEmpty()) {
                if (imageUrl.startsWith("http")) {
                    // Nếu là đường dẫn mạng (http/https)
                    Glide.with(binding.ivCampaignImage.context)
                        .load(imageUrl)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_gallery)
                        .into(binding.ivCampaignImage)
                } else {
                    // Nếu là chuỗi mã hóa Base64 do Admin tạo ra
                    try {
                        val decodedBytes = Base64.decode(imageUrl, Base64.DEFAULT)
                        val decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                        binding.ivCampaignImage.setImageBitmap(decodedBitmap)
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
}