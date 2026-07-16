package com.Pamneuroncraft.jobapplicationtracker.ui.navigation

import androidx.navigation3.runtime.NavKey

class Navigator(val state: NavigationState) {
    fun navigate(route: NavKey) {
        if (route in state.backStacks.keys) {
            state.topLevelRoute = route
        } else {
            state.backStacks[state.topLevelRoute]?.add(route)
        }
    }

    fun goBack() {
        val currentStack = state.backStacks[state.topLevelRoute]
            ?: error("Stack for ${state.topLevelRoute} not found")
        
        if (currentStack.size > 1) {
            currentStack.removeAt(currentStack.size - 1)
        } else if (state.topLevelRoute != state.startRoute) {
            state.topLevelRoute = state.startRoute
        }
    }
}
