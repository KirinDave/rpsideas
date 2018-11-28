package com.github.kamefrede.rpsideas.spells.trick;

import com.github.kamefrede.rpsideas.spells.base.SpellRuntimeExceptions;
import net.minecraft.block.Block;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import org.apache.commons.lang3.tuple.Pair;
import vazkii.psi.api.internal.Quat;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.*;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceTrick;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class PieceTrickMoveBlockSequence extends PieceTrick {

    SpellParam position;
    SpellParam target;
    SpellParam maxBlocks;
    SpellParam direction;

    public PieceTrickMoveBlockSequence(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(position = new ParamVector(SpellParam.GENERIC_NAME_POSITION, SpellParam.BLUE, false, false));
        addParam(target = new ParamVector(SpellParam.GENERIC_NAME_TARGET, SpellParam.YELLOW, false, false));
        addParam(maxBlocks = new ParamNumber(SpellParam.GENERIC_NAME_MAX, SpellParam.RED, false, true));
        addParam(direction = new ParamVector("psi.spellparam.direction", SpellParam.GREEN, false, false));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException, ArithmeticException {
        super.addToMetadata(meta);

        Double maxBlocksVal = this.<Double>getParamEvaluation(maxBlocks);
        if(maxBlocksVal == null || maxBlocksVal <= 0)
            throw new SpellCompilationException(SpellCompilationException.NON_POSITIVE_VALUE, x, y);

        meta.addStat(EnumSpellStat.POTENCY, (int) (maxBlocksVal * 10));
        meta.addStat(EnumSpellStat.COST, (int) (maxBlocksVal * 15));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        if(context.caster.getEntityWorld().isRemote)
            return null;

        Vector3 directionVal = this.<Vector3>getParamValue(context, direction);
        Vector3 positionVal = this.<Vector3>getParamValue(context, position);
        Vector3 targetVal = this.<Vector3>getParamValue(context, target);
        Double maxBlocksVal = this.<Double>getParamValue(context, maxBlocks);
        int maxBlocksInt = maxBlocksVal.intValue();


        List<Pair<IBlockState, BlockPos>> toset = new ArrayList<>();

        if(positionVal == null)
            throw new SpellRuntimeException(SpellRuntimeException.NULL_VECTOR);
        if(!context.isInRadius(positionVal))
            throw new SpellRuntimeException(SpellRuntimeException.OUTSIDE_RADIUS);
        int len = (int) targetVal.mag();
        Vector3 directNorm = directionVal.copy().normalize();
        Vector3 targetNorm = targetVal.copy().normalize();
        if(!directionVal.isAxial() || directionVal.isZero())
            throw new SpellRuntimeException(SpellRuntimeExceptions.NON_AXIAL_VECTOR);
        for(int i = 0; i < Math.min(len, maxBlocksInt); ++i){
            Vector3 blockVec = positionVal.copy().add(targetNorm.copy().multiply(i));
            if(!context.isInRadius(blockVec))
                throw new SpellRuntimeException(SpellRuntimeException.OUTSIDE_RADIUS);

            World world = context.caster.getEntityWorld();
            BlockPos pos = new BlockPos(blockVec.x, blockVec.y, blockVec.z);
            IBlockState state = world.getBlockState(pos);
            Block block = state.getBlock();
            BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, pos, state, context.caster);
            MinecraftForge.EVENT_BUS.post(event);

            if(event.isCanceled())
                continue;

            if(world.getTileEntity(pos) != null || block.getPushReaction(state) != EnumPushReaction.NORMAL || !block.canSilkHarvest(world, pos, state, context.caster) || block.getPlayerRelativeBlockHardness(state, context.caster, world, pos) <= 0 || !ForgeHooks.canHarvestBlock(block, context.caster, world, pos))
                continue;

            Vector3 axis = directionVal.normalize();
            int x = pos.getX() + (int) axis.x;
            int y = pos.getY() + (int) axis.y;
            int z = pos.getZ() + (int) axis.z;

            BlockPos pos1 = new BlockPos(x, y, z);
            IBlockState state1 = world.getBlockState(pos1);

            if(!world.isBlockModifiable(context.caster, pos) || (new Vector3(x, y ,z) == positionVal.copy().add(targetNorm.copy()).multiply(i + 1) && (i + 1) < Math.min(len, maxBlocksInt)))
                continue;

            if(y > 256 || y < 1) continue;

            if(world.isAirBlock(pos1) || (new Vector3(x, y ,z) == positionVal.copy().add(targetNorm.copy()).multiply(i + 1) && (i + 1) < Math.min(len, maxBlocksInt)) ) {
                world.setBlockToAir(pos);
                world.playEvent(2001, pos, Block.getIdFromBlock(block) + (block.getMetaFromState(state) << 12));
                Pair<IBlockState, BlockPos> pair = Pair.of(state, pos1);
                toset.add(pair);

            }
        }

        for (Pair<IBlockState, BlockPos> aToset : toset) {
            World world = context.caster.getEntityWorld();
            IBlockState state = aToset.getLeft();
            BlockPos pos1 = aToset.getRight();
            world.setBlockState(pos1, state, 1 | 2);

        }

        return null;
    }
}