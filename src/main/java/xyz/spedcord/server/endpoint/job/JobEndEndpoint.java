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
        Optional<Double> payOptional = getQueryParamAsDouble("pay", ctx);
        if (payOptional.isEmpty()) {
            Responses.error("Invalid pay param").respondTo(ctx);
            return;
        }
        double pay = payOptional.get();

        Optional<User> optional = getUserFromPath("discordId", true, ctx, userController);
        if (optional.isEmpty()) {
            Responses.error("Unknown user / Invalid request").respondTo(ctx);
            return;
        }
        User user = optional.get();

        if (jobController.getPendingJob(user.getDiscordId()) == null) {
            Responses.error("You don't have a pending job").respondTo(ctx);
            return;
        }

        Job job = jobController.getPendingJob(user.getDiscordId());
        jobController.endJob(user.getDiscordId(), pay);
        user.getJobList().add(job.getId());
        userController.updateUser(user);

        Statistics statistics = statsController.getStatistics();
        statistics.setTotalJobs(statistics.getTotalJobs() + 1);
        statistics.setTotalMoneyMade(statistics.getTotalMoneyMade() + pay);
        statsController.update();

        double companyPay = pay * (35d / 100d);
        Optional<Company> companyOptional = companyController.getCompany(user.getCompanyId());
        if (companyOptional.isPresent()) {
            Company company = companyOptional.get();
            company.setBalance(company.getBalance() + companyPay);
            companyController.updateCompany(company);
        }

        Responses.success("Job ended").respondTo(ctx);
    }

}
