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
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.drive.Drive
import com.google.android.gms.drive.DriveApi
import com.google.android.gms.drive.DriveId
import com.google.android.gms.drive.MetadataChangeSet
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
//import kotlinx.android.synthetic.main.content_scrolling.button_stop_record as buttonStopRecord
import kotlinx.android.synthetic.main.activity_scrolling.fab_take_photo as buttonTakePhoto
import kotlinx.android.synthetic.main.activity_scrolling.fab_take_notes as buttonTakeNotes
import kotlinx.android.synthetic.main.content_scrolling.list_view_files as listViewFiles
import kotlinx.android.synthetic.main.content_scrolling.sliding_tabs as tabLayout

class ItemActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

  private var CATEGORY: String = ""
  private var ITEM: String = ""
  private val TAG = "ItemActivity"
  private var mMediaRecorder: MediaRecorder? = null
  private var mMediaPlayer: MediaPlayer? = null

  private var adapter: ArrayAdapter<String>? = null

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


//    val tabLayout = findViewById(R.id.sliding_tabs) as TabLayout
    tabLayout.addTab(tabLayout.newTab().setText("Fotos"));
    tabLayout.addTab(tabLayout.newTab().setText("Áudios"));
    tabLayout.addTab(tabLayout.newTab().setText("Anotações"));

//    val fab = findViewById(com.example.sergio.nota_agil.R.id.fab) as FloatingActionButton
//    fab.setOnClickListener { view ->
//      val itemsList = fetchItem()
//      for(item in itemsList ){
//        saveFileToDrive(item)
//      }
//      Snackbar.make(view, "Sincronização iniciada", Snackbar.LENGTH_LONG)
//          .setAction("Action", null).show()
//    }

    CATEGORY = intent.getStringExtra("category")
    ITEM = intent.getStringExtra("item")

    toolbar_title.text = ITEM
//    supportActionBar?.title = ITEM

//    var mListener = AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
//      if (toolbar_layout.getHeight() + verticalOffset < 2 * ViewCompat.getMinimumHeight(toolbar_layout)) {
//        text_view_item_status.animate().alpha(1F).setDuration(600)
//      } else {
//        text_view_item_status.animate().alpha(0F).setDuration(600)
//      }
//    }
//    appBarLayout.addOnOffsetChangedListener(mListener)

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
    menuInflater.inflate(R.menu.menu_scrolling, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    val id = item.itemId
    if (id == R.id.action_settings) {
      val itemsList = fetchItem()
      for(item in itemsList ){
        saveFileToDrive(item)
      }
      toast("Sincronização iniciada")
      return true
    }
    return super.onOptionsItemSelected(item)
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

  private fun restoreDriveId() {
    var driveIdRestored: DriveId? = Paper.book("driveId").read(CATEGORY)
    if (driveIdRestored == null) {
      createFolderInDrive()
    } else {
      mFolderDriveId = driveIdRestored
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

//    adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, fetchItem())
//    listViewFiles.adapter = adapter

//    return adapter as ArrayAdapter<String>
  }

  override fun onCreateContextMenu(
      menu: ContextMenu,
      view: View,
      menuInfo: ContextMenu.ContextMenuInfo) {

    val allCategories = fetchItem()
    val info = menuInfo as AdapterView.AdapterContextMenuInfo
//    val fileClicked = allCategories[info.position]
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

  private fun fecthAbsolutePath() =  baseContext.getExternalFilesDir(null).absolutePath //Environment.getExternalStorageDirectory().getAbsolutePath()


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

  override fun onConnectionSuspended(cause: Int) {
    Log.i(TAG, "GoogleApiClient connection suspended")
  }

  private var mBitmapToSave: Bitmap? = null

  private fun saveBitmap(bitmap: Bitmap?, path: String) {
    if (bitmap != null) {
      try {
        var outputStream: FileOutputStream? = null
        try {
          outputStream = FileOutputStream(path)

          bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream) // bitmap is your Bitmap instance, if you want to compress it you can compress reduce percentage
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

  override fun onConnected(connectionHint: Bundle?) {
    Log.i(TAG, "API client connected.")
//    if (mBitmapToSave == null) {
//      // This activity has no UI of its own. Just start the camera.
//      startActivityForResult(Intent(MediaStore.ACTION_IMAGE_CAPTURE),
//          ItemActivity.REQUEST_CODE_CAPTURE_IMAGE)
//      return
//    }
//    saveFileToDrive()
  }

  private var mGoogleApiClient: GoogleApiClient? = null

  private fun saveFileToDrive(fileName: String) {
    // Start by creating a new contents, and setting a callback.
    Log.i(TAG, "Creating new contents.")
    toast("Gravando Imagens")
      val imageFile = File(getCompletePath(fileName))

    Drive.DriveApi.newDriveContents(mGoogleApiClient)
        .setResultCallback(ResultCallback<DriveApi.DriveContentsResult> { result ->

          if (!result.status.isSuccess) {
            Log.i(TAG, "Failed to create new contents.")
            return@ResultCallback
          }
          Log.i(TAG, "New contents created.")
          val outputStream = result.driveContents.outputStream

          val fileByte = FileUtils.readFileToByteArray(imageFile)

          try {
            outputStream.write(fileByte)
          } catch (e1: IOException) {
            Log.i(TAG, "Unable to write file contents.")
          }

          val metadataChangeSet = MetadataChangeSet.Builder()
              .setTitle(fileName).build()

          Drive.DriveApi.getRootFolder(mGoogleApiClient!!)
              .createFile(mGoogleApiClient!!, metadataChangeSet, result.driveContents)
              .setResultCallback { toast("Gravado" + " - "+ imageFile ) }

        })
  }

  private var mFolderDriveId: DriveId? = null

  private fun saveFileInDriveRootFolder(image: Bitmap?) {
//
//    val saveInDriveFolder = {
//      Drive.DriveApi.newDriveContents(mGoogleApiClient)
//          .setResultCallback({ result ->
//            val folder = mFolderDriveId!!.asDriveFolder();
//            val outputStream = result.driveContents.outputStream
//            val bitmapStream = ByteArrayOutputStream()
//            image!!.compress(Bitmap.CompressFormat.PNG, 100, bitmapStream)
//            try {
//              outputStream.write(bitmapStream.toByteArray())
//            } catch (e1: IOException) {
//              Log.i(TAG, "Unable to write file contents.")
//            }
//
//            val metadataChangeSet = MetadataChangeSet.Builder()
//                .setMimeType("image/jpeg").setTitle("Android Photo.jpg").build()
//
//            folder.createFile(mGoogleApiClient!!, metadataChangeSet, result.driveContents)
//                .setResultCallback { toast("Gravado") }
//          })
//    }

//  findFolderInDrive()

  }

  private fun findFolderInDrive() {
    Drive.DriveApi.fetchDriveId(mGoogleApiClient, CATEGORY)
        .setResultCallback {
          if (!it.getStatus().isSuccess()) {
            toast("Pasta não encontrada");
            createFolderInDrive()
          } else {
          }
        }
  }

  private fun createFolderInDrive() {
    val changeSet = MetadataChangeSet.Builder()
        .setTitle(CATEGORY).build()

    Drive.DriveApi.getRootFolder(mGoogleApiClient).createFolder(
        mGoogleApiClient, changeSet).setResultCallback {
      toast("Pasta Criada")
      Paper.book("driveId").write(CATEGORY, it.driveFolder.driveId)
      mFolderDriveId = it.driveFolder.driveId
    }
  }


  override fun onResume() {
    super.onResume()
    if (mGoogleApiClient == null) {
      connectToGoogleDrive()
    } else {
      mGoogleApiClient!!.connect()
    }
  }

  override fun onPause() {
    if (mGoogleApiClient != null) {
      mGoogleApiClient!!.disconnect()
    }
    super.onPause()
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

  override fun onConnectionFailed(result: ConnectionResult) {
//    // Called whenever the API client fails to connect.
//    Log.i(TAG, "GoogleApiClient connection failed: " + result.toString())
//    if (!result.hasResolution()) {
//      // show the localized error dialog.
//      GoogleApiAvailability.getInstance().getErrorDialog(this, result.errorCode, 0).show()
//      returnv3
//    }
//    // The failure has a resolution. Resolve it.
//    // Called typically when the app is not yet authorized, and an
//    // authorization
//    // dialog is displayed to the user.
//    try {
//      result.startResolutionForResult(this, ItemActivity.REQUEST_CODE_RESOLUTION)
//    } catch (e: IntentSender.SendIntentException) {
//      Log.e(TAG, "Exception while starting resolution activity", e)
//    }

  }

  companion object {
    private val REQUEST_CODE_CAPTURE_IMAGE = 1
    private val REQUEST_CODE_CREATOR = 2
    private val REQUEST_CODE_RESOLUTION = 3
  }

  private fun connectToGoogleDrive() {
    mGoogleApiClient = GoogleApiClient.Builder(this)
        .addApi(Drive.API)
        .addScope(Drive.SCOPE_FILE)
        .addConnectionCallbacks(this)
        .addOnConnectionFailedListener(this)
        .build()

    mGoogleApiClient!!.connect()
  }
}