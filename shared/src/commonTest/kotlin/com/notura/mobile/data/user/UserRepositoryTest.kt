package com.notura.mobile.data.user

import com.notura.mobile.domain.user.BillingEntitlementStatus
import com.notura.mobile.domain.user.BillingProvider
import com.notura.mobile.domain.user.CurrentUser
import com.notura.mobile.domain.user.MeetingQuotaBlockCode
import com.notura.mobile.domain.user.Plan
import com.notura.mobile.network.ApiError
import com.notura.mobile.network.ApiResult
import com.notura.mobile.network.NoturaJson
import com.notura.mobile.testing.MockApi
import com.notura.mobile.testing.TEST_BASE_URL
import com.notura.mobile.testing.respondJson
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/** Example body from `docs/api-codex-mobile.md` (Notura-App), section 4.1. */
private const val CONTRACT_USER_ME = """
{
  "user": {
    "id": "uuid",
    "email": "user@example.com",
    "name": "Nome",
    "company": "Empresa",
    "whatsappNumber": "5511999999999",
    "plan": "free",
    "effectivePlan": "free",
    "billingEntitlementStatus": "free",
    "isPaidPlanActive": false,
    "canSendWhatsAppSummary": false,
    "canProcessMeetings": true,
    "meetingQuotaBlockCode": null,
    "meetingQuotaLimit": 3,
    "meetingsThisMonth": 0,
    "monthlyLimit": 3,
    "currentPeriodEnd": null,
    "billingProvider": "stripe",
    "autoRenewEnabled": true,
    "renewalStatus": "idle",
    "abacatepayAutoRenewEnabled": true,
    "abacatepayRenewalStatus": "idle",
    "hasUsedTrial": false,
    "trialEndAt": null,
    "shouldOfferTrial": true
  }
}
"""

private val EXPECTED_USER = CurrentUser(
    id = "uuid",
    email = "user@example.com",
    name = "Nome",
    company = "Empresa",
    whatsappNumber = "5511999999999",
    plan = Plan.Free,
    effectivePlan = Plan.Free,
    billingEntitlementStatus = BillingEntitlementStatus.Free,
    isPaidPlanActive = false,
    canSendWhatsAppSummary = false,
    canProcessMeetings = true,
    meetingQuotaBlockCode = null,
    meetingQuotaLimit = 3,
    meetingsThisMonth = 0,
    monthlyLimit = 3,
    currentPeriodEnd = null,
    billingProvider = BillingProvider.Stripe,
    autoRenewEnabled = true,
    renewalStatus = "idle",
    hasUsedTrial = false,
    trialEndAt = null,
    shouldOfferTrial = true,
)

class CurrentUserSerializationTest {

    @Test
    fun decodesContractExample() {
        val dto = NoturaJson.decodeFromString(CurrentUserResponseDto.serializer(), CONTRACT_USER_ME)

        assertEquals(EXPECTED_USER, dto.user.toDomain())
        assertEquals("idle", dto.user.abacatepayRenewalStatus)
    }

    @Test
    fun roundTripsThroughJson() {
        val dto = NoturaJson.decodeFromString(CurrentUserResponseDto.serializer(), CONTRACT_USER_ME)

        val encoded = Json.encodeToString(CurrentUserResponseDto.serializer(), dto)

        assertEquals(dto, NoturaJson.decodeFromString(CurrentUserResponseDto.serializer(), encoded))
        val fullJson = Json { encodeDefaults = true }
        assertEquals(
            Json.parseToJsonElement(CONTRACT_USER_ME),
            fullJson.parseToJsonElement(fullJson.encodeToString(CurrentUserResponseDto.serializer(), dto)),
        )
    }

    @Test
    fun decodesPaidUserWithQuotaBlockAndUnlimitedMonthlyLimit() {
        val body = CONTRACT_USER_ME
            .replace("\"plan\": \"free\"", "\"plan\": \"pro\"")
            .replace("\"effectivePlan\": \"free\"", "\"effectivePlan\": \"team\"")
            .replace("\"billingEntitlementStatus\": \"free\"", "\"billingEntitlementStatus\": \"grace\"")
            .replace("\"meetingQuotaBlockCode\": null", "\"meetingQuotaBlockCode\": \"subscription_expired\"")
            .replace("\"monthlyLimit\": 3", "\"monthlyLimit\": null")
            .replace("\"billingProvider\": \"stripe\"", "\"billingProvider\": \"abacatepay\"")

        val user = NoturaJson.decodeFromString(CurrentUserResponseDto.serializer(), body).user.toDomain()

        assertEquals(Plan.Pro, user.plan)
        assertEquals(Plan.Team, user.effectivePlan)
        assertEquals(BillingEntitlementStatus.Grace, user.billingEntitlementStatus)
        assertEquals(MeetingQuotaBlockCode.SubscriptionExpired, user.meetingQuotaBlockCode)
        assertEquals(null, user.monthlyLimit)
        assertEquals(BillingProvider.AbacatePay, user.billingProvider)
    }

    @Test
    fun ignoresFieldsAddedByNewerServers() {
        val body = CONTRACT_USER_ME.replace("\"id\": \"uuid\",", "\"id\": \"uuid\", \"newField\": {\"a\": 1},")

        val dto = NoturaJson.decodeFromString(CurrentUserResponseDto.serializer(), body)

        assertEquals(EXPECTED_USER, dto.user.toDomain())
    }
}

class UserRepositoryTest {

    @Test
    fun fetchesCurrentUser() = runTest {
        val api = MockApi { respondJson(CONTRACT_USER_ME) }

        val result = NoturaUserRepository(api.client).fetchCurrentUser()

        assertEquals(ApiResult.Success(EXPECTED_USER), result)
        val request = api.requests.single()
        assertEquals(HttpMethod.Get, request.method)
        assertEquals("$TEST_BASE_URL/api/user/me", request.url.toString())
    }

    @Test
    fun propagatesUnauthorized() = runTest {
        val api = MockApi { respondJson("""{"error":"Não autenticado."}""", HttpStatusCode.Unauthorized) }

        val result = NoturaUserRepository(api.client).fetchCurrentUser()

        assertEquals(ApiResult.Failure(ApiError.Unauthorized("Não autenticado.")), result)
    }

    @Test
    fun propagatesServerError() = runTest {
        val api = MockApi { respondJson("""{"error":"Erro ao carregar usuário."}""", HttpStatusCode.InternalServerError) }

        val result = NoturaUserRepository(api.client).fetchCurrentUser()

        assertEquals(ApiResult.Failure(ApiError.Server(500, "Erro ao carregar usuário.")), result)
    }

    @Test
    fun rejectsPlanOutsideTheContract() = runTest {
        val api = MockApi { respondJson(CONTRACT_USER_ME.replace("\"plan\": \"free\"", "\"plan\": \"enterprise\"")) }

        val result = NoturaUserRepository(api.client).fetchCurrentUser()

        val error = assertIs<ApiError.Unexpected>(assertIs<ApiResult.Failure>(result).error)
        assertEquals("Unknown value 'enterprise' for 'plan'", error.message)
    }
}
