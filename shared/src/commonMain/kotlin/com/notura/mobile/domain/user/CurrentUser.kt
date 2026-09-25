package com.notura.mobile.domain.user

/** Authenticated user profile, plan and quota, as returned by `GET /api/user/me`. */
data class CurrentUser(
    val id: String,
    val email: String,
    val name: String,
    /** Empty when not filled in. */
    val company: String,
    /** Empty when not filled in; otherwise normalized with the `55` country code. */
    val whatsappNumber: String,
    val plan: Plan,
    val effectivePlan: Plan,
    val billingEntitlementStatus: BillingEntitlementStatus,
    val isPaidPlanActive: Boolean,
    val canSendWhatsAppSummary: Boolean,
    val canProcessMeetings: Boolean,
    val meetingQuotaBlockCode: MeetingQuotaBlockCode?,
    val meetingQuotaLimit: Int,
    val meetingsThisMonth: Int,
    /** Null means unlimited. */
    val monthlyLimit: Int?,
    val currentPeriodEnd: String?,
    val billingProvider: BillingProvider,
    val autoRenewEnabled: Boolean,
    val renewalStatus: String,
    val hasUsedTrial: Boolean,
    val trialEndAt: String?,
    val shouldOfferTrial: Boolean,
)

enum class Plan(val apiValue: String) {
    Free("free"),
    Pro("pro"),
    Team("team");

    companion object {
        fun fromApi(value: String): Plan? = entries.firstOrNull { it.apiValue == value }
    }
}

enum class BillingEntitlementStatus(val apiValue: String) {
    Free("free"),
    Trialing("trialing"),
    Active("active"),
    Expired("expired"),
    Grace("grace");

    companion object {
        fun fromApi(value: String): BillingEntitlementStatus? = entries.firstOrNull { it.apiValue == value }
    }
}

enum class MeetingQuotaBlockCode(val apiValue: String) {
    LifetimeQuotaExceeded("lifetime_quota_exceeded"),
    PeriodQuotaExceeded("period_quota_exceeded"),
    SubscriptionExpired("subscription_expired");

    companion object {
        fun fromApi(value: String): MeetingQuotaBlockCode? = entries.firstOrNull { it.apiValue == value }
    }
}

enum class BillingProvider(val apiValue: String) {
    Stripe("stripe"),
    AbacatePay("abacatepay");

    companion object {
        fun fromApi(value: String): BillingProvider? = entries.firstOrNull { it.apiValue == value }
    }
}
