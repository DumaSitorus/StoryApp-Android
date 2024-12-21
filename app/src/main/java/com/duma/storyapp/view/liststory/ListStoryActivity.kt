package com.duma.storyapp.view.liststory

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.duma.storyapp.R
import com.duma.storyapp.databinding.ActivityListStoryBinding
import com.duma.storyapp.di.ViewModelFactory
import com.duma.storyapp.view.addstory.AddStoryActivity
import com.duma.storyapp.view.main.MainActivity
import com.duma.storyapp.view.main.MainViewModel
import com.duma.storyapp.view.maps.MapsActivity
import com.duma.storyapp.view.welcome.WelcomeActivity

class ListStoryActivity : AppCompatActivity() {
    private var binding: ActivityListStoryBinding? = null
    private lateinit var adapter: ListStoryAdapter

    private val mainViewModel by viewModels<MainViewModel> {
        ViewModelFactory.getInstance(this)
    }

    private val viewModelStory by viewModels<ListStoryViewModel> {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListStoryBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        checkSession()
        setupView()

        viewModelStory.getStories()
        observeViewModel()

        adapter = ListStoryAdapter()
        binding?.rvListStoryItem?.apply {
            layoutManager = LinearLayoutManager(this@ListStoryActivity)
            adapter = this@ListStoryActivity.adapter
        }
    }

    private fun observeViewModel() {
        viewModelStory.isLoading.observe(this) { isLoading ->
            showLoading(isLoading)
        }

        viewModelStory.stories.observe(this) { pagingData ->
            adapter.submitData(lifecycle, pagingData)
        }

        viewModelStory.message.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding?.progressBar?.visibility = if (isLoading) View.VISIBLE else View.GONE
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
        supportActionBar?.title = getString(R.string.list_story)

        binding?.fabAddStory?.setOnClickListener {
            startActivity(Intent(this, AddStoryActivity::class.java))
        }
    }

    private fun checkSession() {
        mainViewModel.getSession().observe(this) { user ->
            if (!user.isLogin) {
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.list_story_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_logout -> {
                val moveIntent = Intent(this@ListStoryActivity, MainActivity::class.java)
                startActivity(moveIntent)
                return true
            }
            R.id.action_language -> {
                startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
                return true
            }
            R.id.action_maps -> {
                val mapIntent = Intent(this@ListStoryActivity, MapsActivity::class.java)
                startActivity(mapIntent)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}