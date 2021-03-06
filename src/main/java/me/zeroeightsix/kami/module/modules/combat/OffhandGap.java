package me.zeroeightsix.kami.module.modules.combat;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.*;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;

import java.util.Map;

import static me.zeroeightsix.kami.module.modules.combat.AutoReplenish.getInventorySlots;

/**
 * @author polymer (main function and autototem compatibility)
 * @author S-B99 (made epic and smooth and cleaned up code <3)
 * Created by polymer on 21/02/20
 * Updated by S-B99 on 24/02/20
 */
@Module.Info(name = "OffhandGap", category = Module.Category.COMBAT, description = "Holds a God apple when right clicking your sword!")
public class OffhandGap extends Module {
	private Setting<Boolean> preventDesync = register(Settings.b("Prevent Desync", false));
	private Setting<Double> disableHealth = register(Settings.doubleBuilder("Disable Health").withMinimum(0.0).withValue(4.0).withMaximum(20.0).build());
	private Setting<Boolean> weaponCheck = register(Settings.b("Sword or Axe Only", true));
//	private Setting<Mode> modeSetting = register(Settings.e("Use Mode", Mode.GAPPLE));

	private enum Mode {
		GAPPLE, FOOD, CUSTOM
	}

	int gaps = -1;
	boolean wasEnabled = false;
	Item usedItem; // epic meme
	Item toUseItem;
	
	@EventHandler
	private Listener<PacketEvent.Send> sendListener = new Listener<>(e ->{
		if (e.getPacket() instanceof CPacketPlayerTryUseItem) {
			if (mc.player.getHeldItemMainhand().getItem() instanceof ItemSword || mc.player.getHeldItemMainhand().getItem() instanceof ItemAxe || passItemCheck()) {
				if (ModuleManager.isModuleEnabled("AutoTotem") && mc.player.getHealth() + mc.player.getAbsorptionAmount() >= disableHealth.getValue()) {
					wasEnabled = true;
					ModuleManager.getModuleByName("AutoTotem").disable();
				}
				if (preventDesync.getValue()) {
					usedItem = mc.player.getHeldItemMainhand().getItem();
				}
				moveToOffhand(gaps);
			} 
		}
		try {
			/* If you stop holding right click move totem back */
			if (wasEnabled == !ModuleManager.isModuleEnabled("AutoTotem") && !mc.gameSettings.keyBindUseItem.isKeyDown() && mc.player.getHeldItemOffhand().getItem() == Items.GOLDEN_APPLE) {
				disableGaps();
			}
			/* In case you didn't stop right clicking but you switched items by scrolling or something */
			/* Only with preventDesync enabled */
			else if (preventDesync.getValue() && wasEnabled == !ModuleManager.isModuleEnabled("AutoTotem") && (usedItem != mc.player.getHeldItemMainhand().getItem()) && mc.player.getHeldItemOffhand().getItem() == Items.GOLDEN_APPLE) {
				usedItem = mc.player.getHeldItemMainhand().getItem();
				disableGaps();
			}
			/* Force disable if under health limit */
			else if (mc.player.getHealth() + mc.player.getAbsorptionAmount() >= disableHealth.getValue()) {
				disableGaps();
			}
		}
		catch (NullPointerException ignored) { }
	});

	public void disableGaps() {
		moveFromOffhand(gaps);
		ModuleManager.getModuleByName("AutoTotem").enable();
	}

	@Override
	public void onUpdate() {
		if (mc.player == null) return;
//		if (modeSetting.getValue().equals(Mode.GAPPLE)) {
			toUseItem = Items.GOLDEN_APPLE;
//		} else if (modeSetting.getValue().equals(Mode.FOOD)) {
//			if (getFullInventory().containsKey(Items.))
//		}
		if (mc.player.getHeldItemOffhand().getItem() != Items.GOLDEN_APPLE) {
			for (int i = 0; i < 45; i++) {
				if (mc.player.inventory.getStackInSlot(i).getItem() == Items.GOLDEN_APPLE) {
					gaps = i;
					break;
				}
			}
		}
	}

	private static Map<Integer, ItemStack> getFullInventory() { return getInventorySlots(0, 45); }

	/* If weaponCheck is disabled, check if they're not holding an item you'd want to use normally */
	public boolean passItemCheck() {
		if (weaponCheck.getValue()) return false;
		else {
			Item item = mc.player.getHeldItemMainhand().getItem();
			if (item instanceof ItemBow) return false;
			if (item instanceof ItemSnowball) return false;
			if (item instanceof ItemEgg) return false;
			if (item instanceof ItemPotion) return false;
			if (item instanceof ItemEnderEye) return false;
			if (item instanceof ItemEnderPearl) return false;
			if (item instanceof ItemFood) return false;
			if (item instanceof ItemShield) return false;
			if (item instanceof ItemFlintAndSteel) return false;
			if (item instanceof ItemFishingRod) return false;
			if (item instanceof ItemArmor) return false;
		}
		return true;
	}

	void moveToOffhand(int slot) {
		if (mc.player.getHeldItemOffhand().getItem() != Items.GOLDEN_APPLE) {
			mc.playerController.windowClick(0, slot < 9 ? slot + 36 : slot, 0, ClickType.PICKUP, mc.player);
			mc.playerController.windowClick(0, 45, 0, ClickType.PICKUP, mc.player);
		}
	}

	void moveFromOffhand(int slot) {
		if (mc.player.getHeldItemOffhand().getItem() == Items.GOLDEN_APPLE) {
			mc.playerController.windowClick(0, 45, 0, ClickType.PICKUP, mc.player);
			mc.playerController.windowClick(0, slot, 0, ClickType.PICKUP, mc.player);
		}
	}
}
