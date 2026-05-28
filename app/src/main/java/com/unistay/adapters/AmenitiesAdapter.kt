package com.unistay.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.unistay.R

class AmenitiesAdapter(
    private val amenities: List<String>
) : RecyclerView.Adapter<AmenitiesAdapter.AmenityViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AmenityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_amenity, parent, false)
        return AmenityViewHolder(view)
    }

    override fun onBindViewHolder(holder: AmenityViewHolder, position: Int) {
        val amenity = amenities[position]
        holder.bind(amenity)
    }

    override fun getItemCount() = amenities.size

    inner class AmenityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvAmenity: TextView = itemView.findViewById(R.id.tvAmenity)

        fun bind(amenity: String) {
            tvAmenity.text = amenity
        }
    }
}