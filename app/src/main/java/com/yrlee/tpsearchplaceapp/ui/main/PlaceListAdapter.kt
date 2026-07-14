package com.yrlee.tpsearchplaceapp.ui.main

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.gson.Gson
import com.yrlee.tpsearchplaceapp.databinding.RvItemPlaceBinding
import com.yrlee.tpsearchplaceapp.model.PlaceUiModel
import com.yrlee.tpsearchplaceapp.ui.detail.PlaceDetailActivity

class PlaceListAdapter(private val context: Context, private val onLikeClick:(PlaceUiModel)->Unit) : Adapter<PlaceListAdapter.VH>(){

    private var placeList = mutableListOf<PlaceUiModel>()
    private var page = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(RvItemPlaceBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun getItemCount(): Int = placeList.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val place = placeList[position].place
        with(holder.binding){
            tvPlaceName.text = place.place_name
            tvDistance.text = place.distance + "m"
            tvPhone.text = place.phone
            tvAddress.text = if(place.road_address_name=="") place.address_name else place.road_address_name
            cbFavorite.isChecked = placeList[position].isFavorite
        }
    }

    inner class VH(val binding: RvItemPlaceBinding): ViewHolder(binding.root){

        init {
            binding.root.setOnClickListener {
                val intent = Intent(context, PlaceDetailActivity::class.java)

                val place = placeList[layoutPosition].place
                val s: String = Gson().toJson(place)
                intent.putExtra("place", s)
                context.startActivity(intent)
            }

            binding.cbFavorite.setOnClickListener {
                onLikeClick(placeList[layoutPosition])
            }
        }
    }



    fun addPlaceList(list: List<PlaceUiModel>){
        this.placeList.addAll(list)
        notifyDataSetChanged()
    }

    fun clear(){
        page = 1
        this.placeList.clear()
        notifyDataSetChanged()
    }

    fun getPage(): Int{
        return page
    }

    fun increasePage(){
        page++
    }

}