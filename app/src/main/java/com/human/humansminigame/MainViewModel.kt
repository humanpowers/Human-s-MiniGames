package com.human.humansminigame

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.CountDownTimer
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.human.humansminigame.cardFragment.RandomCardImage
import com.human.humansminigame.leftRightFragment.LeftRightImage
import com.human.humansminigame.movieFragment.MovieData
import com.human.humansminigame.rankFragment.RankData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.random.Random
import kotlin.random.nextInt

class MainViewModel(private val application: Application) : AndroidViewModel(application) {
    val TAG = "MainViewModelTAG"
    private val mainModel = MainModel()

    private val _movies = MutableLiveData<List<MovieData>>()
    val movies : LiveData<List<MovieData>>
        get() =_movies

    private val _movieEmpty = MutableLiveData<String>().apply { value = "init" }
    val movieEmpty : LiveData<String>
        get() = _movieEmpty

    private val _rank = MutableLiveData<List<RankData>>()
    val rank: LiveData<List<RankData>> = _rank

    private var movieRandomNumber: Int = 0
    private var movieRandomBoolean: Boolean = false
    private var movieClickCount: Int = 0

    private val _gameStatus = MutableLiveData<String>().apply{value= "movie"}
    val gameStatus: LiveData<String>
        get() = _gameStatus

    private var _lifeCount = MutableLiveData<Int>()
    val lifeCount: LiveData<Int>
        get() = _lifeCount

    private var _getRank = MutableLiveData<ArrayList<RankData>>()
    val getRank: LiveData<ArrayList<RankData>>
        get() = _getRank

    private var tts: TextToSpeech? = null

    private val _dialogTimer = MutableLiveData<String>().apply{value= "3"}
    val dialogTimer: LiveData<String>
        get() = _dialogTimer

    private var timer: CountDownTimer? = null // 타이머 객체

    private val _isTimeExpired = MutableLiveData<String>().apply{value= "init"}
    val isTimeExpired: LiveData<String>
        get() = _isTimeExpired

    private val _progress = MutableLiveData<Int>().apply{value= 100}
    val progress: LiveData<Int>
        get() = _progress

    private var _leftRightImageArrayList = MutableLiveData<ArrayList<LeftRightImage>>()
    val leftRightImageArrayList: LiveData<ArrayList<LeftRightImage>>
        get() = _leftRightImageArrayList

    private val _leftImage = MutableLiveData<LeftRightImage>()

    val leftImage: LiveData<LeftRightImage>
        get() = _leftImage

    private val _rightImage = MutableLiveData<LeftRightImage>()
    val rightImage: LiveData<LeftRightImage>
        get() = _rightImage

    private val _targetImage = MutableLiveData<LeftRightImage>()
    val targetImage: LiveData<LeftRightImage>
        get() = _targetImage

    private val _score = MutableLiveData<Int>().apply{value= 0}
    val score: LiveData<Int>
        get() = _score

    private val _operationsRight = MutableLiveData<Int>()
    val operationsRight: LiveData<Int>
        get() = _operationsRight

    private val _operationsLeft = MutableLiveData<Int>()
    val operationsLeft: LiveData<Int>
        get() = _operationsLeft

    private val _operationsOperator = MutableLiveData<String>()
    val operationsOperator: LiveData<String>
        get() = _operationsOperator

    private val _operationsAnswer = MutableLiveData<String>()
    val operationsAnswer: LiveData<String>
        get() = _operationsAnswer

    private val _operationsCorrectAnswer = MutableLiveData<Int>()

    private val _internetError = MutableLiveData<String>().apply{value= "init"}
    val internetError: LiveData<String>
        get() = _internetError

    private val _dataError = MutableLiveData<Boolean>().apply { value = false }
    val dataError: LiveData<Boolean>
        get() = _dataError

    val _id = MutableLiveData<String>()

    fun setId(id: String) {
        _id.value= id
    }

    fun initDataError(){
        _dataError.value = false
    }

    fun initRank() {
        _rank.value= null
        _getRank.value= null
        _movies.value = null
    }

    private val _myScoreNum = MutableLiveData<Int>()
    val myScoreNum: LiveData<Int>
        get() = _myScoreNum

    private val _myScore = MutableLiveData<RankData>()
    val myScore: LiveData<RankData>
        get() = _myScore

    private val _myBestScore = MutableLiveData<String>()
    val myBestScore: LiveData<String>
        get() = _myBestScore

    private val _myCurrentScore = MutableLiveData<String>()
    val myCurrentScore: LiveData<String>
        get() = _myCurrentScore

    private var userNickname : ArrayList<String> = ArrayList()


    fun getNickname(){
        mainModel.getNickname(object : MainModel.OnGetNicknameCallback {
            override fun onNicknameReady(data: ArrayList<String>) {
                userNickname = data
                Log.d(TAG, "onNicknameReady: $userNickname")
            }

            override fun onError(error: String) {
                _dataError.value = true
            }
        })
    }

    fun duplicateNickname(nickname: String) : Boolean{
        return userNickname!!.contains(nickname)
    }



    fun findMyScoreNum(list: ArrayList<RankData>) {

        for (i in 0 until list.size) {

            if (list[i].name == _id.value) {
                _myScoreNum.value= i + 1
                Log.d(TAG, "findMyScore: ${_myScoreNum.value}")
                break
            }
        }
    }

    fun findMyScore(num: Int) {
        _myScore.value= _getRank.value?.get(num - 1)
    }

    fun myScoreInit() {
        _myScoreNum.value= 0
    }


    fun fetchMovieData() {
        mainModel.fetchMovieData(object : MainModel.OnDataReadyCallback {
            override fun onDataReady(data: List<MovieData>) {

                _movies.value= data
                _score.value= 0
                _lifeCount.value= 3
                Log.d(TAG, "onDataReady:" + _movies.value)
                Log.d(TAG, "onDataReady: " + _movies.value!!.get(0).title)
                Log.d(TAG, "onDataReady: " + _movies.value!!.size)
            }

            override fun onError(error: String) {
                _dataError.value = true
                Log.d(TAG, "onError: $error")
            }

        })
    }



    fun playBtnClick(): Boolean {
        //문제 출제가 안되어 있는 경우 퀴즈를 생성한다
        if (!movieRandomBoolean) {
            makeMovieQuiz()
            speakMovieContent(_movies.value!![movieRandomNumber].content)
            movieClickCount++

            return true

            // 문제 출제가 되어 있고, playButton을 2회 이하 누른 경우
        } else if (movieClickCount < 3 && movieRandomBoolean) {
            speakMovieContent(_movies.value!![movieRandomNumber].content)
            movieClickCount++
            return true
        }

        return false
    }

    private fun makeMovieQuiz(){
        movieRandomNumber = Random.nextInt(1, _movies.value!!.size) //출제 문제
        movieRandomBoolean = true // 초기 문제 출제를 위한 식별자
    }


    fun answerBtnClick(answer: String) {

        if (_lifeCount.value!! > 1&& _movies.value!!.isNotEmpty()) {

            when(answer == _movies.value!![movieRandomNumber].title){
                true -> {
                    _score.value = _score.value?.plus(10)
                    _isCorrectAnswer.value = "true"
                    removeMovieDataAtIndex(movieRandomNumber)
                    setDifficulty(0)
                    initMovieStatus()
                }

                false -> {
                    _lifeCount.value = lifeCount.value!! - 1
                    movieClickCount = 0
                    setDifficulty(3 - _lifeCount.value!!)
                    _isCorrectAnswer.value = "false"
                }
            }
        } else if(_lifeCount.value!! == 1&& _movies.value!!.isNotEmpty()) {

            when(answer == _movies.value!![movieRandomNumber].title){
                true -> {
                    _score.value = _score.value?.plus(10)
                    _isCorrectAnswer.value = "true"
                }
                false -> {
                    if(_score.value != 0){
                        _score.value = _score.value?.minus(10)
                    }
                    _isCorrectAnswer.value = "false"
                }
            }
            initMovieStatus()
            removeMovieDataAtIndex(movieRandomNumber)
            setDifficulty(0)
        }
    }

    private fun initMovieStatus() {
        _lifeCount.value = 3
        movieClickCount = 0
        movieRandomBoolean = false
    }



    fun initializeTextToSpeech(textToSpeech: TextToSpeech) {
        tts = textToSpeech
        tts?.language= Locale.US// 기기의 기본 언어로 설정할 수도 있습니다.
        tts?.setPitch(0.5f)
        tts?.setSpeechRate(2f)
    }

    private fun setDifficulty(status: Int) {
        when (status) {
            0 -> tts?.setSpeechRate(2f)
            1 -> tts?.setSpeechRate(1.5f)
            2 -> tts?.setSpeechRate(1f)
        }
    }

    private fun speakMovieContent(content: String) {
        tts?.speak(content, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun removeMovieDataAtIndex(index: Int) {
        val currentList = _movies.value.orEmpty().toMutableList() // 현재 리스트를 가변형으로 가져옵니다.

        if (index >= 0 && index < currentList.size) { // 유효한 인덱스인지 확인합니다.
            currentList.removeAt(index) // 특정 인덱스의 영화를 제거합니다.
            _movies.value= currentList // 변경된 리스트를 MutableLiveData에 할당합니다.
            if(currentList.isEmpty()){
                _movieEmpty.value = "true"
            }
        } else {
            // 인덱스가 유효하지 않을 경우 처리할 내용을 여기에 추가합니다.
        }
    }


    fun dialogTimer() {
        val initialTime = TimeUnit.SECONDS.toMillis(4)
        timer = object : CountDownTimer(initialTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                if ((millisUntilFinished / 1000).toInt() != 0) {
                    _dialogTimer.value= (millisUntilFinished / 1000).toString()
                } else {
                    _dialogTimer.value= "start"
                }
            }

            override fun onFinish() {
                _isTimeExpired.value= "startDialog"
            }
        }
        timer?.start()
    }


    fun startTimer(seconds: Long, type: String) {
        val initialTime = TimeUnit.SECONDS.toMillis(seconds)
        timer = object : CountDownTimer(initialTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {

                var progressValue = (millisUntilFinished / initialTime.toFloat() * 100).toInt()

                _progress.value= progressValue
            }

            override fun onFinish() {
                //타이머가 종료되면
                _progress.value= 0
                _isTimeExpired.value= type

                _gameStatus.value= type
                _isCorrectAnswer.value= "init"


            }
        }
        timer?.start()
    }

    fun resetBackPressToExit(){
        _progress.value= 0
        _score.value= 0
        _isCorrectAnswer.value= "init"
    }

    fun stopTimer() {
        timer?.cancel()
    }

    override fun onCleared() {
        // ViewModel이 소멸될 때 타이머도 중지
        stopTimer()
        super.onCleared()
    }

    fun saveId(sharedPreferences: SharedPreferences, id: String) {
        mainModel.saveid(sharedPreferences, id)
        Log.d(TAG, "saveId: $id")
    }

    fun movieGameInit(){
        _lifeCount.value = 3
        movieClickCount = 0
        movieRandomBoolean = false
    }

    fun getMainList(resources: Resources): ArrayList<String> {
        val stringArray = resources.getStringArray(R.array.mainList)
        var mainArrayList: ArrayList<String> = ArrayList()

        for (title in stringArray) {
            mainArrayList.add(title)
        }

        return mainArrayList

    }


    /**
     * LeftRight
     */


    fun updateLeftRightImageList(index: Int) {
        if (_leftRightImageArrayList.value!!.size == 5) {
            _leftRightImageArrayList.value= ArrayList()
        } else {
            _leftRightImageArrayList.value?.removeAt(index)
        }

        Log.d(TAG, "setLeftRightImageArrayList: ${_leftRightImageArrayList.value?.size}")
    }

    private var _cardsPair = MutableLiveData<Pair<Int, Int>>()

    fun assignRandomNumbers(resources: Resources) {
        val randomNumber1 = Random.nextInt(0, 7) // 0부터 7까지의 랜덤한 숫자
        var randomNumber2: Int

        do {
            randomNumber2 = Random.nextInt(0, 7)
        } while (randomNumber2 == randomNumber1) // randomNumber1과 중복되지 않도록 함

        val imageStrings = resources.getStringArray(R.array.left_right_images)

        val card1 = resources.getIdentifier(
            imageStrings[randomNumber1],
            "drawable",
            application.packageName
        )
        val card2 = resources.getIdentifier(
            imageStrings[randomNumber2],
            "drawable",
            application.packageName
        )

        _cardsPair.value= Pair(card1, card2)
    }

    fun getLeftRightImageList(resources: Resources): ArrayList<LeftRightImage> {
        assignRandomNumbers(resources)

        var leftRightImageList = ArrayList<LeftRightImage>()

        for (i in 0..48) {
            when (Random.nextInt(2)) {
                0 -> leftRightImageList.add(
                    LeftRightImage(
                        ContextCompat.getDrawable(
                            application.applicationContext,
                            _cardsPair.value!!.first
                        )!!,
                        0
                    )
                )

                1 -> leftRightImageList.add(
                    LeftRightImage(
                        ContextCompat.getDrawable(
                            application.applicationContext,
                            _cardsPair.value!!.second
                        )!!,
                        1
                    )
                )
            }
        }

        return leftRightImageList

    }

    fun setLeftRightImageArrayList(newList: ArrayList<LeftRightImage>) {
        _leftRightImageArrayList.value = newList
    }

    fun setInitLeftRightImage() {
        _leftImage.value = LeftRightImage(
            ContextCompat.getDrawable(
                application.applicationContext,
                _cardsPair.value!!.first
            )!!, 0
        )
        _rightImage.value = LeftRightImage(
            ContextCompat.getDrawable(
                application.applicationContext,
                _cardsPair.value!!.second
            )!!, 1
        )

        val random = Random.nextInt(2)

        when (random) {
            0 -> _targetImage.value = LeftRightImage(
                ContextCompat.getDrawable(
                    application.applicationContext,
                    _cardsPair.value!!.first
                )!!, 0
            )

            1 -> _targetImage.value = LeftRightImage(
                ContextCompat.getDrawable(
                    application.applicationContext,
                    _cardsPair.value!!.second
                )!!, 1
            )
        }
    }

    fun checkLeftRightImage(position: Int) {

        when(_targetImage.value!!.position){
            position -> {
                _score.value = _score.value!!.plus(10)
                _isCorrectAnswer.value = "true"
            }
            else -> {
                if(_score.value != 0){
                    _score.value = _score.value!!.minus(10)
                }
                _isCorrectAnswer.value = "false"
            }
        }

        setTargetLeftRightImage()
    }

    private fun setTargetLeftRightImage() {

        var arraySize = _leftRightImageArrayList.value!!.size

        _targetImage.value = leftRightImageArrayList.value!![arraySize - 1]

    }

    /**
     * Operations
     */

    fun getKeyBoardList(): ArrayList<Int> {
        var numberArrayList: ArrayList<Int> = ArrayList()
        for (i in 1..9) {
            numberArrayList.add(i)
        }
        numberArrayList.add(0)
        return numberArrayList
    }

    fun setProblem() {
        for (i in 0..1) {
            var randomNumber = Random.nextInt(0..9)
            when (i) {
                0 -> _operationsLeft.value = randomNumber
                1 -> _operationsRight.value = randomNumber
            }
        }

        var randomOperator = Random.nextInt(2)
        when (randomOperator) {
            0 -> {
                _operationsOperator.value = "+"
                _operationsCorrectAnswer.value = _operationsLeft.value!! + _operationsRight.value!!
            }

            1 -> {
                _operationsOperator.value = "x"
                _operationsCorrectAnswer.value = _operationsLeft.value!! * _operationsRight.value!!
            }
        }
    }

    fun setNumber(position: Int) {
        if (_operationsAnswer.value == null) {
            when (position) {
                9 -> _operationsAnswer.value = "0"
                else -> _operationsAnswer.value = (position + 1).toString()
            }
        } else {
            when (position) {
                9 -> _operationsAnswer.value = _operationsAnswer.value?.plus("0")
                else -> _operationsAnswer.value = _operationsAnswer.value?.plus(position + 1)
            }

        }
        checkOperation()
    }

    private fun checkOperation() {

        val lengthMatch = _operationsAnswer.value?.length == _operationsCorrectAnswer.value.toString().length

        if (lengthMatch) {
            val answerMatch = _operationsAnswer.value == _operationsCorrectAnswer.value.toString()

            when(answerMatch){
                true ->  _score.value = _score.value!!.plus(10)
                false -> {
                    if (_score.value != 0) {
                        _score.value = _score.value!!.minus(10)
                    }
                }
            }

            _isCorrectAnswer.value = (answerMatch).toString()
            setProblem()
            _operationsAnswer.value = null
        }

    }


    private var dataSaveCallback: ((String) -> Unit)? = null


    fun dataSave(type: String) {
        Log.d(TAG, "dataSave: $type")
        dataSaveCallback ={
            when (it) {
                "saveSuccessful" -> fetchRankData()
                "notHigherScore" -> fetchRankData()
            }
            _score.value = 0
        }
        mainModel.saveRank(type, _id.value!!, _score.value!!.toString(), dataSaveCallback!!)
        Log.d(TAG, "dataSave score: ${_score.value}")
        _myCurrentScore.value= _score.value.toString()
        _movieEmpty.value = "init"
    }

    fun firstDataSave(name: String) {
        Log.d(TAG, "firstDataSave: $name")
        mainModel.firstSaveRank("movie", name)
        mainModel.firstSaveRank("operations", name)
        mainModel.firstSaveRank("card", name)
        mainModel.firstSaveRank("leftRight", name)
    }



    fun fetchRankData() {

        mainModel.fetchRankData(object : MainModel.OnRankDataReadyCallback {
            override fun onDataReady(data: List<RankData>) {

                val sortData = sortRankDataDescending(data)
                _rank.value= sortData
                showRanking(_gameStatus.value.toString())
                Log.d(TAG, "onDataReady: "+_rank.value)
            }

            override fun onError(error: String) {
                _dataError.value = true
            }
        })
    }

    fun sortRankDataDescending(rankList: List<RankData>): List<RankData> {
        return rankList.sortedByDescending{it.score.toInt()}
    }

    fun showRanking(type: String) {
        if (_gameStatus.value != type) {
            _gameStatus.value = type
        }

        _getRank.value?.clear() //두번째 게임을 생각해서

        var getRankBefore = ArrayList<RankData>()

        if (_rank.value != null) {
            for (i in 0 until _rank.value!!.size) {
                if (_rank.value!![i].type == type) {
                    getRankBefore.add(rank.value!![i])
                }
            }
            _getRank.value = getRankBefore
        }

    }

    fun setInitIsTimerExpired() {
        _isTimeExpired.value = "init"
    }

    private var _randomCardList = MutableLiveData<ArrayList<RandomCardImage>>()
    val randomCardList: LiveData<ArrayList<RandomCardImage>>
        get() = _randomCardList

    private var cardChecking: Boolean = false

    fun getCardList(resources: Resources) {

        _randomCardList.value = ArrayList()
        val cardImage = resources.getStringArray(R.array.cards)

        for ((index, name) in cardImage.withIndex()) {
            if (name.isNotEmpty()) {
                var card = resources.getIdentifier(name, "drawable", application.packageName)

                _randomCardList.value?.add(RandomCardImage(card, index + 1, cardChecking))
                _randomCardList.value?.add(RandomCardImage(card, index + 9, cardChecking))
            }
        }
        _randomCardList.value?.shuffle()
    }


    private var _clickCardCount = MutableLiveData<Int>()
    val clickCardCount: LiveData<Int>
        get() = _clickCardCount

    private var _clickFirst = MutableLiveData<Int>()

    private var _clickSecond = MutableLiveData<Int>()

    fun initClickCard() {
        _clickCardCount.value = 0
        _clickFirst.value = 0
        _clickSecond.value = 0
    }

    fun initCard(){
        _answerCount.value = 0
    }

    private var _answerCount = MutableLiveData<Int>().apply{value = 0}
    val answerCount: LiveData<Int>
        get() = _answerCount

    fun checkClickCount(position: Int) {
        _randomCardList.value!![position].cardChecking = true

        when (_clickCardCount.value) {

            0 -> _clickFirst.value = position

            1 -> _clickSecond.value = position

        }
        _clickCardCount.value = _clickCardCount.value!!.plus(1)
    }

    private var _isCorrectAnswer = MutableLiveData<String>().apply{value= "init"}
    val isCorrectAnswer: LiveData<String>
        get() = _isCorrectAnswer


    fun checkCardCorrect() {

        if (_randomCardList.value!![_clickFirst.value!!].number % 8 ==
            _randomCardList.value!![_clickSecond.value!!].number % 8
        ) {
            _randomCardList.value!![_clickFirst.value!!].cardChecking = true
            _randomCardList.value!![_clickSecond.value!!].cardChecking = true

            _answerCount.value = _answerCount.value!!.plus(1)
            _score.value = _score.value!!.plus(10)
            _isCorrectAnswer.value = "true"

        } else {
            // 오답인 경우 두 카드의 상태를 다시 뒤집는
            viewModelScope.launch{
                delay(300)
                _randomCardList.value!![_clickFirst.value!!].cardChecking = false
                _randomCardList.value!![_clickSecond.value!!].cardChecking = false
            }
            _isCorrectAnswer.value = "false"

        }

    }


    fun newGame() {

        for (i in 0 until randomCardList.value!!.size) {
            randomCardList.value!![i].cardChecking = false
        }
        Log.d(TAG, "newGame: 들어옴" + randomCardList.value!!.size)
        randomCardList.value!!.shuffle()
        _answerCount.value = 0

    }

    fun internetError(){
        if(isConnectInternet() != "null"){
            Log.d(TAG, "internetError: 인터넷 연결 성공")
            _internetError.value="false"

        } else{

            Log.d(TAG, "internetError: 인터넷 연결 끊김")
            _internetError.value="true"

        }
    }

    private fun isConnectInternet(): String { // 인터넷 연결 체크 함수
        val cm: ConnectivityManager =
            application.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo: NetworkInfo? = cm.activeNetworkInfo

        return networkInfo.toString()
    }

    fun initOperation(){
        _operationsAnswer.value = null
    }

    fun myBestScore(type : String){
        Log.d(TAG, "myBestScore: ")
        if (_rank.value != null) {
            for (i in 0 until _rank.value!!.size) {
                if (_rank.value!![i].type == type&& _rank.value!![i].name==_id.value) {
                    _myBestScore.value=_rank.value!![i].score
                }
            }
        }
    }

    fun initInternet(){
        _internetError.value="init"
    }

}