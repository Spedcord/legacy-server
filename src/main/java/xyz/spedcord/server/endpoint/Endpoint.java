package xyz.spedcord.server.endpoint;

import io.javalin.http.Context;
import xyz.spedcord.server.SpedcordServer;
import xyz.spedcord.server.company.CompanyRole;
import xyz.spedcord.server.user.User;
import xyz.spedcord.server.user.UserController;

import java.util.Optional;

/**
 * Base class for endpoints
 *
 * @author Maximilian Dorn
 * @version 2.0.0
 * @since 1.0.0
 */
public abstract class Endpoint extends dev.lukaesebrot.jal.endpoints.Endpoint {

    /**
     * @param key     The path parameter key
     * @param context The context
     * @return The path parameter wrapped in an optional
     */
    protected Optional<String> getPathParam(String key, Context context) {
        return Optional.of(context.pathParam(key));
    }

    /**
     * @param key     The path parameter key
     * @param context The context
     * @return The path parameter as int wrapped in an optional
     */
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

    /**
     * @param key     The path parameter key
     * @param context The context
     * @return The path parameter as long wrapped in an optional
     */
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

    /**
     * @param key     The path parameter key
     * @param context The context
     * @return The path parameter as boolean wrapped in an optional
     */
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

    /**
     * @param key     The query parameter key
     * @param context The context
     * @return The query parameter wrapped in an optional
     */
    protected Optional<String> getQueryParam(String key, Context context) {
        return Optional.ofNullable(context.queryParam(key));
    }

    /**
     * @param key     The query parameter key
     * @param context The context
     * @return The query parameter as int wrapped in an optional
     */
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

    /**
     * @param key     The query parameter key
     * @param context The context
     * @return The query parameter as long wrapped in an optional
     */
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

    /**
     * @param key     The query parameter key
     * @param context The context
     * @return The query parameter as double wrapped in an optional
     */
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

    /**
     * @param key     The query parameter key
     * @param context The context
     * @return The query parameter as boolean wrapped in an optional
     */
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

    /**
     * Tries to resolve a company role object from the body
     *
     * @param context The context
     * @return The company role wrapped in an optional
     */
    protected Optional<CompanyRole> getCompanyRoleFromBody(Context context) {
        CompanyRole companyRole = SpedcordServer.GSON.fromJson(context.body(), CompanyRole.class);
        return Optional.ofNullable(companyRole);
    }

    /**
     * Tries to resolve a user from the query parameters
     *
     * @param key            The key for the users Discord id
     * @param passNeeded     Is password needed or not
     * @param context        The context
     * @param userController The user controller
     * @return The user wrapped in an optional.
     * If passNeeded=true and no password / wrong password is provided, the optional will be empty.
     */
    protected Optional<User> getUserFromQuery(String key, boolean passNeeded, Context context, UserController userController) {
        return this.getUser(key, false, passNeeded, context, userController);
    }

    /**
     * Tries to resolve a user from the path parameters
     *
     * @param key            The key for the users Discord id
     * @param passNeeded     Is password needed or not
     * @param context        The context
     * @param userController The user controller
     * @return The user wrapped in an optional.
     * If passNeeded=true and no password / wrong password is provided, the optional will be empty.
     */
    protected Optional<User> getUserFromPath(String key, boolean passNeeded, Context context, UserController userController) {
        return this.getUser(key, true, passNeeded, context, userController);
    }

    /**
     * Tries to resolve a user from the provided parameters
     *
     * @param key            The key for the users Discord id
     * @param path           Whether path params should be used or not
     * @param passNeeded     Is password needed or not
     * @param context        The context
     * @param userController The user controller
     * @return The user wrapped in an optional.
     * If passNeeded=true and no password / wrong password is provided, the optional will be empty.
     */
    private Optional<User> getUser(String key, boolean path, boolean passNeeded, Context context, UserController userController) {
        // Resolve Discord id
        Optional<Long> paramOptional = path ? this.getPathParamAsLong(key, context) : this.getQueryParamAsLong(key, context);
        if (paramOptional.isEmpty()) {
            return Optional.empty();
        }
        long discordId = paramOptional.get();

        // Resolve user
        Optional<User> optional = userController.getUser(discordId);
        if (optional.isEmpty()) {
            return Optional.empty();
        }

        User user = optional.get();
        if (passNeeded) {
            // Resolve password and check for match
            Optional<String> providedKey = path ? this.getPathParam("key", context) : this.getQueryParam("key", context);
            if (providedKey.isEmpty() || !providedKey.get().equals(user.getKey())) {
                return Optional.empty();
            }
        }

        return Optional.of(user);
    }

}
