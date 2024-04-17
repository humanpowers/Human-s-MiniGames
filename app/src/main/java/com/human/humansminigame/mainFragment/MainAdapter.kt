package com.human.humansminigame.mainFragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.human.humansminigame.R
import com.human.humansminigame.databinding.MainFragmentItemBinding

class MainAdapter(val clickListener: MainClickListener) : RecyclerView.Adapter<MainAdapter.MainViewHolder>() {
    val TAG = "MainAdapterTAG"
    private var mainList = emptyList<String>()

    class MainViewHolder(val binding : MainFragmentItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        val binding = MainFragmentItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MainViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        holder.binding.item = mainList[position]


        if (mainList[position] == "?") {
            holder.binding.mainFragmentItem.setBackgroundResource(R.drawable.main_item_gray)
        } else {
            if (position % 2 == 0) {
                holder.binding.mainFragmentItem.setBackgroundResource(R.drawable.main_item_green)
            } else {
                holder.binding.mainFragmentItem.setBackgroundResource(R.drawable.main_item_yellow)
            }
        }


        holder.binding.mainFragmentItem.setOnClickListener {
            clickListener.onItemClick(position)
        }


    }

    override fun getItemCount(): Int {
        return mainList.size
    }

    fun setMainList(mainList : ArrayList<String>){
        this.mainList = mainList
        notifyDataSetChanged()
    }


}