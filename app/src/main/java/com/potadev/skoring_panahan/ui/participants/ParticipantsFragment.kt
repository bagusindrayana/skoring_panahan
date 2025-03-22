package com.potadev.skoring_panahan.ui.participants

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.potadev.skoring_panahan.R
import com.potadev.skoring_panahan.adapters.ParticipantsAdapter
import com.potadev.skoring_panahan.data.entity.Participant

class ParticipantsFragment : Fragment() {

    private lateinit var participantViewModel: ParticipantViewModel
    private lateinit var adapter: ParticipantsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_participants, container, false)
        
        participantViewModel = ViewModelProvider(this).get(ParticipantViewModel::class.java)
        
        val recyclerView = root.findViewById<RecyclerView>(R.id.rvParticipants)
        recyclerView.layoutManager = LinearLayoutManager(context)
        
        participantViewModel.allParticipants.observe(viewLifecycleOwner) { participants ->
            adapter = ParticipantsAdapter(participants, participantViewModel) { participant ->
                showAddEditParticipantDialog(participant)
            }

            if(participants.isEmpty()){
                root.findViewById<TextView>(R.id.textNoData).visibility = View.VISIBLE
            } else {
                root.findViewById<TextView>(R.id.textNoData).visibility = View.GONE
            }
            recyclerView.adapter = adapter
        }
        
        val fab = root.findViewById<FloatingActionButton>(R.id.fabAddParticipant)
        fab.setOnClickListener {
            showAddEditParticipantDialog(null)
        }
        
        return root
    }
    
    private fun showAddEditParticipantDialog(participant: Participant?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_participant, null)
        val etParticipantName = dialogView.findViewById<EditText>(R.id.etParticipantName)
        val tvTitle = dialogView.findViewById<TextView>(R.id.tvTitle)
        
        if (participant != null) {
            tvTitle.text = "Edit Participant"
            etParticipantName.setText(participant.name)
        } else {
            tvTitle.text = "Add Participant"
        }
        
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()
        
        dialogView.findViewById<Button>(R.id.btnSave).setOnClickListener {
            val name = etParticipantName.text.toString().trim()
            
            if (name.isNotEmpty()) {
                if (participant != null) {
                    // Update existing participant
                    val updatedParticipant = participant.copy(name = name)
                    participantViewModel.update(updatedParticipant)
                } else {
                    // Create new participant
                    val newParticipant = Participant(name = name)
                    participantViewModel.insert(newParticipant)
                }
                dialog.dismiss()
            }
        }
        
        dialog.show()
    }
}
