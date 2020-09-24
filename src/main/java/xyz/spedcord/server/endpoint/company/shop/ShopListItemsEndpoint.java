package xyz.spedcord.server.endpoint.company.shop;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.javalin.http.Context;
import xyz.spedcord.server.company.Company;
import xyz.spedcord.server.company.CompanyController;
import xyz.spedcord.server.company.shop.CompanyShop;
import xyz.spedcord.server.endpoint.Endpoint;
import xyz.spedcord.server.response.Responses;

import java.util.Optional;

/**
 * Lists available shop items
 *
 * @author Maximilian Dorn
 * @version 2.1.11
 * @since 2.1.11
 */
public class ShopListItemsEndpoint extends Endpoint {

    private final CompanyController companyController;

    public ShopListItemsEndpoint(CompanyController companyController) {
        this.companyController = companyController;
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

        Company company = optional.get();

        JsonArray array = new JsonArray();
        CompanyShop.getItems().forEach(shopItem -> {
            JsonObject object = new JsonObject();
            object.addProperty("id", shopItem.getId());
            object.addProperty("name", shopItem.getName());
            object.addProperty("price", shopItem.getPrice(company));

            array.add(object);
        });

        context.result(array.toString()).status(200);
    }

}
