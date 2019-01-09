// Copyright 2013 Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import static jminusminus.CLConstants.*;

/**
 * The AST node for an if-statement.
 */

class JThrowStatement extends JStatement {

    /** Throw expression. */
    private JExpression throwExpression;
    
    /** Checks to see if the thrown expression is an identifier or a newly created object */
    private boolean isNewOp;

    /**
     * Construct an AST node for an if-statement given its line number, the test
     * expression, the consequent, and the alternate.
     * 
     * @param line
     *            line in which the if-statement occurs in the source file.
     * @param condition
     *            test expression.
     */

    public JThrowStatement(int line, JExpression throwExpression, boolean hasNew) {
        super(line);
        this.throwExpression = throwExpression;
        this.isNewOp = hasNew;        
    }

    /**
     * Analyzing the if-statement means analyzing its components and checking
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
     * Code generation for an if-statement. We generate code to branch over the
     * consequent if !test; the consequent is followed by an unconditonal branch
     * over (any) alternate.
     * 
     * @param output
     *            the code emitter (basically an abstraction for producing the
     *            .class file).
     */

    public void codegen(CLEmitter output) {
    	//nothin yet
    }

    /**
     * @inheritDoc
     */

    public void writeToStdOut(PrettyPrinter p) {
        p.printf("<JThrowStatement line=\"%d\">\n", line());
        p.indentRight();
        throwExpression.writeToStdOut(p);
        p.indentLeft();
        p.printf("</JThrowStatement>\n");
    }

}
