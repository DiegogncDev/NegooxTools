package com.onedeepath.negooxtools.ui.view.statistics.ranking

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.onedeepath.negooxtools.domain.model.productList.Date
import com.google.android.material.chip.Chip
import com.onedeepath.negooxtools.R


class MonthsRankingAdapter(private var months: List<Date>,
                           private var onMonthSelected: (Date) -> Unit,
                           context: Context
) : RecyclerView.Adapter<MonthsRankingAdapter.MonthsRankingViewHolder>() {

    private var selectedMonths = mutableSetOf<String>()


    inner class MonthsRankingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val chMonth: Chip = itemView.findViewById(R.id.chMonth)

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthsRankingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_products_list_date, parent, false)
        return MonthsRankingViewHolder(view)

    }

    override fun getItemCount(): Int {
        return months.size
    }

    override fun onBindViewHolder(holder: MonthsRankingViewHolder, position: Int) {

        val month = months[position]

        holder.chMonth.text = month.name

        holder.chMonth.setOnClickListener {

            onMonthSelected(month)

        }

    }




}
