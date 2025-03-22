package com.potadev.skoring_panahan.ui.rounds

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.databinding.DataBindingUtil
import com.potadev.skoring_panahan.R
import com.potadev.skoring_panahan.databinding.FragmentRankingBinding
import com.potadev.skoring_panahan.adapters.RankingAdapter
import com.potadev.skoring_panahan.data.repository.RoundRepository
import androidx.lifecycle.ViewModelProvider
import com.potadev.skoring_panahan.adapters.RoundsAdapter
import com.potadev.skoring_panahan.data.AppDatabase
import com.potadev.skoring_panahan.ui.participants.ParticipantViewModel

class RankingFragment : Fragment() {

    private var _binding: FragmentRankingBinding? = null
    private val binding get() = _binding!!
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RankingAdapter
    private lateinit var roundViewModel: RoundViewModel
    private lateinit var scoreViewModel: ScoreViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        scoreViewModel = ViewModelProvider(this)[ScoreViewModel::class.java]
        roundViewModel = ViewModelProvider(this)[RoundViewModel::class.java]



        _binding = FragmentRankingBinding.inflate(inflater, container, false)
        recyclerView = binding.rvRankings
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = RankingAdapter()
        recyclerView.adapter = adapter

        // Get round ID from arguments
        val roundId = arguments?.getLong("roundId") ?: 0L
        fetchRankingData(roundId)
        return binding.root
    }

    private fun fetchRankingData(roundId: Long) {
        Log.i("fetchRankingData","$roundId")
        // Set the round ID in the view model
//        scoreViewModel.setRound(roundId)
//        scoreViewModel.getScoresInRound(roundId)?.let {
//            adapter.setData(it)
//            Log.i("RANKING SIZE","${it.size}")
//            adapter.notifyDataSetChanged()
//        }

        scoreViewModel.getScoresInRound(roundId).observe(viewLifecycleOwner) { scores ->

                        var rankingItems = scores?.groupBy { it.score.participantId }
                ?.map { (id, sc) ->
                    val totalScore = sc.sumOf { it.score.score }
                    RankingAdapter.RankingItem(
                        rank = 0, // Rank will be assigned after sorting
                        name = sc.first().participant.name, // Placeholder name, should be replaced with actual participant name
                        score = totalScore
                    )
                }
                ?.sortedByDescending { it.score }
                ?.mapIndexed { index, item ->
                    item.copy(rank = index + 1)
                }
           if(rankingItems != null){
               adapter.setData(rankingItems)
               adapter.notifyDataSetChanged()
           }

            scores.map { (score,participant) ->
                Log.i("SCORE", "${participant.name} : ${score.score}")
            }
        }



    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
