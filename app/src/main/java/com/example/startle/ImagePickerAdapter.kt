package com.example.countershockkotlin

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.startle.R
import com.example.startle.ShockUtils
import com.example.startle.imageModel
import java.io.File


class ImagePickerAdapter(var items:List<imageModel>, var callback:Callback):
    RecyclerView.Adapter<ImagePickerAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.grid_item, parent, false)//view inflates the grid_item

        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items.get(position)
        holder.itemView.setOnClickListener {
            callback.itemSelected(item)
        }

        val imgUri:Uri
        if(item.isAsset){
            imgUri = ShockUtils.getDrawableUri(holder.itemView.context, item.imgfilename)//fetching the uri from drawable if is an asset
        }else{
            imgUri = Uri.fromFile(File(item.imgfilename))//Uri fetched from local file
        }
        //
        Glide.with(holder.itemView.context)
            .load(imgUri)
            .into(holder.imageView)
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {//view holder is of type recycler view

        val imageView:ImageView

        init {
            imageView = itemView.findViewById(R.id.gridImageView)
        }

    }

    interface Callback{//communication between the adapter and the corresponding
        fun itemSelected(item:imageModel)
    }

}