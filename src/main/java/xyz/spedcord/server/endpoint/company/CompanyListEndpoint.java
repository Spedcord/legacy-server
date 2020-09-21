package xyz.spedcord.server.endpoint.company;

import io.javalin.http.Context;
import xyz.spedcord.server.SpedcordServer;
import xyz.spedcord.server.company.Company;
import xyz.spedcord.server.company.CompanyController;
import xyz.spedcord.server.endpoint.Endpoint;
import xyz.spedcord.server.user.User;
import xyz.spedcord.server.user.UserController;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Maximilian Dorn
 * @version 2.0.0
 * @since 1.0.0
 */
public class CompanyListEndpoint extends Endpoint {

    private static UserController userController;
    private final CompanyController companyController;

    public CompanyListEndpoint(CompanyController companyController, UserController userController) {
        this.companyController = companyController;
        CompanyListEndpoint.userController = userController;
    }

    @Override
    public void handle(Context ctx) {
        SortMode sortMode;
        try {
            sortMode = SortMode.valueOf(ctx.pathParam("sortMode"));
        } catch (Exception ignored) {
            sortMode = SortMode.MOST_MONEY;
        }

        List<Company> companies = this.companyController.getCompanies().stream()
                .limit(5)
                .sorted(sortMode.comparator.reversed())
                .collect(Collectors.toList());
        ctx.status(200).result(SpedcordServer.GSON.toJson(companies));
    }

    public enum SortMode {
        MOST_MONEY(Comparator.comparingDouble(Company::getBalance)),
        MOST_MEMBERS(Comparator.comparingInt(c -> c.getMemberDiscordIds().size())),
        MOST_JOBS(Comparator.comparingInt(c -> {
            int jobs = 0;
            for (long id : c.getMemberDiscordIds()) {
                Optional<User> user = userController.getUser(id);
                if (user.isPresent()) {
                    jobs += user.get().getJobList().size();
                }
            }

            Optional<User> user = userController.getUser(c.getOwnerDiscordId());
            if (user.isPresent()) {
                jobs += user.get().getJobList().size();
            }

            return jobs;
        }));

        private final Comparator<Company> comparator;

        SortMode(Comparator<Company> comparator) {
            this.comparator = comparator;
        }

        public Comparator<Company> getComparator() {
            return this.comparator;
        }

    }
}
