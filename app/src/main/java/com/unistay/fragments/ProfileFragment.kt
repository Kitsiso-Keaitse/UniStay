package com.unistay.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.unistay.R
import com.unistay.activities.RoleSelectActivity
import com.unistay.models.User

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var ivAvatar: ImageView? = null
    private var tvName: TextView? = null
    private var tvEmail: TextView? = null
    private var tvRole: TextView? = null
    private var tvMemberSince: TextView? = null
    private var tvPhone: TextView? = null
    private var tvReservedCount: TextView? = null
    private var btnLogout: Button? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        initViews(view)
        loadUserData()
        listenToUserStats()

        btnLogout?.setOnClickListener {
            auth.signOut()
            val intent = Intent(requireContext(), RoleSelectActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun initViews(view: View) {
        ivAvatar = view.findViewById(R.id.ivAvatar)
        tvName = view.findViewById(R.id.tvProfileName)
        tvEmail = view.findViewById(R.id.tvProfileEmail)
        tvRole = view.findViewById(R.id.tvRole)
        tvMemberSince = view.findViewById(R.id.tvMemberSince)
        tvPhone = view.findViewById(R.id.tvPhone)
        tvReservedCount = view.findViewById(R.id.tvReservedCount)
        btnLogout = view.findViewById(R.id.btnLogout)
        
        ivAvatar?.setOnClickListener {
            Toast.makeText(requireContext(), "Using placeholder avatars", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<Button>(R.id.btnEditProfile)?.setOnClickListener { 
            Toast.makeText(requireContext(), "Edit feature coming soon", Toast.LENGTH_SHORT).show() 
        }
        
        view.findViewById<Button>(R.id.btnChangePassword)?.setOnClickListener { 
            Toast.makeText(requireContext(), "Change password coming soon", Toast.LENGTH_SHORT).show() 
        }

        view.findViewById<Button>(R.id.btnHelp)?.setOnClickListener {
            Toast.makeText(requireContext(), "Help & Support coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null || !isAdded) return@addSnapshotListener
            
            val user = snapshot.toObject(User::class.java)
            user?.let {
                tvName?.text = if (it.firstName.isNotEmpty()) "${it.firstName} ${it.lastName}" else "User Name"
                tvEmail?.text = it.email.ifEmpty { "user@email.com" }
                tvPhone?.text = it.phoneNumber.ifEmpty { "Not set" }
                
                val sdf = java.text.SimpleDateFormat("MMM yyyy", java.util.Locale.getDefault())
                tvMemberSince?.text = sdf.format(java.util.Date(it.createdAt))

                val roleText = if (it.role == "student") "🎓 Student Account" else "🏠 Property Provider"
                tvRole?.text = roleText

                val avatarUrl = if (it.profileImage.isNotEmpty()) it.profileImage 
                                else "https://i.pravatar.cc/150?u=${it.userId}"
                
                ivAvatar?.let { imageView ->
                    Glide.with(this)
                        .load(avatarUrl)
                        .circleCrop()
                        .placeholder(R.drawable.ic_profile)
                        .into(imageView)
                }
            }
        }
    }

    private fun listenToUserStats() {
        val userId = auth.currentUser?.uid ?: return
        
        db.collection("reservations").whereEqualTo("studentId", userId)
            .addSnapshotListener { snapshots, _ ->
                if (isAdded) {
                    tvReservedCount?.text = (snapshots?.size() ?: 0).toString()
                }
            }
    }
}
