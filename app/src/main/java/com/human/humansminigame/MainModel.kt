package com.human.humansminigame

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.human.humansminigame.movieFragment.MovieData
import com.human.humansminigame.rankFragment.RankData
import com.google.firebase.database.*


class MainModel{
    val TAG = "MainModelTAG"

    interface OnDataReadyCallback {
        fun onDataReady(data: List<MovieData>)
        fun onError(error: String)
    }

    fun fetchMovieData(callback: OnDataReadyCallback) {
        val movieList = ArrayList<MovieData>()
        val databaseReference = FirebaseDatabase.getInstance().reference.child("movie")
        FirebaseDatabase.getInstance().reference.child("movie").get().addOnSuccessListener {
            Log.d(TAG, "fetchMovieData: 성공")
            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    for (movieSnapshot in snapshot.children) {
                        val title = movieSnapshot.child("title").getValue(String::class.java) ?: ""
                        val content = movieSnapshot.child("content").getValue(String::class.java) ?: ""
                        val movieData = MovieData(title, content)
                        movieList.add(movieData)

                    }
                    callback.onDataReady(movieList)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d(TAG, "onCancelled: 에러")
                    callback.onError(error.message)
                }
            })

        }
    }

    fun saveid(sharedPreferences: SharedPreferences, id : String){
        val editor = sharedPreferences.edit()
        editor.putString("userid",id)
        editor.apply()
    }


    fun saveRank(type : String, name : String, newScore : String, callback : ((String) -> Unit)){
        val databaseReference = FirebaseDatabase.getInstance().reference.child("ranking")

        databaseReference.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                val score = snapshot.child(type).child(name).getValue(String::class.java)

                    if(Integer.parseInt(score)<Integer.parseInt(newScore)){
                        databaseReference.child(type).child(name).setValue(newScore)
                            .addOnSuccessListener {
                                Log.d(TAG, "putMovieRank: 저장 성공")
                                callback("saveSuccessful")

                            }
                            .addOnFailureListener{
                                Log.d(TAG, "putMovieRank: 저장 실패")
                                callback("saveFailed")
                            }
                    }else{
                        Log.d(TAG, "onDataChange: 원래 점수가 더 높음")
                        callback("notHigherScore")
                    }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

    }

    fun firstSaveRank(type: String, name: String) {
        val databaseReference = FirebaseDatabase.getInstance().reference.child("ranking")

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val typeReference = databaseReference.child(type)

                if (!snapshot.hasChild(type)) {
                    typeReference.setValue(name)
                        .addOnSuccessListener {
                            Log.d(TAG, "putMovieRank: 저장 성공")
                            typeReference.child(name).setValue("0")
                        }
                        .addOnFailureListener {
                            Log.d(TAG, "putMovieRank: 저장 실패")
                        }
                } else {
                    val typeData = snapshot.child(type)
                    if (!typeData.hasChild(name)) {
                        typeReference.child(name).setValue("0")
                            .addOnSuccessListener {
                                Log.d(TAG, "putMovieRank: 저장 성공")
                            }
                            .addOnFailureListener {
                                Log.d(TAG, "putMovieRank: 저장 실패")
                            }
                    } else {
                        Log.d(TAG, "이름이 이미 들어가있음")
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }



    interface OnRankDataReadyCallback {
        fun onDataReady(data: List<RankData>)
        fun onError(error: String)
    }

    fun fetchRankData(callback: OnRankDataReadyCallback) {
        val rankList = ArrayList<RankData>()

        val databaseReference = FirebaseDatabase.getInstance().reference.child("ranking")

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                for (categorySnapshot in snapshot.children) {
                    val category = categorySnapshot.key ?: ""

                    for (nameSnapshot in categorySnapshot.children) {
                        val name = nameSnapshot.key ?: ""
                        val score = nameSnapshot.getValue(String::class.java) ?: ""
                        Log.d(TAG, "onDataChange: category: $category name: $name score: $score")

                        val rankData = RankData(category, name, score)
                        rankList.add(rankData)
                    }
                }

                callback.onDataReady(rankList)
            }




            override fun onCancelled(error: DatabaseError) {
                callback.onError(error.message)
            }
        })
    }

    interface OnGetNicknameCallback {
        fun onNicknameReady(data: ArrayList<String>)
        fun onError(error: String)
    }

    fun getNickname(callback: OnGetNicknameCallback){
        val databaseReference = FirebaseDatabase.getInstance().reference.child("ranking").child("card")
        val userList = ArrayList<String>()
        databaseReference.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                for (nicknameSnapshot in snapshot.children) {
                    val userName = nicknameSnapshot.key ?: ""
                    userList.add(userName)
                }

                callback.onNicknameReady(userList)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
                callback.onError(error.message)
            }

        })
    }

}