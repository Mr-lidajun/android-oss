package com.kickstarter.viewmodels

import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.models.OptimizelyFeature
import com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.RewardUtils
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.RewardsItem
import com.kickstarter.models.ShippingRule
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.ui.viewholders.BackingAddOnViewHolder
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.math.RoundingMode
import kotlin.math.min

class BackingAddOnViewHolderViewModel {
    interface Inputs {
        /** Configure with the current [ProjectData] and [Reward].
         * @param projectData we get the Project for currency
         * @param AddOn  the actual addOn item loading on the ViewHolder
         * @param selectedShippingRule the selected shipping rule
         */
        fun configureWith(projectDataAndAddOn: Triple<ProjectData, Reward, ShippingRule>)

        /** Emits if the increase button has been pressed */
        fun addButtonIsVisible(isVisible: Boolean)

        /** Emits the current quantity displayed on the addons stepper */
        fun currentQuantity(quantity: Int)
    }

    interface Outputs {
        /** Emits a pair with the add on title and the quantity in order to build the stylized title  */
        fun titleForAddOn(): Observable<String>

        /** Emits the add on's description.  */
        fun description(): Observable<String>

        /** Emits the add on's minimum amount */
        fun minimum(): Observable<CharSequence>

        /** Emits the add on's converted minimum amount */
        fun convertedMinimum(): Observable<CharSequence>

        /** Emits whether or not the conversion text view is visible */
        fun conversionIsGone(): Observable<Boolean>

        /** Emits the conversion amount */
        fun conversion(): Observable<CharSequence>

        /** Emits the reward items */
        fun rewardItems(): Observable<List<RewardsItem>>

        /** Emits whether or not the remaining quantity pill is visible */
        fun remainingQuantityPillIsGone(): Observable<Boolean>

        /** Emits whether or not the backer limit pill is visible */
        fun backerLimitPillIsGone(): Observable<Boolean>

        /** Emits the backer limit */
        fun backerLimit(): Observable<String>

        /** Emits the remaining quantity*/
        fun remainingQuantity(): Observable<String>

        /** Emits whether or not the shipping amount is visible */
        fun shippingAmountIsGone(): Observable<Boolean>

        /** Emits the shipping amount */
        fun shippingAmount(): Observable<String>

        /** Emits whether or not the countdown pill is gone */
        fun deadlineCountdownIsGone(): Observable<Boolean>

        /** Emits the reward to be used to calculate the deadline countdown */
        fun deadlineCountdown(): Observable<Reward>

        /** Emits whether or not the reward items list is gone */
        fun rewardItemsAreGone(): Observable<Boolean>

        /** Emits quantity selected for which id*/
        fun quantityPerId(): PublishSubject<Pair<Int, Long>>

        /** Emits the maximum quantity for available addon*/
        fun maxQuantity(): Observable<Int>

        /** Emits a boolean that determines if the local PickUp section should be hidden **/
        fun localPickUpIsGone(): Observable<Boolean>

        /** Emits the String with the Local Pickup Displayable name **/
        fun localPickUpName(): Observable<String>
    }

    /**
     *  Logic to handle the UI for backing `Add On` card
     *  Configuring the View for [BackingAddOnViewHolder]
     *  - No interaction with the user just displaying information
     *  - Loading in [BackingAddOnViewHolder] -> [BackingAddOnsAdapter] -> [BackingAddOnsFragment]
     */
    class ViewModel(@NonNull environment: Environment) : ActivityViewModel<BackingAddOnViewHolder>(environment), Inputs, Outputs {

        private val ksCurrency = requireNotNull(environment.ksCurrency())
        private val projectDataAndAddOn = PublishSubject.create<Triple<ProjectData, Reward, ShippingRule>>()
        private val title = PublishSubject.create<String>()
        private val description = PublishSubject.create<String>()
        private val minimum = PublishSubject.create<CharSequence>()
        private val convertedMinimum = PublishSubject.create<CharSequence>()
        private val conversionIsGone = PublishSubject.create<Boolean>()
        private val conversion = PublishSubject.create<CharSequence>()
        private val rewardItems = PublishSubject.create<List<RewardsItem>>()
        private val remainingQuantityPillIsGone = PublishSubject.create<Boolean>()
        private val backerLimitPillIsGone = PublishSubject.create<Boolean>()
        private val remainingQuantity = PublishSubject.create<String>()
        private val backerLimit = PublishSubject.create<String>()
        private val shippingAmountIsGone = PublishSubject.create<Boolean>()
        private val shippingAmount = PublishSubject.create<String>()
        private val deadlineCountdown = PublishSubject.create<Reward>()
        private val deadlineCountdownIsGone = PublishSubject.create<Boolean>()
        private val rewardItemsAreGone = PublishSubject.create<Boolean>()
        private val addButtonIsVisible = PublishSubject.create<Boolean>()
        private val quantity = PublishSubject.create<Int>()
        private val quantityPerId = PublishSubject.create<Pair<Int, Long>>()
        private val maxQuantity = PublishSubject.create<Int>()
        private val localPickUpIsGone = BehaviorSubject.create<Boolean>()
        private val localPickUpName = BehaviorSubject.create<String>()
        private val optimizely = environment.optimizely()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {

            val addOn = projectDataAndAddOn
                .map { it.second }

            addOn
                .map { it.title() }
                .subscribe(this.title)

            addOn
                .map { it.description() }
                .filter { ObjectUtils.isNotNull(it) }
                .subscribe(this.description)

            addOn.map { !RewardUtils.isItemized(it) }
                .compose(bindToLifecycle())
                .subscribe(this.rewardItemsAreGone)

            addOn.filter { RewardUtils.isItemized(it) }
                .map { if (it.isAddOn()) it.addOnsItems() else it.rewardsItems() }
                .compose(bindToLifecycle())
                .subscribe(this.rewardItems)

            projectDataAndAddOn.map { this.ksCurrency.format(it.second.minimum(), it.first.project()) }
                .subscribe(this.minimum)

            projectDataAndAddOn.map { this.ksCurrency.format(it.second.convertedMinimum(), it.first.project()) }
                .subscribe(this.convertedMinimum)

            projectDataAndAddOn.map { it.first.project() }
                .map { it.currency() == it.currentCurrency() }
                .compose(bindToLifecycle())
                .subscribe(this.conversionIsGone)

            projectDataAndAddOn
                .map { this.ksCurrency.format(it.second.convertedMinimum(), it.first.project(), true, RoundingMode.HALF_UP, true) }
                .compose(bindToLifecycle())
                .subscribe(this.conversion)

            projectDataAndAddOn
                .map { !ObjectUtils.isNotNull(it.second.limit()) }
                .compose(bindToLifecycle())
                .subscribe(this.backerLimitPillIsGone)

            addOn
                .map { !ObjectUtils.isNotNull(it.remaining()) }
                .compose(bindToLifecycle())
                .subscribe(this.remainingQuantityPillIsGone)

            addOn.map { it.limit().toString() }
                .compose(bindToLifecycle())
                .subscribe(this.backerLimit)

            addOn
                .filter { ObjectUtils.isNotNull(it.remaining()) }
                .map { it.remaining().toString() }
                .subscribe(this.remainingQuantity)

            projectDataAndAddOn.map { ObjectUtils.isNotNull(it.second.endsAt()) }
                .compose(bindToLifecycle())
                .map { !it }
                .subscribe(this.deadlineCountdownIsGone)

            projectDataAndAddOn.map { it.second }
                .compose(bindToLifecycle())
                .subscribe(this.deadlineCountdown)

            addOn.map { it.shippingRules()?.isEmpty() }
                .compose(bindToLifecycle())
                .subscribe(this.shippingAmountIsGone)

            projectDataAndAddOn.map {
                getShippingCost(it.second.shippingRules(), it.first.project(), it.third)
            }
                .compose(bindToLifecycle())
                .subscribe(this.shippingAmount)

            addOn
                .map { it?.quantity() ?: 0 }
                .distinctUntilChanged()
                .subscribe(this.quantity)

            addOn
                .map { maximumLimit(it) }
                .filter { ObjectUtils.isNotNull(it) }
                .compose(bindToLifecycle())
                .subscribe(this.maxQuantity)

            addOn
                .filter { !RewardUtils.isShippable(it) }
                .map {
                    RewardUtils.isLocalPickup(it) && optimizely?.isFeatureEnabled(
                        OptimizelyFeature.Key.ANDROID_LOCAL_PICKUP
                    ) == true
                }
                .compose(bindToLifecycle())
                .subscribe {
                    this.localPickUpIsGone.onNext(!it)
                }

            addOn
                .filter { !RewardUtils.isShippable(it) }
                .filter { RewardUtils.isLocalPickup(it) }
                .map { it.localReceiptLocation()?.displayableName() }
                .filter { ObjectUtils.isNotNull(it) }
                .compose(bindToLifecycle())
                .subscribe(this.localPickUpName)

            this.quantity
                .compose<Pair<Int, Reward>>(combineLatestPair(addOn))
                .map { data -> Pair(data.first, data.second.id()) }
                .distinctUntilChanged { quantityPerId1, quantityPerId2 ->
                    quantityPerId1.first == quantityPerId2.first && quantityPerId1.second == quantityPerId2.second
                }
                .compose(bindToLifecycle())
                .subscribe(this.quantityPerId)
        }

        /**
         * If the addOn is available and within a valid time range for the campaign, the
         * maximumLimit will be the lowest of either the limit or the remaining
         * if the addOns is not available or the campaign is over, the maxLimit will be
         * the current selected quantity allowing the user to modify the already backed amount
         * just to decrease it.
         *
         * @param addOn: The current selected addon)
         * @return maxmimum limit: remaining, limit, or quantity
         */
        private fun maximumLimit(addOn: Reward): Int? {
            val limit = addOn.limit()
            val remaining = addOn.remaining()
            return if (addOn.isAvailable() && RewardUtils.isValidTimeRange(addOn)) {
                when {
                    remaining != null && limit != null -> min(remaining, limit)
                    remaining != null -> remaining
                    limit != null -> limit
                    else -> null
                }
            } else {
                addOn.quantity()
            }
        }

        private fun getShippingCost(shippingRules: List<ShippingRule>?, project: Project, selectedShippingRule: ShippingRule) =
            if (shippingRules.isNullOrEmpty()) ""
            else shippingRules?.let {
                var cost = 0.0
                it.filter {
                    it.location()?.id() == selectedShippingRule.location()?.id()
                }.map {
                    cost += it.cost()
                }
                this.ksCurrency.format(cost, project)
            }

        // - Inputs
        override fun configureWith(projectDataAndAddOn: Triple<ProjectData, Reward, ShippingRule>) = this.projectDataAndAddOn.onNext(projectDataAndAddOn)
        override fun addButtonIsVisible(isVisible: Boolean) { this.addButtonIsVisible.onNext(isVisible) }
        override fun currentQuantity(quantity: Int) = this.quantity.onNext(quantity)

        // - Outputs
        override fun titleForAddOn(): PublishSubject<String> = this.title

        override fun description(): PublishSubject<String> = this.description

        override fun minimum(): PublishSubject<CharSequence> = this.minimum

        override fun convertedMinimum(): PublishSubject<CharSequence> = this.convertedMinimum

        override fun conversionIsGone(): PublishSubject<Boolean> = this.conversionIsGone

        override fun conversion(): PublishSubject<CharSequence> = this.conversion

        override fun rewardItems(): PublishSubject<List<RewardsItem>> = this.rewardItems

        override fun remainingQuantityPillIsGone(): PublishSubject<Boolean> = this.remainingQuantityPillIsGone

        override fun backerLimitPillIsGone(): PublishSubject<Boolean> = this.backerLimitPillIsGone

        override fun backerLimit(): PublishSubject<String> = this.backerLimit

        override fun remainingQuantity(): PublishSubject<String> = this.remainingQuantity

        override fun shippingAmountIsGone(): PublishSubject<Boolean> = this.shippingAmountIsGone

        override fun shippingAmount(): PublishSubject<String> = this.shippingAmount

        override fun deadlineCountdown(): PublishSubject<Reward> = this.deadlineCountdown

        override fun deadlineCountdownIsGone(): PublishSubject<Boolean> = this.deadlineCountdownIsGone

        override fun rewardItemsAreGone(): PublishSubject<Boolean> = this.rewardItemsAreGone

        override fun quantityPerId(): PublishSubject<Pair<Int, Long>> = this.quantityPerId

        override fun maxQuantity(): Observable<Int> = this.maxQuantity

        override fun localPickUpIsGone(): Observable<Boolean> = this.localPickUpIsGone

        override fun localPickUpName(): Observable<String> = this.localPickUpName
    }
}
