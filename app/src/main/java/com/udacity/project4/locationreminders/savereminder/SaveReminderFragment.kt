package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.PendingIntent
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startIntentSenderForResult
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(requireContext(), 10, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }
    private lateinit var geofencingClient: GeofencingClient
    private val ENABLE_LOCATION_PERMISSION=2
    private val BACKGROUND_REQUEST_CODE=50
    private val REQUEST_TURN_DEVICE_LOCATION_ON=60
    private val ENABLE_BOTH_PERMISSIONS=70
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel

        geofencingClient = LocationServices.getGeofencingClient(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription.value //
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude.value  //
            val longitude = _viewModel.longitude.value
            val radius = 300f
            val reminderItem = ReminderDataItem(title, description, location, latitude, longitude)

//           use the user entered reminder details to:
//             1) add a geofencing request
//             2) save the reminder to the local db
            if (title != null && description != null && location != null && latitude != null && longitude != null) {
                if (checkBackgroundAndForegroundPermissions()){
                    val locationSettingsResponseTask=checkDeviceLocationSettings()
                    locationSettingsResponseTask?.addOnCompleteListener {
                        if (it.isSuccessful) {
                            // device location enabled
                            addGeofence(reminderItem, radius)
                        }
                    }
                }else{
                    requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_BACKGROUND_LOCATION),ENABLE_BOTH_PERMISSIONS)
                }


            }
        }
    }



    private fun addGeofence(reminderItem: ReminderDataItem, radius: Float) {
        val geofence = Geofence.Builder()
            .setRequestId(reminderItem.id)
            .setCircularRegion(
                reminderItem.latitude!!,
                reminderItem.longitude!!,
                radius
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

//            geofencingClient.removeGeofences(geofencePendingIntent)?.run {
//                addOnCompleteListener{
                    if (ContextCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        geofencingClient.addGeofences(geofencingRequest,geofencePendingIntent)?.run {
                            addOnSuccessListener {
                                Toast.makeText(
                                    requireContext(), "geofence added",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                                Log.e("Add Geofence", geofence.requestId)
                                _viewModel.validateAndSaveReminder(reminderItem)
                            }
                            addOnFailureListener {
                                Toast.makeText(
                                    requireContext(), R.string.geofences_not_added,
                                    Toast.LENGTH_SHORT
                                ).show()
                                if ((it.message != null)) {
                                    Log.w(TAG, it.message!!)
                                }
                            }

                        }
                    }else{
                        //Q+ versions needs Background permission
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            requestPermissions(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),BACKGROUND_REQUEST_CODE)
                        }
                        Toast.makeText(requireContext(), "app need to access your location to send notification when you enter this area again\n please go to setting>app>location reminder app>permissions>Location>allow while using app", Toast.LENGTH_LONG).show()
                    }

        }



    private fun checkDeviceLocationSettings(resolve:Boolean = true): Task<LocationSettingsResponse>? {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val requestBuilder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireContext())
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(requestBuilder.build())
        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                try {
                    startIntentSenderForResult(exception.resolution.intentSender,REQUEST_TURN_DEVICE_LOCATION_ON,null,0,0,0,null)

                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(TAG, "Error getting location settings resolution: " + sendEx.message)
                }
            }
        }
        return locationSettingsResponseTask
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }

    private fun checkBackgroundAndForegroundPermissions():Boolean {
        val hasForegroundPermission = ContextCompat.checkSelfPermission(
            activity!!,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasForegroundPermission) {
            val hasBackgroundPermission = ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            if (hasBackgroundPermission) {
                return true
            } else {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    BACKGROUND_REQUEST_CODE
                )
            }
        }
        return false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == BACKGROUND_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0]!=PackageManager.PERMISSION_GRANTED){
                Toast.makeText(requireContext(), "we need background permission to add geofence", Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
            checkDeviceLocationSettings()
        }
    }

}
