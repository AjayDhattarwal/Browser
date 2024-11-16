package com.ar.idm.data.local.preferences

import android.content.Context

class PausePreferencesManager(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("download_prefs", Context.MODE_PRIVATE)

    fun isPaused(workerId: String): Boolean {
        return getPausedWorkers().contains(workerId)
    }

    fun pauseWorker(workerId: String) {
        val pausedWorkers = getPausedWorkers().toMutableSet()
        pausedWorkers.add(workerId)
        savePausedWorkers(pausedWorkers)
    }

    fun resumeWorker(workerId: String) {
        val pausedWorkers = getPausedWorkers().toMutableSet()
        pausedWorkers.remove(workerId)
        savePausedWorkers(pausedWorkers)
    }

    fun togglePause(workerId: String) {
        if (isPaused(workerId)) {
            resumeWorker(workerId)
        } else {
            pauseWorker(workerId)
        }
    }

    private fun getPausedWorkers(): Set<String> {
        return sharedPreferences.getStringSet("paused_workers", emptySet()) ?: emptySet()
    }

    private fun savePausedWorkers(workers: Set<String>) {
        sharedPreferences.edit().putStringSet("paused_workers", workers).apply()
    }


}
