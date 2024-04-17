package com.human.humansminigame.operationsFragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.human.humansminigame.databinding.OperationsItemBinding
import com.human.humansminigame.mainFragment.MainClickListener

class OperationsAdapter(val clickListener: MainClickListener)
    : RecyclerView.Adapter<OperationsAdapter.OperationsViewHolder>(){

    private var numberList = emptyList<Int>()

    class OperationsViewHolder(val binding : OperationsItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): OperationsViewHolder {
        val binding = OperationsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OperationsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OperationsViewHolder, position: Int) {
        holder.binding.item = numberList[position]

        holder.binding.operationsItem.setOnClickListener {
            clickListener.onItemClick(position)
        }
    }

    override fun getItemCount(): Int {
        return numberList.size
    }

    fun setNumberList(numberList : ArrayList<Int>){
        this.numberList = numberList
        notifyDataSetChanged()
    }
}