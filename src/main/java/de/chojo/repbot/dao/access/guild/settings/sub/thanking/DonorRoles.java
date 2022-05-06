package de.chojo.repbot.dao.access.guild.settings.sub.thanking;

import de.chojo.repbot.dao.access.guild.settings.sub.Thanking;
import net.dv8tion.jda.api.entities.Guild;

import java.util.Set;

public class DonorRoles extends RolesHolder {
    private final Thanking thanking;

    public DonorRoles(Thanking thanking, Set<Long> roleIds) {
        super(thanking, roleIds);
        this.thanking = thanking;
    }

    @Override
    public Guild guild() {
        return thanking.guild();
    }

    @Override
    protected String targetTable() {
        return "donor_roles";
    }

}
