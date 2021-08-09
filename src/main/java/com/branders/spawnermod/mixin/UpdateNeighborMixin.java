package com.branders.spawnermod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.branders.spawnermod.event.SpawnerEventHandler;

import net.minecraft.block.Block;
import net.minecraft.block.SpawnerBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(World.class)
public class UpdateNeighborMixin {
	
	@Inject(
			at = @At("HEAD"),
			method = "neighborChanged(Lnet/minecraft/util/math/BlockPos;"
					+ "Lnet/minecraft/block/Block;"
					+ "Lnet/minecraft/util/math/BlockPos;)V",
			cancellable = true)
	private void neighborChanged(BlockPos pos, Block sourceBlock, BlockPos neighborPos, CallbackInfo ci) {
		World world = (World)(Object)this;
		
		if(world.getBlockState(neighborPos).getBlock() instanceof SpawnerBlock)
			SpawnerEventHandler.updateNeighbor(neighborPos, world);
		else if(world.getBlockState(pos).getBlock() instanceof SpawnerBlock)
			SpawnerEventHandler.updateNeighbor(pos, world);
	}
}
