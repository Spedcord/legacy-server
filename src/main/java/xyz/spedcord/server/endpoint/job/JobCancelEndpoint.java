package xyz.spedcord.server.endpoint.job;

import io.javalin.http.Context;
import xyz.spedcord.server.endpoint.Endpoint;
import xyz.spedcord.server.job.JobController;
import xyz.spedcord.server.response.Responses;
import xyz.spedcord.server.user.User;
import xyz.spedcord.server.user.UserController;

import java.util.Optional;

/**
 * @author Maximilian Dorn
 * @version 2.0.0
 * @since 1.0.0
 */
public class JobCancelEndpoint extends Endpoint {

    private final JobController jobController;
    private final UserController userController;

    public JobCancelEndpoint(JobController jobController, UserController userController) {
        this.jobController = jobController;
        this.userController = userController;
    }

    @Override
    public void handle(Context ctx) {
        Optional<User> optional = this.getUserFromQuery("discordId", true, ctx, this.userController);
        if (optional.isEmpty()) {
            Responses.error("Unknown user / Invalid request").respondTo(ctx);
            return;
        }
        User user = optional.get();

        if (this.jobController.getPendingJob(user.getDiscordId()) == null) {
            Responses.error("You don't have a pending job").respondTo(ctx);
            return;
        }

        this.jobController.cancelJob(user.getDiscordId());
        Responses.success("Job cancelled").respondTo(ctx);
    }

}
