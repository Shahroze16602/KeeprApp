package com.systematics.monetization.ui.utils


object MonetizationConditionalsState {

    private const val TAG = "MonetizationSharedStateTAG"

    private val conditionsMap: MutableMap<String, Boolean> = mutableMapOf()

    fun isConditionTrue(condition: String): Boolean {
        return conditionsMap[condition] ?: false
    }

    fun setCondition(condition: String, value: Boolean) {
        conditionsMap[condition] = value
    }
}