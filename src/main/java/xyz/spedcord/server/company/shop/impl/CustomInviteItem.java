package xyz.spedcord.server.company.shop.impl;

import xyz.spedcord.server.company.Company;
import xyz.spedcord.server.company.shop.ShopItem;
import xyz.spedcord.server.joinlink.JoinLinkController;

/**
 * Shop item implementation for custom invites
 *
 * @author Maximilian Dorn
 * @version 2.1.9
 * @since 2.1.9
 */
public class CustomInviteItem extends ShopItem {

    private final JoinLinkController joinLinkController;

    public CustomInviteItem(JoinLinkController joinLinkController) {
        super(0, "Custom Invite", false);
        this.joinLinkController = joinLinkController;
    }

    @Override
    public void postPurchase(Company company, Object... args) {
        this.joinLinkController.addCustomLink((String) args[0], company.getId(), -1);
    }

    @Override
    public double getPrice(Company company) {
        return 150_000D;
    }

}
