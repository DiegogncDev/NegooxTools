package com.onedeepath.negooxtools.statistics.ranking

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPageRankingAdapter(fa: FragmentActivity): FragmentStateAdapter(fa) {

    private val fragmentMap = mutableMapOf<Int, ViewPageRankingFragment>()

//    private val fragments: List<ViewPageRankingFragment> = listOf(
//        ViewPageRankingFragment.newInstance("profit"),
//        ViewPageRankingFragment.newInstance("price"),
//        ViewPageRankingFragment.newInstance("weight"),
//        ViewPageRankingFragment.newInstance("sales"),
//        ViewPageRankingFragment.newInstance("speed")
//    )

    // Numero de fragmentos(secciones) que vamos a tener
    override fun getItemCount(): Int = 5

    // Pasar fragmento que usaremos, y darle datos
    override fun createFragment(position: Int) : Fragment {

        val fragment = ViewPageRankingFragment.newInstance(
            when (position) {
                0 -> "profit"
                1 -> "price"
                2 -> "weight"
                3 -> "sales"
                4 -> "speed"
                else -> ""
            }
        )

        fragmentMap[position] = fragment

        return fragment


    }

    fun getFragmentAt(position: Int): ViewPageRankingFragment {

        return fragmentMap[position]!!

    }

    fun getAllFragments(): List<ViewPageRankingFragment> {
        return fragmentMap.values.toList()
    }


}