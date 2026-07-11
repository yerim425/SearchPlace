package com.yrlee.tpsearchplaceapp.ui.main

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.gson.Gson
import com.yrlee.tpsearchplaceapp.databinding.RecyclerItemListFragmentBinding
import com.yrlee.tpsearchplaceapp.model.Place
import com.yrlee.tpsearchplaceapp.ui.detail.PlaceDetailActivity

class PlaceFavorAdapter(val context: Context, val documents: List<Place>) : Adapter<PlaceFavorAdapter.VH>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(RecyclerItemListFragmentBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun getItemCount(): Int = documents.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val place = documents[position]
        with(holder.binding){
            tvPlaceName.text = place.place_name
            tvDistance.text = place.distance + "m"
            tvPhone.text = place.phone
            tvAddress.text = if(place.road_address_name=="") place.address_name else place.road_address_name
            cbFavorite.isChecked = true
        }
    }

    inner class VH(val binding: RecyclerItemListFragmentBinding): ViewHolder(binding.root){

        init {
            binding.root.setOnClickListener {
                val intent = Intent(context, PlaceDetailActivity::class.java)

                val place = documents[layoutPosition]
                val s: String = Gson().toJson(place)
                intent.putExtra("place", s)
                context.startActivity(intent)
            }

//            binding.cbFavorite.setOnCheckedChangeListener { buttonView, isChecked ->
//                if(isChecked){
//                    (context as MainActivity).insertFavorPlace(documents[layoutPosition])
//                }
//            }
        }
    }
}