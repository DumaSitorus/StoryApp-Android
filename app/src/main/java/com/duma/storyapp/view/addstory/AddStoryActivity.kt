package com.duma.storyapp.view.addstory

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.duma.storyapp.R
import com.duma.storyapp.databinding.ActivityAddStoryBinding
import com.duma.storyapp.di.ViewModelFactory
import com.duma.storyapp.view.util.getImageUri
import com.duma.storyapp.view.liststory.ListStoryActivity
import com.duma.storyapp.view.util.reduceFileImage
import com.duma.storyapp.view.util.uriToFile
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.io.File

class AddStoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddStoryBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var userLocation: Location? = null
    private var currentImageUri: Uri? = null

    private val viewModel by viewModels<AddStoryViewModel> {
        ViewModelFactory.getInstance(this)
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(this, getString(R.string.permission_granted), Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_LONG).show()
            }
        }

    private fun allPermissionsGranted() =
        ContextCompat.checkSelfPermission(
            this,
            REQUIRED_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (!allPermissionsGranted()) {
            requestPermissionLauncher.launch(REQUIRED_PERMISSION)
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding.galleryButton.setOnClickListener { startGallery() }
        binding.cameraButton.setOnClickListener { startCamera() }
        binding.checkboxIncludeLocation.setOnCheckedChangeListener{_, isChecked ->
            if (isChecked) {
                getLastKnownLocation()
            } else {
                userLocation = null
            }
        }
        binding.buttonAdd.setOnClickListener { uploadStory() }
    }

    private fun getLastKnownLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    userLocation = location
                } else {
                    Toast.makeText(this, getString(R.string.location_unavailable), Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            showImage()
        } else {
            Toast.makeText(this, getString(R.string.no_media_selected), Toast.LENGTH_SHORT).show()
        }
    }

    private fun startCamera() {
        currentImageUri = getImageUri(this)

        currentImageUri?.let {
            launcherIntentCamera.launch(it)
        } ?: run {
            showToast(getString(R.string.faild_capture_uri))
        }
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            showImage()
        } else {
            showToast(getString(R.string.faild_capture_image))
        }
    }

    private fun showImage() {
        currentImageUri?.let {
            binding.previewImageView.setImageURI(it)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("current_image_uri", currentImageUri?.toString())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val savedUri = savedInstanceState.getString("current_image_uri")
        if (savedUri != null) {
            currentImageUri = Uri.parse(savedUri)
            showImage()
        }
    }

    private fun uploadStory() {
        if (currentImageUri == null) {
            showToast(getString(R.string.empty_image_warning))
            return
        }
        val imageFile = try {
            uriToFile(currentImageUri!!, this).reduceFileImage()
        } catch (e: Exception) {
            showToast(getString(R.string.failed_image_process))
            return
        }
        val description = binding.edAddDescription.text.toString()
        val latitude = userLocation?.latitude
        val longitude = userLocation?.longitude

        observeViewModel(imageFile, description, latitude, longitude)
    }


    private fun observeViewModel(imageFile: File, description:String, latitude: Double?, longitude: Double?) {
        viewModel.isLoading.observe(this) { isLoading ->
            showLoading(isLoading)
        }
        viewModel.addNewStory(imageFile, description, latitude, longitude)
        viewModel.isError.observe(this) { isError ->
            isError?.let { error ->
                viewModel.message.observe(this) { message ->
                    if (error) {
                        showAlertDialog("Opss!", message, getString(R.string.try_again))
                    } else {
                        showAlertDialog("Yeah!", message, getString(R.string.next)) {
                            moveToList()
                        }
                    }
                }
            }
        }
    }

    private fun showAlertDialog(title: String, message: String?, buttonText: String, onPositive: (() -> Unit)? = null) {
        AlertDialog.Builder(this).apply {
            setTitle(title)
            setMessage(message)
            setPositiveButton(buttonText) { dialog, _ ->
                onPositive?.invoke()
                dialog.dismiss()
            }
            create()
            show()
        }
    }

    private fun moveToList() {
        val intent = Intent(this, ListStoryActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val REQUIRED_PERMISSION = Manifest.permission.CAMERA
    }
}