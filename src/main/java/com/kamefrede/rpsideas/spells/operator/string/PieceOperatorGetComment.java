package com.kamefrede.rpsideas.spells.operator.string;

import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.piece.PieceOperator;

public class PieceOperatorGetComment extends PieceOperator {

    public PieceOperatorGetComment(Spell spell) {
        super(spell);
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        if (this.comment != null && !this.comment.isEmpty()) return comment;
        return null;
    }

    @Override
    public Class<?> getEvaluationType() {
        return String.class;
    }
}
