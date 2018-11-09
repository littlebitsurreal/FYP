package com.example.skeleton.redux

import redux.api.Reducer
import redux.api.Store

@Suppress("unused")
class ClientStore {
    // State
    data class State(
        val example: Int = 1
    )
    // Actions
    sealed class Action
    companion object {
        // Reducer
        private val reducer = Reducer<State> { state, action ->
            when (action) {
                else -> state
            }
        }
        fun createStore(): Store<State> {
            return redux.createStore(reducer, State())
        }
    }
}
