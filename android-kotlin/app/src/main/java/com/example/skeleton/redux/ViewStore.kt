package com.example.skeleton.redux

import org.json.JSONException
import org.json.JSONObject
import redux.api.Reducer
import redux.api.Store

class ViewStore {
    // State
    data class State(
            val agreeTermsConditions: Boolean = false
    )

    // Actions
    sealed class Action {
        class _PresistenceRestore(val state: State) : Action()
        class AgreeTermsConditions : Action()
    }

    companion object {
        private const val TAG = "ViewStore"

        // persistence
        @Throws(JSONException::class, NullPointerException::class)
        fun load(json: JSONObject?): State? {
            if (json == null) return null
            return State(
                    agreeTermsConditions = json.getBoolean("agreeTermsConditions")
            )
        }

        @Throws(JSONException::class, NullPointerException::class)
        fun save(state: State): JSONObject {
            return JSONObject()
                    .put("agreeTermsConditions", state.agreeTermsConditions)
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
                else -> state
            }
        }

        fun createStore(): Store<State> {
            return redux.createStore(reducer, State())
        }
    }
}
