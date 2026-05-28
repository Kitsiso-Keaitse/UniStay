package com.unistay.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.unistay.R
import com.unistay.models.Accommodation

class AccommodationAdapter(
    private var listings: List<Accommodation>,
    private val onItemClick: (Accommodation) -> Unit,
    private val onChatClick: (Accommodation) -> Unit
) : RecyclerView.Adapter<AccommodationAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_accommodation, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val listing = listings[position]
        holder.bind(listing, onChatClick)
        holder.itemView.setOnClickListener { onItemClick(listing) }
    }

    override fun getItemCount() = listings.size

    fun updateList(newListings: List<Accommodation>) {
        listings = newListings
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvLocation: TextView = itemView.findViewById(R.id.tvLocation)
        private val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val ivImage: ImageView = itemView.findViewById(R.id.ivImage)
        private val btnChat: Button = itemView.findViewById(R.id.btnItemChat)

        fun bind(listing: Accommodation, onChatClick: (Accommodation) -> Unit) {
            tvTitle.text = listing.title
            tvLocation.text = "📍 ${listing.location}"
            tvPrice.text = "BWP ${listing.pricePerMonth}/mo"
            tvStatus.text = listing.status.uppercase()
            
            // Set status color
            when(listing.status.lowercase()) {
                "available" -> tvStatus.setBackgroundResource(R.drawable.bg_tag_success)
                "reserved" -> tvStatus.setBackgroundResource(R.drawable.bg_tag_reserved)
                else -> tvStatus.setBackgroundResource(R.drawable.bg_role_card)
            }

            if (listing.images.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(listing.images[0])
                    .placeholder(R.drawable.ic_placeholder)
                    .into(ivImage)
            }
            
            btnChat.setOnClickListener { onChatClick(listing) }
        }
    }
}
