package de.chojo.repbot.web.pojo.settings.sub;

import de.chojo.repbot.dao.access.guild.settings.sub.integrationbypass.Bypass;

import java.util.HashMap;
import java.util.Map;

public class IntegrationBypassPOJO {
    protected final Map<Long, Bypass> bypasses = new HashMap<>();
}
