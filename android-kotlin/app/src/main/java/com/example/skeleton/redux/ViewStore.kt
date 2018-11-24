package com.example.skeleton.redux

import org.json.JSONException
import org.json.JSONObject
import redux.api.Reducer
import redux.api.Store

class ViewStore {
    // State
    data class State(
            val agreeTermsConditions: Boolean = false,
            val isReminderOn: Boolean = false,
            val isStrictModeOn: Boolean = false,
            val foregroundOn: Boolean = false,
            val usageLimit: Int = 30,
            val isNotTrackingListUpdating: Boolean = false
    )

    // Actions
    sealed class Action {
        class _PresistenceRestore(val state: State) : Action()
        class AgreeTermsConditions : Action()
        class SetReminder(val on: Boolean) : Action()
        class SetStrictMode(val on: Boolean) : Action()
        class SetForeground(val on: Boolean) : Action()
        class SetUsageLimit(val time: Int) : Action()
        class UpdateNotTrackingListBegin : Action()
        class UpdateNotTrackingListComplete : Action()
    }

    companion object {
        // persistence
        @Throws(JSONException::class, NullPointerException::class)
        fun load(json: JSONObject?): State? {
            if (json == null) return null
            return State(
                    agreeTermsConditions = json.getBoolean("agreeTermsConditions"),
                    isReminderOn = json.getBoolean("isReminderOn"),
                    isStrictModeOn = json.getBoolean("isStrictModeOn"),
                    usageLimit = json.getInt("usageLimit")
            )
        }

        @Throws(JSONException::class, NullPointerException::class)
        fun save(state: State): JSONObject {
            return JSONObject()
                    .put("agreeTermsConditions", state.agreeTermsConditions)
                    .put("isReminderOn", state.isReminderOn)
                    .put("isStrictModeOn", state.isStrictModeOn)
                    .put("usageLimit", state.usageLimit)
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
                is Action.SetReminder -> {
                    state.copy(isReminderOn = action.on)
                }
                is Action.SetStrictMode -> {
                    state.copy(isStrictModeOn = action.on)
                }
                is Action.SetForeground -> {
                    state.copy(foregroundOn = action.on)
                }
                is Action.SetUsageLimit -> {
                    state.copy(usageLimit = action.time)
                }
                is Action.UpdateNotTrackingListBegin -> {
                    state.copy(isNotTrackingListUpdating = true)
                }
                is Action.UpdateNotTrackingListComplete -> {
                    state.copy(isNotTrackingListUpdating = false)
                }
                else -> state
            }
        }

        fun createStore(): Store<State> {
            return redux.createStore(reducer, State())
        }
    }
}
