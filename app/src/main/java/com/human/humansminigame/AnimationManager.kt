package com.human.humansminigame

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.content.ContextCompat

class AnimationManager {

    fun correctAnim(backgroundView : View, buttonView : View,  context : Context){

        animateItemBackgroundColor(backgroundView,
            ContextCompat.getColor(context, R.color.light_green),
            ContextCompat.getColor(context, R.color.light_gray)
        )
        animateItemBackgroundColor(buttonView,
            ContextCompat.getColor(context, R.color.light_green),
            ContextCompat.getColor(context, R.color.light_gray)
        )
    }

    fun wrongAnim(backgroundView : View, buttonView : View, context : Context){

        animateItemBackgroundColor(backgroundView,
            ContextCompat.getColor(context, R.color.warning),
            ContextCompat.getColor(context, R.color.light_gray)
        )
        animateItemBackgroundColor(buttonView,
            ContextCompat.getColor(context, R.color.warning),
            ContextCompat.getColor(context, R.color.light_gray)
        )

    }

    fun singleViewAnim(backgroundView: View, answer : String, context : Context){
        when(answer){
            "true" -> {
                animateItemBackgroundColor(backgroundView,
                    ContextCompat.getColor(context, R.color.light_green),
                    ContextCompat.getColor(context, R.color.light_gray))
            }

            "false" -> animateItemBackgroundColor(backgroundView,
                ContextCompat.getColor(context, R.color.warning),
                ContextCompat.getColor(context, R.color.light_gray)
            )
        }
    }

    private fun animateItemBackgroundColor(view: View, startColor: Int, endColor: Int) {
        val colorAnimation = ValueAnimator.ofObject(
            ArgbEvaluator(),
            startColor,
            endColor
        )
        colorAnimation.duration = 1000 // 1초 동안 애니메이션 실행
        colorAnimation.interpolator = DecelerateInterpolator()
        colorAnimation.addUpdateListener { animator -> view.setBackgroundColor(animator.animatedValue as Int) }
        colorAnimation.start()
    }
}