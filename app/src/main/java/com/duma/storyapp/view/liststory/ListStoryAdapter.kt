package com.duma.storyapp.view.liststory

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.duma.storyapp.data.api.response.ListStoryItem
import com.duma.storyapp.databinding.StoryItemBinding
import com.duma.storyapp.view.detailstory.DetailStoryActivity
import androidx.core.util.Pair
import androidx.paging.PagingDataAdapter
import com.duma.storyapp.R
import com.duma.storyapp.view.util.formatDate

class ListStoryAdapter : PagingDataAdapter<ListStoryItem, ListStoryAdapter.ListStoryViewHolder>(
    DIFF_CALLBACK
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListStoryViewHolder {
        val binding = StoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ListStoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ListStoryViewHolder, position: Int) {
        val storyItem = getItem(position)
        if (storyItem != null){
            holder.bind(storyItem)
        }
    }

    inner class ListStoryViewHolder(private val binding: StoryItemBinding) :
        RecyclerView.ViewHolder(binding.root) {


        fun bind(story: ListStoryItem) {
            binding.apply {
                if(!story.photoUrl.isNullOrEmpty()) {
                    Glide.with(itemView.context)
                        .load(story.photoUrl)
                        .into(ivItemPhoto)
                }
                else {
                    ivItemPhoto.scaleType = ImageView.ScaleType.FIT_CENTER
                    ivItemPhoto.setImageDrawable(ContextCompat.getDrawable(itemView.context, R.drawable.ic_image_not_supported_24))
                }
                tvItemName.text = story.name
                tvItemDescription.text = story.description
                tvItemCreatedAt.text = formatDate(story.createdAt)
            }

            itemView.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, DetailStoryActivity::class.java)
                intent.putExtra(DetailStoryActivity.EXTRA_STORY_ID, story.id)

                val optionsCompat: ActivityOptionsCompat =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                        itemView.context as Activity,
                        Pair(binding.ivItemPhoto, "photo"),
                        Pair(binding.tvItemName, "name"),
                        Pair(binding.tvItemDescription, "description"),
                        Pair(binding.tvItemCreatedAt, "createdAt")
                    )
                itemView.context.startActivity(intent, optionsCompat.toBundle())
            }
        }
    }

    companion object {
         val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ListStoryItem>() {
            override fun areItemsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem): Boolean {
                return oldItem.id == newItem.id
            }
            override fun areContentsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}