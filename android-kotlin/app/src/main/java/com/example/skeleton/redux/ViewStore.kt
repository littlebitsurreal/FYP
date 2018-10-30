package com.example.skeleton.redux

import org.json.JSONException
import org.json.JSONObject
import redux.api.Reducer
import redux.api.Store

class ViewStore {
    // State
    data class State(
            val agreeTermsConditions: Boolean = false,
            val reminderOn: Boolean = false,
            val strictModeOn: Boolean = false
    )

    // Actions
    sealed class Action {
        class _PresistenceRestore(val state: State) : Action()
        class AgreeTermsConditions : Action()
        class setReminder(val on: Boolean) : Action()
        class setStrictMode(val on: Boolean) : Action()
    }

    companion object {
        private const val TAG = "ViewStore"

        // persistence
        @Throws(JSONException::class, NullPointerException::class)
        fun load(json: JSONObject?): State? {
            if (json == null) return null
            return State(
                    agreeTermsConditions = json.getBoolean("agreeTermsConditions"),
                    reminderOn = json.getBoolean("reminderOn"),
                    strictModeOn = json.getBoolean("strictModeOn")
            )
        }

        @Throws(JSONException::class, NullPointerException::class)
        fun save(state: State): JSONObject {
            return JSONObject()
                    .put("agreeTermsConditions", state.agreeTermsConditions)
                    .put("reminderOn", state.reminderOn)
                    .put("strictModeOn", state.strictModeOn)
        }

        // Reducer
        private val reducer = Reducer<State> { state, action ->
            when (action) {
                is Action._PresistenceRestore -> {
                    action.state
                }
                is Action.AgreeTermsConditions -> {
                    state.copy(agreeTermsConditions = true)
                }
                is Action.setReminder -> {
                    state.copy(reminderOn = action.on)
                }
                is Action.setStrictMode -> {
                    state.copy(strictModeOn = action.on)
                }
                else -> state
            }
        }

        fun createStore(): Store<State> {
            return redux.createStore(reducer, State())
        }
    }
}
