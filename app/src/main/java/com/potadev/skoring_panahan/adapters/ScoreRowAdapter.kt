package com.potadev.skoring_panahan.adapters

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.potadev.skoring_panahan.R
import com.potadev.skoring_panahan.data.entity.Score
import com.potadev.skoring_panahan.ui.rounds.ScoreViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ScoreRowAdapter(
    private val roundId: Long,
    private var participantId: Long,
    private val numberOfEnds: Int,
    private val shootsPerEnd: Int,
    private var scores: List<Score>,
    private val scoreViewModel: ScoreViewModel
) : RecyclerView.Adapter<ScoreRowAdapter.ViewHolder>() {
    
    // Declare as separate properties to ensure they're mutable
//    private var participantId: Long = participantId
//    private var scores: List<Score> = scores

    // Group scores by end number, filtering for current participant only
    private var scoresByEnd = scores.filter { it.participantId == participantId }.groupBy { it.endNumber }
    
    // Update scores when participant changes
    @SuppressLint("NotifyDataSetChanged")
    fun updateScores(newScores: List<Score>, newParticipantId: Long? = null) {
        // Update participant ID if provided
        if (newParticipantId != null) {
//            Log.i("NEW PARTICIPANT ID","$newParticipantId")
            this.participantId = newParticipantId
        }
        
        // Filter scores for the current participant only
        this.scores = newScores.filter { it.participantId == this.participantId }
        this.scoresByEnd = this.scores.groupBy { it.endNumber }
        
        // Force a complete refresh of the adapter
        notifyDataSetChanged()
    }
    
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvEndNumber: TextView = view.findViewById(R.id.tvEndNumber)
        val scoreInputsContainer: LinearLayout = view.findViewById(R.id.scoreInputsContainer)
        val tvEndTotal: TextView = view.findViewById(R.id.tvEndTotal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_score_row, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = numberOfEnds

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val endNumber = position + 1
        holder.tvEndNumber.text = "End $endNumber"
        
        // Clear previous views
        holder.scoreInputsContainer.removeAllViews()
        
        // Get scores for this end
        val endScores = scoresByEnd[endNumber] ?: emptyList()
        
        var endTotal = 0
//        Log.i("PARTICIPANT ID","$participantId")
        // Add score inputs for each shoot
        for (shootNumber in 1..shootsPerEnd) {
            // Since we've already filtered scores by participant ID in updateScores,
            // we only need to find by shootNumber here
            val score = endScores.find { it.shootNumber == shootNumber }?.score ?: 0
            endTotal += score
            
            val scoreInputView = createScoreInputView(
                holder.itemView.context.layoutInflater,
                holder.scoreInputsContainer,
                score,
                shootNumber,
                endNumber
            )
            
            holder.scoreInputsContainer.addView(scoreInputView)
        }
        
        // Set end total
        holder.tvEndTotal.text = endTotal.toString()
    }
    
    private fun createScoreInputView(
        inflater: LayoutInflater,
        parent: ViewGroup,
        initialScore: Int,
        shootNumber: Int,
        endNumber: Int
    ): View {
        val view = inflater.inflate(R.layout.item_score_input, parent, false)
        
        val tvScore = view.findViewById<TextView>(R.id.tvScore)
        val btnMinus = view.findViewById<Button>(R.id.btnMinus)
        val btnPlus = view.findViewById<Button>(R.id.btnPlus)
        val tvShootNumber = view.findViewById<TextView>(R.id.tvShootNumber)
        
        // Always set the text to the current score value
        tvScore.text = initialScore.toString()
        tvShootNumber.text = "Shoot $shootNumber"
        
        btnMinus.setOnClickListener {
            Log.i("MINUS","$endNumber - $shootNumber")
            if (tvScore.text.toString().toInt() > 0) {
                val newScore = tvScore.text.toString().toInt() - 1
                tvScore.text = newScore.toString()
                updateScore(endNumber, shootNumber, newScore)
                notifyItemChanged(endNumber - 1) // Refresh to update total
            }
        }
        
        btnPlus.setOnClickListener {
            Log.i("PLUS","$endNumber - $shootNumber")
            if (tvScore.text.toString().toInt() < 10) {
                val newScore = tvScore.text.toString().toInt() + 1
                tvScore.text = newScore.toString()
                updateScore(endNumber, shootNumber, newScore)
                notifyItemChanged(endNumber - 1) // Refresh to update total
            }
        }
        
        return view
    }
    
    private fun updateScore(endNumber: Int, shootNumber: Int, score: Int) {
        Log.i("UPDATE_SCORE", "Updating score for participant: $participantId, end: $endNumber, shoot: $shootNumber, score: $score")
        CoroutineScope(Dispatchers.IO).launch {
            scoreViewModel.updateScore(roundId, participantId, endNumber, shootNumber, score)
        }
    }
}

// Extension function to get LayoutInflater from Context
private val android.content.Context.layoutInflater: LayoutInflater
    get() = LayoutInflater.from(this)
