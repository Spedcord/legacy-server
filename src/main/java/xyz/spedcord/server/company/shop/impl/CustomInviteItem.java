package xyz.spedcord.server.company.shop.impl;

import xyz.spedcord.server.company.Company;
import xyz.spedcord.server.company.shop.ShopItem;
import xyz.spedcord.server.joinlink.JoinLinkController;

/**
 * Shop item implementation for custom invites
 *
 * @author Maximilian Dorn
 * @version 2.1.10
 * @since 2.1.9
 */
public class CustomInviteItem extends ShopItem {

    private final JoinLinkController joinLinkController;

    public CustomInviteItem(JoinLinkController joinLinkController) {
        super(0, "Custom Invite", false);
        this.joinLinkController = joinLinkController;
    }

    @Override
    public boolean activate(Company company, Object[] args) {
        if (args.length != 1) {
            return false;
        }
        if (!(args[0] instanceof String)) {
            return false;
        }

        this.joinLinkController.addCustomLink((String) args[0], company.getId(), -1);
        return true;
    }

    @Override
    public double getPrice(Company company) {
        return 150_000D;
    }

}
