package com.potadev.skoring_panahan.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.potadev.skoring_panahan.R
import com.potadev.skoring_panahan.data.entity.Participant

class ParticipantSelectionAdapter(
    private val participants: List<Participant>,
    private val selectedParticipantIds: MutableSet<Long>
) : RecyclerView.Adapter<ParticipantSelectionAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val checkBox: CheckBox = view.findViewById(R.id.cbParticipant)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(inflater.inflate(R.layout.item_participant_selection, parent, false))
    }

    override fun getItemCount(): Int {
        return participants.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val participant = participants[position]
        holder.checkBox.text = participant.name
        holder.checkBox.isChecked = selectedParticipantIds.contains(participant.id)
        
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedParticipantIds.add(participant.id)
            } else {
                selectedParticipantIds.remove(participant.id)
            }
        }
    }
    
    fun getSelectedParticipantIds(): List<Long> {
        return selectedParticipantIds.toList()
    }
}
