package xyz.spedcord.server.endpoint.job;

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

public class JobVerifyEndpoint extends Endpoint {

    private final JobController jobController;
    private final UserController userController;

    public JobVerifyEndpoint(JobController jobController, UserController userController) {
        this.jobController = jobController;
        this.userController = userController;
    }

    @Override
    public void handle(Context context) {
        Optional<Integer> jobIdOptional = getQueryParamAsInt("jobId", context);
        if (jobIdOptional.isEmpty()) {
            Responses.error("Invalid jobId param").respondTo(context);
            return;
        }

        Optional<Long> userIdOptional = getQueryParamAsLong("userId", context);
        if (userIdOptional.isEmpty()) {
            Responses.error(HttpStatus.UNAUTHORIZED_401, "Unauthorized").respondTo(context);
            return;
        }

        Optional<String> keyOptional = getQueryParam("key", context);
        if (keyOptional.isEmpty()) {
            Responses.error(HttpStatus.UNAUTHORIZED_401, "Unauthorized").respondTo(context);
            return;
        }

        long userId = userIdOptional.get();
        String key = keyOptional.get();

        Optional<User> userOptional = userController.getUser(userId);
        if (userOptional.isEmpty()) {
            Responses.error(HttpStatus.UNAUTHORIZED_401, "Unauthorized").respondTo(context);
            return;
        }

        User user = userOptional.get();
        if (!user.getKey().equals(key)) {
            Responses.error(HttpStatus.UNAUTHORIZED_401, "Unauthorized").respondTo(context);
            return;
        }

        if (Arrays.stream(SpedcordServer.MODERATORS).noneMatch(l -> l == user.getDiscordId())) {
            Responses.error(HttpStatus.UNAUTHORIZED_401, "Unauthorized").respondTo(context);
            return;
        }

        Optional<Boolean> verifyOptional = getQueryParamAsBoolean("verify", context);
        boolean verify = verifyOptional.orElse(true);

        Job job = jobController.getJob(jobIdOptional.get());
        job.setVerified(verify);
        jobController.updateJob(job);

        Responses.success("Job was " + (verify ? "" : "not ") + "verified").respondTo(context);
    }
}
