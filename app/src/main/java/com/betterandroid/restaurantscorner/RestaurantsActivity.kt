package com.betterandroid.restaurantscorner

import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.betterandroid.restaurantscorner.mocks.MockCreator
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_restaurants.*
import java.util.*
import kotlin.collections.ArrayList

class RestaurantsActivity : AppCompatActivity() {

    private val disposable = CompositeDisposable()
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
        disposable.dispose()
    }

    private fun showRestaurants() {
        getRestaurants { response ->
            //parsing
            val parsedRestaurants = parseRestaurants(response)
            //filtering
            val filteredRestaurants = filterRestaurants(parsedRestaurants)
            // displaying
            displayRestaurants(filteredRestaurants)
        }
    }

    private fun getRestaurants(completionHandler: (response: RestaurantListResponse) -> Unit) {
        val client = RestaurantsRestClient()
        val userId = MockCreator.getUserId()
        disposable.add(
            client.getRestaurants(userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    completionHandler.invoke(response)
                }, {})
        )
    }

    private fun parseRestaurants(response: RestaurantListResponse): List<Restaurant> {
       return response.restaurants
            ?.filter { restaurantResponse ->
                restaurantResponse.name != null
                        && restaurantResponse.imageUrl != null
            }?.map { restaurantResponse ->
                val location = SimpleLocation(
                    restaurantResponse.locationLatitude,
                    restaurantResponse.locationLongitude
                )
                return@map Restaurant(
                    id = restaurantResponse.id,
                    name = restaurantResponse.name!!,
                    imageUrl = restaurantResponse.imageUrl!!,
                    location = location,
                    closingHour = restaurantResponse.closingHour,
                    type = restaurantResponse.type
                )
            }.orEmpty()
    }

    private fun filterRestaurants(restaurants: List<Restaurant>): ArrayList<Restaurant> {
        val filteredRestaurants = arrayListOf<Restaurant>()
        for (parsedRestaurant in restaurants) {
            if (parsedRestaurant.closingHour < 6)
                filteredRestaurants.add(parsedRestaurant)
        }

        // val latitude = MockCreator.getUserLatitude()
        for (filteredRestaurant in filteredRestaurants) {
            val userLat = MockCreator.getUserLatitude()
            val userLong = MockCreator.getUserLongitude()
            val distance = FloatArray(2)
            Location.distanceBetween(
                userLat,
                userLong,
                filteredRestaurant.location.latitude,
                filteredRestaurant.location.longitude,
                distance
            )

            val distanceResult = distance[0] / 1000
            filteredRestaurant.distance = distanceResult.toInt()
            Log.d("DISTANCE_LOGS", "found distance at $distanceResult")
        }
        Collections.sort(filteredRestaurants, RestaurantDistanceSorter())
        return filteredRestaurants
    }

    private fun displayRestaurants(restaurants: ArrayList<Restaurant>) {
        val displayRestaurants = arrayListOf<RestaurantDisplayItem>()
        restaurants.forEach { restaurant ->
            displayRestaurants.add(
                RestaurantDisplayItem(
                    id = restaurant.id,
                    displayName = "Restaurant ${restaurant.name}",
                    displayDistance = "at ${restaurant.distance} KM distance",
                    imageUrl = restaurant.imageUrl,
                    type = when (restaurant.type) {
                        "EAT_IN" -> RestaurantType.EAT_IN
                        "TAKE_AWAY" -> RestaurantType.TAKE_AWAY
                        else -> RestaurantType.DRIVE_THROUGH
                    }
                )
            )
        }

        val adapter = restaurantsAdapter
        if (adapter != null) {
            adapter.restaurants = displayRestaurants
            adapter.clickListener =
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


}