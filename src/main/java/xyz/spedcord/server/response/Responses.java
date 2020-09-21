package xyz.spedcord.server.response;

import dev.lukaesebrot.jal.responses.ResponseBuilder;
import dev.lukaesebrot.jal.responses.ResponseType;
import org.eclipse.jetty.http.HttpStatus;

/**
 * @author Maximilian Dorn
 * @version 2.0.0
 * @since 1.0.0
 */
public class Responses {

    private Responses() {
        throw new UnsupportedOperationException();
    }

    public static ResponseBuilder error(String message, String... details) {
        return error(HttpStatus.BAD_REQUEST_400, message, details);
    }

    public static ResponseBuilder error(int code, String message, String... details) {
        return new ResponseBuilder(code)
                .withResponseType(ResponseType.ERROR)
                .addData("message", message)
                .addData("details", details);
    }

    public static ResponseBuilder success(String message) {
        return success(HttpStatus.OK_200, message);
    }

    public static ResponseBuilder success(int code, String message) {
        return new ResponseBuilder(code)
                .withResponseType(ResponseType.SUCCESS)
                .addData("message", message);
    }

/*    public static ResponseBuilder userInfo(User user, String providedPass) {
        ResponseBuilder responseBuilder = new ResponseBuilder(HttpStatus.OK_200)
                .withResponseType(ResponseType.SUCCESS)
                .addData("name", user.getName())
                .addData("discordId", user.getDiscordId())
                .addData("avatarLink", user.getAvatarLink())
                .addData("balance", Double.parseDouble(String.format("%.2f", user.getBalance())))
                .addData("companyId", user.getCompanyId());

        if (user.getHashedPassword().equals(providedPass)) {
            responseBuilder.addData("jobs", JsonParser.parseString("[" + user.getJobs().stream().map(Job::toJson)
                    .collect(Collectors.joining(",")) + "]").getAsJsonArray());
        }

        return responseBuilder;
    }

    public static ResponseBuilder companyInfo(Company company) {
        return new ResponseBuilder(HttpStatus.OK_200)
                .withResponseType(ResponseType.SUCCESS)
                .addData("id", company.getId())
                .addData("name", company.getName())
                .addData("discordServerId", company.getDiscordId())
                .addData("ownerId", company.getOwnerId())
                .addData("balance", Double.parseDouble(String.format("%.2f", company.getBalance())));
    }*/

}
