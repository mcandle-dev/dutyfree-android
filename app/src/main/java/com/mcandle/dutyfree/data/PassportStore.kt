package com.mcandle.dutyfree.data

import android.content.Context
import android.content.SharedPreferences

class PassportStore(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("passport_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_PASSPORT_NUM = "passport_num"
        private const val KEY_FIRST_NAME = "first_name"
        private const val KEY_LAST_NAME = "last_name"
        private const val KEY_EXPIRY_DATE = "expiry_date"
        private const val KEY_AIRLINE = "airline"
        private const val KEY_FLIGHT_NUM = "flight_num"
        private const val KEY_DEPARTURE_TIME = "departure_time"
        private const val KEY_MEMBERSHIP_NO = "membership_no"
        private const val KEY_MEMBER_LEVEL = "member_level"
        private const val KEY_PHONE_NUMBER = "phone_number"
        private const val KEY_MEMBER_NAME = "member_name"
    }

    fun getPassportInfo(): PassportInfo {
        return PassportInfo(
            passportNumber = prefs.getString(KEY_PASSPORT_NUM, "") ?: "",
            firstName = prefs.getString(KEY_FIRST_NAME, "") ?: "",
            lastName = prefs.getString(KEY_LAST_NAME, "") ?: "",
            expiryDate = prefs.getString(KEY_EXPIRY_DATE, "") ?: "",
            airline = prefs.getString(KEY_AIRLINE, "") ?: "",
            flightNumber = prefs.getString(KEY_FLIGHT_NUM, "") ?: "",
            departureTime = prefs.getString(KEY_DEPARTURE_TIME, "") ?: "",
            membershipNo = prefs.getString(KEY_MEMBERSHIP_NO, "") ?: "",
            memberLevel = prefs.getString(KEY_MEMBER_LEVEL, "VIP") ?: "VIP",
            phoneNumber = prefs.getString(KEY_PHONE_NUMBER, "") ?: "",
            memberName = prefs.getString(KEY_MEMBER_NAME, "") ?: ""
        )
    }

    fun savePassportInfo(info: PassportInfo) {
        prefs.edit().apply {
            putString(KEY_PASSPORT_NUM, info.passportNumber)
            putString(KEY_FIRST_NAME, info.firstName)
            putString(KEY_LAST_NAME, info.lastName)
            putString(KEY_EXPIRY_DATE, info.expiryDate)
            putString(KEY_AIRLINE, info.airline)
            putString(KEY_FLIGHT_NUM, info.flightNumber)
            putString(KEY_DEPARTURE_TIME, info.departureTime)
            putString(KEY_MEMBERSHIP_NO, info.membershipNo)
            putString(KEY_MEMBER_LEVEL, info.memberLevel)
            putString(KEY_PHONE_NUMBER, info.phoneNumber)
            putString(KEY_MEMBER_NAME, info.memberName)
            apply()
        }
    }
}

data class PassportInfo(
    val passportNumber: String,
    val firstName: String,
    val lastName: String,
    val expiryDate: String,
    val airline: String,
    val flightNumber: String,
    val departureTime: String,
    val membershipNo: String = "",
    val memberLevel: String = "VIP",
    val phoneNumber: String = "",
    val memberName: String = ""
)
