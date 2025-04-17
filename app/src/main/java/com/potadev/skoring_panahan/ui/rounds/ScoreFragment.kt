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
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
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

    var selectedScore: Score? = null
    private lateinit var keyButtonLayout : LinearLayout
    private lateinit var sv : ScrollView

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
        scoreTable = root.findViewById(R.id.scoreTable)

        keyButtonLayout = root.findViewById<LinearLayout>(R.id.btnLayout)
        keyButtonLayout.visibility = View.GONE

        sv = root.findViewById(R.id.scrollView)
        //change padding bottom 0dp
        sv.setPadding(0, 0, 0, 0)
        
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

        val buttons = listOf(
            root.findViewById<Button>(R.id.btn1),
            root.findViewById<Button>(R.id.btn2),
            root.findViewById<Button>(R.id.btn3),
            root.findViewById<Button>(R.id.btn4),
            root.findViewById<Button>(R.id.btn5),
            root.findViewById<Button>(R.id.btn6),
            root.findViewById<Button>(R.id.btn7),
            root.findViewById<Button>(R.id.btn8),
            root.findViewById<Button>(R.id.btn9),
            root.findViewById<Button>(R.id.btn10),
            root.findViewById<Button>(R.id.btnX),
            root.findViewById<Button>(R.id.btnM)
        )

        buttons.forEach { button ->
            Log.i("BUTTON", button?.text.toString())
            button.setOnClickListener {
                val newScore = when (button.id) {
                    R.id.btnX -> 10
                    R.id.btnM -> 0
                    else -> button.text.toString().toInt()
                }

                if(selectedScore != null){
                    updateScore(selectedScore!!.roundId, selectedScore!!.endNumber, selectedScore!!.shootNumber, newScore)
                }
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
        
        // heading
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
        
        // Add header row to table
        scoreTable.addView(headerRow)
        
        // Group scores by end number
        val scoresByEnd = scores.groupBy { it.endNumber }
        
        // Create new rows
        for (endNumber in 1..round.numberOfEnds) {
            val endScores = scoresByEnd[endNumber] ?: emptyList()
            val tableRow = TableRow(context).apply {
                layoutParams = TableLayout.LayoutParams(
                    TableLayout.LayoutParams.WRAP_CONTENT,
                    TableLayout.LayoutParams.WRAP_CONTENT
                )

                // Alternate row colors
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
            
            // Add score input (plus/minus buttons)
            for (shootNumber in 1..round.shootsPerEnd) {
                val score = endScores.find { it.shootNumber == shootNumber }?.score ?: 0
                endTotal += score
                
                val scoreInputView = createScoreInputView(score, shootNumber, endNumber, round.id)
                val cellContainer = TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT
                )
                tableRow.addView(scoreInputView, cellContainer)
            }
            
            // Add total score column at the end of the row
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
            
            // Add row to table
            scoreTable.addView(tableRow)
        }
    }




    // Function to create the custom keyboard input view
    @SuppressLint("MissingInflatedId")
    private fun createScoreInputView(initialScore: Int, shootNumber: Int, endNumber: Int, roundId: Long): View {
        val view = layoutInflater.inflate(R.layout.item_row, null)
        val tvScore = view.findViewById<TextView>(R.id.tileTitle)
        val tvSub = view.findViewById<TextView>(R.id.tileSubtitle)
        tvScore.text = initialScore.toString()
        tvSub.text = "$endNumber x $shootNumber"
        val selectableTile: LinearLayout = view.findViewById(R.id.selectableTileContainer)
        if(selectedScore != null){
            val findScore = scores.find { it.endNumber == endNumber && it.shootNumber == shootNumber }
            if(findScore != null && selectedScore!!.id == findScore.id){
                selectableTile.isSelected = true
            } else {
                selectableTile.isSelected = false
            }
        } else {
            selectableTile.isSelected = false
        }
        selectableTile.setOnClickListener {
            selectedScore = scores.find { it.endNumber == endNumber && it.shootNumber == shootNumber }

            // Toast.makeText(context, "Tile Clicked!", Toast.LENGTH_SHORT).show()

            if(selectedParticipant != null && currentRound != null){
                loadScores(currentRound!!, selectedParticipant!!)
            }

            keyButtonLayout.visibility = View.VISIBLE
            sv.setPadding(0, 0, 0, 440)

        }

//
        
        return view
    }
    

    // Function to update score in the database
    private fun updateScore(roundId: Long, endNumber: Int, shootNumber: Int, score: Int) {
        Log.i("UPDATE_SCORE", "Updating score for participant: ${selectedParticipant?.id}, end: $endNumber, shoot: $shootNumber, score: $score")
        CoroutineScope(Dispatchers.IO).launch {
            scoreViewModel.updateScore(roundId, selectedParticipant!!.id, endNumber, shootNumber, score, false, false)
        }
    }

    // Function to insert a new score into the database
    private fun insertScore(roundId: Long, endNumber: Int, shootNumber: Int, score: Int, bullseye: Boolean, miss: Boolean) {
        val newScore = Score(
            roundId = roundId,
            participantId = selectedParticipant!!.id,
            endNumber = endNumber,
            shootNumber = shootNumber,
            score = score,
            bullseye = bullseye,
            miss = miss
        )
        CoroutineScope(Dispatchers.IO).launch {
            scoreViewModel.insertScore(newScore, bullseye, miss)
        }
    }
}
