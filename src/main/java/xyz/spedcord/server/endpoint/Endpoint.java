package xyz.spedcord.server.endpoint;

import io.javalin.http.Context;

import java.util.Optional;

public abstract class Endpoint extends dev.lukaesebrot.jal.endpoints.Endpoint {

    protected Optional<String> getPathParam(String key, Context context) {
        return Optional.of(context.pathParam(key));
    }

    protected Optional<Integer> getPathParamAsInt(String key, Context context) {
        Optional<String> param = getPathParam(key, context);
        if (param.isEmpty()) {
            return Optional.empty();
        }

        try {
            return Optional.of(Integer.parseInt(param.get()));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    protected Optional<Long> getPathParamAsLong(String key, Context context) {
        Optional<String> param = getPathParam(key, context);
        if (param.isEmpty()) {
            return Optional.empty();
        }

        try {
            return Optional.of(Long.parseLong(param.get()));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    protected Optional<Boolean> getPathParamAsBoolean(String key, Context context) {
        Optional<String> param = getPathParam(key, context);
        if (param.isEmpty()) {
            return Optional.empty();
        }

        switch (param.get().toLowerCase()) {
            case "yes":
            case "true":
                return Optional.of(true);
            case "no":
            case "false":
                return Optional.of(false);
            default:
                return Optional.empty();
        }
    }

    protected Optional<String> getQueryParam(String key, Context context) {
        return Optional.ofNullable(context.queryParam(key));
    }

    protected Optional<Integer> getQueryParamAsInt(String key, Context context) {
        Optional<String> param = getQueryParam(key, context);
        if (param.isEmpty()) {
            return Optional.empty();
        }

        try {
            return Optional.of(Integer.parseInt(param.get()));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    protected Optional<Long> getQueryParamAsLong(String key, Context context) {
        Optional<String> param = getQueryParam(key, context);
        if (param.isEmpty()) {
            return Optional.empty();
        }

        try {
            return Optional.of(Long.parseLong(param.get()));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    protected Optional<Double> getQueryParamAsDouble(String key, Context context) {
        Optional<String> param = getQueryParam(key, context);
        if (param.isEmpty()) {
            return Optional.empty();
        }

        try {
            return Optional.of(Double.parseDouble(param.get()));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    protected Optional<Boolean> getQueryParamAsBoolean(String key, Context context) {
        Optional<String> param = getQueryParam(key, context);
        if (param.isEmpty()) {
            return Optional.empty();
        }

        switch (param.get().toLowerCase()) {
            case "yes":
            case "true":
                return Optional.of(true);
            case "no":
            case "false":
                return Optional.of(false);
            default:
                return Optional.empty();
        }
    }

}
