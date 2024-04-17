package com.human.humansminigame.movieFragment

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.human.humansminigame.AnimationManager
import com.human.humansminigame.CustomDialog
import com.human.humansminigame.MainActivityEvent
import com.human.humansminigame.MainViewModel
import com.human.humansminigame.R
import com.human.humansminigame.SoundManager
import com.human.humansminigame.databinding.FragmentMovieBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MovieFragment : Fragment() {
    val TAG = "MovieFragmentTAG"
    private lateinit var context : Context
    lateinit var binding : FragmentMovieBinding
    private lateinit var mainViewModel : MainViewModel
    private lateinit var textToSpeech: TextToSpeech

    var event : MainActivityEvent? =null

    private lateinit var sound : SoundManager
    private lateinit var animation : AnimationManager
    private lateinit var dialog : CustomDialog

    var internetBoolean:Boolean = false

    private var doubleBackToExitPressedOnce = false

    private val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (doubleBackToExitPressedOnce) {
                mainViewModel.initRank()
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_movie, container, false)
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
        val loadingDialog = dialog.getLoadingDialog(context)

        val internetDialog = dialog.getInternetDialog(context)

        internetDialog.findViewById<Button>(R.id.internetDialogReconnectButton).setOnClickListener {
            internetDialog.dismiss()
            mainViewModel.internetError()
        }

        val startDialog = dialog.startDialog(context)

        mainViewModel.internetError()


        mainViewModel.internetError.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "internetError: $it")
            when(it){
                "true" -> {
                    internetDialog.show()
                    internetBoolean=false
                }

                "false" -> {
                    mainViewModel.fetchMovieData()//Model에서 movieData를 정제하는 과정
                    startDialog.show()
                    mainViewModel.dialogTimer()
                    internetBoolean=true
                }
            }
        })


        textToSpeech = TextToSpeech(context) { status -> // TTS초기화 & 초기값 설정
            if (status != TextToSpeech.ERROR) {
                mainViewModel.initializeTextToSpeech(textToSpeech)
            }
        }

        binding.movieStartButton.setOnClickListener{ //실행 버튼 클릭시 TTS실행

            if(!mainViewModel.playBtnClick()){
                Toast.makeText(context,
                    "더 이상 들을수 없습니다.\n정답을 입력해주세요.",
                    Toast.LENGTH_SHORT).show()
            }
        }

        binding.answerButton.setOnClickListener { // 정답 버튼 클릭시 점수와 life 조절

            var answerText = binding.movieAnswer.text.toString()

            if(answerText != null){
                mainViewModel.answerBtnClick(answerText)
            }else{
                Toast.makeText(context, "정답을 입력해주세요", Toast.LENGTH_SHORT).show()
            }

            binding.movieAnswer.setText("")
        }

        mainViewModel.lifeCount.observe(viewLifecycleOwner) {

            when(it){
                3 -> {
                    binding.movieSecondLife.setImageResource(R.drawable.life_yes)
                    binding.movieFirstLife.setImageResource(R.drawable.life_yes)

                    binding.movieFirstLife.setColorFilter(
                        ContextCompat.getColor(context, R.color.human)
                    )

                    binding.movieSecondLife.setColorFilter(
                        ContextCompat.getColor(context, R.color.human)
                    )
                }

                2 -> {
                    binding.movieSecondLife.setImageResource(R.drawable.life_yes)
                    binding.movieFirstLife.setImageResource(R.drawable.life_no)

                    binding.movieFirstLife.setColorFilter(Color.BLACK)
                }

                1 -> {
                    binding.movieSecondLife.setImageResource(R.drawable.life_no)
                    binding.movieFirstLife.setImageResource(R.drawable.life_no)

                    binding.movieFirstLife.setColorFilter(Color.BLACK)
                    binding.movieSecondLife.setColorFilter(Color.BLACK)
                }
            }

        }



        mainViewModel.isTimeExpired.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "isTimeExpired: $it")
            when(it){
                "startDialog" -> {
                    startDialog.dismiss()
                    mainViewModel.startTimer(179,"movie")
                }

                "movie" -> {
                    mainViewModel.dataSave("movie")
                    sound.release()
                    loadingDialog.show()
                }
            }

        })

        mainViewModel.rank.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "init: rank값: "+it)
            if(it!=null){
                loadingDialog.dismiss()

                event?.moveToScoreFragment()
                mainViewModel.myBestScore("movie")
                mainViewModel.initInternet()
                mainViewModel.setInitIsTimerExpired()
            }
        })

        mainViewModel.movieEmpty.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "init: asdfasdfsdafsda $it")
            if(it == "true"){
                Toast.makeText(context,"준비된 모든 문제가 끝났습니다.ㅠ.ㅠ",Toast.LENGTH_SHORT).show()
                mainViewModel.dataSave("movie")
                sound.release()
                loadingDialog.show()
            }
        })

        mainViewModel.dialogTimer.observe(viewLifecycleOwner, Observer {
            if(internetBoolean){
                startDialog.findViewById<TextView>(R.id.dialogTimer).text = it.toString()
            }
        })

        mainViewModel.isCorrectAnswer.observe(viewLifecycleOwner, Observer {
            sound.play(it)

            when(it){
                "true" ->  animation.correctAnim(binding.movieView, binding.movieStartButton, context)
                "false" -> animation.wrongAnim(binding.movieView, binding.movieStartButton, context)
            }
        })

        this.requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), callback)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        // TextToSpeech 해제
        textToSpeech.stop()
        textToSpeech.shutdown()
    }


    override fun onDestroy() {
        super.onDestroy()
        sound.release()
        mainViewModel.movieGameInit()
    }
}