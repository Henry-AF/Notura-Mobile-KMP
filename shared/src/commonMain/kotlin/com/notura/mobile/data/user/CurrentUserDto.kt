package com.notura.mobile.data.user

import kotlinx.serialization.Serializable

/** Body of `GET /api/user/me` and `PATCH /api/user/me`. */
@Serializable
data class CurrentUserResponseDto(val user: CurrentUserDto)

/**
 * Wire shape of the `user` object, field for field with `CurrentUser` in
 * `src/lib/user/current-user-types.ts` (Notura-App). Enum-like fields stay strings here and are
 * validated in [toDomain].
 */
@Serializable
data class CurrentUserDto(
    val id: String,
    val email: String,
    val name: String,
    val company: String,
    val whatsappNumber: String,
    val plan: String,
    val effectivePlan: String,
    val billingEntitlementStatus: String,
    val isPaidPlanActive: Boolean,
    val canSendWhatsAppSummary: Boolean,
    val canProcessMeetings: Boolean,
    val meetingQuotaBlockCode: String? = null,
    val meetingQuotaLimit: Int,
    val meetingsThisMonth: Int,
    val monthlyLimit: Int? = null,
    val currentPeriodEnd: String? = null,
    val billingProvider: String,
    val autoRenewEnabled: Boolean,
    val renewalStatus: String,
    val abacatepayAutoRenewEnabled: Boolean,
    val abacatepayRenewalStatus: String,
    val hasUsedTrial: Boolean,
    val trialEndAt: String? = null,
    val shouldOfferTrial: Boolean,
)
