package com.notura.mobile.data.user

import com.notura.mobile.domain.user.BillingEntitlementStatus
import com.notura.mobile.domain.user.BillingProvider
import com.notura.mobile.domain.user.CurrentUser
import com.notura.mobile.domain.user.MeetingQuotaBlockCode
import com.notura.mobile.domain.user.Plan

/** Thrown when the API sends a value outside the contract's enums. */
class UnknownApiValueException(field: String, value: String) :
    IllegalArgumentException("Unknown value '$value' for '$field'")

fun CurrentUserDto.toDomain(): CurrentUser = CurrentUser(
    id = id,
    email = email,
    name = name,
    company = company,
    whatsappNumber = whatsappNumber,
    plan = parsePlan("plan", plan),
    effectivePlan = parsePlan("effectivePlan", effectivePlan),
    billingEntitlementStatus = BillingEntitlementStatus.fromApi(billingEntitlementStatus)
        ?: throw UnknownApiValueException("billingEntitlementStatus", billingEntitlementStatus),
    isPaidPlanActive = isPaidPlanActive,
    canSendWhatsAppSummary = canSendWhatsAppSummary,
    canProcessMeetings = canProcessMeetings,
    meetingQuotaBlockCode = meetingQuotaBlockCode?.let {
        MeetingQuotaBlockCode.fromApi(it) ?: throw UnknownApiValueException("meetingQuotaBlockCode", it)
    },
    meetingQuotaLimit = meetingQuotaLimit,
    meetingsThisMonth = meetingsThisMonth,
    monthlyLimit = monthlyLimit,
    currentPeriodEnd = currentPeriodEnd,
    billingProvider = BillingProvider.fromApi(billingProvider)
        ?: throw UnknownApiValueException("billingProvider", billingProvider),
    autoRenewEnabled = autoRenewEnabled,
    renewalStatus = renewalStatus,
    hasUsedTrial = hasUsedTrial,
    trialEndAt = trialEndAt,
    shouldOfferTrial = shouldOfferTrial,
)

private fun parsePlan(field: String, value: String): Plan =
    Plan.fromApi(value) ?: throw UnknownApiValueException(field, value)
