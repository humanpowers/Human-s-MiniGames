package com.human.humansminigame

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.human.humansminigame.databinding.FragmentLoginBinding


class LoginFragment : Fragment() {

    val TAG = "MainLoginFragmentTAG"

    private lateinit var context : Context
    lateinit var binding : FragmentLoginBinding
    private lateinit var mainViewModel : MainViewModel
    var event : MainActivityEvent? =null
    var userid :String = ""

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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        binding.mainViewModel = mainViewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
    }

    fun init(){

        mainViewModel.getNickname()

        mainViewModel.dataError.observe(viewLifecycleOwner, Observer {
            if(it){
                Toast.makeText(context,"데이터를 불러오지 못했습니다.\n잠시후에 다시 시도해주세요.",Toast.LENGTH_SHORT).show()
                mainViewModel.initDataError()
            }
        })

        binding.loginButton.setOnClickListener{
            userid = binding.loginNicknameEdit.text.toString().trim()
            if(userid==""){
                Toast.makeText(context,"닉네임을 입력해주세요.",Toast.LENGTH_SHORT).show()
            }else{
                if(mainViewModel.duplicateNickname(userid)){
                    Toast.makeText(context,"이미 존재하는 닉네임입니다.",Toast.LENGTH_SHORT).show()
                }else{
                    val sharedpreferences = context.getSharedPreferences("userinfo", Context.MODE_PRIVATE)
                    mainViewModel.saveId(sharedpreferences, userid)
                    mainViewModel.setId(userid)
                    mainViewModel.firstDataSave(userid)
                    event?.moveToMainFragment()
                    Log.d(TAG, "init: $userid")
                }
            }
        }

    }
}
