// Copyright 2013 Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import static jminusminus.CLConstants.*;

/**
 * The AST node for a binary expression. A binary expression has an operator and
 * two operands: a lhs and a rhs.
 */

abstract class JBinaryExpression extends JExpression {

    /** The binary operator. */
    protected String operator;

    /** The lhs operand. */
    protected JExpression lhs;

    /** The rhs operand. */
    protected JExpression rhs;

    /**
     * Construct an AST node for a binary expression given its line number, the
     * binary operator, and lhs and rhs operands.
     * 
     * @param line
     *            line in which the binary expression occurs in the source file.
     * @param operator
     *            the binary operator.
     * @param lhs
     *            the lhs operand.
     * @param rhs
     *            the rhs operand.
     */

    protected JBinaryExpression(int line, String operator, JExpression lhs,
            JExpression rhs) {
        super(line);
        this.operator = operator;
        this.lhs = lhs;
        this.rhs = rhs;
    }

    /**
     * @inheritDoc
     */

    public void writeToStdOut(PrettyPrinter p) {
        p.printf("<JBinaryExpression line=\"%d\" type=\"%s\" "
                + "operator=\"%s\">\n", line(), ((type == null) ? "" : type
                .toString()), Util.escapeSpecialXMLChars(operator));
        p.indentRight();
        p.printf("<Lhs>\n");
        p.indentRight();
        lhs.writeToStdOut(p);
        p.indentLeft();
        p.printf("</Lhs>\n");
        p.printf("<Rhs>\n");
        p.indentRight();
        rhs.writeToStdOut(p);
        p.indentLeft();
        p.printf("</Rhs>\n");
        p.indentLeft();
        p.printf("</JBinaryExpression>\n");
    }

}

/**
 * The AST node for a plus (+) expression. In j--, as in Java, + is overloaded
 * to denote addition for numbers and concatenation for Strings.
 */

class JPlusOp extends JBinaryExpression {

    /**
     * Construct an AST node for an addition expression given its line number,
     * and the lhs and rhs operands.
     * 
     * @param line
     *            line in which the addition expression occurs in the source
     *            file.
     * @param lhs
     *            the lhs operand.
     * @param rhs
     *            the rhs operand.
     */

    public JPlusOp(int line, JExpression lhs, JExpression rhs) {
        super(line, "+", lhs, rhs);
    }

    /**
     * Analysis involves first analyzing the operands. If this is a string
     * concatenation, we rewrite the subtree to make that explicit (and analyze
     * that). Otherwise we check the types of the addition operands and compute
     * the result type.
     * 
     * @param context
     *            context in which names are resolved.
     * @return the analyzed (and possibly rewritten) AST subtree.
     */

    public JExpression analyze(Context context) {
        lhs = (JExpression) lhs.analyze(context);
        rhs = (JExpression) rhs.analyze(context);
        if (lhs.type() == Type.STRING || rhs.type() == Type.STRING) {
            return (new JStringConcatenationOp(line, lhs, rhs))
                    .analyze(context);
        } else if (lhs.type() == Type.INT && rhs.type() == Type.INT) {
            type = Type.INT;
        } else {
            type = Type.ANY;
            JAST.compilationUnit.reportSemanticError(line(),
                    "Invalid operand types for +");
        }
        return this;
    }

    /**
     * Any string concatenation has been rewritten as a JStringConcatenationOp
     * (in analyze()), so code generation here involves simply generating code
     * for loading the operands onto the stack and then generating the
     * appropriate add instruction.
     * 
     * @param output
     *            the code emitter (basically an abstraction for producing the
     *            .class file).
     */

    public void codegen(CLEmitter output) {
        if (type == Type.INT) {
            lhs.codegen(output);
            rhs.codegen(output);
            output.addNoArgInstruction(IADD);
        }
    }

}

/**
 * The AST node for a subtraction (-) expression.
 */

class JSubtractOp extends JBinaryExpression {

    /**
     * Construct an AST node for a subtraction expression given its line number,
     * and lhs and rhs operands.
     * 
     * @param line
     *            line in which the subtraction expression occurs in the source
     *            file.
     * @param lhs
     *            the lhs operand.
     * @param rhs
     *            the rhs operand.
     */

    public JSubtractOp(int line, JExpression lhs, JExpression rhs) {
        super(line, "-", lhs, rhs);
    }

    /**
     * Analyzing the - operation involves analyzing its operands, checking
     * types, and determining the result type.
     * 
     * @param context
     *            context in which names are resolved.
     * @return the analyzed (and possibly rewritten) AST subtree.
     */

    public JExpression analyze(Context context) {
        lhs = (JExpression) lhs.analyze(context);
        rhs = (JExpression) rhs.analyze(context);
        lhs.type().mustMatchExpected(line(), Type.INT);
        rhs.type().mustMatchExpected(line(), Type.INT);
        type = Type.INT;
        return this;
    }

    /**
     * Generating code for the - operation involves generating code for the two
     * operands, and then the subtraction instruction.
     * 
     * @param output
     *            the code emitter (basically an abstraction for producing the
     *            .class file).
     */

    public void codegen(CLEmitter output) {
        lhs.codegen(output);
        rhs.codegen(output);
        output.addNoArgInstruction(ISUB);
    }

}

/**
 * The AST node for a multiplication (*) expression.
 */

class JMultiplyOp extends JBinaryExpression {

    /**
     * Construct an AST for a multiplication expression given its line number,
     * and the lhs and rhs operands.
     * 
     * @param line
     *            line in which the multiplication expression occurs in the
     *            source file.
     * @param lhs
     *            the lhs operand.
     * @param rhs
     *            the rhs operand.
     */

    public JMultiplyOp(int line, JExpression lhs, JExpression rhs) {
        super(line, "*", lhs, rhs);
    }

    /**
     * Analyzing the * operation involves analyzing its operands, checking
     * types, and determining the result type.
     * 
     * @param context
     *            context in which names are resolved.
     * @return the analyzed (and possibly rewritten) AST subtree.
     */

    public JExpression analyze(Context context) {
        lhs = (JExpression) lhs.analyze(context);
        rhs = (JExpression) rhs.analyze(context);
        lhs.type().mustMatchExpected(line(), Type.INT);
        rhs.type().mustMatchExpected(line(), Type.INT);
        type = Type.INT;
        return this;
    }

    /**
     * Generating code for the * operation involves generating code for the two
     * operands, and then the multiplication instruction.
     * 
     * @param output
     *            the code emitter (basically an abstraction for producing the
     *            .class file).
     */

    public void codegen(CLEmitter output) {
        lhs.codegen(output);
        rhs.codegen(output);
        output.addNoArgInstruction(IMUL);
    }
}

//////////////////////// 
//Added
////////////////////////

/**
 * The AST node for a division (/) expression.
 */

class JDivideOp extends JBinaryExpression {

    /**
     * Construct an AST for a division expression given its line number,
     * and the lhs and rhs operands.
     *
     * @param line
     *            line in which the multiplication expression occurs in the
     *            source file.
     * @param lhs
     *            the lhs operand.
     * @param rhs
     *            the rhs operand.
     */

    public JDivideOp(int line, JExpression lhs, JExpression rhs) {
        super(line, "/", lhs, rhs);
    }

    /**
     * Analyzing the / operation involves analyzing its operands, checking
     * types, and determining the result type.
     *
     * @param context
     *            context in which names are resolved.
     * @return the analyzed (and possibly rewritten) AST subtree.
     */

    public JExpression analyze(Context context) {
        lhs = (JExpression) lhs.analyze(context);
        rhs = (JExpression) rhs.analyze(context);
        lhs.type().mustMatchExpected(line(), Type.INT);
        rhs.type().mustMatchExpected(line(), Type.INT);
        type = Type.INT;
        return this;
    }

    /**
     * Generating code for the / operation involves generating code for the two
     * operands, and then the multiplication instruction.
     *
     * @param output
     *            the code emitter (basically an abstraction for producing the
     *            .class file).
     */

    public void codegen(CLEmitter output) {
        lhs.codegen(output);
        rhs.codegen(output);
        output.addNoArgInstruction(IDIV);
    }
}

/**
 * The AST node for a remainder (%) expression.
 */

class JRemOp extends JBinaryExpression {

    /**
     * Construct an AST for a remainder expression given its line number,
     * and the lhs and rhs operands.
     *
     * @param line
     *            line in which the remainder expression occurs in the
     *            source file.
     * @param lhs
     *            the lhs operand.
     * @param rhs
     *            the rhs operand.
     */

    public JRemOp(int line, JExpression lhs, JExpression rhs) {
        super(line, "%", lhs, rhs);
    }

    /**
     * Analyzing the % operation involves analyzing its operands, checking
     * types, and determining the result type.
     *
     * @param context
     *            context in which names are resolved.
     * @return the analyzed (and possibly rewritten) AST subtree.
     */

    public JExpression analyze(Context context) {
        lhs = (JExpression) lhs.analyze(context);
        rhs = (JExpression) rhs.analyze(context);
        lhs.type().mustMatchExpected(line(), Type.INT);
        rhs.type().mustMatchExpected(line(), Type.INT);
        type = Type.INT;
        return this;
    }

    /**
     * Generating code for the % operation involves generating code for the two
     * operands, and then the multiplication instruction.
     *
     * @param output
     *            the code emitter (basically an abstraction for producing the
     *            .class file).
     */

    public void codegen(CLEmitter output) {
        lhs.codegen(output);
        rhs.codegen(output);
        output.addNoArgInstruction(IREM);
    }

}

class JMinusAssignOp extends JBinaryExpression {
	public JMinusAssignOp(int line, JExpression lhs, JExpression rhs) {
		super(line, "-=", lhs, rhs);
	}

	public JExpression analyze(Context context) {
    	//FIXME Temporary placeholder
		lhs = (JExpression) lhs.analyze(context);
		rhs = (JExpression) rhs.analyze(context);
		lhs.type().mustMatchExpected(line(), Type.INT);
		rhs.type().mustMatchExpected(line(), Type.INT);
		type = Type.INT;
		return this;
	}

	public void codegen(CLEmitter output) {
		//TODO
	}
}

class JMultiplyAssignOp extends JBinaryExpression {
	public JMultiplyAssignOp(int line, JExpression lhs, JExpression rhs) {
		super(line, "*=", lhs, rhs);
	}

	public JExpression analyze(Context context) {
    	//FIXME Temporary placeholder 
		lhs = (JExpression) lhs.analyze(context);
		rhs = (JExpression) rhs.analyze(context);
		lhs.type().mustMatchExpected(line(), Type.INT);
		rhs.type().mustMatchExpected(line(), Type.INT);
		type = Type.INT;
		return this;
	}

	public void codegen(CLEmitter output) {
		//TODO
	}
}

class JDivideAssignOp extends JBinaryExpression {
	public JDivideAssignOp(int line, JExpression lhs, JExpression rhs) {
		super(line, "/=", lhs, rhs);
	}

	public JExpression analyze(Context context) {
    	//FIXME Temporary placeholder
		lhs = (JExpression) lhs.analyze(context);
		rhs = (JExpression) rhs.analyze(context);
		lhs.type().mustMatchExpected(line(), Type.INT);
		rhs.type().mustMatchExpected(line(), Type.INT);
		type = Type.INT;
		return this;
	}

	public void codegen(CLEmitter output) {
		//TODO
	}
}

class JModuloAssignOp extends JBinaryExpression {
	public JModuloAssignOp(int line, JExpression lhs, JExpression rhs) {
		super(line, "%=", lhs, rhs);
	}

	public JExpression analyze(Context context) {
    	//FIXME Temporary placeholder 
		lhs = (JExpression) lhs.analyze(context);
		rhs = (JExpression) rhs.analyze(context);
		lhs.type().mustMatchExpected(line(), Type.INT);
		rhs.type().mustMatchExpected(line(), Type.INT);
		type = Type.INT;
		return this;
	}

	public void codegen(CLEmitter output) {
		//TODO
	}
}
class JLShiftAssignOp extends JBinaryExpression {
	public JLShiftAssignOp(int line, JExpression lhs, JExpression rhs) {
		super(line, "<<=", lhs, rhs);
	}

	public JExpression analyze(Context context) {
    	//FIXME Temporary placeholder
		lhs = (JExpression) lhs.analyze(context);
		rhs = (JExpression) rhs.analyze(context);
		lhs.type().mustMatchExpected(line(), Type.INT);
		rhs.type().mustMatchExpected(line(), Type.INT);
		type = Type.INT;
		return this;
	}

	public void codegen(CLEmitter output) {
		//TODO
	}
}

class JRShiftAssignOp extends JBinaryExpression {
	public JRShiftAssignOp(int line, JExpression lhs, JExpression rhs) {
		super(line, ">>=", lhs, rhs);
	}

	public JExpression analyze(Context context) {
    	//FIXME Temporary placeholder 
		lhs = (JExpression) lhs.analyze(context);
		rhs = (JExpression) rhs.analyze(context);
		lhs.type().mustMatchExpected(line(), Type.INT);
		rhs.type().mustMatchExpected(line(), Type.INT);
		type = Type.INT;
		return this;
	}

	public void codegen(CLEmitter output) {
		//TODO
	}
}

class JUSRShiftAssignOp extends JBinaryExpression {
	public JUSRShiftAssignOp(int line, JExpression lhs, JExpression rhs) {
		super(line, ">>=", lhs, rhs);
	}

	public JExpression analyze(Context context) {
    	//FIXME Temporary 
		lhs = (JExpression) lhs.analyze(context);
		rhs = (JExpression) rhs.analyze(context);
		lhs.type().mustMatchExpected(line(), Type.INT);
		rhs.type().mustMatchExpected(line(), Type.INT);
		type = Type.INT;
		return this;
	}

	public void codegen(CLEmitter output) {
		//TODO
	}
}

class JBitAndAssignOp extends JBinaryExpression {
	public JBitAndAssignOp(int line, JExpression lhs, JExpression rhs) {
		super(line, "&=", lhs, rhs);
	}

	public JExpression analyze(Context context) {
    	//FIXME Temporary placeholder 
		lhs = (JExpression) lhs.analyze(context);
		rhs = (JExpression) rhs.analyze(context);
		lhs.type().mustMatchExpected(line(), Type.INT);
		rhs.type().mustMatchExpected(line(), Type.INT);
		type = Type.INT;
		return this;
	}

	public void codegen(CLEmitter output) {
		//TODO
	}
}

class JBitOrAssignOp extends JBinaryExpression {
	public JBitOrAssignOp(int line, JExpression lhs, JExpression rhs) {
		super(line, "|=", lhs, rhs);
	}

	public JExpression analyze(Context context) {
    	//FIXME Temporary placeholder
		lhs = (JExpression) lhs.analyze(context);
		rhs = (JExpression) rhs.analyze(context);
		lhs.type().mustMatchExpected(line(), Type.INT);
		rhs.type().mustMatchExpected(line(), Type.INT);
		type = Type.INT;
		return this;
	}

	public void codegen(CLEmitter output) {
		//TODO
	}
}
class JBitXorAssignOp extends JBinaryExpression {
	public JBitXorAssignOp(int line, JExpression lhs, JExpression rhs) {
		super(line, "^=", lhs, rhs);
	}

	public JExpression analyze(Context context) {
    	//FIXME Temporary placeholder 
		lhs = (JExpression) lhs.analyze(context);
		rhs = (JExpression) rhs.analyze(context);
		lhs.type().mustMatchExpected(line(), Type.INT);
		rhs.type().mustMatchExpected(line(), Type.INT);
		type = Type.INT;
		return this;
	}

	public void codegen(CLEmitter output) {
		//TODO
	}
}

class JLogicalOrOp extends JBinaryExpression {
	public JLogicalOrOp(int line, JExpression lhs, JExpression rhs) {
		super(line, "||", lhs, rhs);
	}

	public JExpression analyze(Context context) {
    	//FIXME Temporary placeholder 
		lhs = (JExpression) lhs.analyze(context);
		rhs = (JExpression) rhs.analyze(context);
		lhs.type().mustMatchExpected(line(), Type.INT);
		rhs.type().mustMatchExpected(line(), Type.INT);
		type = Type.INT;
		return this;
	}

	public void codegen(CLEmitter output) {
		//TODO
	}
}

class JBitOrOp extends JBinaryExpression {
	public JBitOrOp(int line, JExpression lhs, JExpression rhs) {
		super(line, "|", lhs, rhs);
	}

	public JExpression analyze(Context context) {
    	//FIXME Temporary placeholder 
		lhs = (JExpression) lhs.analyze(context);
		rhs = (JExpression) rhs.analyze(context);
		lhs.type().mustMatchExpected(line(), Type.INT);
		rhs.type().mustMatchExpected(line(), Type.INT);
		type = Type.INT;
		return this;
	}

	public void codegen(CLEmitter output) {
		//TODO
	}
}

class JBitXorOp extends JBinaryExpression {
	public JBitXorOp(int line, JExpression lhs, JExpression rhs) {
		super(line, "^", lhs, rhs);
	}

	public JExpression analyze(Context context) {
    	//FIXME Temporary placeholder 
		lhs = (JExpression) lhs.analyze(context);
		rhs = (JExpression) rhs.analyze(context);
		lhs.type().mustMatchExpected(line(), Type.INT);
		rhs.type().mustMatchExpected(line(), Type.INT);
		type = Type.INT;
		return this;
	}

	public void codegen(CLEmitter output) {
		//TODO
	}
}

class JBitAndOp extends JBinaryExpression {
	public JBitAndOp(int line, JExpression lhs, JExpression rhs) {
		super(line, "&", lhs, rhs);
	}

	public JExpression analyze(Context context) {
    	//FIXME Temporary placeholder
		lhs = (JExpression) lhs.analyze(context);
		rhs = (JExpression) rhs.analyze(context);
		lhs.type().mustMatchExpected(line(), Type.INT);
		rhs.type().mustMatchExpected(line(), Type.INT);
		type = Type.INT;
		return this;
	}

	public void codegen(CLEmitter output) {
		//TODO
	}
}

class JNotEqualsOp extends JBinaryExpression {
	public JNotEqualsOp(int line, JExpression lhs, JExpression rhs) {
		super(line, "!=", lhs, rhs);
	}

	public JExpression analyze(Context context) {
    	//FIXME Temporary placeholder that may not be correct.
		lhs = (JExpression) lhs.analyze(context);
		rhs = (JExpression) rhs.analyze(context);
		lhs.type().mustMatchExpected(line(), Type.INT);
		rhs.type().mustMatchExpected(line(), Type.INT);
		type = Type.INT;
		return this;
	}

	public void codegen(CLEmitter output) {
		//TODO
	}
}

class JGreaterEqualOp extends JBinaryExpression {
	public JGreaterEqualOp(int line, JExpression lhs, JExpression rhs) {
		super(line, ">=", lhs, rhs);
	}

	public JExpression analyze(Context context) {
    	//FIXME Temporary placeholder that may not be correct.
		lhs = (JExpression) lhs.analyze(context);
		rhs = (JExpression) rhs.analyze(context);
		lhs.type().mustMatchExpected(line(), Type.INT);
		rhs.type().mustMatchExpected(line(), Type.INT);
		type = Type.INT;
		return this;
	}

	public void codegen(CLEmitter output) {
		//TODO
	}
}
class JLessThanOp extends JBinaryExpression {
	public JLessThanOp(int line, JExpression lhs, JExpression rhs) {
		super(line, "<", lhs, rhs);
	}

	public JExpression analyze(Context context) {
    	//FIXME Temporary placeholder that may not be correct.
		lhs = (JExpression) lhs.analyze(context);
		rhs = (JExpression) rhs.analyze(context);
		lhs.type().mustMatchExpected(line(), Type.INT);
		rhs.type().mustMatchExpected(line(), Type.INT);
		type = Type.INT;
		return this;
	}

	public void codegen(CLEmitter output) {
		//TODO
	}
}

class JLeftShiftOp extends JBinaryExpression {
	public JLeftShiftOp(int line, JExpression lhs, JExpression rhs) {
		super(line, "<<", lhs, rhs);
	}

	public JExpression analyze(Context context) {
    	//FIXME Temporary placeholder that may not be correct.
		lhs = (JExpression) lhs.analyze(context);
		rhs = (JExpression) rhs.analyze(context);
		lhs.type().mustMatchExpected(line(), Type.INT);
		rhs.type().mustMatchExpected(line(), Type.INT);
		type = Type.INT;
		return this;
	}

	public void codegen(CLEmitter output) {
		//TODO
	}
}

class JRightShiftOp extends JBinaryExpression {
	public JRightShiftOp(int line, JExpression lhs, JExpression rhs) {
		super(line, ">>", lhs, rhs);
	}

	public JExpression analyze(Context context) {
    	//FIXME Temporary placeholder that may not be correct.
		lhs = (JExpression) lhs.analyze(context);
		rhs = (JExpression) rhs.analyze(context);
		lhs.type().mustMatchExpected(line(), Type.INT);
		rhs.type().mustMatchExpected(line(), Type.INT);
		type = Type.INT;
		return this;
	}

	public void codegen(CLEmitter output) {
		//TODO
	}
}

class JUnsignedRightShiftOp extends JBinaryExpression {
	public JUnsignedRightShiftOp(int line, JExpression lhs, JExpression rhs) {
		super(line, ">>>", lhs, rhs);
	}

	public JExpression analyze(Context context) {
    	//FIXME Temporary placeholder that may not be correct.
		lhs = (JExpression) lhs.analyze(context);
		rhs = (JExpression) rhs.analyze(context);
		lhs.type().mustMatchExpected(line(), Type.INT);
		rhs.type().mustMatchExpected(line(), Type.INT);
		type = Type.INT;
		return this;
	}

	public void codegen(CLEmitter output) {
		//TODO
	}
}