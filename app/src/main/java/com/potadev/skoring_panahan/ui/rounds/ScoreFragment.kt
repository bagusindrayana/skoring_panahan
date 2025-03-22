package com.potadev.skoring_panahan.ui.rounds

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.potadev.skoring_panahan.R
import com.potadev.skoring_panahan.data.entity.Participant
import com.potadev.skoring_panahan.data.entity.Round
import com.potadev.skoring_panahan.data.entity.Score
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
    private lateinit var scoreTable: TableLayout
    
    private var currentRound: Round? = null
    private var participants: List<Participant> = emptyList()
    private var scores: List<Score> = emptyList()
    
    var selectedParticipant: Participant? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_score_table_new, container, false)
        
        scoreViewModel = ViewModelProvider(this).get(ScoreViewModel::class.java)
        roundViewModel = ViewModelProvider(this).get(RoundViewModel::class.java)
        
        tvRoundName = root.findViewById(R.id.tvRoundName)
        tvRoundDetails = root.findViewById(R.id.tvRoundDetails)
        spinnerParticipants = root.findViewById(R.id.spinnerParticipants)
        scoreTable = root.findViewById(R.id.scoreTable)
        
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
            if(selectedParticipant != null && currentRound != null){
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
                scoreViewModel.getScoresForParticipantInRound(currentRound!!.id, selectedParticipant!!.id).removeObservers(viewLifecycleOwner)
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }
    
    private fun loadScores(round: Round, participant: Participant) {
        scoreViewModel.getScoresForParticipantInRound(round.id, participant.id).observe(viewLifecycleOwner) { _scores ->
            if (_scores.isNotEmpty() && _scores.first().participantId != selectedParticipant!!.id) {
                return@observe
            }
            
            scores = _scores.filter { it.participantId == selectedParticipant!!.id }
            Log.i("LOADSCORES", "${selectedParticipant?.id}")
            
            // Rebuild the table with the new scores
            buildScoreTable(round, scores)
        }
    }

    fun isNightMode(context: Context): Boolean {
        val nightModeFlags =
            context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
    }
    
    @SuppressLint("ResourceAsColor")
    private fun buildScoreTable(round: Round, scores: List<Score>) {
        // Clear existing table
        scoreTable.removeAllViews()
        var nightMode = isNightMode(requireContext())
        
        // Create header row
        val headerRow = TableRow(context).apply {
            layoutParams = TableLayout.LayoutParams(
                TableLayout.LayoutParams.WRAP_CONTENT,
                TableLayout.LayoutParams.WRAP_CONTENT
            )
            setBackgroundResource(R.color.target_gold)
        }
        
        // Add "End" header
        headerRow.addView(TextView(context).apply {
            layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
            )
            text = "End"
            setPadding(8, 8, 8, 8)
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
            gravity = android.view.Gravity.CENTER
            minWidth = 80
            if(nightMode){
                setTextColor(R.color.target_black)
            }
        })
        
        // Add shoot headers
        for (i in 1..round.shootsPerEnd) {
            headerRow.addView(TextView(context).apply {
                layoutParams = TableRow.LayoutParams(
                    TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT
                )
                text = "Shoot $i"
                setPadding(8, 8, 8, 8)
                textSize = 16f
                setTypeface(null, android.graphics.Typeface.BOLD)
                gravity = android.view.Gravity.CENTER
                minWidth = 160
                if(nightMode){
                    setTextColor(R.color.target_black)
                }
            })
        }
        
        // Add "Total" header
        headerRow.addView(TextView(context).apply {
            layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
            )
            text = "Total"
            setPadding(8, 8, 8, 8)
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
            gravity = android.view.Gravity.CENTER
            minWidth = 80
            if(nightMode){
                setTextColor(R.color.target_black)
            }
        })
        
        scoreTable.addView(headerRow)
        
        // Group scores by end number
        val scoresByEnd = scores.groupBy { it.endNumber }
        
        // Create rows for each end
        for (endNumber in 1..round.numberOfEnds) {
            val endScores = scoresByEnd[endNumber] ?: emptyList()
            val tableRow = TableRow(context).apply {
                layoutParams = TableLayout.LayoutParams(
                    TableLayout.LayoutParams.WRAP_CONTENT,
                    TableLayout.LayoutParams.WRAP_CONTENT
                )
                if (endNumber % 2 == 0) {
                    if(nightMode){
                        setBackgroundColor(0xFF212121.toInt())
                    } else {
                        setBackgroundColor(0xFFEEEEEE.toInt())
                    }

                }
            }
            
            // Add end number cell
            tableRow.addView(TextView(context).apply {
                layoutParams = TableRow.LayoutParams(
                    TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT
                )
                text = "End $endNumber"
                setPadding(8, 8, 8, 8)
                textSize = 16f
                setTypeface(null, android.graphics.Typeface.BOLD)
                gravity = android.view.Gravity.CENTER
                minWidth = 80
            })
            
            var endTotal = 0
            
            // Add score input cells for each shoot
            for (shootNumber in 1..round.shootsPerEnd) {
                val score = endScores.find { it.shootNumber == shootNumber }?.score ?: 0
                endTotal += score
                
                val scoreInputView = createScoreInputView(score, shootNumber, endNumber, round.id)
                val cellContainer = TableRow.LayoutParams(
                    TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT
                )
                tableRow.addView(scoreInputView, cellContainer)
            }
            
            // Add end total cell
            tableRow.addView(TextView(context).apply {
                layoutParams = TableRow.LayoutParams(
                    TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT
                )
                text = endTotal.toString()
                setPadding(8, 8, 8, 8)
                textSize = 16f
                setTypeface(null, android.graphics.Typeface.BOLD)
                gravity = android.view.Gravity.CENTER
                minWidth = 80
            })
            
            scoreTable.addView(tableRow)
        }
    }
    
    private fun createScoreInputView(initialScore: Int, shootNumber: Int, endNumber: Int, roundId: Long): View {
        val view = layoutInflater.inflate(R.layout.table_cell_score_input, null)
        
        val tvScore = view.findViewById<TextView>(R.id.tvScore)
        val btnMinus = view.findViewById<Button>(R.id.btnMinus)
        val btnPlus = view.findViewById<Button>(R.id.btnPlus)
        val tvShootNumber = view.findViewById<TextView>(R.id.tvShootNumber)
        
        tvScore.text = initialScore.toString()
        tvShootNumber.text = "Shoot $shootNumber"
        
        btnMinus.setOnClickListener {
            Log.i("MINUS", "$endNumber - $shootNumber")
            if (tvScore.text.toString().toInt() > 0) {
                val newScore = tvScore.text.toString().toInt() - 1
                tvScore.text = newScore.toString()
                updateScore(roundId, endNumber, shootNumber, newScore)
            }
        }
        
        btnPlus.setOnClickListener {
            Log.i("PLUS", "$endNumber - $shootNumber")
            if (tvScore.text.toString().toInt() < 10) {
                val newScore = tvScore.text.toString().toInt() + 1
                tvScore.text = newScore.toString()
                updateScore(roundId, endNumber, shootNumber, newScore)
            }
        }
        
        return view
    }
    
    private fun updateScore(roundId: Long, endNumber: Int, shootNumber: Int, score: Int) {
        Log.i("UPDATE_SCORE", "Updating score for participant: ${selectedParticipant?.id}, end: $endNumber, shoot: $shootNumber, score: $score")
        CoroutineScope(Dispatchers.IO).launch {
            scoreViewModel.updateScore(roundId, selectedParticipant!!.id, endNumber, shootNumber, score)
        }
    }
}
