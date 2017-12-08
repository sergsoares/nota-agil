package com.example.sergio.nota_agil.activity.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.provider.MediaStore
import android.support.design.widget.Snackbar
import android.support.design.widget.TabLayout
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import com.example.sergio.nota_agil.R
import com.google.common.base.Predicates
import com.google.common.collect.Collections2
import com.google.common.collect.Lists
import io.paperdb.Paper
import kotlinx.android.synthetic.main.activity_scrolling.*
import org.apache.commons.io.FileUtils
import org.jetbrains.anko.onItemClick
import org.jetbrains.anko.toast
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlinx.android.synthetic.main.activity_scrolling.app_bar as appBarLayout
import kotlinx.android.synthetic.main.activity_scrolling.fab_record_audio as buttonRecord
import kotlinx.android.synthetic.main.activity_scrolling.fab_take_photo as buttonTakePhoto
import kotlinx.android.synthetic.main.activity_scrolling.fab_take_notes as buttonTakeNotes
import kotlinx.android.synthetic.main.content_scrolling.list_view_files as listViewFiles
import kotlinx.android.synthetic.main.content_scrolling.sliding_tabs as tabLayout

class ItemActivity : AppCompatActivity() {

  private var CATEGORY: String = ""
  private var ITEM: String = ""
  private val TAG = "ItemActivity"
  private var mMediaRecorder: MediaRecorder? = null
  private var mMediaPlayer: MediaPlayer? = null

  public override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    if (Build.VERSION.SDK_INT >= 24) {
      try {
        val m = StrictMode::class.java.getMethod("disableDeathOnFileUriExposure")
        m.invoke(null)
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }

    setContentView(com.example.sergio.nota_agil.R.layout.activity_scrolling)

    val toolbar = findViewById(R.id.toolbar) as Toolbar
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true);

    tabLayout.addTab(tabLayout.newTab().setText("Fotos"));
    tabLayout.addTab(tabLayout.newTab().setText("Áudios"));
    tabLayout.addTab(tabLayout.newTab().setText("Anotações"));

    CATEGORY = intent.getStringExtra("category")
    ITEM = intent.getStringExtra("item")

    toolbar_title.text = ITEM

    isRecordPermissionGranted()
    isStoragePermissionGranted()
    reloadAdapter()
    setListeners()
    registerForContextMenu(listViewFiles)
    saveLastItemVisited()

    tabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
      override fun onTabSelected(tab: TabLayout.Tab){
        defineFilterByPosition(tab.position)
      }
      override fun onTabUnselected(tab: TabLayout.Tab){  }
      override fun onTabReselected(tab: TabLayout.Tab){ }
    })

  }

  private fun defineFilterByPosition(id: Int) {
    val filesList = fetchItem()

    val jpgFiltered = Lists.newArrayList(Collections2.filter(filesList,
            Predicates.containsPattern(".jpg")))

    val audioFiltered = Lists.newArrayList(Collections2.filter(filesList,
            Predicates.containsPattern(".3gp")))

    val textFiltered = Lists.newArrayList(Collections2.filter(filesList,
            Predicates.containsPattern(".txt")))

    if (id === 0) {
      filterContentBy(jpgFiltered)
    } else if (id === 1) {
      filterContentBy(audioFiltered)
    } else if (id === 2) {
      filterContentBy(textFiltered)
    }
  }

  private fun filterContentBy(pattern: java.util.ArrayList<String>) {
    listViewFiles.adapter = ArrayAdapter<String>(baseContext, android.R.layout.simple_list_item_1, pattern)
  }

  override fun onSupportNavigateUp(): Boolean {
    finish()
    return true
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
//    menuInflater.inflate(R.menu.menu_scrolling, menu)
    return true
  }

  private fun saveLastItemVisited() {
    val settings = getSharedPreferences("PREFERENCES", 0)
    val editor = settings.edit()

    editor.putString("CATEGORY", CATEGORY)
    editor.putString("ITEM", ITEM)
    editor.commit()
  }

  private fun setListeners() {
    buttonRecord.setOnTouchListener(object: View.OnTouchListener {
      override fun onTouch(view:View, event: MotionEvent):Boolean {
        when (event.action) {
          MotionEvent.ACTION_DOWN -> {
            startRecord()
            Snackbar.make(view, "Gravação iniciada.", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
          }
          MotionEvent.ACTION_UP -> {
            stopRecord()
            Snackbar.make(view, "Gravação encerrada.", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
          }
        }
        return false
      }
    })

    buttonTakePhoto.setOnClickListener { view ->
      Snackbar.make(view, "Camera Iniciada.", Snackbar.LENGTH_LONG)
          .setAction("Action", null).show()
      startActivityForResult(Intent(MediaStore.ACTION_IMAGE_CAPTURE),
              REQUEST_CODE_CAPTURE_IMAGE)
    }

    buttonTakeNotes.setOnClickListener { view ->
      val input = EditText(this)
      input.minLines = 5

      AlertDialog.Builder(this)
          .setView(input)
          .setTitle("Insira anotação")
          .setPositiveButton("OK") { _, _ ->
            val newText = input.text.toString()
            val txtFileName = getTimestamp() + ".txt"
            val path = getCompletePath(txtFileName)
            val file = File(path)
            FileUtils.writeStringToFile(file, newText)
            saveFile(txtFileName)
            reloadAdapter()
          }.show()

    }

    listViewFiles.onItemClick { adapterView, view, i, l ->
      val fileName = listViewFiles.adapter.getItem(i).toString()
      executeMedia(fileName)
    }


  }

  private var audioFileName: String? = ""

  private fun startRecord() {
    if (isStoragePermissionGranted() && isRecordPermissionGranted()) {
      val pmanager = this.applicationContext.packageManager
      if (pmanager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE)) {
        mMediaRecorder = MediaRecorder()
        mMediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        mMediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        mMediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        audioFileName = getTimestamp() + ".3gp"
        mMediaRecorder?.setOutputFile(getCompletePath(audioFileName.toString()))
        try {
          Log.e(TAG, "Start recording")
          mMediaRecorder?.prepare()
          mMediaRecorder?.start()
        } catch (e: IOException) {
          Log.e(TAG, "prepare() failed")
        }

        Log.e(TAG, "File is in " + getCompletePath(audioFileName.toString()))
      }
    }
  }

  private fun getCompletePath(itemName: String) = fecthAbsolutePath() + "/" + itemName

  private fun stopRecord() {
    mMediaRecorder?.stop()
    saveFile(audioFileName.toString())
    reloadAdapter()
  }

  private fun saveFile(fileName: String) {
    val itemTemp = fetchItem()
    itemTemp.add(fileName)
    Paper.book(CATEGORY).write(ITEM, itemTemp)
  }

  private fun executeMedia(itemName: String) {

    toast(itemName)
    if (itemName.endsWith(".3gp")) {
      try {
        mMediaPlayer = MediaPlayer()
        mMediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
        mMediaPlayer?.setDataSource(getCompletePath(itemName))
        mMediaPlayer?.prepare()
        mMediaPlayer?.start()
      } catch (e: IOException) {
        e.printStackTrace()
      }
    }

    if (itemName.endsWith(".jpg")) {
      try {
        val intent = Intent()
        intent.action = android.content.Intent.ACTION_VIEW
        val uri = Uri.parse("file://" + getCompletePath(itemName))
        intent.setDataAndType(uri, "image/*")
        startActivity(intent)
      } catch (e: IOException) {
        e.printStackTrace()
      }
    }

    if (itemName.endsWith(".txt")) {
      val path = getCompletePath(itemName)
      val file = File(path)
      val input = EditText(this)
      input.minLines = 5
      input.text.insert(0, FileUtils.readFileToString(file))

      AlertDialog.Builder(this)
          .setView(input)
          .setTitle("Alterações na nota")
          .setPositiveButton("OK") { _, _ ->

            val allCategories = fetchItem()
            allCategories.remove(itemName)
            Paper.book(CATEGORY).write(ITEM, allCategories)
            val newText = input.text.toString()
            FileUtils.forceDelete(file)
            FileUtils.writeStringToFile(file, newText)
            saveFile(itemName)
            reloadAdapter()

          }.setNegativeButton("Cancelar") { _, _ ->}.show()
    }
  }


  private fun fetchItem(): ArrayList<String> = Paper.book(CATEGORY).read(ITEM)

  private fun reloadAdapter(){
    defineFilterByPosition(tabLayout.selectedTabPosition)
  }

  override fun onCreateContextMenu(
      menu: ContextMenu,
      view: View,
      menuInfo: ContextMenu.ContextMenuInfo) {

    val allCategories = fetchItem()
    val info = menuInfo as AdapterView.AdapterContextMenuInfo
    val fileClicked = listViewFiles.getItemAtPosition(info.position).toString()


    menu.add("Deletar").setOnMenuItemClickListener {

      allCategories.remove(fileClicked)
      Paper.book(CATEGORY).write(ITEM, allCategories)
      val file = File(getCompletePath(fileClicked))

      if (file.delete()) {
        toast(file.name + " foi Deletado!")
      } else {
        toast("Delete operation is failed.")
      }
      reloadAdapter()
      false
    }

    menu.add("Compartilhar").setOnMenuItemClickListener {

      fileClicked

      val sendIntent = Intent()
      sendIntent.action = Intent.ACTION_SEND
      sendIntent.putExtra(Intent.EXTRA_TEXT, "This is my text to send.")
      sendIntent.type = "text/plain"
      startActivity(Intent.createChooser(sendIntent, "Envie seus dados."))
      false
    }

    menu.add("Renomear").setOnMenuItemClickListener {
      val input = EditText(this)

      var type: String = ""
      if (fileClicked.endsWith(".3gp")) {
        type = ".3gp"
      }
      if (fileClicked.endsWith(".jpg")) {
        type = ".jpg"
      }
      if (fileClicked.endsWith(".txt")) {
        type = ".txt"
      }

      AlertDialog.Builder(this)
          .setView(input)
          .setTitle("Insira novo nome")
          .setPositiveButton("OK") { _ , _ ->


            val newFileName = input.text.toString().plus(type)

            if(newFileName.isEmpty() || allCategories.contains(newFileName)){
              toast("Arquivo já existe renomeado.")
              return@setPositiveButton
            }

            val oldFile = File(getCompletePath(fileClicked))
            val newFile = File(getCompletePath(newFileName))
            Log.e(TAG, "OldFile is in " + getCompletePath(fileClicked))
            Log.e(TAG, "NewFile is in " + getCompletePath(newFileName))

            if (oldFile.renameTo(newFile)){
              allCategories.remove(fileClicked)
              allCategories.add(newFileName)
              toast("Arquivo Renomeado.")
            } else {
              toast("Arquivo não pode ser renomeado.");
            }

            Paper.book(CATEGORY).write(ITEM, allCategories)
            reloadAdapter()
          }.show();

      false
    }

  }

  fun isRecordPermissionGranted(): Boolean {
    if (Build.VERSION.SDK_INT >= 23) {
      if (this.checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) === PackageManager.PERMISSION_GRANTED) {
        Log.v(TAG, "Permission is granted")
        return true
      } else {
        Log.v(TAG, "Permission is revoked")
        ActivityCompat.requestPermissions(this,
            arrayOf<String>(Manifest.permission.RECORD_AUDIO), 2)
        return false
      }
    } else { //permission is automatically granted on sdk<23 upon installation
      Log.v(TAG, "Permission is granted")
      return true
    }
  }

  fun isStoragePermissionGranted(): Boolean {
    if (Build.VERSION.SDK_INT >= 23) {
      if (this.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) === PackageManager.PERMISSION_GRANTED) {
        Log.v(TAG, "Permission is granted")
        return true
      } else {
        Log.v(TAG, "Permission is revoked")
        ActivityCompat.requestPermissions(this,
            arrayOf<String>(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        return false
      }
    } else { //permission is automatically granted on sdk<23 upon installation
      Log.v(TAG, "Permission is granted")
      return true
    }
  }

  //Environment.getExternalStorageDirectory().getAbsolutePath()
  private fun fecthAbsolutePath() =  baseContext.getExternalFilesDir(null).absolutePath

  private fun getTimestamp(): String {
    val time = (System.currentTimeMillis() / 10).toString()
    return ITEM.substring(0,1) + time.substring(time.length - 4)
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (grantResults?.get(0) == PackageManager.PERMISSION_GRANTED) {
      Log.v(TAG, "Permission: " + permissions[0] + " was " + grantResults[0])
    }
    }

  private var mBitmapToSave: Bitmap? = null

  private fun saveBitmap(bitmap: Bitmap?, path: String) {
    if (bitmap != null) {
      try {
        var outputStream: FileOutputStream? = null
        try {
          outputStream = FileOutputStream(path)

          bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
          // bitmap is your Bitmap instance, if you want to compress it you can compress reduce percentage
          // PNG is a lossless format, the compression factor (100) is ignored
        } catch (e: Exception) {
          e.printStackTrace()
        } finally {
          try {
            if (outputStream != null) {
              outputStream!!.close()
            }
          } catch (e: IOException) {
            e.printStackTrace()
          }

        }
      } catch (e: Exception) {
        e.printStackTrace()
      }

    }
  }

  private var imageFileName: String = ""

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

    when (requestCode) {
      REQUEST_CODE_CAPTURE_IMAGE ->
        if (resultCode == Activity.RESULT_OK) {
          mBitmapToSave = data?.extras?.get("data") as Bitmap
          imageFileName = getTimestamp() + ".jpg"
          saveBitmap(mBitmapToSave, getCompletePath(imageFileName))
          saveFile(imageFileName)
          reloadAdapter()
        }
      REQUEST_CODE_CREATOR ->
        if (resultCode == Activity.RESULT_OK) {
          Log.i(TAG, "Image successfully saved.")
          mBitmapToSave = null
        }
    }
  }

  companion object {
    private val REQUEST_CODE_CAPTURE_IMAGE = 1
    private val REQUEST_CODE_CREATOR = 2
    private val REQUEST_CODE_RESOLUTION = 3
  }

}