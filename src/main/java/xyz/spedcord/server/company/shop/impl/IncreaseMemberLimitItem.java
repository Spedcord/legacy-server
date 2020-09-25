package xyz.spedcord.server.company.shop.impl;

import xyz.spedcord.server.company.Company;
import xyz.spedcord.server.company.shop.ShopItem;

/**
 * Shop item implementation for increasing member limits
 *
 * @author Maximilian Dorn
 * @version 2.1.12
 * @since 2.1.9
 */
public class IncreaseMemberLimitItem extends ShopItem {

    public IncreaseMemberLimitItem() {
        super(1, "Increase Member Limit", true);
    }

    @Override
    public boolean activate(Company company, Object[] args) {
        company.setMemberLimit(company.getMemberLimit() + 1);
        return true;
    }

    @Override
    public double getPrice(Company company) {
        int purchasedUpgrades = company.getMemberLimit() - Company.DEFAULT_MEMBER_LIMIT;
        return Math.floor(this.exponentialFunction(purchasedUpgrades) * 100D) / 100D;
    }

    /**
     * Calculates the price for the next upgrades with x purchased upgrades
     *
     * @param x The amount of ugrades
     * @return The calculates price
     */
    private double exponentialFunction(int x) {
        double a = 1000D;
        double b = 1.1D;
        return a * Math.pow(b, x);
    }

}
