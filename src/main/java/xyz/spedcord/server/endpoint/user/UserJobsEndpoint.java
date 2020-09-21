package xyz.spedcord.server.endpoint.user;

import io.javalin.http.Context;
import xyz.spedcord.server.SpedcordServer;
import xyz.spedcord.server.endpoint.Endpoint;
import xyz.spedcord.server.job.Job;
import xyz.spedcord.server.job.JobController;
import xyz.spedcord.server.response.Responses;
import xyz.spedcord.server.user.User;
import xyz.spedcord.server.user.UserController;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Returns the jobs of a user
 *
 * @author Maximilian Dorn
 * @version 2.0.0
 * @since 1.0.0
 */
public class UserJobsEndpoint extends Endpoint {

    private final UserController userController;
    private final JobController jobController;

    public UserJobsEndpoint(UserController userController, JobController jobController) {
        this.userController = userController;
        this.jobController = jobController;
    }

    @Override
    public void handle(Context context) {
        // Get internal user
        Optional<User> optional = this.getUserFromPath("discordId", false, context, this.userController);
        if (optional.isEmpty()) {
            Responses.error("Unknown user / Invalid request").respondTo(context);
            return;
        }
        User user = optional.get();

        // Retrieve jobs
        List<Job> jobs = new ArrayList<>(user.getJobList()).stream()
                .map(this.jobController::getJob)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        context.result(SpedcordServer.GSON.toJson(jobs)).status(200);
    }

}
