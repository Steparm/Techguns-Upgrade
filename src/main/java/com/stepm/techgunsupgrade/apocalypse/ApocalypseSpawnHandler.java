package com.stepm.techgunsupgrade.apocalypse;

import com.stepm.techgunsupgrade.TechgunsUpgradeMod;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = TechgunsUpgradeMod.MODID)
public class ApocalypseSpawnHandler {

    @SubscribeEvent
    public static void onLivingSpawn(LivingSpawnEvent.CheckSpawn event) {
        if (ZombieApocalypseManager.isApocalypseActiveServer()) {
            event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public static void onLivingSpawnSpecial(LivingSpawnEvent.SpecialSpawn event) {
        if (ZombieApocalypseManager.isApocalypseActiveServer()) {
            event.setCanceled(true);
        }
    }
}