package com.example.startle

import java.io.Serializable
//similar to imageModel. This will handle all the audios in the project. Less comments because have explained most of them in imageModel.
class AudioModel: Serializable {//serialisable because objects will be stored in the shared preferences(as we converted the objects as a Json string)

    var id:Int
    var audioFilename:String = ""
    var descriptionMessage:String// description of the audio clip
    var isAsset:Boolean = false// if it is created by the user or is an asset provided by the project
    var isTTS:Boolean

    constructor(id: Int, audioFilename: String, descriptionMessage: String, isAsset: Boolean) {
        this.id = id
        this.audioFilename = audioFilename
        this.descriptionMessage = descriptionMessage
        this.isAsset = isAsset
        this.isTTS = false
    }

    constructor(id:Int, descriptionMessage: String){
        this.id = id;
        this.descriptionMessage = descriptionMessage
        this.isTTS = true;
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AudioModel

        if (id != other.id) return false
        if (audioFilename != other.audioFilename) return false
        if (descriptionMessage != other.descriptionMessage) return false
        if (isAsset != other.isAsset) return false
        if (isTTS != other.isTTS) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + audioFilename.hashCode()
        result = 31 * result + descriptionMessage.hashCode()
        result = 31 * result + isAsset.hashCode()
        result = 31 * result + isTTS.hashCode()
        return result
    }


}