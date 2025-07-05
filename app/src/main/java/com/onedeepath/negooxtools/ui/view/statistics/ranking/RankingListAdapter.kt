package com.onedeepath.negooxtools.ui.view.statistics.ranking

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.onedeepath.negooxtools.domain.model.statistics.Top3
import com.onedeepath.negooxtools.R

class RankingListAdapter (private var rankingList: List<Top3>, context: Context) : RecyclerView.Adapter<RankingListAdapter.RankingListViewHolder>() {


    inner class RankingListViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val ivRankingListStar: ImageView = view.findViewById(R.id.ivRankingListStar)
        val tvRankingListProductName: TextView = view.findViewById(R.id.tvRankingListProductName)
        val tvRankingListCount: TextView = view.findViewById(R.id.tvRankingListCount)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankingListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ranking_list, parent, false)
        return RankingListViewHolder(view)
    }

    override fun getItemCount(): Int {
        return rankingList.size
    }

    override fun onBindViewHolder(holder: RankingListViewHolder, position: Int) {

        Log.i("Prueba1", "rankingListAdapter rankingItems: $rankingList")

        val rankingItems = rankingList[position]


        if (position < 3) {

            holder.ivRankingListStar.visibility = View.VISIBLE

            when(position) {

                0 -> holder.ivRankingListStar.setColorFilter(Color.YELLOW)
                1 -> holder.ivRankingListStar.setColorFilter(Color.GRAY)
                2 -> holder.ivRankingListStar.setColorFilter(Color.RED)
            }

        }else{

            holder.ivRankingListStar.visibility = View.INVISIBLE

        }

        Log.i("Prueba1", "rankingListAdapter productname:${rankingItems.productName}")
        holder.tvRankingListProductName.text = rankingItems.productName
        holder.tvRankingListCount.text = rankingItems.count.toString()

    }

    fun refreshRanking(newRanking: List<Top3>) {

        rankingList.toMutableList().clear()

        rankingList = newRanking

        Log.i("Prueba2", "newRanking: $rankingList")

        notifyDataSetChanged()


    }

}
