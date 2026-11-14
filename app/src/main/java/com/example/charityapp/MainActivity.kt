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
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.FirebaseFirestore

@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var campaignAdapter: CampaignAdapter

    // Lưu lại Listener để có thể hủy khi Activity destroy, tránh rò rỉ bộ nhớ
    private var campaignListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // 1. Khởi tạo RecyclerView 1 lần duy nhất chuẩn kiến trúc
        initRecyclerView()

        // 2. Thiết lập các sự kiện tương tác UI
        setupClickListeners()
    }

    override fun onResume() {
        super.onResume()
        // Tải lại thông tin user & phân quyền mỗi khi quay lại màn hình chính
        loadUserHeaderInfo()
        checkAdminRole()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Hủy lắng nghe Firestore khi Activity bị hủy để giải phóng tài nguyên
        campaignListener?.remove()
    }

    private fun setupClickListeners() {
        // Sự kiện bấm vào Avatar mở trang cá nhân
        binding.userHeader.ivUserAvatar.setOnClickListener {
            startActivity(Intent(this, UserProfileActivity::class.java))
        }

        // Sự kiện Đăng xuất
        binding.btnlogout.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "Đã đăng xuất tài khoản", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }

        // Sự kiện mở Trang Quản Trị Admin
        binding.btnAdminDashboard.setOnClickListener {
            startActivity(Intent(this, AdminDashboardActivity::class.java))
        }

        // Sự kiện mở Hộp Thư Thông Báo
        binding.btnInbox.setOnClickListener {
            startActivity(Intent(this, NotificationActivity::class.java))
        }
    }

    private fun initRecyclerView() {
        // Khởi tạo Adapter với danh sách rỗng ban đầu
        campaignAdapter = CampaignAdapter(emptyList()) { campaign ->
            val intent = Intent(this, CampaignDetailActivity::class.java).apply {
                putExtra("CAMPAIGN_ID", campaign.id)
            }
            startActivity(intent)
        }

        binding.rvCampaigns.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = campaignAdapter
            setHasFixedSize(true) // Tối ưu hiệu suất render danh sách
        }

        // Lắng nghe dữ liệu thời gian thực từ Firestore
        listenToCampaigns()
    }

    private fun listenToCampaigns() {
        campaignListener = db.collection("campaigns")
            .whereEqualTo("active", true)
            .addSnapshotListener { value, error ->
                if (isFinishing || isDestroyed) return@addSnapshotListener

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

                // Cập nhật dữ liệu vào Adapter sẵn có thay vì tạo mới
                campaignAdapter.updateData(campaigns)
            }
    }

    private fun loadUserHeaderInfo() {
        val currentUser = auth.currentUser ?: return
        db.collection("users").document(currentUser.uid).get()
            .addOnSuccessListener { doc ->
                if (isFinishing || isDestroyed) return@addOnSuccessListener

                if (doc.exists()) {
                    val name = doc.getString("name") ?: "Thành viên"
                    val photoUrl = doc.getString("photoUrl")

                    binding.userHeader.tvHelloUser.text = "Hello, $name"
                    binding.userHeader.tvUserRoleBadge.text = "Tài khoản Nhà hảo tâm"

                    if (!photoUrl.isNullOrEmpty()) {
                        if (photoUrl.startsWith("http://") || photoUrl.startsWith("https://")) {
                            Glide.with(this)
                                .load(photoUrl)
                                .circleCrop()
                                .into(binding.userHeader.ivUserAvatar)
                        } else {
                            try {
                                val cleanStr = if (photoUrl.contains(",")) photoUrl.substringAfter(",") else photoUrl
                                val decodedBytes = android.util.Base64.decode(cleanStr.replace("\\s".toRegex(), ""), android.util.Base64.DEFAULT)
                                val decodedBitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                                if (decodedBitmap != null) {
                                    Glide.with(this)
                                        .load(decodedBitmap)
                                        .circleCrop()
                                        .into(binding.userHeader.ivUserAvatar)
                                } else {
                                    binding.userHeader.ivUserAvatar.setImageResource(android.R.drawable.ic_menu_camera)
                                }
                            } catch (e: Exception) {
                                binding.userHeader.ivUserAvatar.setImageResource(android.R.drawable.ic_menu_camera)
                            }
                        }
                    }
                }
            }
    }

    private fun checkAdminRole() {
        val currentUser = auth.currentUser ?: return
        db.collection("users").document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                if (isFinishing || isDestroyed) return@addOnSuccessListener

                if (document.exists()) {
                    val role = document.getString("role")?.lowercase()
                    binding.btnAdminDashboard.isVisible = (role == "admin")
                }
            }
            .addOnFailureListener {
                if (isFinishing || isDestroyed) return@addOnFailureListener
                binding.btnAdminDashboard.isVisible = false
            }
    }
}