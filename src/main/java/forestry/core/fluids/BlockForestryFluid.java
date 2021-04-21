/*******************************************************************************
 * Copyright (c) 2011-2014 SirSengir.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Various Contributors including, but not limited to:
 * SirSengir (original work), CovertJaguar, Player, Binnie, MysteriousAges
 ******************************************************************************/
package forestry.core.fluids;

import java.awt.Color;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.block.material.Material;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import forestry.modules.features.FeatureFluid;
import forestry.modules.features.FluidProperties;

public class BlockForestryFluid extends FlowingFluidBlock {

	private final FeatureFluid feature;
	private final boolean flammable;
	private final int flammability;
	private final Color color;

	public BlockForestryFluid(FeatureFluid feature) {
		super(feature::fluid, Block.Properties.of(feature.fluid().getAttributes().getTemperature() > 505 ? Material.LAVA : Material.WATER)
				.noCollission()
				.strength(100.0F).noDrops());
		this.feature = feature;
		FluidProperties properties = feature.properties();
		this.flammability = properties.flammability;
		this.flammable = properties.flammable;

		this.color = properties.particleColor;
	}

	@Override
	public void randomTick(BlockState blockState, ServerWorld world, BlockPos pos, Random rand) {
		double x = pos.getX();
		double y = pos.getY();
		double z = pos.getZ();

		if (this.material == Material.WATER) {
			int i = blockState.getValue(LEVEL);

			if (i > 0 && i < 8) {
				if (getFluid().getAttributes().getViscosity(world, pos) < 5000 && rand.nextInt(64) == 0) {
					world.playLocalSound(x + 0.5D, y + 0.5D, z + 0.5D, SoundEvents.WATER_AMBIENT, SoundCategory.BLOCKS, rand.nextFloat() * 0.25F + 0.75F, rand.nextFloat() + 0.5F, false);
				}
			} else if (rand.nextInt(10) == 0) {
				world.addParticle(ParticleTypes.UNDERWATER, x + rand.nextFloat(), y + rand.nextFloat(), z + rand.nextFloat(), 0.0D, 0.0D, 0.0D);
			}
		}

		if (this.material == Material.LAVA && world.getBlockState(pos.above()).getMaterial() == Material.AIR && !world.getBlockState(pos.above()).isSolidRender(world, pos.above())) {
			if (rand.nextInt(100) == 0) {
				double d8 = x + rand.nextFloat();
				double d4 = y + 1;
				double d6 = z + rand.nextFloat();
				world.addParticle(ParticleTypes.LAVA, d8, d4, d6, 0.0D, 0.0D, 0.0D);
				world.playLocalSound(d8, d4, d6, SoundEvents.LAVA_POP, SoundCategory.BLOCKS, 0.2F + rand.nextFloat() * 0.2F, 0.9F + rand.nextFloat() * 0.15F, false);
			}

			if (rand.nextInt(200) == 0) {
				world.playLocalSound(x, y, z, SoundEvents.LAVA_AMBIENT, SoundCategory.BLOCKS, 0.2F + rand.nextFloat() * 0.2F, 0.9F + rand.nextFloat() * 0.15F, false);
			}
		}

		if (rand.nextInt(10) == 0 && Block.canSupportCenter(world, pos.below(), Direction.DOWN)) {
			Material material = world.getBlockState(pos.below(2)).getMaterial();

			if (!material.blocksMotion() && !material.isLiquid()) {
				double px = x + rand.nextFloat();
				double py = y - 1.05D;
				double pz = z + rand.nextFloat();

				/*Particle fx = new ParticleColoredDripParticle(world, px, py, pz, color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f);
				Minecraft.getInstance().particles.addEffect(fx);*/
			}
		}
	}

	@Override
	public int getFireSpreadSpeed(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
		return flammable ? 30 : 0;
	}

	@Override
	public int getFlammability(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
		return flammability;
	}

	@Override
	public boolean isFlammable(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
		return flammable;
	}

	private static boolean isFlammable(IBlockReader world, BlockPos pos) {
		BlockState blockState = world.getBlockState(pos);
		Block block = blockState.getBlock();
		return blockState.isFlammable(world, pos, Direction.UP);
	}

	@Override
	public boolean isFireSource(BlockState state, IWorldReader world, BlockPos pos, Direction side) {
		return flammable && flammability == 0;
	}

	public Color getColor() {
		return color;
	}

	@Override
	public void tick(BlockState state, ServerWorld world, BlockPos pos, Random rand) {
		super.tick(state, world, pos, rand);

		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();

		// Start fires if the fluid is lava-like
		if (material == Material.LAVA) {
			int rangeUp = rand.nextInt(3);

			for (int i = 0; i < rangeUp; ++i) {
				x += rand.nextInt(3) - 1;
				++y;
				z += rand.nextInt(3) - 1;
				BlockState blockState = world.getBlockState(new BlockPos(x, y, z));
				if (blockState.getMaterial() == Material.AIR) {
					if (isNeighborFlammable(world, x, y, z)) {
						world.setBlockAndUpdate(new BlockPos(x, y, z), Blocks.FIRE.defaultBlockState());
						return;
					}
				} else if (blockState.getMaterial().blocksMotion()) {
					return;
				}
			}

			if (rangeUp == 0) {
				int startX = x;
				int startZ = z;

				for (int i = 0; i < 3; ++i) {
					x = startX + rand.nextInt(3) - 1;
					z = startZ + rand.nextInt(3) - 1;

					BlockPos posAbove = new BlockPos(pos.getX(), y + 1, z);
					if (world.isEmptyBlock(posAbove) && isFlammable(world, new BlockPos(x, y, z))) {
						world.setBlockAndUpdate(posAbove, Blocks.FIRE.defaultBlockState());
					}
				}
			}
		}

		// explode if very flammable and near fire
		int flammability = getFlammability(state, world, pos, null);
		if (flammability > 0) {
			// Explosion size is determined by flammability, up to size 4.
			float explosionSize = 4F * flammability / 300F;
			if (explosionSize > 1.0 && isNearFire(world, pos.getX(), pos.getY(), pos.getZ())) {
				world.setBlockAndUpdate(pos, Blocks.FIRE.defaultBlockState());
				world.explode(null, pos.getX(), pos.getY(), pos.getZ(), explosionSize, true, Explosion.Mode.DESTROY);
			}
		}
	}

	private static boolean isNeighborFlammable(World world, int x, int y, int z) {
		return isFlammable(world, new BlockPos(x - 1, y, z)) ||
			isFlammable(world, new BlockPos(x + 1, y, z)) ||
			isFlammable(world, new BlockPos(x, y, z - 1)) ||
			isFlammable(world, new BlockPos(x, y, z + 1)) ||
			isFlammable(world, new BlockPos(x, y - 1, z)) ||
			isFlammable(world, new BlockPos(x, y + 1, z));
	}

	private static boolean isNearFire(World world, int x, int y, int z) {
		AxisAlignedBB boundingBox = new AxisAlignedBB(x - 1, y - 1, z - 1, x + 1, y + 1, z + 1);
		// Copied from 'Entity.move', replaces method 'World.isFlammableWithin'
		return BlockPos.betweenClosedStream(boundingBox.deflate(0.001D)).noneMatch((pos) -> {
			BlockState state = world.getBlockState(pos);
			return state.is(BlockTags.FIRE) || state.is(Blocks.LAVA) || state.isBurning(world, pos);
		});
	}
}
