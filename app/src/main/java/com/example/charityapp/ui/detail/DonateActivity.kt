package com.example.charityapp.ui.detail

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.charityapp.Api.CreateOrder
import com.example.charityapp.databinding.ActivityDonateBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import vn.zalopay.sdk.ZaloPayError
import vn.zalopay.sdk.ZaloPaySDK
import vn.zalopay.sdk.listeners.PayOrderListener

class DonateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDonateBinding
    private var campaignId: String? = null
    private var campaignTitle: String? = null
    private var selectedAmount: Long = 0

    // Khai báo finalAmount ở cấp độ class để dùng chung cho cả hàm thanh toán và trả về kết quả
    private var finalAmount: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDonateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Nhận dữ liệu truyền từ CampaignDetailActivity hoặc MainActivity
        campaignId = intent.getStringExtra("CAMPAIGN_ID")
        campaignTitle = intent.getStringExtra("CAMPAIGN_TITLE")

        if (!campaignTitle.isNullOrEmpty()) {
            binding.tvCampaignTitle.text = campaignTitle
        }

        // 2. Xử lý sự kiện chọn nhanh số tiền qua các Chip
        binding.chip50k.setOnClickListener { selectAmount(50000) }
        binding.chip100k.setOnClickListener { selectAmount(100000) }
        binding.chip200k.setOnClickListener { selectAmount(200000) }
        binding.chip500k.setOnClickListener { selectAmount(500000) }

        // 3. Xử lý sự kiện bấm nút "Tiếp tục thanh toán"
        binding.btnProceedPayment.setOnClickListener {
            val inputStr = binding.etAmount.text.toString().trim()

            // Gán giá trị vào biến toàn cục finalAmount
            finalAmount = inputStr.toLongOrNull() ?: selectedAmount
            val message = binding.etMessage.text.toString().trim()

            if (finalAmount < 1000) {
                Toast.makeText(this, "Vui lòng nhập số tiền hợp lệ (Tối thiểu 1.000đ)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Gọi hàm tạo đơn hàng và mở ZaloPay SDK
            requestZaloPayPayment(finalAmount, message)
        }
    }

    private fun selectAmount(amount: Long) {
        selectedAmount = amount
        binding.etAmount.setText(amount.toString())
    }

    private fun requestZaloPayPayment(amount: Long, message: String) {
        Toast.makeText(this, "Đang khởi tạo giao dịch...", Toast.LENGTH_SHORT).show()

        // Sử dụng Coroutine chạy ngầm để gọi API tạo đơn hàng, tránh nghẽn luồng chính
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Gọi API CreateOrder lấy zp_trans_token thật từ ZaloPay Sandbox
                val orderApi = CreateOrder()
                val data = orderApi.createOrder(amount.toString())

                if (data != null && data.optInt("returncode") == 1) {
                    // Đổi từ optString("zp_trans_token") thành optString("zptranstoken")
                    val zpTransToken = data.optString("zptranstoken")

                    withContext(Dispatchers.Main) {
                        ZaloPaySDK.getInstance().payOrder(
                            this@DonateActivity,
                            zpTransToken,
                            "demozpdk://app",
                            object : PayOrderListener {
                                override fun onPaymentSucceeded(transactionId: String, transToken: String, appTransID: String) {
                                    Toast.makeText(this@DonateActivity, "Thanh toán thành công!", Toast.LENGTH_LONG).show()

                                    val successIntent = Intent(this@DonateActivity, DonationSuccessActivity::class.java).apply {
                                        putExtra("TRANSACTION_ID", transactionId)
                                        putExtra("AMOUNT", finalAmount)
                                        putExtra("CAMPAIGN_ID", campaignId)
                                    }
                                    startActivity(successIntent)
                                    finish()
                                }

                                override fun onPaymentCanceled(transToken: String, appTransID: String) {
                                    Toast.makeText(this@DonateActivity, "Đã hủy thanh toán", Toast.LENGTH_SHORT).show()
                                }

                                override fun onPaymentError(zaloPayError: ZaloPayError, transToken: String, appTransID: String) {
                                    Toast.makeText(this@DonateActivity, "Lỗi thanh toán: $zaloPayError", Toast.LENGTH_LONG).show()
                                }
                            }
                        )
                    }
                }else {
                    val returnMessage = data?.optString("return_message") ?: "Không thể tạo đơn hàng"
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@DonateActivity, "Lỗi ZaloPay: $returnMessage", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("DONATE_EXCEPTION", "Lỗi ngoại lệ: ", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@DonateActivity, "Lỗi kết nối: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        ZaloPaySDK.getInstance().onResult(intent)
    }
}