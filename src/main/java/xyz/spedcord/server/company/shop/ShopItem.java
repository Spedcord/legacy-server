package xyz.spedcord.server.company.shop;

import xyz.spedcord.server.company.Company;

/**
 * Represents the base class for shop items
 *
 * @author Maximilian Dorn
 * @version 2.1.10
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
     * Gets called when the item is purchased
     * If this returns false the purchase will be cancelled
     *
     * @param company The company
     */
    public abstract boolean activate(Company company, Object... args);

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
