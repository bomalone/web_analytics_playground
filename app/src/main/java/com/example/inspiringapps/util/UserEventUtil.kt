package com.example.inspiringapps.util

import android.content.Context
import com.example.inspiringapps.model.Sequence
import com.example.inspiringapps.model.UserEvent
import org.apache.commons.io.FileUtils
import org.apache.commons.io.LineIterator
import java.io.File
import java.util.regex.Matcher
import java.util.regex.Pattern

object UserEventUtil {

    //apache log regex from here: https://stackoverflow.com/a/30957416/1183844
    private val regex =
        "^(\\S+) (\\S+) (\\S+) \\[([\\w:/]+\\s[+\\-]\\d{4})] \"(\\S+) (\\S+)\\s*(\\S+)?\\s*\" (\\d{3}) (\\S+)"
    private val pattern = Pattern.compile(regex)

    const val filename = "inspiringapps_web_logs.log"
    const val url =
        "https://files.inspiringapps.com/IAChallenge/30E02AAA-B947-4D4B-8FB6-9C57C43872A9/Apache.log"
    /*
   This gets each user's IP and the page they visited from the logs using our regular expression
   We're parsing this way simply because we aren't working with any other easily deserializable data
   like we could with Moshi/GSON. I suppose the regex is our custom deserializer.
    */
    fun extractEvents(context: Context) : Map<Sequence, Int> {
        val it: LineIterator = FileUtils.lineIterator(File(context.filesDir.toString() + filename
        ))
        it.use { _ ->
            val events: ArrayList<UserEvent> = ArrayList()
            while (it.hasNext()) {
                val line = it.nextLine()
                val matcher: Matcher = pattern.matcher(line)
                if (matcher.find()) {
                    events.add(UserEvent(matcher.group(1), matcher.group(6)))
                }
            }
            return parseLogFile(events)
        }
    }

    /*
   There may be a more performant way of doing this.
   This goes through each user's ordered activity and extracts 3 page sequences,
    including overlap. For example, page visits A,B,C,D would create two sequences: A,B,C & B,C,D
    */
    private fun generateCommonSequences(events: HashMap<String, ArrayList<String>>): Map<Sequence, Int> {
        val sequenceCountMap = LinkedHashMap<Sequence, Int>()
        events.forEach { (_, value) ->
            for (i in 0 until value.size - 2) { // -2 offset is because we only want triples
                val sequence = Sequence(
                    //grab the user's first, second, and third page visit
                    value[i], value[i + 1], value[i + 2]
                )
                if (sequenceCountMap.containsKey(sequence)) {
                    sequenceCountMap[sequence] = sequenceCountMap[sequence]!!.plus(1)
                } else {
                    //first sequence of this type
                    sequenceCountMap[sequence] = 1
                }
            }
        }

        return sequenceCountMap.entries.sortedByDescending { it.value }
            .associateBy({ it.key }, { it.value })
    }

    /*
   We want to get all of the pages that each unique user visited, in order
    */
    private fun parseLogFile(events: ArrayList<UserEvent>) : Map<Sequence, Int> {
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

        return generateCommonSequences(eventMap)
    }



}