package com.notura.mobile.data.user

import com.notura.mobile.domain.user.CurrentUser
import com.notura.mobile.network.ApiError
import com.notura.mobile.network.ApiResult
import com.notura.mobile.network.NoturaApiClient

interface UserRepository {
    /** `GET /api/user/me`. Documented errors: 401, 500. */
    suspend fun fetchCurrentUser(): ApiResult<CurrentUser>
}

class NoturaUserRepository(private val api: NoturaApiClient) : UserRepository {

    override suspend fun fetchCurrentUser(): ApiResult<CurrentUser> =
        when (val result = api.get("/api/user/me", CurrentUserResponseDto.serializer())) {
            is ApiResult.Failure -> result
            is ApiResult.Success -> result.value.user.toDomainResult()
        }
}

private fun CurrentUserDto.toDomainResult(): ApiResult<CurrentUser> = try {
    ApiResult.Success(toDomain())
} catch (error: UnknownApiValueException) {
    ApiResult.Failure(ApiError.Unexpected(status = 200, message = error.message))
}
