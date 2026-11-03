package com.example.charityapp.ui.detail

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.charityapp.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.firebase.firestore.FirebaseFirestore

class CampaignDetailActivity : AppCompatActivity() {

    private var campaignId: String? = null
    private var campaignTitle: String? = null
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_campaign_detail)

        // 1. Nhận campaignId truyền qua từ Intent
        campaignId = intent.getStringExtra("CAMPAIGN_ID")

        if (campaignId.isNullOrEmpty()) {
            Toast.makeText(this, "Không tìm thấy thông tin chiến dịch", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 2. Load dữ liệu chi tiết trực tiếp từ Firestore
        loadCampaignDetailFromFirestore()
    }

    private fun loadCampaignDetailFromFirestore() {
        db.collection("campaigns").document(campaignId!!).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val title = doc.getString("title") ?: "Chiến dịch từ thiện"
                    val desc = doc.getString("description") ?: "Chưa có mô tả chi tiết."
                    val targetAmount = doc.getDouble("targetAmount") ?: 100000000.0
                    val currentAmount = doc.getDouble("currentAmount") ?: 0.0
                    val imageUrl = doc.getString("imageUrl")

                    campaignTitle = title

                    // Ánh xạ View
                    val imgCampaign = findViewById<ImageView>(R.id.imgDetailCampaign)
                    val tvTitle = findViewById<TextView>(R.id.tvDetailTitle)
                    val tvDescription = findViewById<TextView>(R.id.tvDetailDescription)
                    val tvRaised = findViewById<TextView>(R.id.tvDetailRaised)
                    val tvTarget = findViewById<TextView>(R.id.tvDetailTarget)
                    val progressIndicator = findViewById<LinearProgressIndicator>(R.id.progressDetail)
                    val btnDonateNow = findViewById<MaterialButton>(R.id.btnDonateNow)

                    // Hiển thị lên giao diện
                    tvTitle.text = title
                    tvDescription.text = desc
                    tvRaised.text = "Đã đạt: ${String.format("%,.0f", currentAmount)} đ"
                    tvTarget.text = "Mục tiêu: ${String.format("%,.0f", targetAmount)} đ"

                    // Xử lý hiển thị ảnh (Hỗ trợ URL web hoặc chuỗi Base64)
                    if (!imageUrl.isNullOrEmpty()) {
                        if (imageUrl.startsWith("http")) {
                            Glide.with(this).load(imageUrl).into(imgCampaign)
                        } else {
                            try {
                                val decodedBytes = android.util.Base64.decode(imageUrl, android.util.Base64.DEFAULT)
                                val decodedBitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                                imgCampaign.setImageBitmap(decodedBitmap)
                            } catch (e: Exception) {
                                imgCampaign.setImageResource(android.R.drawable.ic_menu_gallery)
                            }
                        }
                    } else {
                        imgCampaign.setImageResource(android.R.drawable.ic_menu_gallery)
                    }

                    // Tính % hoàn thành
                    val progressPercent = if (targetAmount > 0) ((currentAmount / targetAmount) * 100).toInt() else 0
                    progressIndicator.progress = progressPercent

                    // Nút Ủng hộ ngay
                    // Nút Ủng hộ ngay
                    btnDonateNow.setOnClickListener {
                        val intent = Intent(this, DonateActivity::class.java).apply {
                            putExtra("CAMPAIGN_ID", campaignId)
                            putExtra("CAMPAIGN_TITLE", title) // ⚡ Dùng thẳng biến 'title' vừa lấy từ Firestore ở trên
                        }
                        startActivity(intent)
                    }
                } else {
                    Toast.makeText(this, "Chiến dịch không tồn tại trên hệ thống", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Lỗi tải dữ liệu: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}