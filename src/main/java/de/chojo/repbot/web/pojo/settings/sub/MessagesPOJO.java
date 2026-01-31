package de.chojo.repbot.web.pojo.settings.sub;

public class MessagesPOJO {
    protected boolean reactionConfirmation;
    protected boolean commandReputationEphemeral;

    public MessagesPOJO(boolean reactionConfirmation, boolean commandReputationEphemeral) {
        this.reactionConfirmation = reactionConfirmation;
        this.commandReputationEphemeral = commandReputationEphemeral;
    }

    public boolean isReactionConfirmation() {
        return reactionConfirmation;
    }

    public boolean isCommandReputationEphemeral() {
        return commandReputationEphemeral;
    }
}
