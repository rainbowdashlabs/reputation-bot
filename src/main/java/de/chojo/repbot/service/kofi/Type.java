package de.chojo.repbot.service.kofi;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Type {
    @JsonProperty("Subscription")
    SUBSCRIPTION,
    @JsonProperty("Donation")
    DONATION,
    @JsonProperty("Commission")
    COMMISSION,
    @JsonProperty("Shop Order")
    SHOP_ORDER
}
