package com.unistay.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.unistay.R
import com.unistay.models.Reservation
import java.text.SimpleDateFormat
import java.util.*

class ReservationAdapter(
    private val reservations: List<Reservation>
) : RecyclerView.Adapter<ReservationAdapter.ReservationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReservationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reservation, parent, false)
        return ReservationViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReservationViewHolder, position: Int) {
        val reservation = reservations[position]
        holder.bind(reservation)
    }

    override fun getItemCount() = reservations.size

    inner class ReservationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvStudentName: TextView = itemView.findViewById(R.id.tvStudentName)
        private val tvAccommodation: TextView = itemView.findViewById(R.id.tvAccommodation)
        private val tvReference: TextView = itemView.findViewById(R.id.tvReference)
        private val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)

        fun bind(reservation: Reservation) {
            tvStudentName.text = reservation.studentName
            tvAccommodation.text = reservation.accommodationTitle
            tvReference.text = reservation.referenceNumber
            tvAmount.text = "BWP ${reservation.amountPaid}"

            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            tvDate.text = dateFormat.format(Date(reservation.reservedAt))

            when (reservation.status) {
                "confirmed" -> {
                    tvStatus.text = "CONFIRMED"
                    tvStatus.setTextColor(itemView.context.getColor(R.color.green_success))
                }
                "pending" -> {
                    tvStatus.text = "PENDING"
                    tvStatus.setTextColor(itemView.context.getColor(R.color.yellow_warning))
                }
                "cancelled" -> {
                    tvStatus.text = "CANCELLED"
                    tvStatus.setTextColor(itemView.context.getColor(R.color.red_error))
                }
            }
        }
    }
}