package jminusminus;

import static jminusminus.CLConstants.*;

import java.util.ArrayList;
import java.util.List;

public class JSwitchStatement extends JStatement {
	
	private class SwitchPair {
		public JExpression[] myCases;
		public JStatement[] myStatements;
		
		public SwitchPair(List<JExpression> theCases, List<JStatement> statementList) {			
			myCases = new JExpression[theCases.size()];			
			int i = 0;
			for (JExpression c : theCases) {
				myCases[i] = c;
				i++;
			}
			
			myStatements = new JStatement[statementList.size()];
			i = 0;
			for (JStatement s : statementList) {
				myStatements[i] = s;
				i++;
			}
		}
	}
	
	
	private JExpression myClause;
	private List<SwitchPair> mySwitchPairs;
	
	public JSwitchStatement(int line, JExpression theClause) {
		super(line);
		this.mySwitchPairs = new ArrayList<SwitchPair>();
		this.myClause = theClause;
	}
	
	/**
	 * Add a new pair of case statements followed by a body.
	 * @param theCases The list of all cases that share the same body. Set a case to null for default.
	 * @param theBody The body of code to be executed following the statements.
	 * @return true if no duplicates were detected and the add was successful; false otherwise.
	 */
	public boolean addSwitchPair(List<JExpression> theCases, List<JStatement> statementList) {
		for (int i = 0; i < theCases.size() - 1; i++) {
			for (int j = i + 1; j < theCases.size(); j++) {
				if (theCases.get(i).equals(theCases.get(j))) {
					//Duplicate case detected.
					return false;
				}
			}
		}
		for (SwitchPair pair : mySwitchPairs) {
			for (int i = 0; i < pair.myCases.length; i++) {
				for (int j = 0; j < theCases.size(); j++) {
					if (pair.myCases[i] == null || theCases.get(j) == null) {
						
						if (pair.myCases[i] == null && theCases.get(j) == null) return false;
						else continue;
					}
					if (pair.myCases[i].equals(theCases.get(j))) {
						//Duplicate case detected.
						return false;
					}
				}
			}
		}

		mySwitchPairs.add(new SwitchPair(theCases, statementList));		
		return true;
	}
	
    /**
     * Analysis involves analyzing the test, checking its type and analyzing the
     * body statement.
     * 
     * @param context
     *            context in which names are resolved.
     * @return the analyzed (and possibly rewritten) AST subtree.
     */
	
	public JSwitchStatement analyze(Context context) {
        //TODO
        return this;
    }

    /**
     * Generate code for the switch statement.
     * 
     * @param output
     *            the code emitter (basically an abstraction for producing the
     *            .class file).
     */

    public void codegen(CLEmitter output) {
    	//TODO
    	
    }
    
    public void writeToStdOut(PrettyPrinter p) {
    	 p.printf("<JSwitchStatement line=\"%d\">\n", line());
         p.indentRight();
         p.printf("<SwitchExpression>\n");
         p.indentRight();
         this.myClause.writeToStdOut(p);
         p.indentLeft();
         p.printf("</SwitchExpression>\n");
         
         for (SwitchPair pair : mySwitchPairs) {
        	 p.printf("<CasePairing>\n");
             p.indentRight();
             for (int i = 0; i < pair.myCases.length; i++) {
            	 p.printf("<Case>\n");
            	 p.indentRight();
            	 if (pair.myCases[i] == null) {
            		 p.printf("<Default>\n");
            	 } else {
            		 pair.myCases[i].writeToStdOut(p);
            	 }
            	 p.indentLeft();
            	 p.printf("</Case>\n");
             }
             p.printf("<Statements>\n");
             p.indentRight();
             if (pair.myStatements == null) {
            	 p.printf("No Statements.\n");
             } else {
                 for (int i = 0; i < pair.myStatements.length; i++) {
                	//TODO Circumvent this error altogether.
                	if (pair.myStatements[i] == null) {
                		p.printf("Null Statement.\n");
                		break;
                	}
                 	pair.myStatements[i].writeToStdOut(p);
                  }
             }
             p.indentLeft();
             p.printf("</Statements>\n");
             p.indentLeft();
             p.printf("</CasePairing>\n");
         }
         
         p.indentLeft();
         p.printf("</JSwitchStatement>\n");
    }
	
}