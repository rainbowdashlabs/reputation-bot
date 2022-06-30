package de.chojo.repbot.web.routes.v1;

import de.chojo.repbot.dao.provider.Metrics;
import de.chojo.repbot.web.erros.ApiError;
import de.chojo.repbot.web.routes.RoutesBuilder;
import io.javalin.http.Context;
import io.javalin.http.HttpCode;
import org.eclipse.jetty.http.HttpStatus;

public abstract class MetricsHolder implements RoutesBuilder {
    private final Metrics metrics;

    public MetricsHolder(Metrics metrics) {
        this.metrics = metrics;
    }

    public Metrics metrics() {
        return metrics;
    }

    protected void writeImage(Context ctx, byte[] png) {
        ctx.header("Content-Disposition", "attachment; filename=\"stats.png\"");
        ctx.header("X-Content-Type-Options", "nosniff");
        ctx.contentType("image/png");

        ctx.result(png).status(HttpStatus.OK_200);
    }

    protected int offset(Context context, int max) {
        var param = context.pathParam("offset");
        try {
            var offset = Integer.parseInt(param);
            assertSize(offset, 0, max);
            return offset;
        } catch (NumberFormatException e) {
            throw new ApiError(HttpCode.BAD_REQUEST, "Offset is not a number, Got: " + param);
        }
    }

    protected int count(Context context, int max) {
        var param = context.pathParam("count");
        try {
            var offset = Integer.parseInt(param);
            assertSize(offset, 0, max);
            return offset;
        } catch (NumberFormatException e) {
            throw new ApiError(HttpCode.BAD_REQUEST, "Count is not a number, Got: " + param);
        }
    }

    private void assertSize(int value, int min, int max) {
        if (value < min) {
            throw new ApiError(HttpCode.BAD_REQUEST, String.format("Value %s is too small. Min: %s", value, min));
        }
        if (value > max) {
            throw new ApiError(HttpCode.BAD_REQUEST, String.format("Value %s is too large. Max: %s", value, max));
        }
    }
}
