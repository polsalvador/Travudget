package com.travudget.travudget

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.content.Context
import org.json.JSONObject
import android.app.Activity
import android.content.Intent
import android.view.ViewGroup
import android.view.LayoutInflater
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil

class Divises : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: androidx.appcompat.widget.SearchView
    private lateinit var txtRetornar: TextView

    private lateinit var adapter: ListAdapter<Pair<String, String>, DivisesViewHolder>
    private var divisesList: List<Pair<String, String>> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.divises)

        recyclerView = findViewById(R.id.recyclerViewDivises)
        searchView = findViewById(R.id.searchView)
        txtRetornar = findViewById(R.id.txtRetornar)

        divisesList = createDivisesList()

        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = object : ListAdapter<Pair<String, String>, DivisesViewHolder>(MyDiffCallback()) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DivisesViewHolder {
                val itemView = LayoutInflater.from(parent.context).inflate(R.layout.divisa, parent, false)
                return DivisesViewHolder(itemView)
            }

            override fun onBindViewHolder(holder: DivisesViewHolder, position: Int) {
                val pair = getItem(position)
                holder.bind(pair)
                holder.itemView.setOnClickListener {
                    val intent = Intent()
                    intent.putExtra("selectedDivisa", pair.first)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
            }
        }

        recyclerView.adapter = adapter

        adapter.submitList(divisesList)

        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    if (newText.isNotEmpty()) {
                        filterDivises(newText)
                    } else {
                        adapter.submitList(divisesList)
                    }
                }
                return true
            }
        })

        txtRetornar.setOnClickListener {
            finish()
        }
    }

    private fun createDivisesList(): List<Pair<String, String>> {
        val sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val currenciesJson = sharedPreferences.getString("currencies", null)

        val divisesList = mutableListOf<Pair<String, String>>()
        currenciesJson?.let {
            try {
                val jsonObject = JSONObject(it)
                val symbolsObject = jsonObject.getJSONObject("symbols")
                val keys = symbolsObject.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val value = symbolsObject.getString(key)
                    divisesList.add(Pair(key, value))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return divisesList
    }

    private fun filterDivises(query: String) {
        val filteredList = divisesList.filter { it.first.contains(query, ignoreCase = true) }
        adapter.submitList(filteredList)
    }

    private class MyDiffCallback : DiffUtil.ItemCallback<Pair<String, String>>() {
        override fun areItemsTheSame(oldItem: Pair<String, String>, newItem: Pair<String, String>): Boolean {
            return oldItem.first == newItem.first
        }

        override fun areContentsTheSame(oldItem: Pair<String, String>, newItem: Pair<String, String>): Boolean {
            return oldItem == newItem
        }
    }

}
