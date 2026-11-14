package com.example.charityapp.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.charityapp.databinding.ItemDisbursementReportBinding
import com.example.charityapp.models.DisbursementReport

class DisbursementAdapter(
    private val list: List<DisbursementReport>
) : RecyclerView.Adapter<DisbursementAdapter.DisbursementViewHolder>() {

    inner class DisbursementViewHolder(val binding: ItemDisbursementReportBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DisbursementViewHolder {
        val binding = ItemDisbursementReportBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DisbursementViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DisbursementViewHolder, position: Int) {
        val item = list[position]
        holder.binding.apply {
            tvDisbursementTitle.text = item.title
            tvDisbursementDate.text = item.date
            tvDisbursementContent.text = item.content

            val imageUrl = item.imageUrl
            if (!imageUrl.isNullOrEmpty()) {
                if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
                    Glide.with(root.context)
                        .load(imageUrl)
                        .centerCrop()
                        .into(imgDisbursement)
                } else {
                    try {
                        val cleanStr = if (imageUrl.contains(",")) imageUrl.substringAfter(",") else imageUrl
                        val decodedBytes = android.util.Base64.decode(cleanStr.replace("\\s".toRegex(), ""), android.util.Base64.DEFAULT)
                        val decodedBitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                        if (decodedBitmap != null) {
                            Glide.with(root.context)
                                .load(decodedBitmap)
                                .centerCrop()
                                .into(imgDisbursement)
                        } else {
                            imgDisbursement.setImageResource(android.R.drawable.ic_menu_gallery)
                        }
                    } catch (e: Exception) {
                        imgDisbursement.setImageResource(android.R.drawable.ic_menu_gallery)
                    }
                }
            } else {
                imgDisbursement.setImageResource(android.R.drawable.ic_menu_gallery)
            }
        }
    }

    override fun getItemCount(): Int = list.size
}