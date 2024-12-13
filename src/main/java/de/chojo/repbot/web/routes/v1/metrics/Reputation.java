/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.routes.v1.metrics;

import de.chojo.repbot.dao.provider.Metrics;
import de.chojo.repbot.dao.snapshots.statistics.CountsStatistic;
import de.chojo.repbot.dao.snapshots.statistics.DowsStatistic;
import de.chojo.repbot.dao.snapshots.statistics.LabeledCountStatistic;
import de.chojo.repbot.web.routes.v1.MetricsHolder;
import io.javalin.http.Context;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiResponse;

import static de.chojo.repbot.web.routes.v1.MetricsRoute.MAX_MONTH;
import static de.chojo.repbot.web.routes.v1.MetricsRoute.MAX_MONTH_OFFSET;
import static de.chojo.repbot.web.routes.v1.MetricsRoute.MAX_WEEKS;
import static de.chojo.repbot.web.routes.v1.MetricsRoute.MAX_WEEK_OFFSET;
import static de.chojo.repbot.web.routes.v1.MetricsRoute.MAX_YEAR_OFFSET;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;

public class Reputation extends MetricsHolder {
    public Reputation(Metrics metrics, MetricCache cache) {
        super(cache, metrics);
    }

    @OpenApi(
            summary = "Get the counts of given reputation per week.",
            operationId = "countWeek",
            path = "week/{offset}/{count}",
            methods = HttpMethod.GET,
            tags = {"Reputation"},
            responses = {
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = byte[].class, type = "image/png")}),
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = CountsStatistic.class, type = "application/json")})
            },
            pathParams = {
                    @OpenApiParam(name = "offset", type = Integer.class, required = true),
                    @OpenApiParam(name = "count", type = Integer.class, required = true)
            }
    )
    public void countWeek(Context ctx) {
        var stats = metrics().reputation().week(offset(ctx, MAX_WEEK_OFFSET), count(ctx, MAX_WEEKS));
        if ("application/json".equals(ctx.header("Accept"))) {
            ctx.json(stats);
        } else {
            writeImage(ctx, stats.getChart("Reputation per week"));
        }
    }

    @OpenApi(
            summary = "Get the counts of given reputation per month.",
            operationId = "countMonth",
            path = "month/{offset}/{count}",
            methods = HttpMethod.GET,
            tags = {"Reputation"},
            responses = {
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = byte[].class, type = "image/png")}),
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = CountsStatistic.class, type = "application/json")})
            },
            pathParams = {
                    @OpenApiParam(name = "offset", type = Integer.class, required = true),
                    @OpenApiParam(name = "count", type = Integer.class, required = true)
            }
    )
    public void countMonth(Context ctx) {
        var stats = metrics().reputation().month(offset(ctx, MAX_MONTH_OFFSET), count(ctx, MAX_MONTH));
        if ("application/json".equals(ctx.header("Accept"))) {
            ctx.json(stats);
        } else {
            writeImage(ctx, stats.getChart("Reputation per month"));
        }
    }

    @OpenApi(
            summary = "Get the counts of given reputation per week split into type.",
            operationId = "countTypeWeek",
            path = "type/week/{offset}/{count}",
            methods = HttpMethod.GET,
            tags = {"Reputation"},
            responses = {
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = byte[].class, type = "image/png")}),
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = LabeledCountStatistic.class, type = "application/json")})
            },
            pathParams = {
                    @OpenApiParam(name = "offset", type = Integer.class, required = true),
                    @OpenApiParam(name = "count", type = Integer.class, required = true)
            }
    )
    public void countTypeWeek(Context ctx) {
        var stats = metrics().reputation().typeWeek(offset(ctx, MAX_WEEK_OFFSET), count(ctx, MAX_WEEKS));
        if ("application/json".equals(ctx.header("Accept"))) {
            ctx.json(stats);
        } else {
            writeImage(ctx, stats.getChart("Reputation per week by type"));
        }
    }

    @OpenApi(
            summary = "Get the counts of given reputation per month split into type.",
            operationId = "countTypeMonth",
            path = "type/month/{offset}/{count}",
            methods = HttpMethod.GET,
            tags = {"Reputation"},
            responses = {
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = byte[].class, type = "image/png")}),
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = LabeledCountStatistic.class, type = "application/json")})
            },
            pathParams = {
                    @OpenApiParam(name = "offset", type = Integer.class, required = true),
                    @OpenApiParam(name = "count", type = Integer.class, required = true)
            }
    )
    public void countTypeMonth(Context ctx) {
        var stats = metrics().reputation().typeMonth(offset(ctx, MAX_MONTH_OFFSET), count(ctx, MAX_MONTH));
        if ("application/json".equals(ctx.header("Accept"))) {
            ctx.json(stats);
        } else {
            writeImage(ctx, stats.getChart("Reputation per month by type"));
        }
    }

    @OpenApi(
            summary = "Get the total count of reputation in these weeks split into type.",
            operationId = "totalTypeWeek",
            path = "type/total/week/{offset}/{count}",
            methods = HttpMethod.GET,
            tags = {"Reputation"},
            responses = {
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = byte[].class, type = "image/png")}),
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = LabeledCountStatistic.class, type = "application/json")})
            },
            pathParams = {
                    @OpenApiParam(name = "offset", type = Integer.class, required = true),
                    @OpenApiParam(name = "count", type = Integer.class, required = true)
            }
    )
    public void totalTypeWeek(Context ctx) {
        var stats = metrics().reputation().typeTotalWeek(offset(ctx, MAX_WEEK_OFFSET), count(ctx, MAX_WEEKS));
        if ("application/json".equals(ctx.header("Accept"))) {
            ctx.json(stats);
        } else {
            writeImage(ctx, stats.getChart("Total reputation per week by type"));
        }
    }

    @OpenApi(
            summary = "Get the total count of reputation in these months split into type.",
            operationId = "totalTypeMonth",
            path = "type/total/month/{offset}/{count}",
            methods = HttpMethod.GET,
            tags = {"Reputation"},
            responses = {
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = byte[].class, type = "image/png")}),
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = LabeledCountStatistic.class, type = "application/json")})
            },
            pathParams = {
                    @OpenApiParam(name = "offset", type = Integer.class, required = true),
                    @OpenApiParam(name = "count", type = Integer.class, required = true)
            }
    )
    public void totalTypeMonth(Context ctx) {
        var stats = metrics().reputation().typeTotalMonth(offset(ctx, MAX_MONTH_OFFSET), count(ctx, MAX_MONTH));
        if ("application/json".equals(ctx.header("Accept"))) {
            ctx.json(stats);
        } else {
            writeImage(ctx, stats.getChart("Total reputation per month by type"));
        }
    }

    @OpenApi(
            summary = "Get the total count of reputation in these weeks.",
            operationId = "totalWeek",
            path = "total/week/{offset}/{count}",
            methods = HttpMethod.GET,
            tags = {"Reputation"},
            responses = {
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = byte[].class, type = "image/png")}),
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = CountsStatistic.class, type = "application/json")})
            },
            pathParams = {
                    @OpenApiParam(name = "offset", type = Integer.class, required = true),
                    @OpenApiParam(name = "count", type = Integer.class, required = true)
            }
    )
    public void totalWeek(Context ctx) {
        var stats = metrics().reputation().totalWeek(offset(ctx, MAX_WEEK_OFFSET), count(ctx, MAX_WEEKS));
        if ("application/json".equals(ctx.header("Accept"))) {
            ctx.json(stats);
        } else {
            writeImage(ctx, stats.getChart("Total reputation per week"));
        }
    }

    @OpenApi(
            summary = "Get the total count of reputation in these months.",
            operationId = "totalMonth",
            path = "total/month/{offset}/{count}",
            methods = HttpMethod.GET,
            tags = {"Reputation"},
            responses = {
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = byte[].class, type = "image/png")}),
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = CountsStatistic.class, type = "application/json")})
            },
            pathParams = {
                    @OpenApiParam(name = "offset", type = Integer.class, required = true),
                    @OpenApiParam(name = "count", type = Integer.class, required = true)
            }
    )
    public void totalMonth(Context ctx) {
        var stats = metrics().reputation().totalMonth(offset(ctx, MAX_MONTH_OFFSET), count(ctx, MAX_MONTH));
        if ("application/json".equals(ctx.header("Accept"))) {
            ctx.json(stats);
        } else {
            writeImage(ctx, stats.getChart("Total reputation per month"));
        }
    }

    @OpenApi(
            summary = "Get the changed reputation per week.",
            operationId = "changesWeek",
            path = "changes/week/{offset}/{count}",
            methods = HttpMethod.GET,
            tags = {"Reputation"},
            responses = {
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = byte[].class, type = "image/png")}),
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = LabeledCountStatistic.class, type = "application/json")})
            },
            pathParams = {
                    @OpenApiParam(name = "offset", type = Integer.class, required = true),
                    @OpenApiParam(name = "count", type = Integer.class, required = true)
            }
    )
    public void changesWeek(Context ctx) {
        var stats = metrics().reputation().weekChanges(offset(ctx, MAX_WEEK_OFFSET), count(ctx, MAX_WEEKS));
        if ("application/json".equals(ctx.header("Accept"))) {
            ctx.json(stats);
        } else {
            writeImage(ctx, stats.getChart("Reputation changes per week"));
        }
    }

    @OpenApi(
            summary = "Get the changed reputation per month.",
            operationId = "changesMonth",
            path = "changes/month/{offset}/{count}",
            methods = HttpMethod.GET,
            tags = {"Reputation"},
            responses = {
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = byte[].class, type = "image/png")}),
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = LabeledCountStatistic.class, type = "application/json")})
            },
            pathParams = {
                    @OpenApiParam(name = "offset", type = Integer.class, required = true),
                    @OpenApiParam(name = "count", type = Integer.class, required = true)
            }
    )
    public void changesMonth(Context ctx) {
        var stats = metrics().reputation().monthChanges(offset(ctx, MAX_MONTH_OFFSET), count(ctx, MAX_MONTH));
        if ("application/json".equals(ctx.header("Accept"))) {
            ctx.json(stats);
        } else {
            writeImage(ctx, stats.getChart("Reputation changes per month"));
        }
    }

    @OpenApi(
            summary = "Get reputation per day of week.",
            operationId = "dowWeek",
            path = "dow/week/{offset}",
            methods = HttpMethod.GET,
            tags = {"Reputation"},
            responses = {
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = byte[].class, type = "image/png")}),
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = DowsStatistic.class, type = "application/json")})
            },
            pathParams = {
                    @OpenApiParam(name = "offset", type = Integer.class, required = true)
            }
    )
    public void dowWeek(Context ctx) {
        var stats = metrics().reputation().dowWeek(offset(ctx, MAX_WEEK_OFFSET));
        if ("application/json".equals(ctx.header("Accept"))) {
            ctx.json(stats);
        } else {
            writeImage(ctx, stats.getChart("Reputation given per day of week average"));
        }
    }

    @OpenApi(
            summary = "Get average reputation per day of week in a month.",
            operationId = "dowMonth",
            path = "dow/month/{offset}",
            methods = HttpMethod.GET,
            tags = {"Reputation"},
            responses = {
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = byte[].class, type = "image/png")}),
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = DowsStatistic.class, type = "application/json")})
            },
            pathParams = {
                    @OpenApiParam(name = "offset", type = Integer.class, required = true)
            }
    )
    public void dowMonth(Context ctx) {
        var stats = metrics().reputation().dowMonth(offset(ctx, MAX_MONTH_OFFSET));
        if ("application/json".equals(ctx.header("Accept"))) {
            ctx.json(stats);
        } else {
            writeImage(ctx, stats.getChart("Reputation given per day of week average"));
        }
    }

    @OpenApi(
            summary = "Get average reputation per day of week in a year.",
            operationId = "dowYear",
            path = "dow/year/{offset}",
            methods = HttpMethod.GET,
            tags = {"Reputation"},
            responses = {
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = byte[].class, type = "image/png")}),
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = DowsStatistic.class, type = "application/json")})
            },
            pathParams = {
                    @OpenApiParam(name = "offset", type = Integer.class, required = true)
            }
    )
    public void dowYear(Context ctx) {
        var stats = metrics().reputation().dowYear(offset(ctx, MAX_YEAR_OFFSET));
        if ("application/json".equals(ctx.header("Accept"))) {
            ctx.json(stats);
        } else {
            writeImage(ctx, stats.getChart("Reputation given per day of week average"));
        }
    }

    @Override
    public void buildRoutes() {
        path("reputation", () -> {
            path("type", () -> {
                path("count", () -> {
                    get("week/{offset}/{count}", this::countTypeWeek);
                    get("month/{offset}/{count}", this::countTypeMonth);
                });

                path("total", () -> {
                    get("week/{offset}/{count}", this::totalTypeWeek);
                    get("month/{offset}/{count}", this::totalTypeMonth);
                });
            });
            path("count", () -> {
                get("week/{offset}/{count}", this::countWeek);
                get("month/{offset}/{count}", this::countMonth);
            });

            path("total", () -> {
                get("week/{offset}/{count}", this::totalWeek);
                get("month/{offset}/{count}", this::totalMonth);
            });

            path("changes", () -> {
                get("week/{offset}/{count}", this::changesWeek);
                get("month/{offset}/{count}", this::changesMonth);
            });

            path("dow", () -> {
                get("week/{offset}", this::dowWeek);
                get("month/{offset}", this::dowMonth);
                get("year/{offset}", this::dowYear);
            });
        });
    }
}
