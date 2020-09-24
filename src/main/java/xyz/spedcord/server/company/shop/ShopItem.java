package xyz.spedcord.server.company.shop;

import xyz.spedcord.server.company.Company;

/**
 * Represents the base class for shop items
 *
 * @author Maximilian Dorn
 * @version 2.1.9
 * @since 2.1.9
 */
public abstract class ShopItem {

    private final int id;
    private final String name;
    private final boolean multipleAllowed;

    public ShopItem(int id, String name, boolean multipleAllowed) {
        this.id = id;
        this.name = name;
        this.multipleAllowed = multipleAllowed;
    }

    /**
     * Gets called right after the purchase was completed
     *
     * @param company The company
     */
    public abstract void postPurchase(Company company, Object... args);

    /**
     * Calculates the price for the provided company
     *
     * @param company The company
     * @return The calculated price
     */
    public abstract double getPrice(Company company);

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public boolean isMultipleAllowed() {
        return this.multipleAllowed;
    }

}
