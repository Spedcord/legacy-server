package xyz.spedcord.server.endpoint.job;

import io.javalin.http.Context;
import org.eclipse.jetty.http.HttpStatus;
import xyz.spedcord.server.SpedcordServer;
import xyz.spedcord.server.endpoint.Endpoint;
import xyz.spedcord.server.job.JobController;
import xyz.spedcord.server.response.Responses;
import xyz.spedcord.server.user.User;
import xyz.spedcord.server.user.UserController;

import java.util.Optional;

public class JobStartEndpoint extends Endpoint {

    private final JobController jobController;
    private final UserController userController;

    public JobStartEndpoint(JobController jobController, UserController userController) {
        this.jobController = jobController;
        this.userController = userController;
    }

    @Override
    public void handle(Context ctx) {
        String body = ctx.body();
        JobStartBody jobStartBody;
        try {
            jobStartBody = SpedcordServer.GSON.fromJson(body, JobStartBody.class);
        } catch (Exception ignored) {
            Responses.error("Invalid request body").respondTo(ctx);
            return;
        }

        if (!jobStartBody.verify()) {
            Responses.error("Invalid request body").respondTo(ctx);
            return;
        }

        Optional<User> userOptional = this.userController.getUser(jobStartBody.discordId);
        if (userOptional.isEmpty()) {
            Responses.error("Unknown user").respondTo(ctx);
            return;
        }
        User user = userOptional.get();

        if (!user.getKey().equals(jobStartBody.key)) {
            Responses.error(HttpStatus.UNAUTHORIZED_401, "Unauthorized").respondTo(ctx);
            return;
        }

        if (!this.jobController.canStartJob(jobStartBody.discordId)) {
            Responses.error("You already have a pending job").respondTo(ctx);
            return;
        }

        this.jobController.startJob(
                jobStartBody.discordId,
                jobStartBody.fromCity,
                jobStartBody.toCity,
                jobStartBody.truck,
                jobStartBody.cargo,
                Math.ceil(jobStartBody.cargoWeight)
        );
        Responses.success("Job started").respondTo(ctx);
    }

    private static class JobStartBody {
        public Long discordId;
        public String key;
        public String fromCity;
        public String toCity;
        public String truck;
        public String cargo;
        public Double cargoWeight;

        public JobStartBody(Long discordId, String key, String fromCity, String toCity, String truck, String cargo, Double cargoWeight) {
            this.discordId = discordId;
            this.key = key;
            this.fromCity = fromCity;
            this.toCity = toCity;
            this.truck = truck;
            this.cargo = cargo;
            this.cargoWeight = cargoWeight;
        }

        public boolean verify() {
            return this.discordId != null && this.key != null && this.fromCity != null && this.toCity != null &&
                    this.truck != null && this.cargo != null && this.cargoWeight != null;
        }
    }
}
