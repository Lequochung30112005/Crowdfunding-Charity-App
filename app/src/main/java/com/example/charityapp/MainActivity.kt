package com.example.charityapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.charityapp.admin.AdminDashboardActivity
import com.example.charityapp.databinding.ActivityMainBinding
import com.example.charityapp.models.Campaign
import com.example.charityapp.ui.adapter.CampaignAdapter
import com.example.charityapp.ui.auth.LoginActivity
import com.example.charityapp.ui.detail.CampaignDetailActivity
import com.example.charityapp.ui.detail.NotificationActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var campaignAdapter: CampaignAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // 1. Tải thông tin "Hello + Tên user" và Avatar cho Header trang chủ
        loadUserHeaderInfo()

        // 2. Khởi tạo danh sách chiến dịch (RecyclerView)
        setupRecyclerView()

        // 3. Kiểm tra phân quyền Admin để ẩn/hiện nút Admin
        checkAdminRole()

        // 4. Sự kiện bấm vào Avatar để mở trang cá nhân
        binding.userHeader.ivUserAvatar.setOnClickListener {
            val intent = Intent(this, UserProfileActivity::class.java)
            startActivity(intent)
        }

        // 5. Sự kiện Đăng xuất (Dùng chữ Đăng xuất tinh tế btnLogoutText)
        binding.btnLogoutText.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "Đã đăng xuất tài khoản", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // 6. Sự kiện mở Trang Quản Trị (Chỉ hiển thị với Admin)
        binding.btnAdminDashboard.setOnClickListener {
            val intent = Intent(this, AdminDashboardActivity::class.java)
            startActivity(intent)
        }

        // 7. Sự kiện bấm mở Hộp Thư Thông Báo từ Admin
        binding.btnInbox.setOnClickListener {
            val intent = Intent(this, NotificationActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadUserHeaderInfo() {
        val currentUser = auth.currentUser ?: return
        db.collection("users").document(currentUser.uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val name = doc.getString("name") ?: "Thành viên"
                    val photoUrl = doc.getString("photoUrl")

                    // Đổ dữ liệu vào Header dùng chung
                    binding.userHeader.tvHelloUser.text = "Hello, $name"
                    binding.userHeader.tvUserRoleBadge.text = "Tài khoản Nhà hảo tâm"

                    if (!photoUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(photoUrl)
                            .circleCrop()
                            .into(binding.userHeader.ivUserAvatar)
                    }
                }
            }
    }

    private fun checkAdminRole() {
        val currentUser = auth.currentUser ?: return
        db.collection("users").document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val role = document.getString("role")?.lowercase()
                    binding.btnAdminDashboard.isVisible = (role == "admin")
                }
            }
            .addOnFailureListener {
                binding.btnAdminDashboard.isVisible = false
            }
    }

    private fun setupRecyclerView() {
        // Chỉ hiển thị các chiến dịch đang mở (bỏ qua chiến dịch đã ẩn)
        db.collection("campaigns")
            .whereEqualTo("active", true)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Toast.makeText(this, "Lỗi tải dữ liệu: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                val campaigns = mutableListOf<Campaign>()
                if (value != null) {
                    for (doc in value.documents) {
                        val campaign = doc.toObject(Campaign::class.java)
                        if (campaign != null) {
                            campaigns.add(campaign)
                        }
                    }
                }

                campaignAdapter = CampaignAdapter(campaigns) { campaign: Campaign ->
                    val intent = Intent(this, CampaignDetailActivity::class.java).apply {
                        putExtra("CAMPAIGN_ID", campaign.id)
                    }
                    startActivity(intent)
                }

                binding.rvCampaigns.layoutManager = LinearLayoutManager(this)
                binding.rvCampaigns.adapter = campaignAdapter
            }
    }
}