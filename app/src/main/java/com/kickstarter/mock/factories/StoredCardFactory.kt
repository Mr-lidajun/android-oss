package com.kickstarter.mock.factories

import com.kickstarter.models.StoredCard
import type.CreditCardTypes
import java.util.Date

object StoredCardFactory {
    @JvmStatic
    fun discoverCard(): StoredCard {
        return StoredCard.builder()
            .id(IdFactory.id().toString())
            .expiration(Date())
            .lastFourDigits("1234")
            .type(CreditCardTypes.DISCOVER)
            .build()
    }

    @JvmStatic
    fun visa(): StoredCard {
        return StoredCard.builder()
            .id(IdFactory.id().toString())
            .expiration(Date())
            .lastFourDigits("4321")
            .type(CreditCardTypes.VISA)
            .build()
    }
}
