package de.chojo.repbot.data.wrapper;

import de.chojo.jdautil.parsing.Verifier;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GuildSettings {
    private final Guild guild;
    private final GeneralSettings generalSettings;
    private final MessageSettings messageSettings;
    private final AbuseSettings abuseSettings;
    private final ThankSettings thankSettings;


    public GuildSettings(Guild guild, GeneralSettings generalSettings, MessageSettings messageSettings, AbuseSettings abuseSettings, ThankSettings thankSettings) {
        this.guild = guild;
        this.generalSettings = generalSettings;
        this.messageSettings = messageSettings;
        this.abuseSettings = abuseSettings;
        this.thankSettings = thankSettings;
    }


    public Guild guild() {
        return guild;
    }

    public AbuseSettings abuseSettings() {
        return abuseSettings;
    }

    public GeneralSettings generalSettings() {
        return generalSettings;
    }

    public MessageSettings messageSettings() {
        return messageSettings;
    }

    public ThankSettings thankSettings() {
        return thankSettings;
    }
}
