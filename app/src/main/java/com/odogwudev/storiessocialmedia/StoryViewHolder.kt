package com.odogwudev.storiessocialmedia

import android.view.View
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView


class StoryViewHolder(private val view: View, onClick: (view: View) -> Unit) :
    RecyclerView.ViewHolder(view) {

    private val buttonView: Button = this.view.findViewById(R.id.buttonView)

    init {
        buttonView.setOnClickListener(onClick)
    }

    fun bindModel(item: String) {
        this.buttonView.text = item
    }

}