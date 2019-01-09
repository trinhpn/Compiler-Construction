package jminusminus;

import static jminusminus.CLConstants.GOTO;

public class JTernaryExpression extends JExpression {

	private JExpression condition;
	private JExpression resultIfTrue;
	private JExpression resultIfFalse;

	protected JTernaryExpression(int line, JExpression condition,
			JExpression resultIfTrue, JExpression resultIfFalse) {
		super(line);
		this.condition = condition;
		this.resultIfTrue = resultIfTrue;
		this.resultIfFalse = resultIfFalse;
	}

	public JExpression analyze(Context context) {
		// Based on JIfStatement
		condition = (JExpression) condition.analyze(context);
		condition.type().mustMatchExpected(line(), Type.BOOLEAN);

		// Assignment for resultIfTrue and resultIfFalse
		resultIfTrue = (JExpression) resultIfTrue.analyze(context);
		resultIfFalse = (JExpression) resultIfFalse.analyze(context);
		

		return this;
	}

	@Override
	public void codegen(CLEmitter output) {
		// labels
		String ifFalseLabel = output.createLabel();
		String endLabel = output.createLabel();
		// code gen for expression
		condition.codegen(output, ifFalseLabel, false);
		// if true
		resultIfTrue.codegen(output);
		// skip over if false part
		output.addBranchInstruction(GOTO, endLabel);

		// Label for false
		output.addLabel(ifFalseLabel);
		resultIfFalse.codegen(output);
		output.addLabel(endLabel);
	}

	@Override
	public void writeToStdOut(PrettyPrinter p) {
        p.printf("<JTernaryExpression line=\"%d\">\n", line());
        p.indentRight();
        p.printf("<Condition>\n");
        p.indentRight();
        condition.writeToStdOut(p);
        p.indentLeft();
        p.printf("</Condition>\n");
        p.printf("<WhenTrue>\n");
        p.indentRight();
        resultIfTrue.writeToStdOut(p);
        p.indentLeft();
        p.printf("</WhenTrue>\n");
        p.printf("<WhenFalse>\n");
        p.indentRight();
        resultIfFalse.writeToStdOut(p);
        p.indentLeft();
        p.printf("</WhenFalse>\n");
        p.indentLeft();
        p.printf("</JTernaryExpression>\n");
	}
}
