package com.human.humansminigame.cardFragment

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.human.humansminigame.R
import com.human.humansminigame.databinding.CardItemBinding

class RandomCardAdapter (private val cardClickListener: CardClickListener): RecyclerView.Adapter<RandomCardAdapter.CardViewHolder>(){
    class CardViewHolder(val binding : CardItemBinding) : RecyclerView.ViewHolder(binding.root)
    var randomcardList = ArrayList<RandomCardImage>()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val binding = CardItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CardViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return randomcardList.size
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val TAG = "CardAdapterTAG"

        holder.binding.item = randomcardList[position]
        Log.d(TAG, "onBindViewHolder: ${randomcardList[position].number} "+ randomcardList[position].image)

        if(randomcardList[position].cardChecking){
            holder.binding.gridViewImageButton.setImageResource(randomcardList[position].image)
            Log.d(TAG, "onBindViewHolder true: ")
            holder.binding.gridViewImageButton.isEnabled= false
        }else {
            holder.binding.gridViewImageButton.setImageResource(R.drawable.backcard)
            Log.d(TAG, "onBindViewHolder false: ")
            holder.binding.gridViewImageButton.isEnabled= true
        }

        holder.binding.gridViewImageButton.setOnClickListener(){
            cardClickListener.onItemClick(position)
        }

    }


    fun setCardList(cardList: ArrayList<RandomCardImage>) {
        this.randomcardList = cardList
        notifyDataSetChanged()
    }
}