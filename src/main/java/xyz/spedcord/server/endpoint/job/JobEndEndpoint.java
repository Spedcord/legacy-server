package xyz.spedcord.server.endpoint.job;

import io.javalin.http.Context;
import xyz.spedcord.server.company.Company;
import xyz.spedcord.server.company.CompanyController;
import xyz.spedcord.server.endpoint.Endpoint;
import xyz.spedcord.server.job.Job;
import xyz.spedcord.server.job.JobController;
import xyz.spedcord.server.response.Responses;
import xyz.spedcord.server.statistics.Statistics;
import xyz.spedcord.server.statistics.StatisticsController;
import xyz.spedcord.server.user.User;
import xyz.spedcord.server.user.UserController;

import java.util.Optional;

/**
 * Handles job ends
 *
 * @author Maximilian Dorn
 * @version 2.1.4
 * @since 1.0.0
 */
public class JobEndEndpoint extends Endpoint {

    private final JobController jobController;
    private final UserController userController;
    private final CompanyController companyController;
    private final StatisticsController statsController;

    public JobEndEndpoint(JobController jobController, UserController userController, CompanyController companyController, StatisticsController statsController) {
        this.jobController = jobController;
        this.userController = userController;
        this.companyController = companyController;
        this.statsController = statsController;
    }

    @Override
    public void handle(Context ctx) {
        // Get job income
        Optional<Double> payOptional = this.getQueryParamAsDouble("pay", ctx);
        if (payOptional.isEmpty()) {
            Responses.error("Invalid pay param").respondTo(ctx);
            return;
        }
        double pay = payOptional.get();

        // Get user
        Optional<User> optional = this.getUserFromQuery("discordId", true, ctx, this.userController);
        if (optional.isEmpty()) {
            Responses.error("Unknown user / Invalid request").respondTo(ctx);
            return;
        }
        User user = optional.get();

        // Abort if user has no pending job
        if (this.jobController.getPendingJob(user.getDiscordId()) == null) {
            Responses.error("You don't have a pending job").respondTo(ctx);
            return;
        }

        // Calculate company cut
        double companyPay = pay * (35d / 100d);
        Optional<Company> companyOptional = this.companyController.getCompany(user.getCompanyId());
        if (companyOptional.isPresent()) {
            Company company = companyOptional.get();
            company.setBalance(company.getBalance() + companyPay);
            this.companyController.updateCompany(company);
        }

        // End job
        Job job = this.jobController.getPendingJob(user.getDiscordId());
        this.jobController.endJob(user.getDiscordId(), companyPay);

        // Update user
        user.getJobList().add(job.getId());
        this.userController.updateUser(user);

        // Update stats
        Statistics statistics = this.statsController.getStatistics();
        statistics.setTotalJobs(statistics.getTotalJobs() + 1);
        statistics.setTotalMoneyMade(statistics.getTotalMoneyMade() + pay);
        this.statsController.update();

        Responses.success("Job ended").respondTo(ctx);
    }

}
