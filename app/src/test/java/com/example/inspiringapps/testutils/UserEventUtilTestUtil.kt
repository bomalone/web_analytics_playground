package com.example.inspiringapps.testutils

import com.example.inspiringapps.model.UserEvent

object UserEventUtilTestUtil {

    fun getUserEventList() : ArrayList<UserEvent> {
        var events = ArrayList<UserEvent>()
        events.add(UserEvent("1.2.3.4", "/a/"))
        events.add(UserEvent("1.2.3.4", "/b/"))
        events.add(UserEvent("1.2.3.255", "/a/"))
        events.add(UserEvent("1.2.3.255", "/b/"))
        events.add(UserEvent("1.2.3.4", "/c/"))
        events.add(UserEvent("1.2.3.4", "/d/"))
        events.add(UserEvent("1.2.3.255", "/a/"))
        events.add(UserEvent("1.2.3.255", "/b/"))
        events.add(UserEvent("1.2.3.4", "/a/"))
        events.add(UserEvent("1.2.3.255", "/c/"))
        events.add(UserEvent("1.2.3.255", "/d/"))
        events.add(UserEvent("1.2.3.4", "/b/"))
        events.add(UserEvent("1.2.3.255", "/a/"))
        events.add(UserEvent("1.2.3.255", "/a/"))
        events.add(UserEvent("1.2.3.4", "/d/"))
        events.add(UserEvent("1.2.3.255", "/b/"))
        events.add(UserEvent("1.2.3.255", "/d/"))

        return events
    }

    fun getUserEventListWithInvalids() : ArrayList<UserEvent> {
        var events = ArrayList<UserEvent>()
        events.add(UserEvent("1.2.3.4", "/a/"))
        events.add(UserEvent("1.2.3.4", "/b/"))
        events.add(UserEvent("1.2.3.AAA", "/a/"))
        events.add(UserEvent("1.2.3.255", "/b/"))
        events.add(UserEvent("1.2.3.4", "/c/"))
        events.add(UserEvent("1.2.3.4", "/d/"))
        events.add(UserEvent("9.9.9.9", "XXXXXXXX"))
        events.add(UserEvent("1.2.3.255", "/b/"))
        events.add(UserEvent("1.2.3.4", "/a/"))
        events.add(UserEvent("1.2.3.255", "/c/"))
        events.add(UserEvent("1.2.3.255", "/d/"))
        events.add(UserEvent("9.9.9.9", "XXXXXXXX"))
        events.add(UserEvent("1.2.3.255", "/a/"))
        events.add(UserEvent("1.2.3.255", "/a/"))
        events.add(UserEvent("9.9.9.9", "XXXXXXXX"))
        events.add(UserEvent("1.2.3.255", "/b/"))
        events.add(UserEvent("1.2.3.255", "/d/"))

        return events
    }
}