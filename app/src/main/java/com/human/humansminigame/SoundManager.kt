package com.human.humansminigame

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

class SoundManager {

    private lateinit var soundPool : SoundPool
    private var correctSound : Int = 0
    private var wrongSound : Int = 0

    fun setSoundPool(context : Context){
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(2)
            .setAudioAttributes(audioAttributes)
            .build()

        correctSound = soundPool.load(context, R.raw.baby_yeah, 1)
        wrongSound = soundPool.load(context, R.raw.wrong_answer, 1)
    }

    fun play(answer : String){
        when(answer){
            "true" -> soundPool.play(correctSound, 1F, 1F, 0, 0, 1F)
            "false" -> soundPool.play(wrongSound, 1F, 1F, 0, 0, 1F)
        }
    }

    fun release(){
        soundPool.release()
    }


}