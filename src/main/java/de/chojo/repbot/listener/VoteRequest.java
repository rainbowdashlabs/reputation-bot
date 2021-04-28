package de.chojo.repbot.listener;

import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.Map;
import java.util.Optional;

@Getter
@AllArgsConstructor
public class VoteRequest {
    private final Member member;
    private final LocalizedEmbedBuilder embedBuilder;
    private final Message voteMessage;
    private final Message refMessage;
    private final Map<String, Member> voteTargets;
    private int remainingVotes;

    public Optional<Member> getTarget(String emoji) {
        return Optional.ofNullable(voteTargets.get(emoji));
    }

    public void voted() {
        remainingVotes--;
    }

    public MessageEmbed getNewEmbed(String description) {
        return embedBuilder.setDescription(description).build();
    }
}
