// Copyright 2011 Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import static jminusminus.CLConstants.*;

/**
 * The AST node for a return-statement. If the enclosing method
 * in non-void, then there is a value to return, so we keep track
 * of the expression denoting that value and its type.
 */

class JBreakStatement extends JStatement {

    /**
     * Construct an AST node for a break-statement given its
     * line number.
     * 
     * @param line
     *                line in which the break-statement appears
     *                in the source file.
     */

    public JBreakStatement(int line) {
        super(line);
    }

    //TODO
    public JStatement analyze(Context context) {
    	return this;
    }

    //TODO
    public void codegen(CLEmitter output) {
    	
    }

    public void writeToStdOut(PrettyPrinter p) {
    	p.printf("<JBreakStatement line=\"%d\">\n", line());
    }
}
