package com.yrlee.tpsearchplaceapp.ui.favorite

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.yrlee.tpsearchplaceapp.databinding.RvItemPlaceBinding
import com.yrlee.tpsearchplaceapp.model.FavoriteUiModel
import com.yrlee.tpsearchplaceapp.ui.detail.PlaceDetailActivity
import okhttp3.internal.notify

class FavoriteListAdapter(private val context: Context, private val onLikeCancel:(String)-> Unit) : RecyclerView.Adapter<FavoriteListAdapter.VH>(){

    private var itemList = mutableListOf<FavoriteUiModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(RvItemPlaceBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun getItemCount(): Int = itemList.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = itemList[position]
        val place = item.place
        val distance = item.distance
        with(holder.binding){
            tvPlaceName.text = place.place_name
            tvPhone.text = place.phone
            tvAddress.text = if(place.road_address_name=="") place.address_name else place.road_address_name
            cbFavorite.isChecked = true
            tvDistance.text = distance + "m"
        }
    }

    inner class VH(val binding: RvItemPlaceBinding): RecyclerView.ViewHolder(binding.root){

        init {
            binding.root.setOnClickListener {
                val intent = Intent(context, PlaceDetailActivity::class.java)

                val place = itemList[layoutPosition].place
                val s: String = Gson().toJson(place)
                intent.putExtra("place", s)
                context.startActivity(intent)
            }

            binding.cbFavorite.setOnClickListener { v ->
                onLikeCancel(itemList[layoutPosition].place.id)
            }
        }
    }

    fun setItemList(list: List<FavoriteUiModel>){
        this.itemList.clear()
        this.itemList.addAll(list)

        notifyDataSetChanged()
    }
}