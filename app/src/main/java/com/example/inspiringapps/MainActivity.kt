package com.example.inspiringapps

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.inspiringapps.adapter.MainAdapter
import com.example.inspiringapps.model.Sequence
import com.example.inspiringapps.util.UserEventUtil
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class MainActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {

    /*
    Fetch logs immediately, but allow the user to swipe refresh in the event of an updated .log file
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        refreshLayout.setOnRefreshListener(this)

        fetchUserLogs()
    }

    /*
    This updates the recycler and hides the progress bar from the swipe refresh layout
     */
    private fun updateView(events: Map<Sequence, Int>) {
        //display the parsed results on the UI thread
        lifecycleScope.launch(Dispatchers.Main) {
            recycler.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = MainAdapter(events)
            }
            refreshLayout.isRefreshing = false
        }
    }

    /*
    fetch and then parse/sort on an IO thread
     */
    private fun fetchUserLogs() {
        //download & parse the file on IO thread
        lifecycleScope.launch(Dispatchers.IO) {
            URL(UserEventUtil.url).openStream()
                .use { input ->
                    FileOutputStream(File(filesDir.toString() + UserEventUtil.filename)).use { output ->
                        //save the file to disk, it will be scanned line by line
                        input.copyTo(output)

                        //UserEventUtil handles the heavy lifting on a background thread
                        updateView(UserEventUtil.extractEvents(this@MainActivity))
                    }
                }
        }
    }

    override fun onRefresh() {
        fetchUserLogs()
    }
}