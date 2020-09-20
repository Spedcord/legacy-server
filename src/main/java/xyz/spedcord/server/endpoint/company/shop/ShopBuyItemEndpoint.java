package xyz.spedcord.server.endpoint.company.shop;

import io.javalin.http.Context;
import xyz.spedcord.server.company.Company;
import xyz.spedcord.server.company.CompanyController;
import xyz.spedcord.server.endpoint.RestrictedEndpoint;
import xyz.spedcord.server.joinlink.JoinLinkController;
import xyz.spedcord.server.response.Responses;

import java.util.Optional;

public class ShopBuyItemEndpoint extends RestrictedEndpoint {

    private final CompanyController companyController;
    private final JoinLinkController joinLinkController;

    public ShopBuyItemEndpoint(CompanyController companyController, JoinLinkController joinLinkController) {
        this.companyController = companyController;
        this.joinLinkController = joinLinkController;
    }

    @Override
    protected void handleFurther(Context context) {
        System.out.println(0);

        Optional<Long> paramOptional = this.getQueryParamAsLong("discordServerId", context);
        if (paramOptional.isEmpty()) {
            Responses.error("Invalid discordServerId param").respondTo(context);
            return;
        }
        long discordServerId = paramOptional.get();

        System.out.println(1);

        Optional<Company> optional = this.companyController.getCompany(discordServerId);
        if (optional.isEmpty()) {
            Responses.error("Unknown company").respondTo(context);
            return;
        }
        Company company = optional.get();

        System.out.println(2);

        Optional<String> itemOptional = this.getQueryParam("item", context);
        if (itemOptional.isEmpty()) {
            Responses.error("Invalid item param").respondTo(context);
            return;
        }
        String item = itemOptional.get();

        System.out.println(3);

        double price;
        switch (item.toLowerCase()) {
            case "custom perma invite":
                price = 450_000;
                if (price > company.getBalance()) {
                    Responses.error("Not enough money").respondTo(context);
                    return;
                }

                Optional<String> joinIdOptional = this.getQueryParam("joinId", context);
                if (joinIdOptional.isEmpty()) {
                    Responses.error("Custom joinId is not present").respondTo(context);
                    return;
                }
                String joinId = joinIdOptional.get();

                if (this.joinLinkController.getCompanyId(joinId) != -1) {
                    Responses.error("Custom joinId is already taken").respondTo(context);
                    return;
                }

                company.setBalance(company.getBalance() - price);
                this.companyController.updateCompany(company);
                this.joinLinkController.addCustomLink(joinId, company.getId(), -1);

                Responses.success("Item was purchased").respondTo(context);
                break;
            default:
                Responses.error(404, "Item not found").respondTo(context);
                break;
        }
    }
}
