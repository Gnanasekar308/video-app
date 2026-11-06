package com.videoplayer.app.data.local

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Simple key-value persistence. Placeholder: replace with Room for larger libraries. */
class LocalStore(context: Context) {
    private val prefs = context.getSharedPreferences("videoplayer_store", Context.MODE_PRIVATE)

    // Favorites
    fun favorites(): Set<Long> = prefs.getStringSet("favorites", emptySet())!!.mapNotNull { it.toLongOrNull() }.toSet()
    fun setFavorite(id: Long, fav: Boolean) {
        val s = prefs.getStringSet("favorites", emptySet())!!.toMutableSet()
        if (fav) s.add(id.toString()) else s.remove(id.toString())
        prefs.edit().putStringSet("favorites", s).apply()
    }

    // Resume positions + history (id -> position / last played timestamp)
    fun position(id: Long): Long = prefs.getLong("pos_$id", 0L)
    fun savePosition(id: Long, posMs: Long) {
        prefs.edit().putLong("pos_$id", posMs).putLong("ts_$id", System.currentTimeMillis()).apply()
        val ids = historyIds().toMutableList().apply { remove(id); add(0, id) }.take(200)
        prefs.edit().putString("history", ids.joinToString(",")).apply()
    }
    fun historyIds(): List<Long> = prefs.getString("history", "")!!.split(",").mapNotNull { it.toLongOrNull() }
    fun clearHistory() = prefs.edit().remove("history").apply()

    // Playlists (stored as JSON)
    fun playlists(): List<Pair<String, Pair<String, List<Long>>>> {
        val arr = JSONArray(prefs.getString("playlists", "[]"))
        return (0 until arr.length()).map {
            val o = arr.getJSONObject(it)
            val ids = o.getJSONArray("ids").let { a -> (0 until a.length()).map { i -> a.getLong(i) } }
            o.getString("id") to (o.getString("name") to ids)
        }
    }
    fun savePlaylists(list: List<Triple<String, String, List<Long>>>) {
        val arr = JSONArray()
        list.forEach { (id, name, ids) ->
            arr.put(JSONObject().put("id", id).put("name", name).put("ids", JSONArray(ids)))
        }
        prefs.edit().putString("playlists", arr.toString()).apply()
    }

    // Settings
    var subtitleScale: Float
        get() = prefs.getFloat("sub_scale", 1f)
        set(v) = prefs.edit().putFloat("sub_scale", v).apply()
    var biometricEnabled: Boolean
        get() = prefs.getBoolean("biometric", false)
        set(v) = prefs.edit().putBoolean("biometric", v).apply()
}
