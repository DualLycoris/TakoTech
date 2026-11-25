package moe.takochan.takotech.network;

import net.minecraft.entity.player.EntityPlayerMP;

import com.gtnewhorizon.gtnhlib.util.ServerThreadUtil;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import moe.takochan.takotech.client.gui.GuiType;
import moe.takochan.takotech.utils.CommonUtils;
import moe.takochan.takotech.utils.ToolboxHelper;

public class PacketToolboxOpenSelectGUI implements IMessage {

    public PacketToolboxOpenSelectGUI() {}

    @Override
    public void fromBytes(ByteBuf buf) {}

    @Override
    public void toBytes(ByteBuf buf) {}

    public static class Handler implements IMessageHandler<PacketToolboxOpenSelectGUI, IMessage> {

        @Override
        public IMessage onMessage(PacketToolboxOpenSelectGUI message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            ServerThreadUtil.addScheduledTask(() -> {
                if (ToolboxHelper.isItemToolbox(player.inventory.getCurrentItem())) {
                    CommonUtils.openGui(GuiType.GUI_TOOLBOX_PLUS_SELECT, player, null);
                }
            });
            return null;
        }
    }
}
