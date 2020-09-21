package xyz.spedcord.server.task;

import com.google.gson.JsonObject;
import xyz.spedcord.common.config.Config;
import xyz.spedcord.server.company.CompanyController;
import xyz.spedcord.server.user.UserController;
import xyz.spedcord.server.util.WebhookUtil;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A task that is designated to paying the company members
 *
 * @author Maximilian Dorn
 * @version 2.0.0
 * @since 1.0.0
 */
public class PayoutTask implements Runnable {

    private final Config config;
    private final CompanyController companyController;
    private final UserController userController;
    private final AtomicLong lastPayout;

    public PayoutTask(Config config, CompanyController companyController, UserController userController) {
        this.config = config;
        this.companyController = companyController;
        this.userController = userController;

        // If config has a lastPayout entry get that entry and save it to a variable
        String lastPayoutStr = this.config.get("lastPayout");
        if (lastPayoutStr == null) {
            lastPayoutStr = "0";
        }
        this.lastPayout = new AtomicLong(Long.parseLong(lastPayoutStr));
    }

    @Override
    public void run() {
        if (System.currentTimeMillis() - this.lastPayout.get() < TimeUnit.DAYS.toMillis(7)) {
            return;
        }

        // Save current time to config
        this.lastPayout.set(System.currentTimeMillis());
        this.config.set("lastPayout", this.lastPayout.get() + "");
        try {
            this.config.save();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Loop through companies
        this.companyController.getCompanies().forEach(company -> {
            // Send a warn message if the company has 0 or negative balance
            if (company.getBalance() <= 0) {
                JsonObject dataObj = new JsonObject();
                dataObj.addProperty("company", company.getId());
                dataObj.addProperty("msg", "The company has a negative balance. Company members can not be paid.");

                WebhookUtil.callWebhooks(-1, dataObj, "WARN");
                return;
            }

            // Collect total payouts and pay the members their payout
            AtomicReference<Double> totalPayouts = new AtomicReference<>(0D);
            company.getMemberDiscordIds().forEach(memberId ->
                    this.userController.getUser(memberId).ifPresent(user -> {
                        // Get the users payout
                        double payout = company.getRoles().stream()
                                .filter(companyRole -> companyRole.getMemberDiscordIds().contains(memberId))
                                .findAny().get().getPayout();

                        // Set balance, update total payout and update user
                        user.setBalance(user.getBalance() + payout);
                        totalPayouts.set(totalPayouts.get() + payout);
                        this.userController.updateUser(user);
                    }));

            // Also do this with the owner because they are not part of the member list
            this.userController.getUser(company.getOwnerDiscordId()).ifPresent(user -> {
                // Get the owners payout
                double payout = company.getRoles().stream()
                        .filter(companyRole -> companyRole.getMemberDiscordIds().contains(user.getDiscordId()))
                        .findAny().get().getPayout();

                // Set balance, update total payout and update user
                user.setBalance(user.getBalance() + payout);
                totalPayouts.set(totalPayouts.get() + payout);
                this.userController.updateUser(user);
            });

            // Subtract the total payout sum from the company balance
            // The balance *can* be negative
            company.setBalance(company.getBalance() - (totalPayouts.get()));
            this.companyController.updateCompany(company);
        });
        System.out.println("Payouts were paid");
    }

}
