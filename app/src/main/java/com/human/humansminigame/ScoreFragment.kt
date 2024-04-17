package com.human.humansminigame

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.human.humansminigame.databinding.FragmentScoreBinding

class ScoreFragment : Fragment() {

    val TAG = "ScoreFragmentTAG"
    private lateinit var context: Context
    lateinit var binding: FragmentScoreBinding
    private lateinit var mainViewModel: MainViewModel
    var event: MainActivityEvent? = null

    private var doubleBackToExitPressedOnce = false

    private val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (doubleBackToExitPressedOnce) {
                mainViewModel.stopTimer()
                mainViewModel.resetBackPressToExit()
                mainViewModel.initOperation()
                mainViewModel.initRank()
                mainViewModel.initInternet()
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_score,container,false)
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        binding.mainViewModel = mainViewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
    }

    fun init() {
        mainViewModel.myBestScore.observe(viewLifecycleOwner, Observer {
            binding.scoreBestScore.text = it
        })

        mainViewModel.dataError.observe(viewLifecycleOwner, Observer {
            if(it){
                Toast.makeText(context,"데이터를 불러오지 못했습니다.\n잠시후에 다시 시도해주세요.",Toast.LENGTH_SHORT).show()
                mainViewModel.initDataError()
            }
        })

        binding.scoreCheckButton.setOnClickListener {
            event?.moveToMainFragment()
            mainViewModel.initRank()
            mainViewModel.initInternet()
            mainViewModel.stopTimer()
        }

        binding.scoreRankButton.setOnClickListener {
            event?.moveToRankFragment()
        }

        mainViewModel.gameStatus.observe(viewLifecycleOwner, Observer {
            when(it){
                "leftRight"-> binding.scoreType.text = "좌로 우로"
                "movie" -> binding.scoreType.text = "영화 맞추기"
                "operations" -> binding.scoreType.text = "곱하기 더하기"
                "card" -> binding.scoreType.text = "카드 맞추기"
            }

        })

        this.requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), callback)
    }

    override fun onDestroy() {
        super.onDestroy()

    }

}