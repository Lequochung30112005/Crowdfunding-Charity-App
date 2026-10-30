package com.example.charityapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.charityapp.databinding.ActivityDonateBinding

class DonateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDonateBinding
    private var selectedAmount: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDonateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btn50k.setOnClickListener { selectAmount(50000) }
        binding.btn100k.setOnClickListener { selectAmount(100000) }
        binding.btn500k.setOnClickListener { selectAmount(500000) }

        binding.btnConfirmDonate.setOnClickListener {
            val inputStr = binding.edtCustomAmount.text.toString().trim()
            val finalAmount = inputStr.toLongOrNull() ?: selectedAmount
            val message = binding.edtMessage.text.toString().trim()

            if (finalAmount <= 0) {
                Toast.makeText(this, "Vui lòng nhập số tiền hợp lệ", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "Quyên góp: $finalAmount đ | Lời nhắn: $message", Toast.LENGTH_SHORT).show()
        }
    }

    private fun selectAmount(amount: Long) {
        selectedAmount = amount
        binding.edtCustomAmount.setText(amount.toString())
    }
}