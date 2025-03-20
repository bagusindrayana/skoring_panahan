package com.potadev.skoring_panahan.ui.rounds

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.potadev.skoring_panahan.R
import com.potadev.skoring_panahan.adapters.ScoreRowAdapter
import com.potadev.skoring_panahan.data.entity.Participant
import com.potadev.skoring_panahan.data.entity.Round
import com.potadev.skoring_panahan.data.entity.Score
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
    private lateinit var rvScoreRows: RecyclerView
    private lateinit var headerRow: LinearLayout
    
    private var currentRound: Round? = null
    private var participants: List<Participant> = emptyList()

    private var scores: List<Score> = emptyList()
    private var adapter: ScoreRowAdapter? = null

    var selectedParticipant: Participant? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_score_table, container, false)
        
        scoreViewModel = ViewModelProvider(this).get(ScoreViewModel::class.java)
        roundViewModel = ViewModelProvider(this).get(RoundViewModel::class.java)
        
        tvRoundName = root.findViewById(R.id.tvRoundName)
        tvRoundDetails = root.findViewById(R.id.tvRoundDetails)
        spinnerParticipants = root.findViewById(R.id.spinnerParticipants)
        rvScoreRows = root.findViewById(R.id.rvScoreRows)
        headerRow = root.findViewById(R.id.headerRow)
        
        rvScoreRows.layoutManager = LinearLayoutManager(context)


        
        // Set the round ID in the view model
        scoreViewModel.setRound(args.roundId)

        // Observe the current round
        scoreViewModel.currentRound.observe(viewLifecycleOwner) { round ->
            currentRound = round
            updateRoundInfo(round)
            setupHeaderRow(round.shootsPerEnd)
        }

        // Load round with participants
        roundViewModel.getRoundWithParticipants(args.roundId).observe(viewLifecycleOwner) { roundWithParticipants ->
            participants = roundWithParticipants.participants
            setupParticipantsSpinner()
        }

        // Observe the current participant
        scoreViewModel.currentParticipant.observe(viewLifecycleOwner) { participant ->


            if(selectedParticipant != null){
                loadScores(currentRound!!, selectedParticipant!!)
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
    
    private fun setupHeaderRow(shootsPerEnd: Int) {
        // Clear previous headers (except the first and last which are fixed)
        for (i in headerRow.childCount - 2 downTo 1) {
            headerRow.removeViewAt(i)
        }
        
        // Add shoot headers
        for (i in 1..shootsPerEnd) {
            val shootHeader = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    resources.getDimensionPixelSize(R.dimen.score_cell_width),
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                gravity = android.view.Gravity.CENTER
                setPadding(8, 8, 8, 8)
                text = "Shoot $i"
                setTextAppearance(android.R.style.TextAppearance_Medium)
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
            
            // Insert before the last item (Total)
            headerRow.addView(shootHeader, headerRow.childCount - 1)
        }
    }
    
    private fun setupParticipantsSpinner() {
        Log.i("SETUP", "setupParticipantsSpinner")
        val participantNames = participants.map { it.name }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, participantNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerParticipants.adapter = adapter
        
        spinnerParticipants.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                selectedParticipant = participants[position]
                scoreViewModel.setParticipant(selectedParticipant!!.id)
//                scoreViewModel.currentParticipant.removeObservers(viewLifecycleOwner)
                scoreViewModel.getScoresForParticipantInRound(currentRound!!.id, selectedParticipant!!.id).removeObservers(viewLifecycleOwner)
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }
    
    private fun loadScores(round: Round, participant: Participant) {

        scoreViewModel.getScoresForParticipantInRound(round.id, participant.id).observe(viewLifecycleOwner) { _scores ->

            if(_scores.first().participantId != selectedParticipant!!.id){
                return@observe
            }
            scores = _scores.filter { it.participantId == selectedParticipant!!.id }

            Log.i("LOADSCORES", "${selectedParticipant?.id}")


            if (adapter == null) {
                // Create adapter only once
                adapter = ScoreRowAdapter(
                    round.id,
                    participant.id,
                    round.numberOfEnds,
                    round.shootsPerEnd,
                    scores,
                    scoreViewModel
                )
                rvScoreRows.adapter = adapter
            } else {
                // Update existing adapter with new scores and participant ID
                adapter?.updateScores(scores, selectedParticipant?.id)
            }
        }

//        var _scores = scoreViewModel.getScoresForParticipantInRound(round.id, participant.id).value
//        if(_scores == null){
//            _scores = emptyList()
//        }
//        scores = _scores

    }
}
