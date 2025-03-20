package com.potadev.skoring_panahan.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.potadev.skoring_panahan.R
import com.potadev.skoring_panahan.data.entity.Round
import com.potadev.skoring_panahan.ui.rounds.RoundViewModel
import java.text.SimpleDateFormat
import java.util.Locale

class RoundsAdapter(
    private val rounds: List<Round>,
    private val viewModel: RoundViewModel,
    private val onViewScoresClick: (Round) -> Unit,
    private val onEditClick: (Round) -> Unit
) : RecyclerView.Adapter<RoundsAdapter.ViewHolder>() {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val roundName: TextView = view.findViewById(R.id.tvRoundName)
        val date: TextView = view.findViewById(R.id.tvDate)
        val numberOfEnds: TextView = view.findViewById(R.id.tvNumberOfEnds)
        val shootsPerEnd: TextView = view.findViewById(R.id.tvShootsPerEnd)
        val btnViewScores: Button = view.findViewById(R.id.btnViewScores)
        val btnEdit: Button = view.findViewById(R.id.btnEdit)
        val btnDelete: Button = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(inflater.inflate(R.layout.item_round, parent, false))
    }

    override fun getItemCount(): Int {
        return rounds.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val round = rounds[position]
        holder.roundName.text = round.name
        holder.date.text = "Date: ${dateFormat.format(round.date)}"
        holder.numberOfEnds.text = "Ends: ${round.numberOfEnds}"
        holder.shootsPerEnd.text = "Shots per end: ${round.shootsPerEnd}"
        
        holder.btnViewScores.setOnClickListener {
            onViewScoresClick(round)
        }
        
        holder.btnEdit.setOnClickListener {
            onEditClick(round)
        }
        
        holder.btnDelete.setOnClickListener {
            viewModel.deleteRound(round)
        }
    }
}
