package de.chojo.repbot.service.kofi;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KofiShopItem(
        @JsonProperty("direct_link_code") String directLinkCode,
        @JsonProperty("item_name") String itemName,
        @JsonProperty("quantity") int quantity) {
}
