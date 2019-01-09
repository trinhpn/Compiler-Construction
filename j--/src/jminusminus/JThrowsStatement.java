// Copyright 2013 Bill Campbell, Swami Iyer and Bahar Akbal-Delibas
//This file copies the JIfStatement class, only the constructor, classname, fields and writeToStdOut methods have been altered.
//First things t

package jminusminus;

import static jminusminus.CLConstants.*;
import java.util.ArrayList;

/**
 * The AST node for an if-statement.
 */

class JThrowsStatement extends JStatement {

    /** The list of all exceptions that are thrown. */
	ArrayList<String> myExceptions;

    /**
     * Construct an AST node for an if-statement given its line number, the test
     * expression, the consequent, and the alternate.
     * 
     * @param line
     *            line in which the if-statement occurs in the source file.
     * @param condition
     *            test expression.
     * @param thenPart
     *            then clause.
     * @param elsePart
     *            else clause.
     */

    public JThrowsStatement(int line, ArrayList<String> exceptions) {
        super(line);
        //assigning the reference to the parameter is fine since currently it's not used anywhere else 
        myExceptions = exceptions;
    }

    /**
     * Analyzing the throws-statement is currently not done.
     * 
     * @param context
     *            context in which names are resolved.
     * @return the analyzed (and possibly rewritten) AST subtree.
     */

    public JStatement analyze(Context context) {
        return this;
    }

    /**
     * Code generation for a throws-statement.
     * 
     * @param output
     *            the code emitter (basically an abstraction for producing the
     *            .class file).
     */

    public void codegen(CLEmitter output) {
        //nothing yet 
    }
    
    public void addException(String theException) {
    	//perhaps some error checking
    	myExceptions.add(theException);
    }
    
    //true if myExceptions is empty
    public boolean isEmpty() {
    	return myExceptions.isEmpty();
    }

    /**
     * Prints out all exceptions in their own tag, each printed on their own line.
     * @inheritDoc
     */

    public void writeToStdOut(PrettyPrinter p) {
        p.printf("<JThrowsStatement line=\"%d\">\n", line());
        p.indentRight();
	    for (String s : myExceptions) {
	    	p.printf("%s\n", s);
	    }
        p.indentLeft();
        p.printf("</JThrowsStatement>\n");
    }

}
