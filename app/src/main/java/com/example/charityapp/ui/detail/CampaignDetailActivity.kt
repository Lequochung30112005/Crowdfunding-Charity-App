package com.example.charityapp.ui.detail

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.charityapp.DonateActivity
import com.example.charityapp.R
import com.example.charityapp.models.DisbursementReport
import com.example.charityapp.ui.adapter.DisbursementAdapter

class CampaignDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Sử dụng setContentView trực tiếp với file XML layout của bạn
        setContentView(R.layout.activity_campaign_detail)

        // 1. Cấu hình danh sách nhật ký giải ngân bằng findViewById
        setupDisbursementRecyclerView()

        // 2. Bấm nút Ủng hộ ngay để mở màn hình Quyên góp
        val btnDonateNow = findViewById<Button>(R.id.btnDonateNow)
        btnDonateNow.setOnClickListener {
            val intent = Intent(this, DonateActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupDisbursementRecyclerView() {
        // Dữ liệu mẫu báo cáo giải ngân
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