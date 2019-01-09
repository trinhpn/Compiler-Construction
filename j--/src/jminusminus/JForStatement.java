package jminusminus;

import static jminusminus.CLConstants.*;
import java.util.ArrayList;

public class JForStatement extends JStatement {
	
	/** Declarator. */
    private JVariableDeclaration initialization;

    /** The collection. */
    private JExpression condition;

    private JStatement increment;

    private JStatement body;
	
	private int line;
		
	public JForStatement(int line, JVariableDeclaration initialization, JExpression condition, JStatement increment, JStatement body) {
		super(line);
		this.line = line;
		this.initialization = initialization;
		this.condition = condition;
		this.increment = increment;
		this.body = body;
	}
	
    /**
     * Analysis involves analyzing the test, checking its type and analyzing the
     * body statement.
     * 
     * @param context
     *            context in which names are resolved.
     * @return the analyzed (and possibly rewritten) AST subtree.
     */
	
	public JForStatement analyze(Context context) {
        //TODO
		if (initialization != null) {
			initialization = (JVariableDeclaration) initialization.analyze(context);
        }
        if (condition != null) {
            condition = condition.analyze(context);
            condition.type().mustMatchExpected(line(), Type.BOOLEAN);
        }
        if (increment != null) {
            increment = (JStatement) increment.analyze(context);
        }
        body = (JStatement) body.analyze(context); 
        return this;
    }

    /**
     * Generate code for the for loop.
     * 
     * @param output
     *            the code emitter (basically an abstraction for producing the
     *            .class file).
     */

    public void codegen(CLEmitter output) {
    	//TODO
    }
	
	public void writeToStdOut(PrettyPrinter p) {
		p.printf("<JForStatement line=\"%d\">\n", line());
        p.indentRight();

        p.printf("<Initializer>\n");
        p.indentRight();
        initialization.writeToStdOut(p);
        p.indentLeft();
        p.printf("</Initializer>\n");

        p.printf("<Test>\n");
        p.indentRight();
        condition.writeToStdOut(p);
        p.indentLeft();
        p.printf("</Test>\n");

        p.printf("<Increment>\n");
        p.indentRight();
        increment.writeToStdOut(p);
        p.indentLeft();
        p.printf("</Increment>\n");

        p.printf("<Body>\n");
        p.indentRight();
        body.writeToStdOut(p);
        p.indentLeft();
        p.printf("</Body>\n");

        p.indentLeft();
        p.printf("</JForStatement>\n");
    }
}
