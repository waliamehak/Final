package com.example.startle

import java.io.Serializable //serializable because we want to store the objects in shared preferences
class imageModel:Serializable {//to handle and represent each image in the application
    val id:Int
    val imgfilename: String
    val isAsset:Boolean

    constructor(id: Int, imgfilename: String, isAsset: Boolean) {
        this.id = id// to select the images
        this.imgfilename = imgfilename// where is the image located
        this.isAsset = isAsset// image added by user or is part of the project
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as imageModel

        if (id != other.id) return false
        if (imgfilename != other.imgfilename) return false
        if (isAsset != other.isAsset) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + imgfilename.hashCode()
        result = 31 * result + isAsset.hashCode()
        return result
    }

}