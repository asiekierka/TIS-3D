package li.cil.tis3d.client.gui;

import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.common.gui.GuiHandlerCommon;
import li.cil.tis3d.common.init.Items;
import li.cil.tis3d.common.module.ModuleTerminal;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * GUI handler for the client side - which is, still, all we need.
 */
public final class GuiHandlerClient extends GuiHandlerCommon {
    @Override
    @Nullable
    public Object getClientGuiElement(final int id, final EntityPlayer player, final World world, final int x, final int y, final int z) {
        switch (GuiHandlerCommon.GuiId.VALUES[id]) {
            case BOOK_CODE:
                return getGuiBookCode(player);
            case BOOK_MANUAL:
                return new GuiManual();
            case MODULE_TERMINAL:
                return getGuiModuleTerminal(world);
            case MODULE_MEMORY:
                return getGuiModuleMemory(player);
        }
        return null;
    }

    // --------------------------------------------------------------------- //

    @Nullable
    private static Object getGuiBookCode(final EntityPlayer player) {
        if (!Items.isBookCode(player.getHeldItem(EnumHand.MAIN_HAND))) {
            return null;
        }

        return new GuiBookCode(player);
    }

    @Nullable
    private static Object getGuiModuleTerminal(final World world) {
        final RayTraceResult hit = Minecraft.getMinecraft().objectMouseOver;
        if (hit == null || hit.typeOfHit != RayTraceResult.Type.BLOCK) {
            return null;
        }

        final TileEntity tileEntity = world.getTileEntity(hit.getBlockPos());
        if (!(tileEntity instanceof Casing)) {
            return null;
        }

        final Casing casing = (Casing) tileEntity;
        final Module module = casing.getModule(Face.fromEnumFacing(hit.sideHit));
        if (!(module instanceof ModuleTerminal)) {
            return null;
        }

        return new GuiModuleTerminal((ModuleTerminal) module);
    }

    @Nullable
    private static Object getGuiModuleMemory(final EntityPlayer player) {
        if (!Items.isModuleReadOnlyMemory(player.getHeldItem(EnumHand.MAIN_HAND))) {
            return null;
        }

        return new GuiModuleMemory(player);
    }
}
