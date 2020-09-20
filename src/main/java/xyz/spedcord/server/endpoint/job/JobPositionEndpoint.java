package xyz.spedcord.server.endpoint.job;

import io.javalin.http.Context;
import xyz.spedcord.server.endpoint.Endpoint;
import xyz.spedcord.server.job.Job;
import xyz.spedcord.server.job.JobController;
import xyz.spedcord.server.job.Location;
import xyz.spedcord.server.response.Responses;
import xyz.spedcord.server.user.User;
import xyz.spedcord.server.user.UserController;

import java.util.Optional;

public class JobPositionEndpoint extends Endpoint {

    private final UserController userController;
    private final JobController jobController;

    public JobPositionEndpoint(UserController userController, JobController jobController) {
        this.userController = userController;
        this.jobController = jobController;
    }

    @Override
    public void handle(Context ctx) {
        Optional<String> xzOptional = getQueryParam("xz", ctx);
        if (xzOptional.isEmpty()) {
            Responses.error("Invalid xz param").respondTo(ctx);
            return;
        }
        String xz = xzOptional.get();
        long x;
        long z;
        try {
            String[] split = xz.split(";");
            x = Long.parseLong(split[0]);
            z = Long.parseLong(split[1]);
        } catch (Exception ignored) {
            Responses.error("Invalid xz param").respondTo(ctx);
            return;
        }

        Optional<User> optional = getUserFromQuery("discordId", true, ctx, userController);
        if (optional.isEmpty()) {
            Responses.error("Unknown user / Invalid request").respondTo(ctx);
            return;
        }
        User user = optional.get();

        if (jobController.getPendingJob(user.getDiscordId()) == null) {
            Responses.error("You don't have a pending job").respondTo(ctx);
            return;
        }

        Job pendingJob = jobController.getPendingJob(user.getDiscordId());
        pendingJob.getPositions().add(new Location(x, 0, z));

        Responses.success("Position saved").respondTo(ctx);
    }
}
