package com.potadev.skoring_panahan.ui.rounds

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.potadev.skoring_panahan.R
import com.potadev.skoring_panahan.adapters.ParticipantSelectionAdapter
import com.potadev.skoring_panahan.adapters.RoundsAdapter
import com.potadev.skoring_panahan.data.entity.Round
import com.potadev.skoring_panahan.ui.participants.ParticipantViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class RoundsFragment : Fragment() {

    private lateinit var roundViewModel: RoundViewModel
    private lateinit var participantViewModel: ParticipantViewModel
    private lateinit var adapter: RoundsAdapter
    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())


    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_rounds, container, false)
        
        roundViewModel = ViewModelProvider(this)[RoundViewModel::class.java]
        participantViewModel = ViewModelProvider(this)[ParticipantViewModel::class.java]
        
        val recyclerView = root.findViewById<RecyclerView>(R.id.rvRounds)
        recyclerView.layoutManager = LinearLayoutManager(context)


        
        roundViewModel.allRounds.observe(viewLifecycleOwner) { rounds ->
            // Initialize RoundsAdapter properly
            adapter = RoundsAdapter(
                rounds,
                roundViewModel,
                ::navigateToScoreScreen,
                ::navigateToRankingScreen,
                ::showAddEditRoundDialog // Added missing onEditClick parameter
            )

            if(rounds.isEmpty()){
                root.findViewById<TextView>(R.id.textNoData).visibility = View.VISIBLE
            } else {
                root.findViewById<TextView>(R.id.textNoData).visibility = View.GONE
            }

            recyclerView.adapter = adapter
        }
        
        val fab = root.findViewById<FloatingActionButton>(R.id.fabAddRound)
        fab.setOnClickListener {
            showAddEditRoundDialog(null)
        }
        
        return root
    }
    
    private fun navigateToScoreScreen(round: Round) {
        val action = RoundsFragmentDirections.actionRoundsFragmentToScoreFragment(round.id)
        findNavController().navigate(action)
    }

    private fun navigateToRankingScreen(round: Round) {
        val action = RoundsFragmentDirections.actionRoundsFragmentToRankingFragment(round.id)
        findNavController().navigate(action)
    }
    
    private fun showAddEditRoundDialog(round: Round?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_round, null)
        val etRoundName = dialogView.findViewById<EditText>(R.id.etRoundName)
        val etDate = dialogView.findViewById<EditText>(R.id.etDate)
        val etNumberOfEnds = dialogView.findViewById<EditText>(R.id.etNumberOfEnds)
        val etShootsPerEnd = dialogView.findViewById<EditText>(R.id.etShootsPerEnd)
        val tvTitle = dialogView.findViewById<TextView>(R.id.tvTitle)
        val rvParticipants = dialogView.findViewById<RecyclerView>(R.id.rvParticipants)
        
        // Set up date picker
        if (round != null) {
            calendar.time = round.date
        }
        
        etDate.setText(dateFormat.format(calendar.time))
        etDate.setOnClickListener {
            showDatePicker(etDate)
        }
        
        // Set up participant selection
        rvParticipants.layoutManager = LinearLayoutManager(context)
        val selectedParticipantIds = mutableSetOf<Long>()
        var participantSelectionAdapter = ParticipantSelectionAdapter()
        rvParticipants.adapter = participantSelectionAdapter
        if (round != null) {
            tvTitle.text = "Edit Round"
            etRoundName.setText(round.name)
            etNumberOfEnds.setText(round.numberOfEnds.toString())
            etShootsPerEnd.setText(round.shootsPerEnd.toString())
            
            // Load selected participants
            roundViewModel.getRoundWithParticipants(round.id).observe(viewLifecycleOwner) { roundWithParticipants ->
               if(roundWithParticipants != null){
                   roundWithParticipants.participants.forEach { participant ->
                       selectedParticipantIds.add(participant.id)
                       Log.i("PARTICIPANT_ID","${participant.id}")
                   }

                   participantSelectionAdapter.setSelectedParticipantIds(selectedParticipantIds.toList())

                   participantSelectionAdapter?.notifyDataSetChanged()
               }
            }
            Log.i("showAddEditRoundDialog","${round.id}")

            participantViewModel.allParticipants.observe(viewLifecycleOwner) { participants ->
                Log.i("participants","${participants.size}")
                participantSelectionAdapter.setParticipants(participants)


                participantSelectionAdapter?.notifyDataSetChanged()
            }


        } else {
            tvTitle.text = "Add Round"
            participantViewModel.allParticipants.observe(viewLifecycleOwner) { participants ->
                Log.i("participants","${participants.size}")
                participantSelectionAdapter.setParticipants(participants)


                participantSelectionAdapter?.notifyDataSetChanged()
            }
        }
        

        
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()
        
        dialogView.findViewById<Button>(R.id.btnSave).setOnClickListener {
            val name = etRoundName.text.toString().trim()
            val numberOfEndsText = etNumberOfEnds.text.toString().trim()
            val shootsPerEndText = etShootsPerEnd.text.toString().trim()
            
            if (name.isNotEmpty() && numberOfEndsText.isNotEmpty() && shootsPerEndText.isNotEmpty()) {
                val numberOfEnds = numberOfEndsText.toInt()
                val shootsPerEnd = shootsPerEndText.toInt()
                val date = calendar.time
                
                val participantSelectionAdapter = rvParticipants.adapter as? ParticipantSelectionAdapter
                val selectedIds = participantSelectionAdapter?.getSelectedParticipantIds() ?: emptyList()
                
                if (round != null) {
                    // Update existing round
                    val updatedRound = round.copy(
                        name = name,
                        date = date,
                        numberOfEnds = numberOfEnds,
                        shootsPerEnd = shootsPerEnd
                    )
                    roundViewModel.updateRound(updatedRound, selectedIds)
                } else {
                    // Create new round
                    roundViewModel.createRound(name, date, numberOfEnds, shootsPerEnd, selectedIds)
                }
                dialog.dismiss()
            }
        }
        
        dialog.show()
    }
    
    private fun showDatePicker(etDate: EditText) {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            etDate.setText(dateFormat.format(calendar.time))
        }
        
        DatePickerDialog(
            requireContext(),
            dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
}
