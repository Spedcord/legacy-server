package xyz.spedcord.server.company;

import com.mongodb.client.model.Filters;
import com.mongodb.reactivestreams.client.MongoCollection;
import xyz.spedcord.common.mongodb.CallbackSubscriber;
import xyz.spedcord.common.mongodb.MongoDBService;
import xyz.spedcord.server.util.CarelessSubscriber;
import xyz.spedcord.server.util.MongoDBUtil;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class CompanyController {

    private final MongoDBService mongoDBService;

    private final Set<Company> companies = new HashSet<>();
    private MongoCollection<Company> companyCollection;

    public CompanyController(MongoDBService mongoDBService) {
        this.mongoDBService = mongoDBService;
        init();
    }

    private void init() {
        companyCollection = mongoDBService.getDatabase().getCollection("companies", Company.class);
        load();
    }

    private void load() {
        CallbackSubscriber<Company> subscriber = new CallbackSubscriber<>();
        subscriber.doOnNext(companies::add);

        companyCollection.find().subscribe(subscriber);
    }

    public Optional<Company> getCompany(long discordServerId) {
        return companies.stream().filter(company -> company.getDiscordServerId() == discordServerId).findAny();
    }

    public Optional<Company> getCompany(int id) {
        return companies.stream().filter(company -> company.getId() == id).findAny();
    }

    public void createCompany(Company company) {
        long docs = MongoDBUtil.countDocuments(companyCollection);
        company.setId(Long.valueOf(docs).intValue());

        companies.add(company);
        companyCollection.insertOne(company).subscribe(new CarelessSubscriber<>());
    }

    public void updateCompany(Company company) {
        company.getRoles().sort(Comparator.comparingDouble(value -> ((CompanyRole) value).getPayout()).reversed());
        companyCollection.replaceOne(Filters.eq("_id", company.getId()), company).subscribe(new CarelessSubscriber<>());
    }

    public Set<Company> getCompanies() {
        return companies;
    }

}
