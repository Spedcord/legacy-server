package xyz.spedcord.server.company.shop;

import xyz.spedcord.server.company.shop.impl.CustomInviteItem;
import xyz.spedcord.server.company.shop.impl.IncreaseMemberLimitItem;
import xyz.spedcord.server.joinlink.JoinLinkController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Functions as some sort of registry for shop items
 *
 * @author Maximilian Dorn
 * @version 2.1.9
 * @since 2.1.9
 */
public class CompanyShop {

    private static final List<ShopItem> items = new ArrayList<>();

    private CompanyShop() {
    }

    public static void init(JoinLinkController joinLinkController) {
        items.add(new CustomInviteItem(joinLinkController));
        items.add(new IncreaseMemberLimitItem());
    }

    public static List<ShopItem> getItems() {
        return Collections.unmodifiableList(items);
    }

}
