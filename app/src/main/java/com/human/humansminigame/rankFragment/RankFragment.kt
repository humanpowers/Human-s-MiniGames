package com.human.humansminigame.rankFragment


import android.content.Context
import android.os.Bundle

import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.human.humansminigame.CustomDialog
import com.human.humansminigame.MainActivityEvent
import com.human.humansminigame.MainViewModel
import com.human.humansminigame.R
import com.human.humansminigame.databinding.FragmentRankBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class RankFragment : Fragment() {
    val TAG = "RankFragmentTAG"
    private lateinit var context: Context
    lateinit var binding: FragmentRankBinding
    private lateinit var mainViewModel: MainViewModel
    var event: MainActivityEvent? = null

    private lateinit var dialog : CustomDialog

    private var doubleBackToExitPressedOnce = false

    private val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (doubleBackToExitPressedOnce) {
                mainViewModel.initRank()
                event?.moveToMainFragment()
                return
            }
            doubleBackToExitPressedOnce = true
            Toast.makeText(context, "한 번 더 누르면 메인으로 이동합니다.", Toast.LENGTH_SHORT).show()

            lifecycleScope.launch {
                delay(2000) // 2초 대기
                doubleBackToExitPressedOnce = false
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.context = context
        if (context is MainActivityEvent) {
            event = context
        } else {
            throw RuntimeException(
                context.toString()
                        + "must implement FragmentEvent"
            )
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_rank, container, false)
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        binding.viewModel = mainViewModel
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
    }

    fun init() {

        dialog = CustomDialog()
        val loadingDialog = dialog.getLoadingDialog(context)
        loadingDialog.show()

        val internetDialog = dialog.getInternetDialog(context)

        internetDialog.findViewById<Button>(R.id.internetDialogReconnectButton).setOnClickListener {
            internetDialog.dismiss()
            mainViewModel.internetError()
        }

        mainViewModel.dataError.observe(viewLifecycleOwner, Observer {
            if(it){
                Toast.makeText(context,"데이터를 불러오지 못했습니다.\n잠시후에 다시 시도해주세요.",Toast.LENGTH_SHORT).show()
                mainViewModel.initDataError()
            }
        })

        binding.rankCheckButton.setOnClickListener {
            event?.moveToMainFragment()
            mainViewModel.initRank()
            mainViewModel.initInternet()
        }

        mainViewModel.internetError()

        mainViewModel.internetError.observe(viewLifecycleOwner, Observer {
            when(it){
                "true" -> internetDialog.show()
                "false" -> mainViewModel.fetchRankData()
            }
        })

        mainViewModel.rank.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                loadingDialog.dismiss()

                binding.rankFrameLayout.visibility = View.GONE

                binding.rankMovieButton.setOnClickListener {
                    mainViewModel.myScoreInit()
                    mainViewModel.showRanking("movie")
                }

                binding.rankCardButton.setOnClickListener {
                    mainViewModel.myScoreInit()
                    mainViewModel.showRanking("card")
                }

                binding.rankLeftRightButton.setOnClickListener {
                    mainViewModel.myScoreInit()
                    mainViewModel.showRanking("leftRight")

                }

                binding.rankOperationsButton.setOnClickListener {
                    mainViewModel.myScoreInit()
                    mainViewModel.showRanking("operations")

                }
            }
        })

        mainViewModel.gameStatus.observe(viewLifecycleOwner, Observer { status ->
            val movieButtonBackground =
                if (status == "movie") R.drawable.rank_type_background_selected
                else R.drawable.rank_type_background

            val leftRightButtonBackground =
                if (status == "leftRight") R.drawable.rank_type_background_selected
                else R.drawable.rank_type_background

            val cardButtonBackground =
                if (status == "card") R.drawable.rank_type_background_selected
                else R.drawable.rank_type_background

            val operationsButtonBackground =
                if (status == "operations") R.drawable.rank_type_background_selected
                else R.drawable.rank_type_background

            binding.rankMovieButton.setBackgroundResource(movieButtonBackground)
            binding.rankLeftRightButton.setBackgroundResource(leftRightButtonBackground)
            binding.rankCardButton.setBackgroundResource(cardButtonBackground)
            binding.rankOperationsButton.setBackgroundResource(operationsButtonBackground)
        })


        mainViewModel.getRank.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "getRank: $it")

            if (it != null) {
                val defaultName = "---"
                val defaultScore = "0"

                val bindings = listOf(
                    binding.rankFirstName to it.getOrNull(0),
                    binding.rankFirstScore to it.getOrNull(0),

                    binding.rankSecondName to it.getOrNull(1),
                    binding.rankSecondScore to it.getOrNull(1),

                    binding.rankThirdName to it.getOrNull(2),
                    binding.rankThirdScore to it.getOrNull(2),

                    binding.rankFourthName to it.getOrNull(3),
                    binding.rankFourthScore to it.getOrNull(3),

                    binding.rankFifthName to it.getOrNull(4),
                    binding.rankFifthScore to it.getOrNull(4)
                )

                bindings.forEachIndexed { index, (textView, item) ->
                    if (index % 2 == 0) {
                        textView.text = item?.name ?: defaultName
                    } else {
                        textView.text = item?.score ?: defaultScore
                    }
                }

                mainViewModel.findMyScoreNum(it)
            }

        })

        mainViewModel.myScoreNum.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "init: 1234$it")
            myScore(it)
            if (it > 5) {
                binding.rankFifthRanking.text = it.toString() + "th"
                mainViewModel.findMyScore(it)

                binding.rankFourthLinear.visibility = View.INVISIBLE
                binding.rankNoRankingImage.visibility = View.VISIBLE

            }else{
                binding.rankFifthRanking.text = "5th"
                binding.rankFourthLinear.visibility = View.VISIBLE
                binding.rankNoRankingImage.visibility = View.INVISIBLE
            }
        })

        mainViewModel.myScore.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                binding.rankFifthName.text = it.name
                binding.rankFifthScore.text = it.score
            }
        })

        this.requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), callback)

    }

    private fun myScore(num: Int) {

        binding.rankFirstLinear.setBackgroundResource(R.drawable.box_yellow)
        binding.rankSecondLinear.setBackgroundResource(R.drawable.box_yellow)
        binding.rankThirdLinear.setBackgroundResource(R.drawable.box_yellow)
        binding.rankFourthLinear.setBackgroundResource(R.drawable.box_yellow)
        binding.rankFifthLinear.setBackgroundResource(R.drawable.box_yellow)


        when (num) {
            1 -> binding.rankFirstLinear.setBackgroundResource(R.drawable.box_human)

            2 -> binding.rankSecondLinear.setBackgroundResource(R.drawable.box_human)

            3 -> binding.rankThirdLinear.setBackgroundResource(R.drawable.box_human)

            4 -> binding.rankFourthLinear.setBackgroundResource(R.drawable.box_human)

            5 -> binding.rankFifthLinear.setBackgroundResource(R.drawable.box_human)

            else -> binding.rankFifthLinear.setBackgroundResource(R.drawable.box_human)
        }

    }



}