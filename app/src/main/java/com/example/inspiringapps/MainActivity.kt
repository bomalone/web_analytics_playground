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

    /*
    Fetch logs immediately, but allow the user to swipe refresh in the event of an updated .log file
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        refreshLayout.setOnRefreshListener(this)
        filename = filesDir.toString() + "inspiringapps_web_logs.log"

        fetchUserLogs()
    }

    /*
    This updates the recycler and hides the progress bar from the swipe refresh layout
     */
    private fun showResults(events: Map<Sequence, Int>) {
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
    It would probably be best to use a repository pattern for this,
    but I wanted to use some new fancy non-okhttp to get the file and
    coroutines to handle the network/parsing/long operations
     */
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


    /*
    This gets each user's IP and the page they visited from the logs using our regular expression
    We're parsing this way simply because we aren't working with any other easily deserializable data
    like we could with Moshi/GSON. I suppose the regex is our custom deserializer.
     */
    private fun extractEvents() {
        val it: LineIterator = FileUtils.lineIterator(File(filename))
        it.use { it ->
            val events: ArrayList<UserEvent> = ArrayList()
            while (it.hasNext()) {
                val line = it.nextLine()
                val matcher: Matcher = pattern.matcher(line)
                if (matcher.find()) {
                    events.add(UserEvent(matcher.group(1), matcher.group(6)))
                }
            }
            parse(events)
        }
    }

    /*
    We want to get all of the pages that each unique user visited, in order
     */
    private fun parse(events: ArrayList<UserEvent>) {
        val eventMap = HashMap<String, ArrayList<String>>()

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

    /*
    There may be a more performant way of doing this.
    This goes through each user's ordered activity and extracts 3 page sequences,
     including overlap. For example, page visits A,B,C,D would create two sequences: A,B,C & B,C,D
     */
    private fun generateCommonSequences(events: HashMap<String, ArrayList<String>>): Map<Sequence, Int> {
        val sequenceCountMap = LinkedHashMap<Sequence, Int>()
        events.forEach { (_, value) ->
            for (i in 0 until value.size - 2) { //offset is because we only want triples
                val sequence = Sequence(
                    value[i], value[i + 1], value[i + 2]
                )
                if (sequenceCountMap.containsKey(sequence)) {
                    sequenceCountMap[sequence] = sequenceCountMap[sequence]!!.plus(1)
                } else {
                    val count = 1
                    sequenceCountMap[sequence] = count
                }
            }
        }

        return sequenceCountMap.entries.sortedByDescending { it.value }
            .associateBy({ it.key }, { it.value })
    }

    override fun onRefresh() {
        fetchUserLogs()
    }
}