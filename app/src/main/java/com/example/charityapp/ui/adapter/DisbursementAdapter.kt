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

            Glide.with(root.context)
                .load(item.imageUrl)
                .centerCrop()
                .into(imgDisbursement)
        }
    }

    override fun getItemCount(): Int = list.size
}