package com.onedeepath.negooxtools.ui.view.statistics.ranking

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.onedeepath.negooxtools.R


class SectionsFilterAdapter(private val sections: List<String> ,context: Context) : RecyclerView.Adapter<SectionsFilterAdapter.SectionsFilterViewHolder>() {


    inner class SectionsFilterViewHolder(view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SectionsFilterViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ranking_chips, parent, false)
        return SectionsFilterViewHolder(view)
    }

    override fun getItemCount(): Int {
        return sections.size
    }

    override fun onBindViewHolder(holder: SectionsFilterViewHolder, position: Int) {





    }

}