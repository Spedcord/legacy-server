package xyz.spedcord.server.endpoint.job;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.javalin.http.Context;
import org.eclipse.jetty.http.HttpStatus;
import xyz.spedcord.server.SpedcordServer;
import xyz.spedcord.server.endpoint.Endpoint;
import xyz.spedcord.server.job.Job;
import xyz.spedcord.server.job.JobController;
import xyz.spedcord.server.response.Responses;
import xyz.spedcord.server.user.User;
import xyz.spedcord.server.user.UserController;

import java.util.Arrays;
import java.util.Optional;

public class JobListUnverifiedEndpoint extends Endpoint {

    private final JobController jobController;
    private final UserController userController;

    public JobListUnverifiedEndpoint(JobController jobController, UserController userController) {
        this.jobController = jobController;
        this.userController = userController;
    }

    @Override
    public void handle(Context context) {
        Optional<User> optional = getUserFromPath("userId", true, context, userController);
        if (optional.isEmpty()) {
            Responses.error("Unknown user / Invalid request").respondTo(context);
            return;
        }
        User user = optional.get();

        if (Arrays.stream(SpedcordServer.MODERATORS).noneMatch(l -> l == user.getDiscordId())) {
            Responses.error(HttpStatus.UNAUTHORIZED_401, "Unauthorized").respondTo(context);
            return;
        }

        JsonArray array = new JsonArray();
        for (Job job : jobController.getUnverifiedJobs()) {
            JsonObject obj = SpedcordServer.GSON.toJsonTree(job).getAsJsonObject();
            obj.addProperty("userId", userController.getUserByJobId(job.getId()).orElse(User.EMPTY).getDiscordId());
            array.add(obj);
        }

        context.result(array.toString()).status(200);
    }
}
