package com.potadev.skoring_panahan.ui.rounds

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.databinding.DataBindingUtil
import com.potadev.skoring_panahan.R
import com.potadev.skoring_panahan.databinding.FragmentRankingBinding
import com.potadev.skoring_panahan.adapters.RankingAdapter
import com.potadev.skoring_panahan.data.repository.RoundRepository
import androidx.lifecycle.ViewModelProvider
import android.Manifest
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.potadev.skoring_panahan.adapters.RoundsAdapter
import com.potadev.skoring_panahan.data.AppDatabase
import com.potadev.skoring_panahan.data.entity.Round
import com.potadev.skoring_panahan.data.entity.ScoreWithParticipant
import com.potadev.skoring_panahan.excel.ExcelManager
import com.potadev.skoring_panahan.ui.participants.ParticipantViewModel

class RankingFragment : Fragment() {

    private var _binding: FragmentRankingBinding? = null
    private val binding get() = _binding!!
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RankingAdapter
    private lateinit var roundViewModel: RoundViewModel
    private lateinit var scoreViewModel: ScoreViewModel

    private lateinit var excelManager: ExcelManager
    private var excelFilePath: String? = null

    lateinit var roundScores : List<ScoreWithParticipant>
    lateinit var currentRound: Round

    companion object {
        private const val STORAGE_PERMISSION_CODE = 101
    }


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
        // Set the round ID in the view model
        scoreViewModel.setRound(roundId)

        scoreViewModel.currentRound.observe(viewLifecycleOwner) { round ->
            currentRound = round
            fetchRankingData(roundId)
        }
       


        excelManager = ExcelManager(requireContext())

        val createButton = binding.btnCreateExcel

        createButton.setOnClickListener {
            if(roundScores.isNotEmpty()){
                if (checkPermission()) {
                    excelFilePath = excelManager.exportSkor("ranking_${roundId}",currentRound, roundScores)
                    if (excelFilePath != null) {

                        Toast.makeText(requireContext(), "Excel file created successfully : $excelFilePath", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Failed to create Excel file", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    requestPermission()
                }
            }

        }

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
            roundScores = scores
                        var rankingItems = scores?.groupBy { it.score.participantId }
                ?.map { (id, sc) ->
                    val totalScore = sc.sumOf { it.score.score }
                    RankingAdapter.RankingItem(
                        id = id,
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

    private fun checkPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        requestPermissions(
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            STORAGE_PERMISSION_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "Storage Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Storage Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
