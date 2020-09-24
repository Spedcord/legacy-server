package xyz.spedcord.server.endpoint.company.shop;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import io.javalin.http.Context;
import org.eclipse.jetty.http.HttpStatus;
import xyz.spedcord.server.company.Company;
import xyz.spedcord.server.company.CompanyController;
import xyz.spedcord.server.company.shop.CompanyShop;
import xyz.spedcord.server.company.shop.ShopItem;
import xyz.spedcord.server.endpoint.RestrictedEndpoint;
import xyz.spedcord.server.joinlink.JoinLinkController;
import xyz.spedcord.server.response.Responses;

import java.util.Optional;

/**
 * @author Maximilian Dorn
 * @version 2.1.10
 * @since 1.0.0
 */
public class ShopBuyItemEndpoint extends RestrictedEndpoint {

    private final CompanyController companyController;
    private final JoinLinkController joinLinkController;

    public ShopBuyItemEndpoint(CompanyController companyController, JoinLinkController joinLinkController) {
        this.companyController = companyController;
        this.joinLinkController = joinLinkController;
    }

    @Override
    protected void handleFurther(Context context) {
        Optional<Long> paramOptional = this.getQueryParamAsLong("discordServerId", context);
        if (paramOptional.isEmpty()) {
            Responses.error("Invalid discordServerId param").respondTo(context);
            return;
        }
        long discordServerId = paramOptional.get();

        Optional<Company> optional = this.companyController.getCompany(discordServerId);
        if (optional.isEmpty()) {
            Responses.error("Unknown company").respondTo(context);
            return;
        }

        Optional<Integer> itemIdOptional = this.getQueryParamAsInt("item", context);
        if (itemIdOptional.isEmpty()) {
            Responses.error("Invalid item param").respondTo(context);
            return;
        }
        int itemId = itemIdOptional.get();

        Optional<ShopItem> itemOptional = CompanyShop.getShopItemById(itemId);
        if (itemOptional.isEmpty()) {
            Responses.error(HttpStatus.NOT_FOUND_404, "Unknown item").respondTo(context);
            return;
        }

        String argsStr = this.getQueryParam("args", context).orElse("[]");
        JsonArray array;
        try {
            array = JsonParser.parseString(argsStr).getAsJsonArray();
        } catch (Exception ignored) {
            Responses.error("Invalid json").respondTo(context);
            return;
        }

        Object[] args = new Object[array.size()];
        for (int i = 0; i < array.size(); i++) {
            // This should be replaced but it works for now I guess
            args[i] = array.get(i).getAsString();
        }

        Company company = optional.get();
        ShopItem shopItem = itemOptional.get();

        double price = shopItem.getPrice(company);
        if (company.getBalance() < price) {
            Responses.error("Not enough founds").respondTo(context);
            return;
        }

        if (!shopItem.activate(company, args)) {
            Responses.error("Purchase was cancelled (Hint: check your arguments)").respondTo(context);
            return;
        }
        company.setBalance(company.getBalance() - price);

        this.companyController.updateCompany(company);

        Responses.success("Item was purchased").respondTo(context);
    }
}
