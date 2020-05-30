package com.example.inspiringapps

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.inspiringapps.adapter.MainAdapter
import com.example.inspiringapps.model.Sequence
import com.example.inspiringapps.model.UserEvent
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.commons.io.FileUtils
import org.apache.commons.io.LineIterator
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.regex.Matcher
import java.util.regex.Pattern


class MainActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {

    private lateinit var filename: String
    private val url =
        "https://files.inspiringapps.com/IAChallenge/30E02AAA-B947-4D4B-8FB6-9C57C43872A9/Apache.log"

    //apache log regex from here: https://stackoverflow.com/a/30957416/1183844
    private val regex =
        "^(\\S+) (\\S+) (\\S+) \\[([\\w:/]+\\s[+\\-]\\d{4})\\] \"(\\S+) (\\S+)\\s*(\\S+)?\\s*\" (\\d{3}) (\\S+)"
    private val pattern = Pattern.compile(regex)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        refreshLayout.setOnRefreshListener(this)
        filename = filesDir.toString() + "inspiringapps_web_logs.log"

        fetchUserLogs()
    }

    private fun showResults(events: kotlin.collections.Map<Sequence, Int>) {
        //display the parsed results on the UI thread
        lifecycleScope.launch(Dispatchers.Main) {
            recycler.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = MainAdapter(events, context)
            }

            refreshLayout.isRefreshing = false
        }
    }

    private fun fetchUserLogs() {
        //download & parse the file on IO thread
        lifecycleScope.launch(Dispatchers.IO) {
            URL(url).openStream()
                .use { input ->
                    FileOutputStream(File(filename)).use { output ->
                        input.copyTo(output)
                        extractEvents()
                    }
                }
        }
    }


    private fun extractEvents() {
        val it: LineIterator = FileUtils.lineIterator(File(filename))
        try {
            val events: ArrayList<UserEvent> = ArrayList()
            while (it.hasNext()) {
                val line = it.nextLine()
                val matcher: Matcher = pattern.matcher(line)
                if (matcher.find()) {
                    events.add(UserEvent(matcher.group(1), matcher.group(6)))
                }
            }
            parse(events)
        } finally {
            it.close()
        }
    }

    /*
    so here, we want to get all of the pages that each unique user visited, in order
     */
    private fun parse(events: ArrayList<UserEvent>) {
        var eventMap = HashMap<String, ArrayList<String>>()

        //generate an ordered list of each endpoint that each user visited
        for (event in events) {
            if (eventMap.containsKey(event.ipAddress)) {
                eventMap[event.ipAddress]?.add(event.landingPage)
            } else {
                val list = ArrayList<String>()
                list.add(event.landingPage)
                eventMap[event.ipAddress] = list
            }
        }

        showResults(generateCommonSequences(eventMap))
    }

    private fun generateCommonSequences(events: HashMap<String, ArrayList<String>>): kotlin.collections.Map<Sequence, Int> {
        var sequenceCountMap = LinkedHashMap<Sequence, Int>()
        events.forEach { (_, value) ->
            for (i in 0 until value.size-2) { //offset is because we only want triples
                val sequence = Sequence(                    value[i], value[i + 1], value[i + 2]
                )
                if (sequenceCountMap.containsKey(sequence)) {
                    sequenceCountMap[sequence] = sequenceCountMap[sequence]!!.plus(1)
                } else {
                    var count = 1
                    sequenceCountMap[sequence] = count
                }
            }
        }

        return sequenceCountMap.entries.sortedByDescending { it.value }.associateBy({ it.key }, { it.value })
    }

    override fun onRefresh() {
        fetchUserLogs()
    }
}