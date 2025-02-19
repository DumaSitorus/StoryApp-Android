package com.duma.storyapp.view.stackwidget

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.os.bundleOf
import com.duma.storyapp.R

internal class StackRemoteViewsFactory(private val mContext: Context) : RemoteViewsService.RemoteViewsFactory {
    private val mWidgetItems = ArrayList<Bitmap>()
    override fun onDataSetChanged() {
        mWidgetItems.add(BitmapFactory.decodeResource(mContext.resources, R.drawable.story_stack_1))
        mWidgetItems.add(BitmapFactory.decodeResource(mContext.resources, R.drawable.story_stack_2))
        mWidgetItems.add(BitmapFactory.decodeResource(mContext.resources, R.drawable.story_stack_3))
        mWidgetItems.add(BitmapFactory.decodeResource(mContext.resources, R.drawable.story_stack_4))
        mWidgetItems.add(BitmapFactory.decodeResource(mContext.resources, R.drawable.story_stack_5))
    }

    override fun getCount(): Int = mWidgetItems.size

    override fun getViewAt(position: Int): RemoteViews {
        val rv = RemoteViews(mContext.packageName, R.layout.widget_item)
        rv.setImageViewBitmap(R.id.imageView, mWidgetItems[position])
        val extras = bundleOf(
            StoryAppWidget.EXTRA_ITEM to position
        )
        val fillInIntent = Intent()
        fillInIntent.putExtras(extras)

        rv.setOnClickFillInIntent(R.id.imageView, fillInIntent)
        return rv
    }
    override fun onDestroy() {}
    override fun onCreate() {}
    override fun getLoadingView(): RemoteViews? = null
    override fun getViewTypeCount(): Int = 1
    override fun getItemId(i: Int): Long = 0
    override fun hasStableIds(): Boolean = false
}