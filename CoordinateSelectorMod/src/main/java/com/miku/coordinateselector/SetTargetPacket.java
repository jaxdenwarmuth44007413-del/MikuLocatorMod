package com.miku.coordinateselector;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;


public class SetTargetPacket {


    private final BlockPos stabilizerPos;

    private final int targetX;
    private final int targetY;
    private final int targetZ;



    public SetTargetPacket(
            BlockPos stabilizerPos,
            int targetX,
            int targetY,
            int targetZ
    ){

        this.stabilizerPos = stabilizerPos;
        this.targetX = targetX;
        this.targetY = targetY;
        this.targetZ = targetZ;

    }



    public SetTargetPacket(FriendlyByteBuf buf){

        this.stabilizerPos = buf.readBlockPos();
        this.targetX = buf.readInt();
        this.targetY = buf.readInt();
        this.targetZ = buf.readInt();

    }



    public void toBytes(FriendlyByteBuf buf){

        buf.writeBlockPos(this.stabilizerPos);

        buf.writeInt(this.targetX);
        buf.writeInt(this.targetY);
        buf.writeInt(this.targetZ);

    }



    public void handle(Supplier<NetworkEvent.Context> supplier){


        NetworkEvent.Context ctx = supplier.get();


        ctx.enqueueWork(() -> {


            ServerPlayer player = ctx.getSender();

            if(player == null)
                return;



            ServerLevel level = player.serverLevel();


            BlockEntity be =
                    level.getBlockEntity(this.stabilizerPos);



            if(!(be instanceof com.supermartijn642.wormhole.StabilizerBlockEntity stabilizer)){


                player.sendSystemMessage(
                        Component.literal(
                                "不是Wormhole稳定器"
                        )
                );

                return;
            }



            /*
             * 使用 Wormhole 内部目标接口
             */

            stabilizer.setTarget(
                    0,
                    this.targetX,
                    this.targetY,
                    this.targetZ,
                    level.dimension()
            );



            stabilizer.setChanged();



            level.sendBlockUpdated(
                    this.stabilizerPos,
                    be.getBlockState(),
                    be.getBlockState(),
                    3
            );



            player.sendSystemMessage(
                    Component.literal(
                            "Wormhole目标已设置"
                    )
            );


        });


        ctx.setPacketHandled(true);

    }

}
