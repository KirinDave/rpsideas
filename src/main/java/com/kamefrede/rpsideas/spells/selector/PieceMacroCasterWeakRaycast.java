package com.kamefrede.rpsideas.spells.selector;

import com.kamefrede.rpsideas.spells.operator.vector.PieceOperatorWeakRaycast;
import com.kamefrede.rpsideas.util.helpers.SpellHelpers;
import net.minecraft.util.math.RayTraceResult;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.*;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.param.ParamVector;

public class PieceMacroCasterWeakRaycast extends SpellPiece {
    private SpellParam maxDistance;

    public PieceMacroCasterWeakRaycast(Spell spell) {
        super(spell);
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        meta.addStat(EnumSpellStat.COMPLEXITY, 4);
    }
    @Override
    public void initParams() {
        addParam(maxDistance = new ParamNumber(SpellParam.GENERIC_NAME_MAX, SpellParam.PURPLE, true, false));
    }



    @Override
    public Object evaluate() {
        return null;
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Vector3 originVal = Vector3.fromEntity(context.caster).add(0, context.caster.getEyeHeight(), 0);
        Vector3 rayVal = new Vector3(context.caster.getLook(1F));

        double maxLen = SpellHelpers.getBoundedNumber(this, context, maxDistance, SpellContext.MAX_DISTANCE);

        RayTraceResult pos = PieceOperatorWeakRaycast.raycast(context.caster.world, originVal, rayVal, maxLen);
        if (pos == null)
            throw new SpellRuntimeException(SpellRuntimeException.NULL_VECTOR);

        return Vector3.fromBlockPos(pos.getBlockPos());
    }

    @Override
    public EnumPieceType getPieceType() {
        return EnumPieceType.SELECTOR;
    }

    @Override
    public Class<?> getEvaluationType() {
        return Vector3.class;
    }
}
