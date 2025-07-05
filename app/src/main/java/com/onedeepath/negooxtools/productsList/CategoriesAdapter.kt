package com.onedeepath.negooxtools.productsList

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.onedeepath.negooxtools.productsList.ProductsListActivity.Companion.SELECTED_CATEGORIES_ID_AND_QUERY_KEY
import com.onedeepath.negooxtools.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CategoriesAdapter(
    private var categories: List<String>,
    private var onChipClick: (String) -> Unit,
    private val dataStore: DataStore<Preferences>,
    context: Context): RecyclerView.Adapter<CategoriesAdapter.CategoriesViewHolder>() {

    private val categoriesIdAndQuery = mutableSetOf<String>()

    init {

        loadChecksCategories()

    }


    inner class CategoriesViewHolder(itemview: View) : RecyclerView.ViewHolder(itemview) {


        val chCategories: Chip = itemview.findViewById(R.id.chCategories)


        // Binding to save in data store
        fun bindCategories(category: String) {

            chCategories.text = category
            chCategories.isChecked = categoriesIdAndQuery.contains(category)

            chCategories.setOnCheckedChangeListener { buttonView, isChecked ->

                if (isChecked) {

                    categoriesIdAndQuery.add(category)

                } else {

                    categoriesIdAndQuery.remove(category)

                }

                saveIdAndQueryCategories()


            }
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriesViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_categories_product_chip, parent, false)
        return CategoriesViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoriesViewHolder, position: Int) {
        val category = categories[position]

        holder.chCategories.text = category

        holder.chCategories.setOnClickListener {

            onChipClick(category)

        }

        holder.bindCategories(category)


    }

    override fun getItemCount(): Int {
        return categories.size
    }


    private fun loadChecksCategories() {

        CoroutineScope(Dispatchers.IO).launch {

            val prefs = dataStore.data.first()
            val savedCategories = prefs[SELECTED_CATEGORIES_ID_AND_QUERY_KEY]?.split(",")?.filter { it.isNotBlank() }?.toSet() ?: emptySet()
            categoriesIdAndQuery.clear()
            categoriesIdAndQuery.addAll(savedCategories)

            withContext(Dispatchers.Main){

                notifyDataSetChanged()

            }

        }

    }


    private fun saveIdAndQueryCategories() {

        CoroutineScope(Dispatchers.IO).launch {

            dataStore.edit { prefs ->

                val validCategories = categoriesIdAndQuery.filter { categories ->

                    categories.isNotBlank()

                }.toSet()

                if (validCategories.isNotEmpty()) {

                    prefs[SELECTED_CATEGORIES_ID_AND_QUERY_KEY] = validCategories.joinToString(",")

                } else {

                    prefs.remove(SELECTED_CATEGORIES_ID_AND_QUERY_KEY)

                }
            }
        }
    }


}
