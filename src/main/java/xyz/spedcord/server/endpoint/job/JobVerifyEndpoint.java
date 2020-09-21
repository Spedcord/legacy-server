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

/**
 * @author Maximilian Dorn
 * @version 2.0.0
 * @since 1.0.0
 */
public class JobVerifyEndpoint extends Endpoint {

    private final JobController jobController;
    private final UserController userController;

    public JobVerifyEndpoint(JobController jobController, UserController userController) {
        this.jobController = jobController;
        this.userController = userController;
    }

    @Override
    public void handle(Context context) {
        Optional<Integer> jobIdOptional = this.getQueryParamAsInt("jobId", context);
        if (jobIdOptional.isEmpty()) {
            Responses.error("Invalid jobId param").respondTo(context);
            return;
        }

        Optional<User> optional = this.getUserFromQuery("userId", true, context, this.userController);
        if (optional.isEmpty()) {
            Responses.error("Unknown user / Invalid request").respondTo(context);
            return;
        }
        User user = optional.get();

        if (Arrays.stream(SpedcordServer.MODERATORS).noneMatch(l -> l == user.getDiscordId())) {
            Responses.error(HttpStatus.UNAUTHORIZED_401, "Unauthorized").respondTo(context);
            return;
        }

        Optional<Boolean> verifyOptional = this.getQueryParamAsBoolean("verify", context);
        boolean verify = verifyOptional.orElse(true);

        Job job = this.jobController.getJob(jobIdOptional.get());
        job.setVerifyState(verify ? 1 : 2);
        this.jobController.updateJob(job);

        Responses.success("Job was " + (verify ? "" : "not ") + "verified").respondTo(context);
    }
}
