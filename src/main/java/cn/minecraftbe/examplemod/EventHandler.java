package cn.minecraftbe.examplemod;

import lombok.extern.slf4j.Slf4j;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;

import static cn.minecraftbe.examplemod.ExampleMod.MODID;

@Slf4j
@EventBusSubscriber(modid = MODID)
public class EventHandler {

    @SubscribeEvent
    public static void onUseItemOnBlock(UseItemOnBlockEvent event) {
//        if (event.getPlayer() != null) {
//            event.getPlayer().sendSystemMessage(Component.literal("Use event: "+ event));
//        }
//        val context = event.getUseOnContext();
    }
}
