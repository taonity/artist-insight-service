package org.taonity.artistinsightservice.donation.kofi.dto

data class KofiWebhookData (
    val verificationToken: String,
    val messageId: String,
    val timestamp: String,
    val type: String,
    val isPublic: Boolean,
    val fromName: String,
    val message: String,
    val amount: String,
    val url: String,
    val email: String,
    val currency: String,
    val isSubscriptionPayment: Boolean,
    val isFirstSubscriptionPayment: Boolean,
    val kofiTransactionId: String,
    val shopItems: String?,
    val tierName: String?,
    val shipping: String?,
    val discordUsername: String,
    val discordUserid: String
)