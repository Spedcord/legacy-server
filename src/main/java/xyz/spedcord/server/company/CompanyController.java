package xyz.spedcord.server.company;

import com.mongodb.client.model.Filters;
import com.mongodb.reactivestreams.client.MongoCollection;
import xyz.spedcord.common.mongodb.CallbackSubscriber;
import xyz.spedcord.common.mongodb.MongoDBService;
import xyz.spedcord.server.util.CarelessSubscriber;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

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
        AtomicBoolean finished = new AtomicBoolean(false);
        CallbackSubscriber<Company> subscriber = new CallbackSubscriber<>();
        subscriber.doOnNext(companies::add);
        subscriber.doOnComplete(() -> finished.set(true));

        companyCollection.find().subscribe(subscriber);
        while (!finished.get()) ;
    }

    public Optional<Company> getCompany(long discordServerId) {
        return companies.stream().filter(company -> company.getDiscordServerId() == discordServerId).findAny();
    }

    public Optional<Company> getCompany(int id) {
        return companies.stream().filter(company -> company.getId() == id).findAny();
    }

    public void createCompany(Company company) {
        companies.add(company);
        companyCollection.insertOne(company).subscribe(new CarelessSubscriber<>());
    }

    public void updateCompany(Company company) {
        companyCollection.replaceOne(Filters.eq("id", 1), company).subscribe(new CarelessSubscriber<>());
    }

    public Set<Company> getCompanies() {
        return companies;
    }

}
