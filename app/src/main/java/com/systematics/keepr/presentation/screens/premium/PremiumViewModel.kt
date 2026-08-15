package com.systematics.keepr.presentation.screens.premium

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.systematics.billing.core.domain.model.PremiumOffer
import com.systematics.billing.core.domain.usecase.BuyPremiumOfferUseCase
import com.systematics.billing.core.domain.usecase.QueryPremiumOffersUseCase
import com.systematics.billing.core.utils.ResultResource
import com.systematics.keepr.domain.model.PremiumOfferType
import com.systematics.keepr.domain.usecase.GetPremiumOfferIdUseCase
import com.systematics.keepr.presentation.screens.premium.event.PremiumEvents
import com.systematics.keepr.presentation.screens.premium.state.PremiumUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PremiumViewModel(
    private val getPremiumOfferId: GetPremiumOfferIdUseCase,
    private val queryPremiumOffers: QueryPremiumOffersUseCase,
    private val buyPremiumOffer: BuyPremiumOfferUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(PremiumUiState())
    val state = _state.asStateFlow()

    private var loadOffersJob: Job? = null

    init {
        loadOffers()
    }

    private fun loadOffers(onLoaded: ((PremiumOffer?) -> Unit)? = null) {
        loadOffersJob?.cancel()
        loadOffersJob = viewModelScope.launch {
            queryPremiumOffers.invoke(
                subscriptionIds = listOf(getPremiumOfferId(PremiumOfferType.PREMIUM_1)),
                productIds = listOf()
            ).collectLatest { latestState ->
                Log.d(TAG, "loadOffers: ${latestState.data?.map { it.id }}")
                when (latestState) {
                    is ResultResource.Loading -> _state.update {
                        it.copy(isLoading = true, errorMessage = null)
                    }

                    is ResultResource.Error -> _state.update {
                        it.copy(isLoading = false, errorMessage = latestState.error)
                    }

                    is ResultResource.Success -> {
                        val offers = latestState.data ?: listOf()
                        _state.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = null,
                                premiumOffers = offers,
                                selectedOffer = offers.firstOrNull()
                            )
                        }
                        onLoaded?.invoke(offers.firstOrNull())
                    }

                    is ResultResource.Idle -> Unit
                }
            }
        }
    }

    fun onEvent(events: PremiumEvents) {
        when (events) {
            is PremiumEvents.PremiumBuyRequested -> handlePremiumBuyRequested(
                events.activity,
                events.onDone
            )

            is PremiumEvents.OnPremiumOfferSelected -> handleOnPremiumOfferSelected(events.premiumOffer)

            is PremiumEvents.OnPremiumPurchaseTarget -> {
                handleOnPremiumPurchaseTarget(
                    events.activity,
                    events.premiumTarget,
                    events.onDone
                )
            }
        }
    }

    private fun handlePremiumBuyRequested(activity: Activity, onDone: (success: Boolean) -> Unit) {
        val offer = _state.value.selectedOffer
        if (offer != null) {
            buy(activity, offer, onDone)
        } else {
            loadOffers(onLoaded = { loaded -> loaded?.let { buy(activity, it, onDone) } })
        }
    }

    private fun buy(activity: Activity, offer: PremiumOffer, onDone: (success: Boolean) -> Unit) {
        viewModelScope.launch {
            Log.d(TAG, "buy: ${offer.id}")
            buyPremiumOffer.invoke(activity, offer, onDone = onDone)
        }
    }

    private fun handleOnPremiumOfferSelected(premiumOffer: PremiumOffer?) {
        Log.d(TAG, "handleOnPremiumOfferSelected: ${premiumOffer?.id}")
        _state.update { it.copy(selectedOffer = premiumOffer) }
    }

    private fun handleOnPremiumPurchaseTarget(
        activity: Activity,
        premiumTarget: PremiumTarget,
        onDone: (success: Boolean) -> Unit
    ) {
        getTargetOffer(premiumTarget)?.let { offer ->
            viewModelScope.launch {
                Log.d(TAG, "handleOnPremiumPurchaseTarget: ${offer.id}")
                buyPremiumOffer.invoke(activity, offer, onDone = onDone)
            }
        }
    }

    private fun getTargetOffer(target: PremiumTarget?): PremiumOffer? {
        Log.d(TAG, "getTargetOffer: Target = $target")
        val targetOfferId = when (target) {
            PremiumTarget.FLOW1 -> getPremiumOfferId(PremiumOfferType.PREMIUM_1)
            PremiumTarget.FLOW2 -> getPremiumOfferId(PremiumOfferType.PREMIUM_2)
            PremiumTarget.REMOVE_ADS -> getPremiumOfferId(PremiumOfferType.REMOVE_ADS)
            null -> getPremiumOfferId(PremiumOfferType.REMOVE_ADS)
        }

        Log.d(TAG, "getTargetOffer: TargetOfferId = $targetOfferId")
        return _state.value.premiumOffers.find { it.id == targetOfferId }
            ?: _state.value.premiumOffers.firstOrNull()
    }


    companion object {
        private const val TAG = "PremiumScreenViewModelTAG"
    }
}

// you can add multiple targets based on the app scenarios
enum class PremiumTarget {
    FLOW1,
    FLOW2,
    REMOVE_ADS
}
