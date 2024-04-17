package com.human.humansminigame

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.human.humansminigame.databinding.FragmentIntroBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class IntroFragment : Fragment() {

    val TAG = "IntroFragment"
    private lateinit var context: Context
    lateinit var binding: FragmentIntroBinding
    private lateinit var mainViewModel: MainViewModel
    var event: MainActivityEvent? = null
    private lateinit var sharedPreferences: SharedPreferences

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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_intro, container, false)
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
        var animation = AnimationUtils.loadAnimation(context, R.anim.intro)

        binding.introPlayImage.startAnimation(animation)

        lifecycleScope.launch {
            delay(1000)
            checkIdExist()
        }
    }

    fun checkIdExist() {
        sharedPreferences = context.getSharedPreferences("userinfo", AppCompatActivity.MODE_PRIVATE)
        val id: String? = sharedPreferences.getString("userid", "")

        if (id == null || id?.isEmpty() == true) {
            event?.moveToLoginFragment()
        } else {
            mainViewModel.setId(id)
            event?.moveToMainFragment()
        }

    }
}

