package com.mistbeyond.examplemod.item;

import com.mistbeyond.examplemod.Ids;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class Items {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Ids.MODID);

    public static final DeferredItem<Item> EXAMPLE_ITEM = ITEMS.registerSimpleItem("example_item",
            p -> p.food(new FoodProperties.Builder().alwaysEdible().nutrition(1).saturationModifier(2f).build()));
    public static final DeferredItem<Item> TEST_WRENCH = ITEMS.registerItem("test_wrench", TestWrench::new);
    public static final DeferredItem<Item> CRUSHED_IRON_ORE = ITEMS.registerItem("crushed_iron_ore", Item::new);
    public static final DeferredItem<Item> TEST_ITEM = ITEMS.registerItem(
            "test_item", ElectricItem::createInfinite, p -> p.stacksTo(1)
    );
}
