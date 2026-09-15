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


        NetworkEvent.Context context = supplier.get();



        context.enqueueWork(() -> {



            ServerPlayer player = context.getSender();


            if(player == null)
                return;



            ServerLevel level = player.serverLevel();



            BlockEntity be =
                    level.getBlockEntity(this.stabilizerPos);



            if(be == null){


                player.sendSystemMessage(
                        Component.literal(
                                "§c没有找到传送门稳定器"
                        )
                );


                return;

            }




            /*
             * Wormhole 1.1.17 原生接口
             */


            if(be instanceof com.supermartijn642.wormhole.StabilizerBlockEntity stabilizer){



                com.supermartijn642.wormhole.portal.PortalTarget target =

                        new com.supermartijn642.wormhole.portal.PortalTarget(

                                level.dimension(),

                                this.targetX,

                                this.targetY,

                                this.targetZ,

                                player.getYRot(),

                                "Miku Target"

                        );





                // 设置 Wormhole 目标

                stabilizer.setTarget(

                        0,

                        target

                );





                // 保存

                stabilizer.setChanged();





                level.sendBlockUpdated(

                        this.stabilizerPos,

                        be.getBlockState(),

                        be.getBlockState(),

                        3

                );





                // 激活传送门

                boolean result =
                        stabilizer.activate(player);





                if(result){


                    player.sendSystemMessage(

                            Component.literal(
                                    "§aWormhole传送门激活成功"
                            )

                    );


                }else{


                    player.sendSystemMessage(

                            Component.literal(
                                    "§cWormhole激活失败"
                            )

                    );

                }



            }else{


                player.sendSystemMessage(

                        Component.literal(
                                "§c当前方块不是Wormhole稳定器"
                        )

                );


            }




        });



        context.setPacketHandled(true);

    }

}
