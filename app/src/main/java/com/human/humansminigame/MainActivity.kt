package com.human.humansminigame

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.human.humansminigame.cardFragment.CardFragment
import com.human.humansminigame.databinding.ActivityMainBinding
import com.human.humansminigame.operationsFragment.OperationsFragment
import com.human.humansminigame.leftRightFragment.LeftRightFragment
import com.human.humansminigame.mainFragment.MainFragment
import com.human.humansminigame.movieFragment.MovieFragment
import com.human.humansminigame.rankFragment.RankFragment

class MainActivity : AppCompatActivity(), MainActivityEvent {

    val TAG = "MainActivityTAG"

    lateinit var mainViewModel: MainViewModel
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
    }

    fun init() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        binding.mainViewModel = mainViewModel
        binding.lifecycleOwner = this

        supportFragmentManager.beginTransaction().apply {
            replace(R.id.main_frame_layout, IntroFragment())
            commit()
        }

        mainViewModel.dataError.observe(this){
            if(it.equals("true")){
                supportFragmentManager.beginTransaction().apply {
                    replace(R.id.main_frame_layout, MainFragment())
                    commit()
                }
            }
        }

    }

    override fun moveToLoginFragment() {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.main_frame_layout, LoginFragment()) //메인 프래그먼트
            commitAllowingStateLoss()
        }
    }

    override fun moveToMainFragment() {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.main_frame_layout, MainFragment()) //메인 프래그먼트
            commit()
        }
    }

    override fun moveToMovieFragment() {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.main_frame_layout, MovieFragment())
            commit()
        }
    }

    override fun moveToLeftRightFragment() {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.main_frame_layout, LeftRightFragment())
            commit()
        }
    }

    override fun moveToCardFragment() {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.main_frame_layout, CardFragment())
            commit()
        }
    }

    override fun moveToRankFragment() {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.main_frame_layout, RankFragment())
            commit()
        }
    }

    override fun moveToOperationsFragment() {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.main_frame_layout, OperationsFragment())
            commit()
        }
    }

    override fun moveToScoreFragment() {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.main_frame_layout, ScoreFragment())
            commit()
        }
    }

}