package com.betterandroid.restaurantscorner

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_restaurants.*
import java.util.*

class RestaurantsActivity : AppCompatActivity() {

    private val restaurantClient = RestaurantsRestClient()
    private var restaurantsAdapter: RestaurantsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurants)
        restaurantsAdapter = RestaurantsAdapter()
        recyclerViewRestaurants.apply {
            layoutManager = LinearLayoutManager(
                context!!,
                LinearLayoutManager.VERTICAL,
                false
            )
            this.adapter = restaurantsAdapter
        }
        showRestaurants()
    }

    override fun onDestroy() {
        super.onDestroy()
        restaurantClient.stopStream()
    }

    private fun showRestaurants() {
        restaurantClient.getRestaurants { response ->
            //parsing
            val restaurantParser = RestaurantParser()
            val parsedRestaurants = restaurantParser.parseRestaurants(response)
            //filtering
            val restaurantRules = RestaurantRules()
            val filteredRestaurants = restaurantRules.filterRestaurants(parsedRestaurants)
            // displaying
            displayRestaurants(filteredRestaurants)
        }
    }

    private fun displayRestaurants(restaurants: List<Restaurant>) {
        val viewModel = RestaurantViewModel()
        restaurantsAdapter!!.restaurants = viewModel.getDisplayRestaurants(restaurants)
        restaurantsAdapter!!.clickListener =
            object : RestaurantsAdapter.RestaurantClickListener {
                override fun onRestaurantClicked(restaurantId: Int) {
                    Toast.makeText(
                        this@RestaurantsActivity,
                        "Pressed a restaurant!",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
}