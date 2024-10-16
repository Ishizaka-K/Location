package com.example.locationsample

import android.location.Location
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// Adapter for RecyclerView to display location data
class LocationAdapter(private val locationList: MutableList<String>) :
    RecyclerView.Adapter<LocationAdapter.LocationViewHolder>() {

    class LocationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val locationTextView: TextView = view.findViewById(R.id.locationTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.location_item, parent, false)
        return LocationViewHolder(view)
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        val location = locationList[position]
        holder.locationTextView.text = location
    }

    override fun getItemCount() = locationList.size

    // Function to add a new location and notify the adapter
    fun addLocation(location: String) {
        locationList.add(location)
        notifyItemInserted(locationList.size - 1) // Update RecyclerView
    }
}
