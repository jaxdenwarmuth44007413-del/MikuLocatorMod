package com.miku.coordinateselector;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;


/**
 * Miku Locator -> Wormhole Stabilizer target setter
 */
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
                                "§c没有找到方块实体"
                        )
                );

                return;
            }





            /*
             * Wormhole 原生设置目标
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





                /*
                 * 写入 Wormhole 目标
                 */

                stabilizer.setTarget(

                        0,

                        target

                );





                stabilizer.setChanged();





                level.sendBlockUpdated(

                        this.stabilizerPos,

                        be.getBlockState(),

                        be.getBlockState(),

                        3

                );






                /*
                 * 让 Wormhole 执行激活流程
                 */

                boolean activated =
                        stabilizer.activate(player);





                if(activated){


                    player.sendSystemMessage(

                            Component.literal(

                                    "§aMiku Locator: Wormhole 已激活"

                            )

                    );


                }else{


                    player.sendSystemMessage(

                            Component.literal(

                                    "§cWormhole 激活失败，请检查能量和结构"

                            )

                    );


                }




            }else{


                player.sendSystemMessage(

                        Component.literal(

                                "§c目标方块不是 Wormhole 稳定器"

                        )

                );

            }




        });



        context.setPacketHandled(true);

    }

}