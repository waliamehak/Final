package com.example.startle

import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.countershockkotlin.AudioStorer
import com.example.countershockkotlin.ImageStorer
import java.io.File

class SurpriseActivity : AppCompatActivity() {

    lateinit var imageView: ImageView

    lateinit var photoUri: Uri // to fetch the path to the image
    lateinit var soundUri: Uri //to fetch the path to the sound
    lateinit var tts: TextToSpeech //to speak the text
    lateinit var mediaPlayer: MediaPlayer //to play the audio file

    lateinit var imageModel: imageModel
    lateinit var audioModel: AudioModel

    var touchonce: Boolean=true// we accept the touch only once to trigger the scare activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_surprise)

        imageView = findViewById(R.id.imageView) //to fetch the id of the imageView
        imageModel=ImageStorer(this).getSelectedImage()//gets the image
        audioModel=AudioStorer(this).getSelectedAudio()//gets the selected audio

        if(imageModel.isAsset){
            photoUri = ShockUtils.getDrawableUri(this, imageModel.imgfilename)
        }
         else{
            photoUri = Uri.fromFile(File(imageModel.imgfilename))//fetches the uri from file
        }

        if(!audioModel.isTTS){
            soundUri = ShockUtils.getRawUri(this, audioModel.audioFilename)
        }


        Toast.makeText(this, "Ready", Toast.LENGTH_SHORT).show()

        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)// to hide the black bars on the top of screen



    }

    private fun showImage(){
       //to load the image with the help of URI into the imageView
        Glide.with(this)
            .load(photoUri)
            .into(imageView)
        imageView.visibility= View.VISIBLE
    }

    private fun playSoundClip(){
        // to play the media by fetching the source using soundUri
        mediaPlayer=MediaPlayer.create(this,soundUri)
        mediaPlayer.setOnCompletionListener {
            finish()// the completion handler to finish the activity once the mediaPlayer is done playing the file
        }
        mediaPlayer.start()
    }
    private fun handleTTS(){//to play textToSpeech instead of sound clip
        val toSpeak = audioModel.descriptionMessage
        tts = TextToSpeech(this, object: TextToSpeech.OnInitListener{
            override fun onInit(status: Int) {
                val params = HashMap<String, String>()

                params[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = "utterId"//random string is mapped to the ID

                if(status == TextToSpeech.SUCCESS){
                    tts.setOnUtteranceCompletedListener {
                        finish()
                    }
                    tts.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, params)//speaks the text
                }else{
                    finish()
                }


            }


        })
    }

    private fun userTriggeredAction(){//to trigger the scare activity only on the touch
        if(!touchonce){
            return
        }
        touchonce=false // we only want the scare activity to run once.
        if(audioModel.isTTS){
            handleTTS()
        }else{
            playSoundClip()
        }
        showImage()

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        userTriggeredAction()// called on touch event

        return super.onTouchEvent(event)
    }
}
