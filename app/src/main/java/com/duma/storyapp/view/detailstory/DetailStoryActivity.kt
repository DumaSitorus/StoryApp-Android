package com.duma.storyapp.view.detailstory

import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.duma.storyapp.R
import com.duma.storyapp.data.api.response.StoryResponse
import com.duma.storyapp.databinding.ActivityDetailStoryBinding
import com.duma.storyapp.di.ViewModelFactory
import com.duma.storyapp.view.util.formatDate

class DetailStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailStoryBinding
    private var storyId: String? = null
    private val viewModel by viewModels<DetailStoryViewModel> {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        checkStoryId()
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

    private fun checkStoryId() {
        storyId = intent.getStringExtra(EXTRA_STORY_ID)

        storyId?.let {
            viewModel.getDetailStory(it)
            observeViewModel()
        } ?: run {
            Toast.makeText(this, getString(R.string.detail_story_not_found), Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            showLoading(isLoading)
        }

        viewModel.detailStory.observe(this) { detailStory ->
            if (detailStory == null) {
                Toast.makeText(this, getString(R.string.detail_story_not_found), Toast.LENGTH_SHORT).show()
            } else {
                setDetailStory(detailStory)
            }
        }

        viewModel.message.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setDetailStory(story: StoryResponse) {
        binding.tvItemName.text = story.name
        binding.tvItemDescription.text = story.description
        if (!story.photoUrl.isNullOrEmpty()) {
            Glide.with(binding.ivItemPhoto.context)
                .load(story.photoUrl)
                .into(binding.ivItemPhoto)
        } else {
            binding.ivItemPhoto.scaleType = ImageView.ScaleType.FIT_CENTER
            binding.ivItemPhoto.setImageDrawable(
                ContextCompat.getDrawable(binding.ivItemPhoto.context, R.drawable.ic_image_not_supported_24)
            )
        }
        binding.tvItemCreatedAt.text = formatDate(story.createdAt)
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun setupView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    companion object {
        const val EXTRA_STORY_ID = "extra_story_id"
    }
}