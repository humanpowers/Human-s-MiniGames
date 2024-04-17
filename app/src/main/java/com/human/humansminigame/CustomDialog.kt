package com.human.humansminigame

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import kotlin.system.exitProcess

class CustomDialog {

    fun getLoadingDialog(context : Context) : Dialog {
        var loadingDialog = Dialog(context)

        loadingDialog.setContentView(R.layout.loading_dialog)
        loadingDialog.setCancelable(false)

        loadingDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        loadingDialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        loadingDialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

        return loadingDialog
    }

    fun getInternetDialog(context: Context) : Dialog{
        var internetDialog = Dialog(context)
        internetDialog.setContentView(R.layout.internet_dialog)
        internetDialog.setCancelable(false)

        internetDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        internetDialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        internetDialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

        internetDialog.findViewById<Button>(R.id.internetDialogRestartButton).setOnClickListener {
            val packageManager: PackageManager = context.packageManager
            val intent = packageManager.getLaunchIntentForPackage(context.packageName)
            val componentName = intent!!.component
            val mainIntent = Intent.makeRestartActivityTask(componentName)

            context.startActivity(mainIntent)
            exitProcess(0)
            internetDialog.dismiss()
        }

        return internetDialog
    }

    fun startDialog(context: Context) : Dialog{
        var movieDialog = Dialog(context)
        movieDialog.setContentView(R.layout.start_dialog)
        movieDialog.setCancelable(false)
        movieDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        return movieDialog
    }




}