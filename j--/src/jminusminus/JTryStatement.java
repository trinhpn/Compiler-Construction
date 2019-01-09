package jminusminus;

import static jminusminus.CLConstants.*;
import java.util.HashMap;

/**
 * The AST node for an Try-statement.
 */

class JTryStatement extends JStatement {

    /** Test expression. */
    private JStatement tryStatement;
    private HashMap<JFormalParameter, JStatement> catchStatements;
    private JStatement finallyStatement;

    /**
     * Construct an AST node for an Try-statement given its line number, the test
     * expression, the consequent, and the alternate.
     * 
     * @param line
     *            line in which the Try-statement occurs in the source file.
     * @param condition
     *            test expression.
     * @param thenPart
     *            then clause.
     * @param elsePart
     *            else clause.
     */

    public JTryStatement(int line, JStatement tryStatement, HashMap<JFormalParameter, JStatement> catchStatements,
    		JStatement finallyStatement) {
        super(line);
        this.tryStatement = tryStatement;
        this.catchStatements = catchStatements;
        this.finallyStatement = finallyStatement;
        
    }

    /**
     * Analyzing the Try-statement means analyzing its components and checking
     * that the test is boolean.
     * 
     * @param context
     *            context in which names are resolved.
     * @return the analyzed (and possibly rewritten) AST subtree.
     */

    public JStatement analyze(Context context) {
    	return this;
    }

    /**
     * Code generation for an Try-statement. We generate code to branch over the
     * consequent Try !test; the consequent is followed by an unconditonal branch
     * over (any) alternate.
     * 
     * @param output
     *            the code emitter (basically an abstraction for producing the
     *            .class file).
     */

    public void codegen(CLEmitter output) {
            }

    /**
     * @inheritDoc
     */

    public void writeToStdOut(PrettyPrinter p) {
        p.printf("<JTryStatement line=\"%d\">\n", line());
        p.indentRight();
        
        p.printf("<Try Block>\n");
        p.indentRight();
        tryStatement.writeToStdOut(p);
        p.indentLeft();
        p.printf("</Try Block>\n");
        
        p.printf("<Catch Block>\n");
        p.indentRight();
        for (JFormalParameter j : catchStatements.keySet()) {
        	j.writeToStdOut(p);
        	catchStatements.get(j).writeToStdOut(p);
        }
        p.indentLeft();
        p.printf("</Catch Block>\n");
        
        if (finallyStatement != null) {
        	p.printf("<Finally Block>\n");
        	p.indentRight();
        	finallyStatement.writeToStdOut(p);
        	p.indentLeft();
        	p.printf("</Finally Block>\n");
        }
        
        p.indentLeft();
        p.printf("</JTryStatement>\n");
    }

}