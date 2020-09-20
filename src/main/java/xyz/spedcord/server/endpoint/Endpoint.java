package xyz.spedcord.server.endpoint;

import io.javalin.http.Context;
import xyz.spedcord.server.SpedcordServer;
import xyz.spedcord.server.company.CompanyRole;
import xyz.spedcord.server.user.User;
import xyz.spedcord.server.user.UserController;

import java.util.Optional;

public abstract class Endpoint extends dev.lukaesebrot.jal.endpoints.Endpoint {

    protected Optional<String> getPathParam(String key, Context context) {
        return Optional.of(context.pathParam(key));
    }

    protected Optional<Integer> getPathParamAsInt(String key, Context context) {
        Optional<String> param = this.getPathParam(key, context);
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
        Optional<String> param = this.getPathParam(key, context);
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
        Optional<String> param = this.getPathParam(key, context);
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
        Optional<String> param = this.getQueryParam(key, context);
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
        Optional<String> param = this.getQueryParam(key, context);
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
        Optional<String> param = this.getQueryParam(key, context);
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
        Optional<String> param = this.getQueryParam(key, context);
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

    protected Optional<CompanyRole> getCompanyRoleFromBody(Context context) {
        CompanyRole companyRole = SpedcordServer.GSON.fromJson(context.body(), CompanyRole.class);
        return Optional.ofNullable(companyRole);
    }

    protected Optional<User> getUserFromQuery(String key, boolean passNeeded, Context context, UserController userController) {
        return this.getUser(key, false, passNeeded, context, userController);
    }

    protected Optional<User> getUserFromPath(String key, boolean passNeeded, Context context, UserController userController) {
        return this.getUser(key, true, passNeeded, context, userController);
    }

    private Optional<User> getUser(String key, boolean path, boolean passNeeded, Context context, UserController userController) {
        Optional<Long> paramOptional = path ? this.getPathParamAsLong(key, context) : this.getQueryParamAsLong(key, context);
        if (paramOptional.isEmpty()) {
            return Optional.empty();
        }
        long discordId = paramOptional.get();

        Optional<User> optional = userController.getUser(discordId);
        if (optional.isEmpty()) {
            return Optional.empty();
        }

        User user = optional.get();
        if (passNeeded) {
            Optional<String> providedKey = path ? this.getPathParam("key", context) : this.getQueryParam("key", context);
            if (providedKey.isEmpty() || !providedKey.get().equals(user.getKey())) {
                return Optional.empty();
            }
        }

        return Optional.of(user);
    }

}
