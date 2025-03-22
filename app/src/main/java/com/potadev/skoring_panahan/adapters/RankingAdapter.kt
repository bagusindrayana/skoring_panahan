package com.potadev.skoring_panahan.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.potadev.skoring_panahan.databinding.ItemRankingBinding

class RankingAdapter : RecyclerView.Adapter<RankingAdapter.RankingViewHolder>() {

    private val items = mutableListOf<RankingItem>()

    data class RankingItem(val rank: Int, val name: String, val score: Int)

    inner class RankingViewHolder(private val binding: ItemRankingBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: RankingItem) {
            binding.tvRank.text = item.rank.toString()
            binding.tvParticipantName.text = item.name
            binding.tvParticipantScore.text = item.score.toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankingViewHolder {
        val binding = ItemRankingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RankingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RankingViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun setData(newItems: List<RankingItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
