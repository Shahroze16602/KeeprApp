package com.systematics.monetization.ui.exceptions

open class AdShowException(message: String = "") : Exception(message)
open class MonetizationDisabledException(message: String = "Monetization library disabled") :
    Exception(message)

class AdPlacementNotFoundException(message: String) : AdShowException(message)
class AdPlacementNotAllowedException(message: String) : AdShowException(message)
class AdInstantLoadTimeoutException(message: String) : AdShowException(message)
class AdInstantLoadNoInternetException(message: String) : AdShowException(message)
class AdNotPreloadedException(message: String) : AdShowException(message)
class AdCounterShortException(message: String) : AdShowException(message)
class AdTimerShortException(message: String) : AdShowException(message)
