package net.minecraft.network.protocol.minecraft_47.status;

import net.minecraft.network.INetHandler;
import net.minecraft.network.protocol.minecraft_47.status.client.C00PacketServerQuery;
import net.minecraft.network.protocol.minecraft_47.status.client.C01PacketPing;

public interface INetHandlerStatusServer extends INetHandler {

	void processPing(C01PacketPing packetIn);

	void processServerQuery(C00PacketServerQuery packetIn);

}
