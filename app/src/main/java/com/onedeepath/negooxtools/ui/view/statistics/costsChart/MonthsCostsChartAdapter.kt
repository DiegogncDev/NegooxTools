package com.onedeepath.negooxtools.ui.view.statistics.costsChart

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.onedeepath.negooxtools.domain.model.productList.Date
import com.onedeepath.negooxtools.R

class MonthsCostsChartAdapter (var months: List<Date>,
                               private var onMonthSelected: (Date) -> Unit,
                               context: Context
) : RecyclerView.Adapter<MonthsCostsChartAdapter.MonthsCostsChartViewHolder>() {

    private var selectedMonths = mutableSetOf<String>()


    inner class MonthsCostsChartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val chMonth: Chip = itemView.findViewById(R.id.chMonth)

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthsCostsChartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_products_list_date, parent, false)
        return MonthsCostsChartViewHolder(view)

    }

    override fun getItemCount(): Int {
        return months.size
    }

    override fun onBindViewHolder(holder: MonthsCostsChartViewHolder, position: Int) {

        val month = months[position]

        holder.chMonth.text = month.name

        holder.chMonth.setOnClickListener {

            onMonthSelected(month)

        }

    }




}