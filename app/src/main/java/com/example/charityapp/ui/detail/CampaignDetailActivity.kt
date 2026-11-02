package com.example.charityapp.ui.detail

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.charityapp.R
import com.example.charityapp.models.DisbursementReport
import com.example.charityapp.ui.adapter.DisbursementAdapter
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.LinearProgressIndicator

class CampaignDetailActivity : AppCompatActivity() {

    private var campaignId: String? = null
    private var campaignTitle: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_campaign_detail)

        // 1. Nhận dữ liệu truyền qua từ MainActivity
        campaignId = intent.getStringExtra("CAMPAIGN_ID")
        campaignTitle = intent.getStringExtra("CAMPAIGN_TITLE")
        val campaignDesc = intent.getStringExtra("CAMPAIGN_DESC") ?: "Chi tiết về hoàn cảnh khó khăn và kế hoạch thực hiện chiến dịch từ thiện..."
        val targetAmount = intent.getDoubleExtra("CAMPAIGN_TARGET", 100000000.0)
        val currentAmount = intent.getDoubleExtra("CAMPAIGN_CURRENT", 6500000.0)

        // 2. Ánh xạ các thành phần trong XML (Bao gồm cả ImageView mới có)
        val imgCampaign = findViewById<ImageView>(R.id.imgDetailCampaign)
        val tvTitle = findViewById<TextView>(R.id.tvDetailTitle)
        val tvDescription = findViewById<TextView>(R.id.tvDetailDescription)
        val tvRaised = findViewById<TextView>(R.id.tvDetailRaised)
        val tvTarget = findViewById<TextView>(R.id.tvDetailTarget)
        val progressIndicator = findViewById<LinearProgressIndicator>(R.id.progressDetail)
        val btnDonateNow = findViewById<MaterialButton>(R.id.btnDonateNow)

        // 3. Hiển thị dữ liệu lên giao diện
        if (!campaignTitle.isNullOrEmpty()) {
            tvTitle.text = campaignTitle
        }
        tvDescription.text = campaignDesc
        tvRaised.text = "Đã đạt: ${String.format("%,.0f", currentAmount)} đ"
        tvTarget.text = "Mục tiêu: ${String.format("%,.0f", targetAmount)} đ"

        // Gán ảnh mẫu tạm thời (hoặc bạn có thể dùng thư viện Glide/Picasso nếu load từ URL)
        imgCampaign.setImageResource(android.R.drawable.ic_menu_gallery)

        // Tính % hoàn thành cho Progress bar
        val progressPercent = if (targetAmount > 0) ((currentAmount / targetAmount) * 100).toInt() else 0
        progressIndicator.progress = progressPercent

        // 4. Cấu hình danh sách nhật ký giải ngân
        setupDisbursementRecyclerView()

        // 5. Bấm nút "ỦNG HỘ NGAY" để mở DonateActivity và mang theo campaignId, campaignTitle
        btnDonateNow.setOnClickListener {
            val intent = Intent(this, DonateActivity::class.java).apply {
                putExtra("CAMPAIGN_ID", campaignId)
                putExtra("CAMPAIGN_TITLE", campaignTitle)
            }
            startActivity(intent)
        }
    }

    private fun setupDisbursementRecyclerView() {
        val sampleReports = listOf(
            DisbursementReport(
                id = "1",
                title = "Đợt 1: Mua vật liệu xây dựng",
                date = "20/10/2026",
                content = "Đã mua 500 bao xi măng và 2 tấn thép để khởi công xây dựng nhà cộng đồng.",
                imageUrl = "https://picsum.photos/400/200"
            ),
            DisbursementReport(
                id = "2",
                title = "Đợt 2: Trao quà cho bà con",
                date = "25/10/2026",
                content = "Trao tặng 100 phần quà nhu yếu phẩm cho các hộ gia đình khó khăn.",
                imageUrl = "https://picsum.photos/400/201"
            )
        )

        val adapter = DisbursementAdapter(sampleReports)
        val rvDisbursementLogs = findViewById<RecyclerView>(R.id.rvDisbursementLogs)
        rvDisbursementLogs.layoutManager = LinearLayoutManager(this)
        rvDisbursementLogs.adapter = adapter
    }
}