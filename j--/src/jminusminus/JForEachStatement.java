// Copyright 2013 Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import static jminusminus.CLConstants.*;

/**
 * The AST node for a while-statement.
 */

class JForEachStatement extends JStatement {
    /** Declarator. */
    private JVariableDeclarator initializer;

    /** The collection. */
    private JVariableDeclarator collection;

    private JStatement body;


    public JForEachStatement(int line, JVariableDeclarator initializer,
                                       JVariableDeclarator collection,
                                       JStatement body) {
        super(line);
        this.initializer = initializer;
        this.collection = collection;
        this.body = body;
    }


    public JForEachStatement analyze(Context context) {
        initializer = (JVariableDeclarator) initializer.analyze(context);
        collection = (JVariableDeclarator) collection.analyze(context);
        //body = (JExpression) body.analyze(context); // stmnt vs expr?
        return this;
    }


    public void codegen(CLEmitter output) {

    }


    public void writeToStdOut(PrettyPrinter p) {
        p.printf("<JForEachStatement line=\"%d\">\n", line());
        p.indentRight();

        p.printf("<Iterator>\n");
        p.indentRight();
        initializer.writeToStdOut(p);
        p.indentLeft();
        p.printf("</Iterator>\n");

        p.printf("<Collection>\n");
        p.indentRight();
        collection.writeToStdOut(p);
        p.indentLeft();
        p.printf("</Collection>\n");
        p.indentLeft();

        p.indentRight();
        p.printf("<Body>\n");
        p.indentRight();
        body.writeToStdOut(p);
        p.indentLeft();
        p.printf("</Body>\n");

        p.indentLeft();
        p.printf("</JForEachStatement>\n");
    }

}
