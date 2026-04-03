/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.routes.v1.pub;

import de.chojo.repbot.dao.access.user.RepUser;
import de.chojo.repbot.dao.provider.GuildRepository;
import de.chojo.repbot.dao.provider.UserRepository;
import de.chojo.repbot.web.cache.MemberCache;
import de.chojo.repbot.web.routes.RoutesBuilder;
import io.javalin.http.Context;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiResponse;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.sharding.ShardManager;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;

public class PublicRoute implements RoutesBuilder {
    private final GuildRepository guildRepository;
    private final ShardManager shardManager;
    private final MemberCache memberCache;
    private final UserRepository userRepository;

    public PublicRoute(
            GuildRepository guildRepository,
            ShardManager shardManager,
            MemberCache memberCache,
            UserRepository userRepository) {
        this.guildRepository = guildRepository;
        this.shardManager = shardManager;
        this.memberCache = memberCache;
        this.userRepository = userRepository;
    }

    @Override
    public void buildRoutes() {
        path("public", () -> {
            get("/profile", this::getPublicProfile);
        });
    }

    @OpenApi(
            summary = "Get public profile of a user in a guild",
            operationId = "getPublicProfile",
            path = "v1/public/profile",
            methods = HttpMethod.GET,
            queryParams = {
                @OpenApiParam(name = "guildId", required = true, description = "The guild ID"),
                @OpenApiParam(name = "userId", required = true, description = "The user ID")
            },
            tags = {"Public"},
            responses = {
                @OpenApiResponse(status = "200"),
                @OpenApiResponse(status = "400", description = "Invalid or missing guildId or userId"),
                @OpenApiResponse(status = "401", description = "Profile is not public"),
                @OpenApiResponse(status = "404", description = "Guild or user not found")
            })
    private void getPublicProfile(Context ctx) {
        String guildIdParam = ctx.queryParam("guildId");
        String userIdParam = ctx.queryParam("userId");

        if (guildIdParam == null || userIdParam == null) {
            ctx.status(400);
            return;
        }

        long guildId;
        long userId;
        try {
            guildId = Long.parseLong(guildIdParam);
            userId = Long.parseLong(userIdParam);
        } catch (NumberFormatException e) {
            ctx.status(400);
            return;
        }

        var guild = shardManager.getGuildById(guildId);
        if (guild == null) {
            ctx.status(404);
            return;
        }

        Member member = null;
        try {
            member = guild.retrieveMemberById(userId).complete();
        } catch (Exception ignore) {
            // ignore
        }
        if (member == null) {
            ctx.status(404);
            return;
        }

        var repGuild = guildRepository.guild(guild);
        var repMember = repGuild.reputation().user(member);
        RepUser repUser = userRepository.byUser(member.getUser());
        if (!repUser.settings().isPublicProfile()) {
            ctx.status(401);
            return;
        }
        var profile = repMember.profile();

        var memberPOJO = memberCache.get(member);
        ctx.json(new PublicProfilePOJO(memberPOJO, profile.rank(), profile.reputation()));
    }
}
