package com.example.inspiringapps.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.inspiringapps.R
import com.example.inspiringapps.model.Sequence


class MainAdapter(private val resultsMap: Map<Sequence, Int>) :
    RecyclerView.Adapter<MainAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(resultsMap.keys.elementAt(position), resultsMap.values.elementAt(position))
    }

    override fun getItemCount(): Int = resultsMap.keys.size

    class ViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.item_user_event, parent, false)) {
        private var countTextView: TextView? = null
        private var event1TextView: TextView? = null
        private var event2TextView: TextView? = null
        private var event3TextView: TextView? = null

        init {
            event1TextView = itemView.findViewById(R.id.event1)
            event2TextView = itemView.findViewById(R.id.event2)
            event3TextView = itemView.findViewById(R.id.event3)
            countTextView = itemView.findViewById(R.id.count)
        }

        fun bind(sequence: Sequence, count: Int) {
            event1TextView?.text = sequence.event1
            event2TextView?.text = sequence.event2
            event3TextView?.text = sequence.event3
            countTextView?.text = countTextView?.context?.getString(R.string.count, count.toString())
        }
    }
}
