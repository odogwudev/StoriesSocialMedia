package com.odogwudev.storiessocialmedia

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView


class StoryAdapter(private val dataset: Array<VideoJson>, val onClick: (view: View) -> Unit) :
    RecyclerView.Adapter<StoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val linearLayout = LayoutInflater.from(parent.context).inflate(R.layout.row, parent, false)
        return StoryViewHolder(linearLayout, onClick)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        holder.bindModel(dataset[position].name)
    }

    override fun getItemCount() = dataset.size
}