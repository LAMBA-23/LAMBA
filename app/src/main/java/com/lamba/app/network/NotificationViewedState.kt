package com.lamba.app.network

object NotificationViewedState {
    fun normalizeIds(ids: Iterable<String>): Set<String> {
        return ids
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()
    }

    fun hasUnread(currentIds: Iterable<String>, viewedIds: Set<String>): Boolean {
        return normalizeIds(currentIds).any { it !in viewedIds }
    }

    fun retainActiveViewedIds(
        currentIds: Iterable<String>,
        viewedIds: Set<String>,
    ): Set<String> {
        return viewedIds intersect normalizeIds(currentIds)
    }

    fun serialize(ids: Set<String>): String {
        return ids
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .sorted()
            .joinToString(separator = "\n")
    }

    fun deserialize(value: String?): Set<String> {
        if (value.isNullOrBlank()) return emptySet()

        return value
            .lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()
    }
}
