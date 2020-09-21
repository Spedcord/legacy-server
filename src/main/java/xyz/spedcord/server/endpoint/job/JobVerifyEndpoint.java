package xyz.spedcord.server.endpoint.job;

import io.javalin.http.Context;
import org.eclipse.jetty.http.HttpStatus;
import xyz.spedcord.server.endpoint.Endpoint;
import xyz.spedcord.server.job.Job;
import xyz.spedcord.server.job.JobController;
import xyz.spedcord.server.response.Responses;
import xyz.spedcord.server.user.User;
import xyz.spedcord.server.user.UserController;

import java.util.Optional;

/**
 * Handles job verifications
 *
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
        // Get job id
        Optional<Integer> jobIdOptional = this.getQueryParamAsInt("jobId", context);
        if (jobIdOptional.isEmpty()) {
            Responses.error("Invalid jobId param").respondTo(context);
            return;
        }

        // Get user
        Optional<User> optional = this.getUserFromQuery("userId", true, context, this.userController);
        if (optional.isEmpty()) {
            Responses.error("Unknown user / Invalid request").respondTo(context);
            return;
        }
        User user = optional.get();

        // Abort if user is not a mod
        if (user.getAccountType() == User.AccountType.USER) {
            Responses.error(HttpStatus.UNAUTHORIZED_401, "Unauthorized").respondTo(context);
            return;
        }

        // Get verify bool
        Optional<Boolean> verifyOptional = this.getQueryParamAsBoolean("verify", context);
        boolean verify = verifyOptional.orElse(true);

        // Update job
        Job job = this.jobController.getJob(jobIdOptional.get());
        job.setVerifyState(verify ? 1 : 2);
        this.jobController.updateJob(job);

        Responses.success("Job was " + (verify ? "" : "not ") + "verified").respondTo(context);
    }
}
