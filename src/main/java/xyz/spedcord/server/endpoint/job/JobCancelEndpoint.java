package xyz.spedcord.server.endpoint.job;

import io.javalin.http.Context;
import org.eclipse.jetty.http.HttpStatus;
import xyz.spedcord.server.endpoint.Endpoint;
import xyz.spedcord.server.job.JobController;
import xyz.spedcord.server.response.Responses;
import xyz.spedcord.server.user.User;
import xyz.spedcord.server.user.UserController;

import java.util.Optional;

public class JobCancelEndpoint extends Endpoint {

    private final JobController jobController;
    private final UserController userController;

    public JobCancelEndpoint(JobController jobController, UserController userController) {
        this.jobController = jobController;
        this.userController = userController;
    }

    @Override
    public void handle(Context ctx) {
        Optional<User> optional = getUserFromQuery("discordId", true, ctx, userController);
        if (optional.isEmpty()) {
            Responses.error("Unknown user / Invalid request").respondTo(ctx);
            return;
        }
        User user = optional.get();

        if(jobController.getPendingJob(user.getDiscordId()) == null) {
            Responses.error("You don't have a pending job").respondTo(ctx);
            return;
        }

        jobController.cancelJob(user.getDiscordId());
        Responses.success("Job cancelled").respondTo(ctx);
    }

}
