package com.human.humansminigame.operationsFragment


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.human.humansminigame.AnimationManager
import com.human.humansminigame.CustomDialog
import com.human.humansminigame.MainActivityEvent
import com.human.humansminigame.MainViewModel
import com.human.humansminigame.R
import com.human.humansminigame.SoundManager
import com.human.humansminigame.databinding.FragmentOperationsBinding
import com.human.humansminigame.mainFragment.MainClickListener
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class OperationsFragment : Fragment() {

    val TAG = "MainFragmentTAG"
    private lateinit var context: Context
    lateinit var binding: FragmentOperationsBinding
    private lateinit var mainViewModel: MainViewModel
    var event: MainActivityEvent? = null
    private lateinit var operationsAdapter: OperationsAdapter

    private lateinit var sound : SoundManager
    private lateinit var animation : AnimationManager
    private lateinit var dialog : CustomDialog

    private var doubleBackToExitPressedOnce = false

    private val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (doubleBackToExitPressedOnce) {
                mainViewModel.stopTimer()
                mainViewModel.resetBackPressToExit()
                mainViewModel.initOperation()
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_operations, container, false)
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
        sound = SoundManager()
        sound.setSoundPool(context)

        animation = AnimationManager()

        dialog = CustomDialog()
        val startDialog = dialog.startDialog(context)
        if(startDialog != null){
            startDialog.show()
            mainViewModel.dialogTimer()
        }


        val internetDialog = dialog.getInternetDialog(context)
        internetDialog.findViewById<Button>(R.id.internetDialogReconnectButton).setOnClickListener {
            internetDialog.dismiss()
            mainViewModel.internetError()
        }
        val loadingDialog = dialog.getLoadingDialog(context)

        mainViewModel.dataError.observe(viewLifecycleOwner, Observer {
            if(it){
                Toast.makeText(context,"데이터를 불러오지 못했습니다.\n잠시후에 다시 시도해주세요.",Toast.LENGTH_SHORT).show()
                mainViewModel.initDataError()
            }
        })

        operationsAdapter = OperationsAdapter(object : MainClickListener {
            override fun onItemClick(position: Int) {
                mainViewModel.setNumber(position)
            }
        })

        binding.operationsRecyclerview.adapter = operationsAdapter
        binding.operationsRecyclerview.layoutManager = object : GridLayoutManager(context, 5) {
            override fun canScrollVertically(): Boolean {
                return false // 세로 스크롤 차단
            }

            override fun canScrollHorizontally(): Boolean {
                return false // 가로 스크롤 차단
            }
        }

        var keyBordArrayList = mainViewModel.getKeyBoardList()


        if (keyBordArrayList != null) {
            operationsAdapter.setNumberList(keyBordArrayList)
            mainViewModel.setProblem()
        }

        mainViewModel.isTimeExpired.observe(viewLifecycleOwner, Observer {

            when(it){
                "operations" -> {
                    sound.release()
                    mainViewModel.internetError()
                }

                "startDialog" -> {
                    startDialog.dismiss()
                    mainViewModel.startTimer(14, "operations")
                }
            }
        })

        mainViewModel.internetError.observe(viewLifecycleOwner, Observer {

            when(it){
                "true" -> internetDialog.show()

                "false" -> {
                    mainViewModel.initInternet()
                    mainViewModel.dataSave("operations")
                    loadingDialog.show()
                }
            }

        })

        mainViewModel.rank.observe(viewLifecycleOwner, Observer {
            if(it!=null){
                loadingDialog.dismiss()
                event?.moveToScoreFragment()
                mainViewModel.initOperation()
                mainViewModel.myBestScore("operations")
            }
        })

        mainViewModel.isCorrectAnswer.observe(viewLifecycleOwner, Observer {

            sound.play(it)
            animation.singleViewAnim(binding.operationsBackground, it, context)

        })

        mainViewModel.dialogTimer.observe(viewLifecycleOwner, Observer {
            startDialog.findViewById<TextView>(R.id.dialogTimer).text = it.toString()
        })

        this.requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), callback)
    }


}