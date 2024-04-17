package com.human.humansminigame.cardFragment

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
import androidx.recyclerview.widget.GridLayoutManager
import com.human.humansminigame.AnimationManager
import com.human.humansminigame.CustomDialog
import com.human.humansminigame.MainActivityEvent
import com.human.humansminigame.MainViewModel
import com.human.humansminigame.R
import com.human.humansminigame.SoundManager
import com.human.humansminigame.databinding.FragmentCardBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class CardFragment : Fragment() {

    val TAG = "RandomcardFragmentTAG"

    private lateinit var context : Context
    lateinit var binding : FragmentCardBinding
    private lateinit var mainViewModel : MainViewModel
    var event : MainActivityEvent? =null
    private lateinit var randomCardAdapter : RandomCardAdapter

    private lateinit var sound : SoundManager
    private lateinit var animation : AnimationManager
    private lateinit var dialog : CustomDialog

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
                context.toString()+"must implement FragmentEvent"
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_card, container, false)
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        binding.randomcardviewModel = mainViewModel
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
    }

    fun init(){

        mainViewModel.dataError.observe(viewLifecycleOwner, Observer {
            if(it){
                Toast.makeText(context,"데이터를 불러오지 못했습니다.\n잠시후에 다시 시도해주세요.",Toast.LENGTH_SHORT).show()
                mainViewModel.initDataError()
            }
        })

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

        mainViewModel.getCardList(resources)
        mainViewModel.initClickCard()

        mainViewModel.clickCardCount.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "clickCardCount: $it")
            if(it == 2){
                Log.d(TAG, "2")
                mainViewModel.checkCardCorrect()
                lifecycleScope.launch {
                    delay(300)
                    mainViewModel.initClickCard()
                    randomCardAdapter.setCardList(mainViewModel.randomCardList.value!!)
                }
            }
        })

        mainViewModel.isCorrectAnswer.observe(viewLifecycleOwner, Observer {
            if(it != null){
                sound.play(it)
                animation.singleViewAnim(binding.randomCardView, it, context)
            }

        })

        mainViewModel.answerCount.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "answerCount: $it")
            if(it == 8){
                mainViewModel.newGame()
                randomCardAdapter.notifyDataSetChanged()
            }
        })


        randomCardAdapter = RandomCardAdapter(object : CardClickListener {
            override fun onItemClick(position: Int) {
                if(mainViewModel.clickCardCount.value != 2){
                    mainViewModel.checkClickCount(position)
                    randomCardAdapter.setCardList(mainViewModel.randomCardList.value!!)
                }

            }

        })
        binding.cardRecyclerview.adapter = randomCardAdapter
        binding.cardRecyclerview.layoutManager = GridLayoutManager(context, 4)



        mainViewModel.randomCardList.observe(viewLifecycleOwner, Observer {
            if(it.size == 16){
                randomCardAdapter.setCardList(mainViewModel.randomCardList.value!!)
            }
        })

        mainViewModel.isTimeExpired.observe(viewLifecycleOwner, Observer {
            if(it.equals("startDialog")){
                startDialog.dismiss()
                mainViewModel.startTimer(59,"card")

            }else if(it.equals("card")){
                mainViewModel.internetError()
                sound.release()
            }
        })

        mainViewModel.internetError.observe(viewLifecycleOwner, Observer {
            if(it.equals("true")){
                internetDialog.show()
            }else if(it.equals("false")){
                mainViewModel.initInternet()
                mainViewModel.dataSave("card")
                loadingDialog.show()
            }
        })

        mainViewModel.rank.observe(viewLifecycleOwner, Observer {
            if(it!=null){
                loadingDialog.dismiss()
                event?.moveToScoreFragment()
                mainViewModel.myBestScore("card")
                mainViewModel.initCard()
            }
        })

        mainViewModel.dialogTimer.observe(viewLifecycleOwner, Observer {
            startDialog.findViewById<TextView>(R.id.dialogTimer).text = it.toString()
        })

        this.requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), callback)

    }


    override fun onDestroy() {
        super.onDestroy()
        sound.release()
    }



}