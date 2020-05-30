package xyz.spedcord.server.endpoint.company;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.javalin.http.Context;
import xyz.spedcord.server.company.Company;
import xyz.spedcord.server.company.CompanyController;
import xyz.spedcord.server.endpoint.RestrictedEndpoint;
import xyz.spedcord.server.response.Responses;

public class CompanyRegisterEndpoint extends RestrictedEndpoint {

    private final CompanyController companyController;

    public CompanyRegisterEndpoint(CompanyController companyController) {
        this.companyController = companyController;
    }

    @Override
    protected void handleFurther(Context context) {
        Company company;
        try {
            JsonObject jsonObject = JsonParser.parseString(context.body()).getAsJsonObject();
            jsonObject.addProperty("id", -1);
            company = new Gson().fromJson(jsonObject, Company.class);
        } catch (Exception ignored) {
            Responses.error("Invalid request body").respondTo(context);
            return;
        }

        if(company.getName() == null || company.getMemberDiscordIds() == null) {
            Responses.error("Invalid request body").respondTo(context);
            return;
        }

        companyController.createCompany(company);
        Responses.success("Company was registered").respondTo(context);
    }
}
