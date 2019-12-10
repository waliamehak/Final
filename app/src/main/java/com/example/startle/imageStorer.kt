package com.example.countershockkotlin

import android.content.Context
import android.content.SharedPreferences
import com.example.startle.R
import com.example.startle.ShockUtils
import com.example.startle.imageModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
//image storer keeps track of where each image is being stored
class ImageStorer(var context:Context) {
    val preferences:SharedPreferences//objects are being stored in sharedPreferences
    val editor:SharedPreferences.Editor

    init {
        preferences = context.getSharedPreferences(ShockUtils.SHOCK_SHARED_PREFS, Context.MODE_PRIVATE)
        editor = preferences.edit()
    }

    fun storeImages(images:List<imageModel>){//to store the images
        val key = context.getString(R.string.key_stored_images)
        val gson = Gson()// to conert list of objects into Json string
        editor.putString(key, gson.toJson(images))
        editor.commit()
    }

    fun addImage(image:imageModel){//to add the image into the list of stored images.
        val images = getStoredImages() as ArrayList<imageModel>
        images.add(image)
        storeImages(images)
    }

    private fun getStoredImages():List<imageModel>{//to fetch the image from where it is stored at
        val imagesAsJson = preferences.getString(context.getString(R.string.key_stored_images), null)//to get the images as Json
        if(imagesAsJson == null || imagesAsJson.length == 0){
            return ArrayList()//empty arrayList is returned
        }

        val gson = Gson()
        val type = object : TypeToken<List<imageModel>>(){}.type//to turn the string back into the real object
        return gson.fromJson(imagesAsJson, type)
    }

    fun getAllImages():List<imageModel>{//gets all the images which are an asset first and then the images downloaded by the user
        val assetImages = ArrayList<imageModel>()
        assetImages.add(imageModel(0, "doll", true))
        assetImages.add(imageModel(1, "lama", true))
        assetImages.add(imageModel(2, "clown", true))
        assetImages.add(imageModel(3, "saw", true))

        assetImages.addAll(getStoredImages())//add the stored images
        return assetImages
    }

    fun getSelectedImage():imageModel{// which image is currently selected by the user
        val images = getAllImages()

        val defaultImage = images.get(0)//that will be just the first image aas default

        val imageId = preferences.getInt(context.getString(R.string.key_photo_id), 0)//to get the imageId of the actual selected image
        for(image in images){
            if(image.id == imageId){
                return image
            }
        }

        // Fall back on defaults
        editor.putInt(context.getString(R.string.key_photo_id), 0)
        editor.commit()

        return defaultImage
    }//return type is of imageModel class

}