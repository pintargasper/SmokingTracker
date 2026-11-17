package com.gasperpintar.smokingtracker.type

enum class StartPage {
    HOME,
    ADVANCED,
    GRAPH;

    companion object {
        fun fromName(name: String?): StartPage {
            return StartPage.entries.firstOrNull {
                it.name.equals(other = name, ignoreCase = true)
            } ?: HOME
        }
    }
}