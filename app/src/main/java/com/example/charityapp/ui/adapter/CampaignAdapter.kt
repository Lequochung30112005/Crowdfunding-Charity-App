package com.example.charityapp.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.charityapp.databinding.ItemCampaignBinding
import com.example.charityapp.models.Campaign
import com.bumptech.glide.Glide // Thêm thư viện Glide nếu bạn muốn load ảnh qua URL

class CampaignAdapter(
    private val campaignList: List<Campaign>,
    private val onItemClick: (Campaign) -> Unit
) : RecyclerView.Adapter<CampaignAdapter.CampaignViewHolder>() {

    inner class CampaignViewHolder(private val binding: ItemCampaignBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(campaign: Campaign) {
            binding.tvCampaignTitle.text = campaign.title

            // Hiển thị tiến độ quyên góp và thanh progress
            binding.progressDonation.progress = ((campaign.currentAmount / campaign.targetAmount) * 100).toInt()
            binding.tvProgressText.text = "Đã quyên góp: ${campaign.currentAmount}đ / ${campaign.targetAmount}đ"

            // Load ảnh bằng Glide (hoặc dùng ảnh mặc định nếu chưa có Glide)
            Glide.with(binding.ivCampaignImage.context)
                .load(campaign.imageUrl)
                .into(binding.ivCampaignImage)

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