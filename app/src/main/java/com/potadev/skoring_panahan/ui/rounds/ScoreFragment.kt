package com.potadev.skoring_panahan.ui.rounds

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.potadev.skoring_panahan.R
import com.potadev.skoring_panahan.adapters.ScoresAdapter
import com.potadev.skoring_panahan.data.entity.Participant
import com.potadev.skoring_panahan.data.entity.Round
import com.potadev.skoring_panahan.data.repository.ParticipantRepository
import java.text.SimpleDateFormat
import java.util.Locale

class ScoreFragment : Fragment() {

    private lateinit var scoreViewModel: ScoreViewModel
    private lateinit var roundViewModel: RoundViewModel
    private val args: ScoreFragmentArgs by navArgs()
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    
    private lateinit var tvRoundName: TextView
    private lateinit var tvRoundDetails: TextView
    private lateinit var spinnerParticipants: Spinner
    private lateinit var rvScores: RecyclerView
    
    private var currentRound: Round? = null
    private var participants: List<Participant> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_score, container, false)
        
        scoreViewModel = ViewModelProvider(this).get(ScoreViewModel::class.java)
        roundViewModel = ViewModelProvider(this).get(RoundViewModel::class.java)
        
        tvRoundName = root.findViewById(R.id.tvRoundName)
        tvRoundDetails = root.findViewById(R.id.tvRoundDetails)
        spinnerParticipants = root.findViewById(R.id.spinnerParticipants)
        rvScores = root.findViewById(R.id.rvScores)
        
        rvScores.layoutManager = LinearLayoutManager(context)
        
        // Set the round ID in the view model
        scoreViewModel.setRound(args.roundId)
        
        // Observe the current round
        scoreViewModel.currentRound.observe(viewLifecycleOwner) { round ->
            currentRound = round
            updateRoundInfo(round)
        }
        
        // Load round with participants
        roundViewModel.getRoundWithParticipants(args.roundId).observe(viewLifecycleOwner) { roundWithParticipants ->
            participants = roundWithParticipants.participants
            setupParticipantsSpinner()
        }
        
        // Observe the current participant
        scoreViewModel.currentParticipant.observe(viewLifecycleOwner) { participant ->
            if (participant != null && currentRound != null) {
                loadScores(currentRound!!, participant)
            }
        }
        
        return root
    }
    
    private fun updateRoundInfo(round: Round) {
        tvRoundName.text = round.name
        val details = "Date: ${dateFormat.format(round.date)}\n" +
                "Ends: ${round.numberOfEnds}\n" +
                "Shoots per end: ${round.shootsPerEnd}"
        tvRoundDetails.text = details
    }
    
    private fun setupParticipantsSpinner() {
        val participantNames = participants.map { it.name }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, participantNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerParticipants.adapter = adapter
        
        spinnerParticipants.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedParticipant = participants[position]
                scoreViewModel.setParticipant(selectedParticipant.id)
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }
    
    private fun loadScores(round: Round, participant: Participant) {
        scoreViewModel.getScoresForParticipantInRound(round.id, participant.id).observe(viewLifecycleOwner) { scores ->
            val adapter = ScoresAdapter(scores, scoreViewModel, participant, round.id)
            rvScores.adapter = adapter
        }
    }
}
