package cn.minecraftbe.examplemod.inventory;

import cn.minecraftbe.examplemod.ExampleMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(BuiltInRegistries.MENU, ExampleMod.MODID);
    public static final DeferredHolder<MenuType<?>, MenuType<TestMachineMenu>> TEST_MACHINE = MENU_TYPES.register(
            "test_machine", () -> new MenuType<>(TestMachineMenu::new, FeatureFlags.DEFAULT_FLAGS)
    );
}
