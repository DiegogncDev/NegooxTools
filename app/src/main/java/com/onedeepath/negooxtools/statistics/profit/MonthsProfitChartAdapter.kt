package com.onedeepath.negooxtools.statistics.profit

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.onedeepath.negooxtools.R
import com.onedeepath.negooxtools.productsList.Date


class MonthsProfitChartAdapter (var months: List<Date>,
                                private var onMonthSelected: (Date) -> Unit,
                                context: Context
) : RecyclerView.Adapter<MonthsProfitChartAdapter.MonthsProfitChartViewHolder>() {

    private var selectedMonths = mutableSetOf<String>()


    inner class MonthsProfitChartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val chMonth: Chip = itemView.findViewById(R.id.chMonth)

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthsProfitChartViewHolder{
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_products_list_date, parent, false)
        return MonthsProfitChartViewHolder(view)

    }

    override fun getItemCount(): Int {
        return months.size
    }

    override fun onBindViewHolder(holder: MonthsProfitChartViewHolder, position: Int) {

        val month = months[position]

        holder.chMonth.text = month.name

        holder.chMonth.setOnClickListener {

            onMonthSelected(month)

        }

    }




}