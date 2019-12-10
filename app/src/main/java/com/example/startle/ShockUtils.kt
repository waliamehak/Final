package com.example.startle

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import java.io.File

class ShockUtils {
    companion object{
        val SHOCK_SHARED_PREFS= "shock_shared_prefs"//shared preferences file where our objects will be stored at
        val STARTING_ID= 1000// for user added media we want to give them dynamic ID's
        val MEDIA_UPDATED_ACTION="MEDIA_UPDATED_ACTION"// to use Broadcast Manager the send message to the activity to refresh whenever we update/add the media or images

        fun getRawUri(context:Context,assetName:String): Uri {//content resolver resolves the sound files into the URI.
            return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + File.pathSeparator + File.separator + File.separator + context.packageName + "/raw/"+ assetName)
        }
        fun getDrawableUri(context: Context,assetName: String): Uri{//to resolve the image drawables. to reference them by Uri
            return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + File.pathSeparator + File.separator + File.separator + context.packageName + "/drawable/"+ assetName)

        }

    }

}