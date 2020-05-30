package xyz.spedcord.server.endpoint.user;

import com.google.gson.Gson;
import dev.lukaesebrot.jal.endpoints.Endpoint;
import io.javalin.http.Context;
import xyz.spedcord.server.job.Job;
import xyz.spedcord.server.job.JobController;
import xyz.spedcord.server.response.Responses;
import xyz.spedcord.server.user.User;
import xyz.spedcord.server.user.UserController;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class UserJobsEndpoint extends Endpoint {

    private UserController userController;
    private JobController jobController;

    public UserJobsEndpoint(UserController userController, JobController jobController) {
        this.userController = userController;
        this.jobController = jobController;
    }

    @Override
    public void handle(Context context) {
        String rawDiscordId = context.queryParam("discordId");
        if (rawDiscordId == null) {
            Responses.error("Missing discordId param").respondTo(context);
            return;
        }
        long discordId;
        try {
            discordId = Long.parseLong(rawDiscordId);
        } catch (NumberFormatException ignored) {
            Responses.error("Invalid discordId param").respondTo(context);
            return;
        }

        Optional<User> optional = userController.getUser(discordId);
        if (!optional.isPresent()) {
            Responses.error("Unknown user").respondTo(context);
            return;
        }

        User user = optional.get();
        List<Job> jobs = user.getJobList().stream()
                .map(id -> jobController.getJob(id))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        context.result(new Gson().toJson(jobs)).status(200);
    }
}