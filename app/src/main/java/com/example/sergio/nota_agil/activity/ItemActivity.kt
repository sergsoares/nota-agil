package com.example.sergio.nota_agil.activity

import android.Manifest
import android.R
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.drive.Drive
import com.google.android.gms.drive.DriveApi
import com.google.android.gms.drive.DriveId
import com.google.android.gms.drive.MetadataChangeSet
import io.paperdb.Paper
import org.jetbrains.anko.*
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.io.IOException

class ItemActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

  private var CATEGORY: String = ""
  private var ITEM: String = ""
  private val TAG = "ItemActivity"
  //  private val mFileName: StringBuilder = StringBuilder()
  private var mMediaRecorder: MediaRecorder? = null
  private var mMediaPlayer: MediaPlayer? = null
  private var filesListView: ListView? = null
  private var recordButton: Button? = null
  private var stopRecordButton: Button? = null

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

    CATEGORY = intent.getStringExtra("category")
    ITEM = intent.getStringExtra("item")
    isRecordPermissionGranted()
    isStoragePermissionGranted()
    defineLayout()
    reloadAdapter()
    toast(CATEGORY)
    toast(ITEM)

  }

  private fun restoreDriveId() {
    var driveIdRestored: DriveId? = Paper.book("driveId").read(CATEGORY)
    if (driveIdRestored == null) {
      createFolderInDrive()
    } else {
      mFolderDriveId = driveIdRestored
    }
  }

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

      button {
        text = "Send last Image to Google Drive"
        onClick { saveFileToDrive() }
      }

      button {
        text = "Tirar foto"
        onClick {
          startActivityForResult(Intent(MediaStore.ACTION_IMAGE_CAPTURE),
              REQUEST_CODE_CAPTURE_IMAGE)
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

  private var audioFileName: String? = ""

  private fun Button.startRecord() {
    if (isStoragePermissionGranted() && isRecordPermissionGranted()) {
      val pmanager = this.context.getPackageManager()
      if (pmanager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE)) {
        mMediaRecorder = MediaRecorder()
        mMediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        mMediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        mMediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        //TODO: Clean mFileName to dont concat all new media recorded
//        mFileName.setLength(0)
//        mFileName.append(getTimestamp())
        audioFileName = getTimestamp() + ".3gp"
        mMediaRecorder?.setOutputFile(getCompletePath(audioFileName.toString()))
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
//    reloadAdapter()
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
        //TODO: getCorrectPathToFile
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
  }

  private fun fetchItem(): ArrayList<String> = Paper.book(CATEGORY).read(ITEM)

  private fun reloadAdapter() {
    val adapter = ArrayAdapter<String>(this, R.layout.simple_list_item_1, fetchItem())
    filesListView!!.adapter = adapter
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

  private fun fecthAbsolutePath() = Environment.getExternalStorageDirectory().getAbsolutePath()

  private fun getTimestamp() = (System.currentTimeMillis() / 1000).toString()

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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
          outputStream = FileOutputStream(path) //here is set your file path where you want to save or also here you can set file object directly

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

  private fun saveFileToDrive() {
    // Start by creating a new contents, and setting a callback.
    Log.i(TAG, "Creating new contents.")
    toast("Gravando Imagens")
    val image = mBitmapToSave

//    saveFileInDriveRootFolder(image)

    Drive.DriveApi.newDriveContents(mGoogleApiClient)
        .setResultCallback(ResultCallback<DriveApi.DriveContentsResult> { result ->
          // If the operation was not successful, we cannot do anything
          // and must
          // fail.
          if (!result.status.isSuccess) {
            Log.i(TAG, "Failed to create new contents.")
            return@ResultCallback
          }
          // Otherwise, we can write our data to the new contents.
          Log.i(TAG, "New contents created.")
          // Get an output stream for the contents.
          val outputStream = result.driveContents.outputStream
          // Write the bitmap data from it.
          val bitmapStream = ByteArrayOutputStream()
          image!!.compress(Bitmap.CompressFormat.PNG, 100, bitmapStream)
          try {
            outputStream.write(bitmapStream.toByteArray())
          } catch (e1: IOException) {
            Log.i(TAG, "Unable to write file contents.")
          }

          // Create the initial metadata - MIME type and title.
          // Note that the user will be able to change the title later.
          val metadataChangeSet = MetadataChangeSet.Builder()
//              .setMimeType("image/jpeg")
              .setTitle(imageFileName).build()
          // Create an intent for the file chooser, and start it.
//          val intentSender = Drive.DriveApi
//              .newCreateFileActivityBuilder()
//              .setInitialMetadata(metadataChangeSet)
//              .setInitialDriveContents(result.driveContents)
//              .build(mGoogleApiClient!!)

          Drive.DriveApi.getRootFolder(mGoogleApiClient!!)
              .createFile(mGoogleApiClient!!, metadataChangeSet, result.driveContents)
              .setResultCallback { toast("Gravado") }

//          try {
//            startIntentSenderForResult(
//                intentSender, ItemActivity.REQUEST_CODE_CREATOR, null, 0, 0, 0)
//            toast("Gravando Imagens")
//          } catch (e: IntentSender.SendIntentException) {
//            Log.i(TAG, "Failed to launch file chooser.")
//          }
        })
  }

  private var mFolderDriveId: DriveId? = null
  //
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
            //usa ela
          }
//
          //          saveInDriveFolder()
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
      // Create the API client and bind it to an instance variable.
      // We use this instance as the callback for connection and connection
      // failures.
      // Since no account name is passed, the user is prompted to choose.
      connectToGoogleDrive()
    } else {
      mGoogleApiClient!!.connect()
    }
//    findFolderInDrive()
//    restoreDriveId()
  }

  override fun onPause() {
    if (mGoogleApiClient != null) {
      mGoogleApiClient!!.disconnect()
    }
    super.onPause()
  }

  private var imageFileName: String = ""

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
    when (requestCode) {
      ItemActivity.REQUEST_CODE_CAPTURE_IMAGE ->
        // Called after a photo has been taken.
        if (resultCode == Activity.RESULT_OK) {
          // Store the image data as a bitmap for writing later.
          mBitmapToSave = data.extras.get("data") as Bitmap
          imageFileName = getTimestamp() + ".jpg"
          saveBitmap(mBitmapToSave, getCompletePath(imageFileName))
          saveFile(imageFileName)
          reloadAdapter()
        }
      ItemActivity.REQUEST_CODE_CREATOR ->
        // Called after a file is saved to Drive.
        if (resultCode == Activity.RESULT_OK) {
          Log.i(TAG, "Image successfully saved.")
          mBitmapToSave = null
          // Just start the camera again for another photo.
//          startActivityForResult(Intent(MediaStore.ACTION_IMAGE_CAPTURE),
//              REQUEST_CODE_CAPTURE_IMAGE)
        }
    }
  }

  override fun onConnectionFailed(result: ConnectionResult) {
    // Called whenever the API client fails to connect.
    Log.i(TAG, "GoogleApiClient connection failed: " + result.toString())
    if (!result.hasResolution()) {
      // show the localized error dialog.
      GoogleApiAvailability.getInstance().getErrorDialog(this, result.errorCode, 0).show()
      return
    }
    // The failure has a resolution. Resolve it.
    // Called typically when the app is not yet authorized, and an
    // authorization
    // dialog is displayed to the user.
    try {
      result.startResolutionForResult(this, ItemActivity.REQUEST_CODE_RESOLUTION)
    } catch (e: IntentSender.SendIntentException) {
      Log.e(TAG, "Exception while starting resolution activity", e)
    }

  }

  companion object {
    //    private val TAG = "drive-quickstart"
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

    // Connect the client. Once connected, the camera is launched.
    mGoogleApiClient!!.connect()
  }
}