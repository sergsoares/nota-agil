package com.example.sergio.nota_agil.activity

import android.Manifest
import android.R
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import io.paperdb.Paper
import org.jetbrains.anko.*
import java.io.IOException

class ItemActivity : AppCompatActivity() {

  private var CATEGORY: String = ""
  private var ITEM: String = ""
  //  lateinit var ITEM: String
  private val TAG = "ItemActivity"
  private var filesListView: ListView? = null
//  private var mMediaPlayer: MediaPlayer? = null
  private val mFileName: StringBuilder = StringBuilder()

//  companion object {
    private var mMediaRecorder: MediaRecorder? = null
    private var mMediaPlayer: MediaPlayer? = null
//  }


  public override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    CATEGORY = intent.getStringExtra("category")
    ITEM = intent.getStringExtra("item")
    isRecordPermissionGranted()
    isStoragePermissionGranted()
    defineLayout()
    reloadAdapter()
    toast(CATEGORY)
    toast(ITEM)
  }

  private var recordButton: Button? = null
  private var stopRecordButton: Button? = null

  private fun defineLayout() {
    verticalLayout {
      recordButton = button {
        text = "Gravar Audio"
        onClick {
          startRecord()
          recordButton?.visibility = View.GONE
          stopRecordButton?.visibility = View.VISIBLE
        }
      }

      stopRecordButton = button {
        text = "Parar Gravacao"
        visibility = View.GONE
        onClick {
          stopRecord()
          recordButton?.visibility = View.VISIBLE
          stopRecordButton?.visibility = View.GONE
        }
      }

      textView {
        text = ITEM
        textSize = 42f
      }

      filesListView = listView {
        onItemClick { adapterView, view, i, l ->
          executeMedia(fetchItem()[i])
        }
      }
    }
  }

  private fun Button.startRecord() {
    if (isStoragePermissionGranted() && isRecordPermissionGranted()) {
      val pmanager = this.context.getPackageManager()
      if (pmanager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE)) {
        mMediaRecorder = MediaRecorder()
        mMediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        mMediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        mMediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        //TODO: Clean mFileName to dont concat all new media recorded
        mFileName.setLength(0)
        mFileName.append(getTimestamp())
        mMediaRecorder?.setOutputFile(getCompletePath(mFileName.toString()))
        try {
          Log.e(TAG, "Start recording")
          mMediaRecorder?.prepare()
          mMediaRecorder?.start()
        } catch (e: IOException) {
          Log.e(TAG, "prepare() failed")
        }

        Log.e(TAG, "File is in ")
      }
    }
    reloadAdapter()
  }

  private fun getCompletePath(itemName: String) = fecthAbsolutePath() + "/" + itemName + ".3gp"


  private fun stopRecord() {
    mMediaRecorder?.stop()
    val itemTemp = fetchItem()
    itemTemp.add(mFileName.toString())
    Paper.book(CATEGORY).write(ITEM, itemTemp)
    reloadAdapter()
  }

  private fun executeMedia(itemName: String) {
    try {
      mMediaPlayer = MediaPlayer()
      mMediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
      //TODO: getCorrectPathToFile
      mMediaPlayer?.setDataSource(getCompletePath(itemName))
      mMediaPlayer?.prepare()
      mMediaPlayer?.start()
    } catch (e: IOException) {
      e.printStackTrace()
    }
  }

  private fun fetchItem(): ArrayList<String> = Paper.book(CATEGORY).read(ITEM)

  private fun reloadAdapter() {
      val adapter = ArrayAdapter<String>(this, R.layout.simple_list_item_1, fetchItem())
      filesListView!!.adapter = adapter
  }

  fun isRecordPermissionGranted():Boolean {
    if (Build.VERSION.SDK_INT >= 23)
    {
      if (this.checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) === PackageManager.PERMISSION_GRANTED)
      {
        Log.v(TAG, "Permission is granted")
        return true
      }
      else
      {
        Log.v(TAG, "Permission is revoked")
        ActivityCompat.requestPermissions(this,
            arrayOf<String>(Manifest.permission.RECORD_AUDIO), 2)
        return false
      }
    }
    else
    { //permission is automatically granted on sdk<23 upon installation
      Log.v(TAG, "Permission is granted")
      return true
    }
  }

  fun isStoragePermissionGranted():Boolean {
    if (Build.VERSION.SDK_INT >= 23) {
      if (this.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) === PackageManager.PERMISSION_GRANTED) {
        Log.v(TAG, "Permission is granted")
        return true
      }
      else {
        Log.v(TAG, "Permission is revoked")
        ActivityCompat.requestPermissions(this,
            arrayOf<String>(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        return false
      }
    }
    else { //permission is automatically granted on sdk<23 upon installation
      Log.v(TAG, "Permission is granted")
      return true
    }
  }

  private fun fecthAbsolutePath() = Environment.getExternalStorageDirectory().getAbsolutePath()

  private fun getTimestamp() = (System.currentTimeMillis() / 1000).toString()

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      Log.v(TAG, "Permission: " + permissions[0] + " was " + grantResults[0])
    }
  }
}