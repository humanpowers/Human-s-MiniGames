package com.human.humansminigame.leftRightFragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.human.humansminigame.databinding.LeftRightItemBinding

class LeftRightAdapter : RecyclerView.Adapter<LeftRightAdapter.LeftRightViewHolder>() {

    private var imageList = ArrayList<LeftRightImage>()

    class LeftRightViewHolder(val binding : LeftRightItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): LeftRightViewHolder {
        val binding = LeftRightItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LeftRightViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    override fun onBindViewHolder(holder: LeftRightViewHolder, position: Int) {
        holder.binding.item = imageList[position]

    }

    fun setImageList(imageList : ArrayList<LeftRightImage>){
        this.imageList = imageList
        notifyDataSetChanged()
    }
}