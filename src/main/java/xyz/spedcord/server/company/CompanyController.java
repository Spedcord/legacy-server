package xyz.spedcord.server.company;

import com.mongodb.client.model.Filters;
import com.mongodb.reactivestreams.client.MongoCollection;
import xyz.spedcord.common.mongodb.CallbackSubscriber;
import xyz.spedcord.common.mongodb.MongoDBService;
import xyz.spedcord.server.util.CarelessSubscriber;
import xyz.spedcord.server.util.MongoDBUtil;

import java.util.*;

/**
 * A controller that handles everything related to companies
 *
 * @author Maximilian Dorn
 * @version 2.0.0
 * @since 1.0.0
 */
public class CompanyController {

    private final Set<Company> companies = new HashSet<>();

    private final MongoDBService mongoDBService;
    private MongoCollection<Company> companyCollection;

    /**
     * Constructs a new instance of this controller
     *
     * @param mongoDBService The MongoDB service
     */
    public CompanyController(MongoDBService mongoDBService) {
        this.mongoDBService = mongoDBService;
        this.init();
    }

    /**
     * Initializes this controller
     */
    private void init() {
        this.companyCollection = this.mongoDBService.getDatabase().getCollection("companies", Company.class);
        this.load();
    }

    /**
     * Loads every company into a list
     */
    private void load() {
        CallbackSubscriber<Company> subscriber = new CallbackSubscriber<>();
        subscriber.doOnNext(this.companies::add);

        this.companyCollection.find().subscribe(subscriber);
    }

    /**
     * Gets a company with the corresponding Discord server id
     *
     * @param discordServerId The Discord server id
     * @return The company or none
     */
    public Optional<Company> getCompany(long discordServerId) {
        return this.companies.stream().filter(company -> company.getDiscordServerId() == discordServerId).findAny();
    }

    /**
     * Returns the company with the corresponding id
     *
     * @param id The company id
     * @return The company or none
     */
    public Optional<Company> getCompany(int id) {
        return this.companies.stream().filter(company -> company.getId() == id).findAny();
    }

    /**
     * Creates a new company
     *
     * @param company The company object
     */
    public void createCompany(Company company) {
        // Set id
        long docs = MongoDBUtil.countDocuments(this.companyCollection);
        company.setId(Long.valueOf(docs).intValue());

        // Save to database
        this.companies.add(company);
        this.companyCollection.insertOne(company).subscribe(new CarelessSubscriber<>());
    }

    /**
     * Saves the changes
     *
     * @param company The company
     */
    public void updateCompany(Company company) {
        company.getRoles().sort(Comparator.comparingDouble(value -> ((CompanyRole) value).getPayout()).reversed());
        this.companyCollection.replaceOne(Filters.eq("_id", company.getId()), company).subscribe(new CarelessSubscriber<>());
    }

    /**
     * Returns the companies
     *
     * @return The companies
     */
    public Set<Company> getCompanies() {
        return Collections.unmodifiableSet(this.companies);
    }

}
