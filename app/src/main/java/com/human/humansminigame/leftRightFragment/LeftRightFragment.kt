package com.human.humansminigame.leftRightFragment

import android.content.Context
import android.os.Bundle
import android.util.Log
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.human.humansminigame.AnimationManager
import com.human.humansminigame.CustomDialog
import com.human.humansminigame.MainActivityEvent
import com.human.humansminigame.MainViewModel
import com.human.humansminigame.R
import com.human.humansminigame.SoundManager
import com.human.humansminigame.databinding.FragmentLeftRightBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class LeftRightFragment : Fragment() {
    val TAG = "LeftRightFragmentTAG"
    lateinit var binding : FragmentLeftRightBinding
    private lateinit var context : Context
    private lateinit var mainViewModel : MainViewModel
    var event : MainActivityEvent? =null
    private lateinit var leftRightAdapter: LeftRightAdapter

    lateinit var dialog : CustomDialog
    lateinit var sound : SoundManager
    lateinit var animation : AnimationManager

    private var doubleBackToExitPressedOnce = false

    private val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (doubleBackToExitPressedOnce) {
                mainViewModel.stopTimer()
                mainViewModel.resetBackPressToExit()
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_left_right, container, false)
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


        leftRightAdapter = LeftRightAdapter()
        binding.leftRightRecyclerview.adapter = leftRightAdapter

        binding.leftRightRecyclerview.layoutManager = object : LinearLayoutManager(context,VERTICAL, false) {
            override fun canScrollVertically(): Boolean {
                return false // 세로 스크롤 차단
            }

            override fun canScrollHorizontally(): Boolean {
                return false // 가로 스크롤 차단
            }
        }

        var imageList = mainViewModel.getLeftRightImageList(resources)
        mainViewModel.setLeftRightImageArrayList(imageList)


        mainViewModel.leftRightImageArrayList.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "leftRightImageArrayList: ${it.size}")

            when(it.size){
                49 -> {
                    leftRightAdapter.setImageList(it)
                    binding.leftRightRecyclerview.scrollToPosition(mainViewModel.leftRightImageArrayList.value!!.size -1)
                    mainViewModel.setInitLeftRightImage()
                }

                0 -> {
                    mainViewModel.setLeftRightImageArrayList(imageList)
                }
            }
        })

        mainViewModel.isTimeExpired.observe(viewLifecycleOwner, Observer {

            when(it){

                "leftRight" ->{
                    mainViewModel.internetError()
                    sound.release()
                }
                "startDialog" -> {
                    startDialog.dismiss()
                    mainViewModel.startTimer(19, "leftRight")
                }
            }

        })

        mainViewModel.internetError.observe(viewLifecycleOwner, Observer {
            when(it){
                "true" -> internetDialog.show()

                "false" -> {
                    mainViewModel.initInternet()
                    mainViewModel.dataSave("leftRight")
                    loadingDialog.show()
                }
            }
        })

        mainViewModel.rank.observe(viewLifecycleOwner, Observer {
            if(it!=null){
                loadingDialog.dismiss()
                event?.moveToScoreFragment()
                mainViewModel.myBestScore("leftRight")
            }
        })

        mainViewModel.isCorrectAnswer.observe(viewLifecycleOwner, Observer {

            sound.play(it)
            animation.singleViewAnim(binding.leftRightBackground, it, context)

        })

        mainViewModel.dialogTimer.observe(viewLifecycleOwner, Observer {
            startDialog.findViewById<TextView>(R.id.dialogTimer).text = it.toString()
        })


        //좌측을 눌렀을 떄
        binding.leftRightLeft.setOnClickListener{
            mainViewModel.checkLeftRightImage(0)
            var removeIndex = mainViewModel.leftRightImageArrayList.value!!.size -1

            mainViewModel.updateLeftRightImageList(removeIndex)
            leftRightAdapter.setImageList(mainViewModel.leftRightImageArrayList.value!!)

            if(mainViewModel.leftRightImageArrayList.value!!.size == 20){
                imageList = mainViewModel.getLeftRightImageList(resources)
            }
            Log.d(TAG, "array change: ${mainViewModel.leftRightImageArrayList.value!!.size}")

        }

        //우측을 눌렀을 때
        binding.leftRightRight.setOnClickListener{
            mainViewModel.checkLeftRightImage(1)
            var removeIndex = mainViewModel.leftRightImageArrayList.value!!.size -1

            mainViewModel.updateLeftRightImageList(removeIndex)
            leftRightAdapter.setImageList(mainViewModel.leftRightImageArrayList.value!!)

            if(mainViewModel.leftRightImageArrayList.value!!.size == 20){
                imageList = mainViewModel.getLeftRightImageList(resources)
            }
            Log.d(TAG, "array change: ${mainViewModel.leftRightImageArrayList.value!!.size}")
        }

        this.requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), callback)
    }



}