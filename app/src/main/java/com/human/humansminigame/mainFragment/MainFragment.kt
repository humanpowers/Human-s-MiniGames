package com.human.humansminigame.mainFragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.human.humansminigame.MainActivityEvent
import com.human.humansminigame.MainViewModel
import com.human.humansminigame.R
import com.human.humansminigame.databinding.FragmentMainBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainFragment : Fragment() {
    val TAG = "MainFragmentTAG"
    private lateinit var context : Context
    lateinit var binding : FragmentMainBinding
    private lateinit var mainViewModel : MainViewModel
    var event : MainActivityEvent? =null
    private lateinit var mainAdapter: MainAdapter

    private var doubleBackToExitPressedOnce = false

    private val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (doubleBackToExitPressedOnce) {
                requireActivity().finish()
                return
            }
            doubleBackToExitPressedOnce = true
            Toast.makeText(context, "한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show()

            lifecycleScope.launch {
                delay(3000) // 3초 대기
                doubleBackToExitPressedOnce = false
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.context = context
        if(context is MainActivityEvent){
            event = context
        }else{
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false)
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        binding.viewModel = mainViewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
    }

    fun init(){
        mainAdapter = MainAdapter(object : MainClickListener {
            override fun onItemClick(position: Int) {
                when(position){
                    0 -> event?.moveToMovieFragment()
                    1 -> event?.moveToCardFragment()
                    2 -> event?.moveToLeftRightFragment()
                    3 -> event?.moveToOperationsFragment()
                    4 -> Toast.makeText(context, "출시 준비중인 게임입니다.", Toast.LENGTH_SHORT).show()
                    5 -> Toast.makeText(context, "출시 준비중인 게임입니다.", Toast.LENGTH_SHORT).show()
                }
            }
        })
        binding.mainFragmentRecyclerview.adapter = mainAdapter
        binding.mainFragmentRecyclerview.layoutManager = GridLayoutManager(context, 2)

        binding.mainFragmentRank.setOnClickListener {
            event?.moveToRankFragment()
        }

        val mainList = mainViewModel.getMainList(resources)

        if(mainList != null){
            mainAdapter.setMainList(mainList)
        }

        this.requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), callback)

        mainViewModel.setInitIsTimerExpired()
    }


}