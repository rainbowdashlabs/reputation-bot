package de.chojo.repbot.web.pojo.settings.sub.thanking;

import java.util.Collections;
import java.util.Set;

public abstract class ThankwordsPOJO {
    protected final Set<String> thankwords;

    public ThankwordsPOJO(Set<String> thankwords) {
        this.thankwords = thankwords;
    }

    public Set<String> words() {
        return Collections.unmodifiableSet(thankwords);
    }
}
