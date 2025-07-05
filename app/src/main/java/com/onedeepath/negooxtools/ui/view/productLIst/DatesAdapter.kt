package com.onedeepath.negooxtools.ui.view.productLIst

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.onedeepath.negooxtools.R
import com.onedeepath.negooxtools.domain.model.productList.Date


class DatesAdapter(private var months: List<Date>,
                   private var onMonthSelected: (Date) -> Unit,
                   context: Context) : RecyclerView.Adapter<DatesAdapter.MonthsViewHolder>() {


    inner class MonthsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val chMonth: Chip = itemView.findViewById(R.id.chMonth)


    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_products_list_date, parent, false)
        return MonthsViewHolder(view)

    }

    override fun getItemCount(): Int {
        return months.size
    }

    override fun onBindViewHolder(holder: MonthsViewHolder, position: Int) {

        val month = months[position]

        holder.chMonth.text = month.name

        holder.chMonth.setOnClickListener {

            onMonthSelected(month)

        }

    }




}