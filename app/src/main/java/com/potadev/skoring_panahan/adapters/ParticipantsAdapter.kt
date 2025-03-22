package com.potadev.skoring_panahan.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.potadev.skoring_panahan.R
import com.potadev.skoring_panahan.data.entity.Participant
import com.potadev.skoring_panahan.ui.participants.ParticipantViewModel

class ParticipantsAdapter(
    private val participants: List<Participant>,
    private val viewModel: ParticipantViewModel,
    private val onEditClick: (Participant) -> Unit
) : RecyclerView.Adapter<ParticipantsAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val participantName: TextView = view.findViewById(R.id.tvParticipantName)
        val btnEdit: ImageButton = view.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(inflater.inflate(R.layout.item_participant, parent, false))
    }

    override fun getItemCount(): Int {
        return participants.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val participant = participants[position]
        holder.participantName.text = participant.name
        
        holder.btnEdit.setOnClickListener {
            onEditClick(participant)
        }
        
        holder.btnDelete.setOnClickListener {
            viewModel.delete(participant)
        }
    }
}
