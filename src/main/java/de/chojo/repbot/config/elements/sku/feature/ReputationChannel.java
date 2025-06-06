package de.chojo.repbot.config.elements.sku.feature;

import de.chojo.repbot.config.elements.sku.SKUEntry;

public class ReputationChannel {
    private SKUEntry moreChannel = new SKUEntry();
    private int defaultChannel = 2;

    public SKUEntry moreChannel() {
        return moreChannel;
    }

    public int defaultChannel() {
        return defaultChannel;
    }

}
