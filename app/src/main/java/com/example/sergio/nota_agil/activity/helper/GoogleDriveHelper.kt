package com.example.sergio.nota_agil.activity.helper

import android.os.Bundle
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient

/**
 * Created by sergio on 28/07/17.
 */

class GoogleDriveHelper: GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

  private val TAG = "GoogleDriveHelper"

  override fun onConnectionSuspended(p0: Int) {
    Log.i(TAG, "GoogleApiClient connection suspended")
  }

  override fun onConnectionFailed(result: ConnectionResult) {
    // Called whenever the API client fails to connect.
    Log.i(TAG, "GoogleApiClient connection failed: " + result.toString())
    if (!result.hasResolution()) {
      // show the localized error dialog.
//      GoogleApiAvailability.getInstance().getErrorDialog(this, result.errorCode, 0).show()
      return
    }
    // The failure has a resolution. Resolve it.
    // Called typically when the app is not yet authorized, and an
    // authorization
    // dialog is displayed to the user.
    /*try {
      result.startResolutionForResult(this, ItemActivity.REQUEST_CODE_RESOLUTION)
    } catch (e: IntentSender.SendIntentException) {
      Log.e(TAG, "Exception while starting resolution activity", e)
    }*/
  }

  override fun onConnected(p0: Bundle?) {
    Log.i(TAG, "API client connected.")
//    if (mBitmapToSave

  }

}