package org.taonity.artistinsightservice.donation.kofi

object KofiPayloadBuilder {

    private const val DEFAULT_VERIFICATION_TOKEN = "valid-token"
    private const val DEFAULT_MESSAGE = "someSpotifyId1234567890abcdef"
    private const val DEFAULT_AMOUNT = "5.00"

    fun buildJson(
        verificationToken: String = DEFAULT_VERIFICATION_TOKEN,
        message: String = DEFAULT_MESSAGE,
        amount: String = DEFAULT_AMOUNT
    ): String = """
        {
            "verification_token": "$verificationToken",
            "message_id": "msg-001",
            "timestamp": "2025-01-01T00:00:00Z",
            "type": "Donation",
            "is_public": true,
            "from_name": "Donor",
            "message": "$message",
            "amount": "$amount",
            "url": "https://ko-fi.com/test",
            "email": "donor@example.com",
            "currency": "USD",
            "is_subscription_payment": false,
            "is_first_subscription_payment": false,
            "kofi_transaction_id": "txn-001",
            "shop_items": null,
            "tier_name": null,
            "shipping": null,
            "discord_username": "",
            "discord_userid": ""
        }
    """.trimIndent()
}
