package com.onedeepath.negooxtools.statistics.ranking

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.onedeepath.negooxtools.productsList.ProductsDataBaseHelper
import com.onedeepath.negooxtools.statistics.Top3
import androidx.lifecycle.ViewModelProvider
import com.onedeepath.negooxtools.R

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ViewPageRankingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ViewPageRankingFragment : Fragment() {

    private lateinit var rankingListAdapter: RankingListAdapter
    private lateinit var rvRankingFragment: RecyclerView
    private lateinit var db: ProductsDataBaseHelper

    private var filterType: String? = null

    private var yearFilter: String? = null
    private var monthsFilter: List<String> = emptyList()
    private var categoriesFilter: List<String> = emptyList()


    companion object {

        private const val ARG_OBJECT = "object"

        fun newInstance(filterType: String): ViewPageRankingFragment {
            val fragment = ViewPageRankingFragment()
            val args = Bundle()
            args.putString("filterType", filterType)
            fragment.arguments = args
            return fragment
        }

    }

    private val sharedViewModel: RankingSharedViewModel by lazy {
        ViewModelProvider(requireActivity())[RankingSharedViewModel::class.java]
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        filterType = arguments?.getString("filterType")


    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_view_page_ranking, container, false)

    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.i("Prueba2", "onViewCreated llamado en el fragmento $filterType")

        sharedViewModel.selectedLiveYear.observe(viewLifecycleOwner) { year ->
            yearFilter = year
            refreshData()
        }

        sharedViewModel.selectedLiveMonths.observe(viewLifecycleOwner) { months ->
            monthsFilter = months
            refreshData()
        }

        sharedViewModel.selectedLiveCategories.observe(viewLifecycleOwner) { categories ->
            categoriesFilter = categories
            refreshData()
        }


        Log.i("rfilters", "yearFilter: $yearFilter, monthsFilter: $monthsFilter, categoriesFilter: $categoriesFilter")

        db = ProductsDataBaseHelper(requireContext())
        // Config ranking Fragment
        rvRankingFragment = view.findViewById(R.id.rvRankingFragment)
        rvRankingFragment.layoutManager = LinearLayoutManager(requireContext())
        rvRankingFragment.setHasFixedSize(true)

        // Getting filters throught fun by filterType

        // Config rankingListAdapter and setting this adapter to rvRankingFragment
        rankingListAdapter = RankingListAdapter(emptyList(), requireContext())
        rvRankingFragment.adapter = rankingListAdapter



    }

    private fun refreshData() {
        val productsFiltered = getFilteredProducts(filterType)
        rankingListAdapter.refreshRanking(productsFiltered)
    }




    // Get throught sections dbs querys filters
    private fun getFilteredProducts(filterType: String?): List<Top3> {

        return when (filterType) {
            "profit" -> db.getTop3MostProfitFilters(yearFilter, monthsFilter, categoriesFilter)
            "price" -> db.getTop3MostExpensiveFilters(yearFilter, monthsFilter, categoriesFilter)
            "weight" -> db.getTop3MostWeightFilters(yearFilter, monthsFilter, categoriesFilter)
            "sales" -> db.getTop3MostSoldsFilters(yearFilter, monthsFilter, categoriesFilter)
            "speed" -> db.getTop3MostFasterFilters(yearFilter, monthsFilter, categoriesFilter)
            else -> emptyList()
        }

    }












}