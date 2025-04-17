package com.potadev.skoring_panahan.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.potadev.skoring_panahan.R
import com.potadev.skoring_panahan.data.entity.Participant
import com.potadev.skoring_panahan.data.entity.Score
import com.potadev.skoring_panahan.ui.rounds.ScoreViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ScoresAdapter(
    private val scores: List<Score>,
    private val viewModel: ScoreViewModel,
    private val participant: Participant,
    private val roundId: Long
) : RecyclerView.Adapter<ScoresAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val scoreValue: TextView = view.findViewById(R.id.tvScore)
        val endNumber: TextView = view.findViewById(R.id.tvEnds)
        val shootNumber: TextView = view.findViewById(R.id.tvShoots)
        val participantName: TextView = view.findViewById(R.id.tvParticipant)
        val btnMinus: Button = view.findViewById(R.id.btnMinus)
        val btnPlus: Button = view.findViewById(R.id.btnPlus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_score, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return scores.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val score = scores[position]
//        holder.scoreValue.text = score.score.toString()
//        holder.endNumber.text = score.endNumber.toString()
//        holder.shootNumber.text = score.shootNumber.toString()
//        holder.participantName.text = participant.name
//
//        holder.btnMinus.setOnClickListener {
//            if (score.score > 0) {
//                val newScore = score.score - 1
//updateScore(score.roundId, score.participantId, score.endNumber, score.shootNumber, newScore, false, false)
//                holder.scoreValue.text = newScore.toString()
//            }
//        }
//
//        holder.btnPlus.setOnClickListener {
//            if (score.score < 10) {
//                val newScore = score.score + 1
//                updateScore(score.roundId, score.participantId, score.endNumber, score.shootNumber, newScore)
//                holder.scoreValue.text = newScore.toString()
//            }
//        }
    }
    
private fun updateScore(roundId: Long, participantId: Long, endNumber: Int, shootNumber: Int, score: Int, bullseye: Boolean, miss: Boolean) {
    CoroutineScope(Dispatchers.IO).launch {
        viewModel.updateScore(roundId, participantId, endNumber, shootNumber, score, bullseye, miss)
    }
}
}
