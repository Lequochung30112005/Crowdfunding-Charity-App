package com.example.charityapp.repository

import android.net.Uri
import com.example.charityapp.models.Campaign
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class CampaignRepository {

    private val db = FirebaseFirestore.getInstance()
    private val storageRef = FirebaseStorage.getInstance().reference

    // 1. Lấy danh sách chiến dịch từ Firestore
    fun getCampaigns(onSuccess: (List<Campaign>) -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("campaigns")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val list = querySnapshot.toObjects(Campaign::class.java)
                onSuccess(list)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    // 2. Thêm mới một chiến dịch (có tải ảnh lên Firebase Storage)
    fun addCampaign(
        campaign: Campaign,
        imageUri: Uri?,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val docRef = db.collection("campaigns").document()
        val docId = docRef.id

        // Sử dụng copy để tạo object mới với ID vừa sinh ra từ Firestore
        val updatedCampaign = campaign.copy(id = docId)

        if (imageUri != null) {
            val fileRef = storageRef.child("campaign_images/$docId.jpg")
            fileRef.putFile(imageUri)
                .continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let { throw it }
                    }
                    fileRef.downloadUrl
                }
                .addOnSuccessListener { downloadUri ->
                    val finalCampaign = updatedCampaign.copy(imageUrl = downloadUri.toString())
                    docRef.set(finalCampaign)
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { onFailure(it) }
                }
                .addOnFailureListener { onFailure(it) }
        } else {

            docRef.set(updatedCampaign)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { onFailure(it) }
        }
    }
}