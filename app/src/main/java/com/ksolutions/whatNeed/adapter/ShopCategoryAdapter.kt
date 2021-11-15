package com.ksolutions.whatNeed.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.ksolutions.whatNeed.R

class shopCategoryAdapter(ctx: Context,
                       moods: List<String>) :
    ArrayAdapter<String>(ctx, 0, moods) {

    override fun getView(position: Int, recycledView: View?, parent: ViewGroup): View {
        return this.createView(position, recycledView, parent)
    }

    override fun getDropDownView(position: Int, recycledView: View?, parent: ViewGroup): View {
        return this.createView(position, recycledView, parent)
    }

    private fun createView(position: Int, recycledView: View?, parent: ViewGroup): View {

        val mood = getItem(position)

        val view = recycledView ?: LayoutInflater.from(context).inflate(
            R.layout.shop_category_items,
            parent,
            false
        )
        return view
    }
}