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

/**
 * Lists unverified jobs
 *
 * @author Maximilian Dorn
 * @version 2.0.0
 * @since 1.0.0
 */
public class JobListUnverifiedEndpoint extends Endpoint {

    private final JobController jobController;
    private final UserController userController;

    public JobListUnverifiedEndpoint(JobController jobController, UserController userController) {
        this.jobController = jobController;
        this.userController = userController;
    }

    @Override
    public void handle(Context context) {
        // Get user
        Optional<User> optional = this.getUserFromQuery("userId", true, context, this.userController);
        if (optional.isEmpty()) {
            Responses.error("Unknown user / Invalid request").respondTo(context);
            return;
        }
        User user = optional.get();

        // Abort if user is not a mod
        if (Arrays.stream(SpedcordServer.MODERATORS).noneMatch(l -> l == user.getDiscordId())) {
            Responses.error(HttpStatus.UNAUTHORIZED_401, "Unauthorized").respondTo(context);
            return;
        }

        JsonArray array = new JsonArray();
        for (Job job : this.jobController.getUnverifiedJobs()) {
            JsonObject obj = SpedcordServer.GSON.toJsonTree(job).getAsJsonObject();
            obj.addProperty("userId", this.userController.getUserByJobId(job.getId()).orElse(User.EMPTY).getDiscordId());
            array.add(obj);
        }

        context.result(array.toString()).status(200);
    }
}
