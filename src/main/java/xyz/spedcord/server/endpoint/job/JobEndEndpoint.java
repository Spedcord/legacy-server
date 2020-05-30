package xyz.spedcord.server.endpoint.job;

import com.google.gson.Gson;
import io.javalin.http.Context;
import org.eclipse.jetty.http.HttpStatus;
import xyz.spedcord.server.endpoint.Endpoint;
import xyz.spedcord.server.job.Job;
import xyz.spedcord.server.job.JobController;
import xyz.spedcord.server.response.Responses;
import xyz.spedcord.server.user.User;
import xyz.spedcord.server.user.UserController;

import java.util.Optional;

public class JobEndEndpoint extends Endpoint {

    private final JobController jobController;
    private final UserController userController;

    public JobEndEndpoint(JobController jobController, UserController userController) {
        this.jobController = jobController;
        this.userController = userController;
    }

    @Override
    public void handle(Context ctx) {
        Optional<String> keyOptional = getQueryParam("key", ctx);
        if(keyOptional.isEmpty()) {
            Responses.error("Invalid key param").respondTo(ctx);
            return;
        }
        String key = keyOptional.get();

        Optional<Long> discordIdOptional = getQueryParamAsLong("discordId", ctx);
        if(discordIdOptional.isEmpty()) {
            Responses.error("Invalid discordId param").respondTo(ctx);
            return;
        }
        long discordId = discordIdOptional.get();

        Optional<Double> payOptional = getQueryParamAsDouble("pay", ctx);
        if(payOptional.isEmpty()) {
            Responses.error("Invalid pay param").respondTo(ctx);
            return;
        }
        double pay = payOptional.get();

        Optional<User> userOptional = userController.getUser(discordId);
        if (userOptional.isEmpty()) {
            Responses.error("Unknown user").respondTo(ctx);
            return;
        }
        User user = userOptional.get();

        if (!user.getKey().equals(key)) {
            Responses.error(HttpStatus.UNAUTHORIZED_401, "Unauthorized").respondTo(ctx);
            return;
        }

        if(jobController.getPendingJob(discordId) == null) {
            Responses.error("You don't have a pending job").respondTo(ctx);
            return;
        }

        Job job = jobController.getPendingJob(discordId);
        jobController.endJob(discordId, pay);
        user.getJobList().add(job.getId());
        userController.updateUser(user);

        Responses.success("Job ended").respondTo(ctx);
    }

}
