package li.cil.tis3d.common.init;

import li.cil.tis3d.api.API;
import li.cil.tis3d.api.ManualAPI;
import li.cil.tis3d.api.ModuleAPI;
import li.cil.tis3d.api.prefab.manual.ResourceContentProvider;
import li.cil.tis3d.client.manual.provider.GameRegistryPathProvider;
import li.cil.tis3d.common.Constants;
import li.cil.tis3d.common.Settings;
import li.cil.tis3d.common.api.*;
import li.cil.tis3d.common.block.BlockCasing;
import li.cil.tis3d.common.event.TickHandlerInfraredPacket;
import li.cil.tis3d.common.integration.Integration;
import li.cil.tis3d.common.item.ItemBookCode;
import li.cil.tis3d.common.item.ItemKey;
import li.cil.tis3d.common.item.ItemModuleReadOnlyMemory;
import li.cil.tis3d.common.module.*;
import li.cil.tis3d.common.network.Network;
import li.cil.tis3d.common.provider.SimpleModuleProvider;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.events.PlayerInteractionEvent;
import net.fabricmc.fabric.events.TickEvent;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

@SuppressWarnings("unused")
public final class BootstrapCommon implements ModInitializer {
    @Override
    public void onInitialize() {
        // Load our settings first to have all we need for remaining init.
        Settings.load();

        // Initialize API.
        API.creativeTab = ItemGroup.REDSTONE;

        API.fontRendererAPI = new FontRendererAPIImpl();
        API.infraredAPI = new InfraredAPIImpl();
        API.manualAPI = ManualAPIImpl.INSTANCE;
        API.moduleAPI = new ModuleAPIImpl();
        API.serialAPI = SerialAPIImpl.INSTANCE;

        // Register event handlers.
        TickEvent.SERVER.register(server -> TickHandlerInfraredPacket.INSTANCE.serverTick());
        TickEvent.SERVER.register(server -> Network.INSTANCE.serverTick());

        // Register entities.
        Entities.registerEntities();

        // Register blocks.
        Blocks.registerBlocks();
        Blocks.registerTileEntityTypes();

        // Register items.
        Items.registerItems();

        // Register providers for built-in modules.
        ModuleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_AUDIO, ModuleAudio::new));
        ModuleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_BUNDLED_REDSTONE, ModuleBundledRedstone::new));
        ModuleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_DISPLAY, ModuleDisplay::new));
        ModuleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_EXECUTION, ModuleExecution::new));
        ModuleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_INFRARED, ModuleInfrared::new));
        ModuleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_KEYPAD, ModuleKeypad::new));
        ModuleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_QUEUE, ModuleQueue::new));
        ModuleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_RANDOM, ModuleRandom::new));
        ModuleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_RANDOM_ACCESS_MEMORY, ModuleRandomAccessMemory::new));
        ModuleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_READ_ONLY_MEMORY, ModuleReadOnlyMemory::new));
        ModuleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_REDSTONE, ModuleRedstone::new));
        ModuleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_SEQUENCER, ModuleSequencer::new));
        ModuleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_SERIAL_PORT, ModuleSerialPort::new));
        ModuleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_STACK, ModuleStack::new));
        ModuleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_TERMINAL, ModuleTerminal::new));
        ModuleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_TIMER, ModuleTimer::new));

        // Add default manual providers for server side stuff.
        ManualAPI.addProvider(new GameRegistryPathProvider());
        ManualAPI.addProvider(new ResourceContentProvider(API.MOD_ID, "doc/"));
        ManualAPI.addProvider(SerialAPIImpl.INSTANCE.getSerialProtocolContentProvider());

        // Mod integration.
        Integration.init();

        // TODO This is a doesSneakBypassUse hack.
        PlayerInteractionEvent.INTERACT_BLOCK.register(this::onRightClick);
    }

    public ActionResult onRightClick(PlayerEntity entityPlayer, World world, Hand enumHand, BlockPos pos, Direction direction, float hitX, float hitY, float hitZ) {
        if (!entityPlayer.isSneaking()) {
            return ActionResult.PASS;
        }

        ItemStack stack = entityPlayer.getStackInHand(enumHand);
        if (stack.getItem() instanceof ItemBookCode || stack.getItem() instanceof ItemKey || stack.getItem() instanceof ItemModuleReadOnlyMemory) {
            BlockState state = world.getBlockState(pos);
            if (state.getBlock() instanceof BlockCasing) {
                return state.activate(world, pos, entityPlayer, enumHand, direction, hitX, hitY, hitZ) ? ActionResult.SUCCESS : ActionResult.PASS;
            }
        }

        return ActionResult.PASS;
    }
}