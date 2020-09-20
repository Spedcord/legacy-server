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
        this.init();
    }

    private void init() {
        this.companyCollection = this.mongoDBService.getDatabase().getCollection("companies", Company.class);
        this.load();
    }

    private void load() {
        CallbackSubscriber<Company> subscriber = new CallbackSubscriber<>();
        subscriber.doOnNext(this.companies::add);

        this.companyCollection.find().subscribe(subscriber);
    }

    public Optional<Company> getCompany(long discordServerId) {
        return this.companies.stream().filter(company -> company.getDiscordServerId() == discordServerId).findAny();
    }

    public Optional<Company> getCompany(int id) {
        return this.companies.stream().filter(company -> company.getId() == id).findAny();
    }

    public void createCompany(Company company) {
        long docs = MongoDBUtil.countDocuments(this.companyCollection);
        company.setId(Long.valueOf(docs).intValue());

        this.companies.add(company);
        this.companyCollection.insertOne(company).subscribe(new CarelessSubscriber<>());
    }

    public void updateCompany(Company company) {
        company.getRoles().sort(Comparator.comparingDouble(value -> ((CompanyRole) value).getPayout()).reversed());
        this.companyCollection.replaceOne(Filters.eq("_id", company.getId()), company).subscribe(new CarelessSubscriber<>());
    }

    public Set<Company> getCompanies() {
        return this.companies;
    }

}
