package com.example.charityapp.admin

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.charityapp.databinding.ActivityAdminBinding
import com.example.charityapp.models.Campaign
import com.example.charityapp.models.Donation
import com.example.charityapp.ui.adapter.AdminDonationAdapter
import com.example.charityapp.ui.adapter.CampaignAdapter
import com.example.charityapp.ui.auth.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.text.NumberFormat
import java.util.Locale

@SuppressLint("SetTextI18n")
class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private var selectedImageUri: Uri? = null

    // Trình chọn ảnh chiến dịch
    private val pickCampaignImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            Glide.with(this)
                .load(uri)
                .into(binding.ivCampaignImagePreview)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        loadAdminHeaderInfo()
        calculateTotalDonations()
        setupMenuToggles()
        loadAdminDonationsList()

        // Chọn ảnh
        binding.btnSelectCampaignImage.setOnClickListener {
            pickCampaignImageLauncher.launch("image/*")
        }

        // Tạo chiến dịch
        binding.btnSubmitCreateCampaign.setOnClickListener {
            createNewCampaign()
        }

        // Gửi thông báo
        binding.btnSubmitBroadcast.setOnClickListener {
            sendBroadcastMessage()
        }

        // Đăng xuất tài khoản
        binding.btnLogoutAdmin.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "Đã đăng xuất tài khoản", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
        // Nút chuyển nhanh sang trang User để test thông báo và quyên góp
        binding.btnQuickTestUser.setOnClickListener {
            // Thay MainActivity::class.java bằng tên Activity trang chủ/danh sách chiến dịch của User bên bạn
            val intent = Intent(this, com.example.charityapp.MainActivity::class.java)
            startActivity(intent)
            Toast.makeText(this, "Đang mở giao diện User để test...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadAdminHeaderInfo() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                val name = doc.getString("name") ?: "Quản trị viên"
                val photoUrl = doc.getString("photoUrl")

                binding.adminHeader.tvHelloUser.text = "Hello, $name"
                binding.adminHeader.tvUserRoleBadge.text = "Hệ thống Quản trị (Admin)"

                if (!photoUrl.isNullOrEmpty()) {
                    Glide.with(this)
                        .load(photoUrl)
                        .circleCrop()
                        .into(binding.adminHeader.ivUserAvatar)
                }
            }
        }
    }

    private fun setupMenuToggles() {
        binding.cardCreateCampaignMenu.setOnClickListener {
            val isVisible = binding.layoutCreateCampaignDetail.isVisible
            binding.layoutCreateCampaignDetail.isVisible = !isVisible
        }

        binding.cardBroadcastMenu.setOnClickListener {
            val isVisible = binding.layoutBroadcastDetail.isVisible
            binding.layoutBroadcastDetail.isVisible = !isVisible
        }

        binding.cardManageCampaignsMenu?.setOnClickListener {
            val isVisible = binding.layoutManageCampaignsDetail?.isVisible ?: false
            binding.layoutManageCampaignsDetail?.isVisible = !isVisible
            if (!isVisible) {
                loadAdminManageCampaignsList()
            }
        }

        binding.cardDonatorsMenu.setOnClickListener {
            val isVisible = binding.layoutDonatorsDetail.isVisible
            binding.layoutDonatorsDetail.isVisible = !isVisible
        }
    }

    private fun calculateTotalDonations() {
        db.collection("campaigns").addSnapshotListener { value, error ->
            if (error != null) return@addSnapshotListener
            var totalSum = 0.0
            value?.documents?.forEach { doc ->
                totalSum += doc.getDouble("currentAmount") ?: 0.0
            }
            val localeVN = Locale.forLanguageTag("vi-VN")
            val formatted = NumberFormat.getCurrencyInstance(localeVN).format(totalSum)
            binding.tvTotalDonations.text = formatted
        }
    }

    private fun loadAdminDonationsList() {
        db.collection("donations")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) return@addSnapshotListener

                val list = mutableListOf<Donation>()
                value?.documents?.forEach { doc ->
                    try {
                        val id = doc.id
                        val campaignId = doc.getString("campaignId") ?: ""
                        val campaignTitle = doc.getString("campaignTitle") ?: ""
                        val userName = doc.getString("userName") ?: ""
                        val timestamp = doc.getTimestamp("timestamp")

                        val amountObj = doc.get("amount")
                        val amount = when (amountObj) {
                            is Number -> amountObj.toDouble()
                            is String -> amountObj.replace(",", "").toDoubleOrNull() ?: 0.0
                            else -> 0.0
                        }

                        val donation = Donation(
                            id = id,
                            campaignId = campaignId,
                            campaignTitle = campaignTitle,
                            userName = userName,
                            amount = amount,
                            timestamp = timestamp
                        )
                        list.add(donation)
                    } catch (_: Exception) { }
                }

                val adapter = AdminDonationAdapter(list)
                binding.rvAdminDonations.layoutManager = LinearLayoutManager(this)
                binding.rvAdminDonations.adapter = adapter
            }
    }

    private fun loadAdminManageCampaignsList() {
        db.collection("campaigns").addSnapshotListener { value, error ->
            if (error != null) return@addSnapshotListener
            val list = mutableListOf<Campaign>()
            value?.documents?.forEach { doc ->
                val campaign = doc.toObject(Campaign::class.java)
                if (campaign != null) {
                    list.add(campaign)
                }
            }

            val adapter = CampaignAdapter(list) { campaign ->
                toggleCampaignStatus(campaign.id, campaign.active)
            }
            binding.rvAdminManageCampaigns?.layoutManager = LinearLayoutManager(this)
            binding.rvAdminManageCampaigns?.adapter = adapter
        }
    }

    private fun createNewCampaign() {
        val title = binding.etCampaignTitle.text.toString().trim()
        val targetStr = binding.etTargetAmount.text.toString().trim()
        val desc = binding.etDescription.text.toString().trim()

        if (title.isEmpty() || targetStr.isEmpty() || desc.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin chiến dịch", Toast.LENGTH_SHORT).show()
            return
        }

        val campaignId = db.collection("campaigns").document().id
        val targetAmount = targetStr.toDoubleOrNull() ?: 0.0

        val imageString = if (selectedImageUri != null) {
            convertUriToBase64(selectedImageUri!!) ?: "https://picsum.photos/500/300"
        } else {
            "https://picsum.photos/500/300"
        }

        saveCampaignToFirestore(campaignId, title, desc, targetAmount, imageString)
    }

    // ⚡ Hàm nén ảnh Base64 đã sửa sạch lỗi cú pháp toán học
    private fun convertUriToBase64(uri: Uri): String? {
        return try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream) ?: return null

            val targetWidth = 600
            val targetHeight = (originalBitmap.height * (targetWidth.toDouble() / originalBitmap.width)).toInt()
            val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, targetWidth, targetHeight, true)

            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            val byteArray = outputStream.toByteArray()

            Base64.encodeToString(byteArray, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun saveCampaignToFirestore(id: String, title: String, desc: String, target: Double, imageUrlOrBase64: String) {
        val newCampaign = hashMapOf(
            "id" to id,
            "title" to title,
            "description" to desc,
            "targetAmount" to target,
            "currentAmount" to 0.0,
            "imageUrl" to imageUrlOrBase64,
            "active" to true
        )

        db.collection("campaigns").document(id).set(newCampaign)
            .addOnSuccessListener {
                Toast.makeText(this, "Phát hành chiến dịch thành công!", Toast.LENGTH_SHORT).show()

                // ⚡ Tự động đẩy thông báo cho tất cả User khi phát hành thành công
                sendAutoNotificationForNewCampaign(title)

                binding.etCampaignTitle.text?.clear()
                binding.etTargetAmount.text?.clear()
                binding.etDescription.text?.clear()
                binding.ivCampaignImagePreview.setImageResource(android.R.drawable.ic_menu_gallery)
                selectedImageUri = null
                binding.layoutCreateCampaignDetail.isVisible = false
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Lỗi tạo chiến dịch: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun sendAutoNotificationForNewCampaign(campaignTitle: String) {
        val notificationData = hashMapOf(
            "title" to "Chiến dịch mới phát hành!",
            "message" to "Chiến dịch '$campaignTitle' vừa được ra mắt. Hãy tham gia ủng hộ nhé!",
            "timestamp" to com.google.firebase.Timestamp.now()
        )
        db.collection("notifications").add(notificationData)
    }

    fun toggleCampaignStatus(campaignId: String, currentStatus: Boolean) {
        val newStatus = !currentStatus
        val statusText = if (newStatus) "mở lại" else "ẩn/đóng"

        AlertDialog.Builder(this)
            .setTitle("Xác nhận thay đổi")
            .setMessage("Bạn có chắc chắn muốn $statusText chiến dịch này?")
            .setPositiveButton("Đồng ý") { _, _ ->
                db.collection("campaigns").document(campaignId)
                    .update("active", newStatus)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Đã $statusText chiến dịch thành công", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun sendBroadcastMessage() {
        val title = binding.etBroadcastTitle.text.toString().trim()
        val msg = binding.etBroadcastMessage.text.toString().trim()

        if (title.isEmpty() || msg.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tiêu đề và nội dung thông báo", Toast.LENGTH_SHORT).show()
            return
        }

        val notificationData = hashMapOf(
            "title" to title,
            "message" to msg,
            "timestamp" to com.google.firebase.Timestamp.now()
        )

        db.collection("notifications").add(notificationData)
            .addOnSuccessListener {
                Toast.makeText(this, "Đã gửi thông báo chung thành công!", Toast.LENGTH_SHORT).show()
                binding.etBroadcastTitle.text?.clear()
                binding.etBroadcastMessage.text?.clear()
                binding.layoutBroadcastDetail.isVisible = false
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Lỗi gửi thông báo: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}