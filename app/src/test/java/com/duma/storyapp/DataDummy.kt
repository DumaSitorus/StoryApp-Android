package com.duma.storyapp

import com.duma.storyapp.data.api.response.ListStoryItem
import com.duma.storyapp.data.api.response.StoriesResponse

object DataDummy {

    fun generateDummyListStoryItems(): List<ListStoryItem> {
        val items: MutableList<ListStoryItem> = arrayListOf()
        for (i in 0..10) {
            val story = ListStoryItem(
                photoUrl = "https://example.com/photo_$i.jpg",
                createdAt = "2024-11-27T00:00:00Z",
                name = "Author $i",
                description = "Description for story $i",
                lon = 100.0 + i,
                lat = -100.0 - i,
                id = "id_$i"
            )
            items.add(story)
        }
        return items
    }

    fun generateDummyStoriesResponse(): StoriesResponse {
        return StoriesResponse(
            listStory = generateDummyListStoryItems(),
            error = false,
            message = "Stories fetched successfully"
        )
    }
}