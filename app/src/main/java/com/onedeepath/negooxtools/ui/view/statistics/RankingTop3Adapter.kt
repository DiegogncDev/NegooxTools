package com.onedeepath.negooxtools.ui.view.statistics

import android.content.Intent
import android.icu.text.DecimalFormat
import android.icu.text.DecimalFormatSymbols
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

import com.onedeepath.negooxtools.ui.view.statistics.ranking.RankingActivity
import com.onedeepath.negooxtools.R
import com.onedeepath.negooxtools.domain.model.statistics.rankingTop3
import com.onedeepath.negooxtools.ui.viewmodel.statistics.StatisticsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.N)
class RankingTop3Adapter(private val criteriaItems: List<rankingTop3>,
                         private val statisticsViewModel: StatisticsViewModel
) : RecyclerView.Adapter<RankingTop3Adapter.RankingPrincipalViewHolder>(){

    private val df2digits = DecimalFormat("#,###", DecimalFormatSymbols(Locale.GERMANY))

    private var showInGs: Boolean = false
    private var currentGsPrice: Float = 0.0f

    init {


        CoroutineScope(Dispatchers.IO).launch {

            getValuesvStcsViewModel()

            combine(
                statisticsViewModel.showInGsStcs,
                statisticsViewModel.currentGsPriceStcs

            ){_,_ -> }.collect{


                withContext(Dispatchers.Main) {

                    notifyDataSetChanged()

                }
            }

        }


    }

    inner class RankingPrincipalViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val cvItemRankingTop3 = view.findViewById<CardView>(R.id.cvItemRankingTop3)
        val tvRankingName = view.findViewById<TextView>(R.id.tvStatisticsRankingName)

        val tvRankingFirst = view.findViewById<TextView>(R.id.tvRankingFirst)
        val tvRankingFirstCount = view.findViewById<TextView>(R.id.tvRankingFirstCount)

        val tvRankingSecond = view.findViewById<TextView>(R.id.tvRankingSecond)
        val tvRankingSecondCount = view.findViewById<TextView>(R.id.tvRankingSecondCount)

        val tvRankingThirst = view.findViewById<TextView>(R.id.tvRankingThirst)
        val tvRankingThirstCount = view.findViewById<TextView>(R.id.tvRankingThirstCount)



    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankingPrincipalViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_statistics_ranking, parent, false)
        return RankingPrincipalViewHolder(view)
    }

    override fun getItemCount(): Int {
        return criteriaItems.size
    }

    override fun onBindViewHolder(holder: RankingPrincipalViewHolder, position: Int) {

        val criteria = criteriaItems[position]

        holder.tvRankingName.text = criteria.rankingName

        if (criteria.rankingProduct.isNotEmpty()) {
            holder.tvRankingFirst.text = criteria.rankingProduct[0].productName
            holder.tvRankingFirstCount.text = formatValue(criteria.rankingName ,criteria.rankingProduct[0].count)

            if (criteria.rankingProduct.size > 1) {
                holder.tvRankingSecond.text = criteria.rankingProduct[1].productName
                holder.tvRankingSecondCount.text = formatValue(criteria.rankingName, criteria.rankingProduct[1].count)
            } else {
                holder.tvRankingSecond.text = "N/A"
                holder.tvRankingSecondCount.text = "N/A"
            }

            if (criteria.rankingProduct.size > 2) {
                holder.tvRankingThirst.text = criteria.rankingProduct[2].productName
                holder.tvRankingThirstCount.text = formatValue(criteria.rankingName, criteria.rankingProduct[2].count)
            } else {
                holder.tvRankingThirst.text = "N/A"
                holder.tvRankingThirstCount.text = "N/A"
            }
        } else {
            holder.tvRankingFirst.text = "N/A"
            holder.tvRankingFirstCount.text = "N/A"
            holder.tvRankingSecond.text = "N/A"
            holder.tvRankingSecondCount.text = "N/A"
            holder.tvRankingThirst.text = "N/A"
            holder.tvRankingThirstCount.text = "N/A"
        }

        holder.cvItemRankingTop3.setOnClickListener {

            val intent = Intent(holder.itemView.context, RankingActivity::class.java)

            holder.itemView.context.startActivity(intent)


        }

    }

    private suspend fun getValuesvStcsViewModel(){

        CoroutineScope(Dispatchers.IO).launch {

           showInGs = statisticsViewModel.showInGsStcs.value

        }



    }


    private fun formatValue(rankingName: String, value: Double): String {

        CoroutineScope(Dispatchers.IO).launch {

            showInGs = statisticsViewModel.showInGsStcs.value
            currentGsPrice = statisticsViewModel.currentGsPriceStcs.value

        }

        var currentPriceGs = statisticsViewModel.currentGsPriceStcs.value

        Log.i("terere", "showingsTop3:$showInGs currentPriceGs:$currentPriceGs")

        return when(rankingName.lowercase()) {

            "most profit ranking", "most expensive ranking" -> {

                if (showInGs) "${df2digits.format(value * currentPriceGs)}Gs" else "${df2digits.format(value)}$"

            }
            "most faster ranking" -> "${value.toInt()} days"
            "most high weight" -> {

                if (value < 1.0) "${value}g" else "${value}kg"
            }
            "most solds ranking" -> "${value.toInt()}"

            else -> value.toString()

        }




    }


}