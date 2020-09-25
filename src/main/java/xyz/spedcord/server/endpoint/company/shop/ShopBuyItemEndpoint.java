package xyz.spedcord.server.endpoint.company.shop;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import io.javalin.http.Context;
import org.eclipse.jetty.http.HttpStatus;
import xyz.spedcord.server.company.Company;
import xyz.spedcord.server.company.CompanyController;
import xyz.spedcord.server.company.CompanyRole;
import xyz.spedcord.server.company.shop.CompanyShop;
import xyz.spedcord.server.company.shop.ShopItem;
import xyz.spedcord.server.endpoint.Endpoint;
import xyz.spedcord.server.endpoint.RestrictedEndpoint;
import xyz.spedcord.server.response.Responses;
import xyz.spedcord.server.user.User;
import xyz.spedcord.server.user.UserController;

import java.util.Optional;

/**
 * @author Maximilian Dorn
 * @version 2.1.12
 * @since 1.0.0
 */
public class ShopBuyItemEndpoint extends Endpoint {

    private final CompanyController companyController;
    private final UserController userController;

    public ShopBuyItemEndpoint(CompanyController companyController, UserController userController) {
        this.companyController = companyController;
        this.userController = userController;
    }

    @Override
    public void handle(Context context) {
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

        Optional<User> userOptional = this.getUserFromQuery("userDiscordId", !RestrictedEndpoint.isAuthorized(context), context, this.userController);
        if (userOptional.isEmpty()) {
            Responses.error("Unknown user / Invalid request").respondTo(context);
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

        User user = userOptional.get();
        Company company = optional.get();
        ShopItem shopItem = itemOptional.get();

        if (company.getId() != user.getCompanyId()) {
            Responses.error("User is not a member of the company").respondTo(context);
            return;
        }

        if (!company.hasPermission(user.getDiscordId(), CompanyRole.Permission.BUY_ITEMS)) {
            Responses.error("Insufficient permissions").respondTo(context);
            return;
        }

        if (!shopItem.isMultipleAllowed() && company.getPurchasedItems().contains(shopItem.getId())) {
            Responses.error("This item can only be purchased once").respondTo(context);
            return;
        }

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
        company.getPurchasedItems().add(shopItem.getId());

        this.companyController.updateCompany(company);

        Responses.success("Item was purchased").respondTo(context);
    }

}
