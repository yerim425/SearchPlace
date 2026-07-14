package com.yrlee.tpsearchplaceapp.ui.favorite

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import com.yrlee.tpsearchplaceapp.R
import com.yrlee.tpsearchplaceapp.databinding.ActivityFavoriteBinding
import com.yrlee.tpsearchplaceapp.viewmodel.FavoriteViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FavoriteActivity : AppCompatActivity() {

    lateinit var binding: ActivityFavoriteBinding
    private val viewModel: FavoriteViewModel by viewModels()
    lateinit var adapter: FavoriteListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_favorite)
        binding.vm = viewModel
        binding.lifecycleOwner = this

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        viewModel.loadFavoritePlaces()

        adapter = FavoriteListAdapter(this){
            viewModel.likePlace(it)
        }
        binding.rv.adapter = adapter

        viewModel.placeList.observe(this, {
            adapter.setItemList(it)
        })

    }

}