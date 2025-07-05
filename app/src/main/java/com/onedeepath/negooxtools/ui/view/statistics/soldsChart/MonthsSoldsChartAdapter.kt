package com.onedeepath.negooxtools.ui.view.statistics.soldsChart

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.onedeepath.negooxtools.domain.model.productList.Date
import com.onedeepath.negooxtools.R

class MonthsSoldsChartAdapter (var months: List<Date>,
                               private var onMonthSelected: (Date) -> Unit,
                               context: Context
) : RecyclerView.Adapter<MonthsSoldsChartAdapter.MonthsSoldsChartViewHolder>() {

    private var selectedMonths = mutableSetOf<String>()


    inner class MonthsSoldsChartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val chMonth: Chip = itemView.findViewById(R.id.chMonth)

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthsSoldsChartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_products_list_date, parent, false)
        return MonthsSoldsChartViewHolder(view)

    }

    override fun getItemCount(): Int {
        return months.size
    }

    override fun onBindViewHolder(holder: MonthsSoldsChartViewHolder, position: Int) {

        val month = months[position]

        holder.chMonth.text = month.name

        holder.chMonth.setOnClickListener {

            onMonthSelected(month)

        }

    }




}
