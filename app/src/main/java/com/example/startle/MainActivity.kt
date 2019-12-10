package com.example.startle

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.*
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.example.countershockkotlin.AudioPickerDialogFragment
import com.example.countershockkotlin.AudioStorer
import com.example.countershockkotlin.ImagePickerDialogFragment
import com.example.countershockkotlin.ImageStorer
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity() {

//late initialising the objects to the classes
    lateinit var preferences: SharedPreferences
    lateinit var editor: SharedPreferences.Editor
    lateinit var audioStorer: AudioStorer
    lateinit var imageStorer: ImageStorer

    //late initialising the main ImageView and audioText box
    lateinit var scaryImageView:ImageView
    lateinit var audioTextView:TextView

    var mediaPlayer:MediaPlayer?=null//initialising with null value to object
    var tts: TextToSpeech?=null//initialising with null value to the object
    lateinit var playIcon:ImageView//points to the play icon which is of type imageView
    //updateListerner fn is to let the activity know something was updated in the dialog pickers
    val updateListener : BroadcastReceiver=object: BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            updateUI()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.prankSurface).setOnClickListener{
            createNotification()//to generate the notificaiton upon clicking on the prankSurface
            finish()//to quit the application
        }
        preferences=getSharedPreferences(ShockUtils.SHOCK_SHARED_PREFS, Context.MODE_PRIVATE)//to get the sharedPreferences from shockUtils
        editor=preferences.edit()
        audioStorer=AudioStorer(this)
        imageStorer= ImageStorer(this)
        scaryImageView=findViewById(R.id.scareImageView)
        audioTextView=findViewById(R.id.audioTextView)
        updateUI()//updating the UI after initialising scaryImageVIew and audioTextview
        playIcon=findViewById(R.id.playIconImageView)

        findViewById<View>(R.id.audioSurface).setOnClickListener{//works on the click to audioSurface and opens a dialog fragment
            val ft=supportFragmentManager.beginTransaction()
            val prev=supportFragmentManager.findFragmentByTag("dialog")//create the dialog
            if (prev!=null){
                ft.remove(prev)
            }
            ft.addToBackStack(null)
            val dialogFragment= AudioPickerDialogFragment()//pop the dialog up
            dialogFragment.isCancelable=true
            dialogFragment.show(ft,"dialog")
        }
        scaryImageView.setOnClickListener{
            val ft=supportFragmentManager.beginTransaction()
            val prev=supportFragmentManager.findFragmentByTag("dialog")
            if (prev!=null){
                ft.remove(prev)
            }
            ft.addToBackStack(null)
            val dialogFragment=ImagePickerDialogFragment()
            dialogFragment.isCancelable=true
            dialogFragment.show(ft,"dialog")
        }


        findViewById<View>(R.id.playSurface).setOnClickListener{// we want the audio to be played when play surface is clicked
            val audio=audioStorer.getSelectedAudio()//grab the selected audio
            if(audio.isTTS){//is audio TextToSpeech?
                val toSpeak=audio.descriptionMessage
                tts= TextToSpeech(baseContext,TextToSpeech.OnInitListener {
                    if(it==TextToSpeech.SUCCESS){
                        tts?.speak(toSpeak,TextToSpeech.QUEUE_FLUSH,null)//to speak those words

                    }
                })

            }

            else{// it is an audio asset from the raw folder in resources
                val uri = ShockUtils.getRawUri(baseContext, audio.audioFilename)//get uri and feed it to the media player

                if(mediaPlayer != null){
                    if(mediaPlayer!!.isPlaying){
                        mediaPlayer?.stop()
                        updateAudioIcon(false)
                        return@setOnClickListener
                    }
                }

                mediaPlayer = MediaPlayer.create(this, uri)
                mediaPlayer?.setOnCompletionListener {//when the audio is done playing
                    updateAudioIcon(false)//to update the audio icon on play surface back to play icon
                }
                mediaPlayer?.start()
                updateAudioIcon(true)//if media starts to play we need to update the button to play button
            }
        }


    }
        override fun onCreateOptionsMenu(menu: Menu?): Boolean {
            val inflater=menuInflater//to inflate the main menu
            inflater.inflate(R.menu.main_menu,menu)
            return true
        }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if(item?.itemId == R.id.addButton){//to create a pop-up menu
            val popup = PopupMenu(this, findViewById(R.id.addButton))// to attach the view to addButton
            popup.menuInflater.inflate(R.menu.pop_menu, popup.menu)// to inflate the pop up menu

            popup.setOnMenuItemClickListener(object: PopupMenu.OnMenuItemClickListener{
                override fun onMenuItemClick(popItem: MenuItem?): Boolean {
                    when(popItem?.itemId){
                        R.id.addImage -> {//to open the image Dialog upon clicking the add Image
                            addImageDialog()
                        }
                        R.id.addAudio ->{//to open the add dialog fragment upon clicking Add audio from menu
                            addAudioDialog()
                        }
                    }
                    return true;
                }
            })

            popup.show()//to show the pop-up
            return true
        }
        return super.onOptionsItemSelected(item)
    }
    fun updateAudioIcon(isPlaying:Boolean){
        if(isPlaying){//to update icon of the play button according to the state
            playIcon.setImageResource(R.drawable.ic_pause)//shows pause button if audio is playing
        }else{
            playIcon.setImageResource(R.drawable.ic_play)
        }
    }

    private fun addAudioDialog(){// this is the add audio Dialog in the menu to add textToSpeech audio into the application
        val soundEditText = EditText(this)
        soundEditText.setHint("Text to speak")

        val dialog = AlertDialog.Builder(this)
            .setTitle("Add Audio")
            .setMessage("Enter the message for text to speech")
            .setView(soundEditText)
            .setCancelable(true)
            .setPositiveButton(android.R.string.ok, DialogInterface.OnClickListener { dialogInterface, i ->
                val message = soundEditText.text.toString()
                if(message == null || message.trim().isEmpty()){// to handle empty text-box
                    Toast.makeText(baseContext, "message cannot be empty", Toast.LENGTH_SHORT).show()
                    return@OnClickListener
                }else{
                    addTTSAudio(message)//this fn is called to add the message to speak into application
                }
            })
            .setNegativeButton(android.R.string.cancel, null).create()

        dialog.show()
    }
    private fun addTTSAudio(message:String){//to add the TTS audio into the audioStorer (audioStorer is a class that stores all the audios)
        val mediaId=getNextMediaId()//gets the next media ID and stores it in audioStorer
        val audioModel=AudioModel(mediaId,message)//gives it a handler via audioModel class
        audioStorer.addAudio(audioModel)

    }

    private fun getNextMediaId():Int{//to keep track of the media id where we left at
        val mediaId=preferences.getInt(getString(R.string.key_next_media_id), ShockUtils.STARTING_ID)//to initiate shared preferences from the next media id
        editor.putInt(getString(R.string.key_next_media_id),mediaId+1)//to update the mediaId number by 1
        editor.commit()//to commit the changes
        return mediaId

    }

    private fun addImageDialog(){//creates the dialog to download images from the internet
        val urlBox = EditText(this)
        urlBox.setHint("Image to download")

        val dialog = AlertDialog.Builder(this)
            .setTitle("Enter Image Url")
            .setMessage("Import an image from the web")
            .setView(urlBox)
            .setCancelable(true)
            .setPositiveButton(android.R.string.ok, DialogInterface.OnClickListener //handles the clicking of "ok" button
            { dialogInterface, i ->
                val url = urlBox.text.toString()
                if(url == null || url.trim().isEmpty()){//to handle empty strings
                    Toast.makeText(baseContext, "url cannot be empty", Toast.LENGTH_SHORT).show()
                    return@OnClickListener
                }else{
                    downloadImageToFile(url)//passes the url from where the image is to be downloaded
                }
            })
            .setNegativeButton(android.R.string.cancel, null)// cancel is the negative button
            .create()

        dialog.show()
    }

    private fun downloadImageToFile(url:String){//to download the image to a file
        Glide.with(this)
            .asBitmap()
            .load(url)
            .into(object : SimpleTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    saveImage(resource)//resource is of type bitmap
                }

            })
    }

    private fun saveImage(bitmap: Bitmap){//will save the bitmap into a file
        var output: FileOutputStream? =null
        val file= createInternalFile(UUID.randomUUID().toString())//whenever we create a file it does not has a same name, that is why UUID is chose randomly
        val imageModel= imageModel(getNextMediaId(), file.absolutePath, false)//to create an object of imageModel which will go into the file.

        try {
            output = FileOutputStream(File(imageModel.imgfilename))
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100,output)
            output?.close()

            imageStorer.addImage(imageModel)//added to the sharedPrefs using imageStorer

        }catch (ex:IOException){
            ex.printStackTrace()
        }
    }

    private fun createInternalFile(filename:String):File{//to create an internal file and get the directory
        val outputDir= externalCacheDir//to get the absolute path to application-specific directory
        return File(outputDir, filename)

    }
    private fun updateUI(){//to update the UI by showing the selected image and media description of the selected media
        val image=imageStorer.getSelectedImage()
        val imgUri:Uri
        if (image.isAsset){
            imgUri=ShockUtils.getDrawableUri(this,image.imgfilename)
        }
        else{
            imgUri=Uri.fromFile(File(image.imgfilename))
        }
        //this will update the image
        Glide.with(this)
            .load(imgUri)
            .into(scaryImageView)
        //this will update the audioTextView to current selected audio
        val audio=audioStorer.getSelectedAudio()
        audioTextView.setText(audio.descriptionMessage)
    }

    override fun onStart() {//broadcast receiver is registered on start
        super.onStart()
        LocalBroadcastManager.getInstance(this).registerReceiver(updateListener, IntentFilter(ShockUtils.MEDIA_UPDATED_ACTION))
    }

    override fun onStop() {//broadcast receiver is registered on stop
        super.onStop()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(updateListener)
    }

    private fun createNotification(){//to create notification
        val requestId = System.currentTimeMillis().toInt()

        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)//sound played when notification is hit
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationIntent = Intent(this, SurpriseActivity::class.java)// to open the surpriseActivity is the intent
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)//to clear the existing notifications before generating the new one

        val contentIntent = PendingIntent.getActivity(this, requestId, notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT)

        val message = "Tap to prank your friends"
        val builder = NotificationCompat.Builder(this)//it builds the notification
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Startle notification")
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setContentText(message)
            .setAutoCancel(true)
            .setSound(alarmSound)
            .setContentIntent(contentIntent)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channelId = "ChannelId"
            val channel = NotificationChannel(channelId, "Channel Title",
                NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
            builder.setChannelId(channelId)
        }

        notificationManager.notify(42233, builder.build())

    }

}
