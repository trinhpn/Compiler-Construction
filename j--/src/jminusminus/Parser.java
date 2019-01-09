// Part 2 and Part 3 done
//
// Copyright 2013 Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import jminusminus.JSwitchStatement;
import static jminusminus.TokenKind.*;

/**
 * A recursive descent parser that, given a lexical analyzer (a
 * LookaheadScanner), parses a Java compilation unit (program file), taking
 * tokens from the LookaheadScanner, and produces an abstract syntax tree (AST)
 * for it.
 */

public class Parser {

    /**
     * The lexical analyzer with which tokens are scanned.
     */
    private LookaheadScanner scanner;

    /**
     * Whether a parser error has been found.
     */
    private boolean isInError;

    /**
     * Wheter we have recovered from a parser error.
     */
    private boolean isRecovered;

    /**
     * Construct a parser from the given lexical analyzer.
     *
     * @param scanner the lexical analyzer with which tokens are scanned.
     */

    public Parser(LookaheadScanner scanner) {
        this.scanner = scanner;
        isInError = false;
        isRecovered = true;
        scanner.next(); // Prime the pump
    }

    /**
     * Has a parser error occurred up to now?
     *
     * @return true or false.
     */

    public boolean errorHasOccurred() {
        return isInError;
    }

    // ////////////////////////////////////////////////
    // Parsing Support ///////////////////////////////
    // ////////////////////////////////////////////////

    /**
     * Is the current token this one?
     *
     * @param sought the token we're looking for.
     * @return true iff they match; false otherwise.
     */

    private boolean see(TokenKind sought) {
        return (sought == scanner.token().kind());
    }

    /**
     * Look at the current (unscanned) token to see if it's one we're looking
     * for. If so, scan it and return true; otherwise return false (without
     * scanning a thing).
     *
     * @param sought the token we're looking for.
     * @return true iff they match; false otherwise.
     */

    private boolean have(TokenKind sought) {
        if (see(sought)) {
            scanner.next();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Attempt to match a token we're looking for with the current input token.
     * If we succeed, scan the token and go into a "isRecovered" state. If we
     * fail, then what we do next depends on whether or not we're currently in a
     * "isRecovered" state: if so, we report the error and go into an
     * "Unrecovered" state; if not, we repeatedly scan tokens until we find the
     * one we're looking for (or EOF) and then return to a "isRecovered" state.
     * This gives us a kind of poor man's syntactic error recovery. The strategy
     * is due to David Turner and Ron Morrison.
     *
     * @param sought the token we're looking for.
     */

    private void mustBe(TokenKind sought) {
        if (scanner.token().kind() == sought) {
            scanner.next();
            isRecovered = true;
        } else if (isRecovered) {
            isRecovered = false;
            reportParserError("%s found where %s sought on line %d", scanner.token()
                    .image(), sought.image());
        } else {
            // Do not report the (possibly spurious) error,
            // but rather attempt to recover by forcing a match.
            while (!see(sought) && !see(EOF)) {
                scanner.next();
            }
            if (see(sought)) {
                scanner.next();
                isRecovered = true;
            }
        }
    }

    /**
     * Pull out the ambiguous part of a name and return it.
     *
     * @param name with an ambiguos part (possibly).
     * @return ambiguous part or null.
     */

    private AmbiguousName ambiguousPart(TypeName name) {
        String qualifiedName = name.toString();
        int lastDotIndex = qualifiedName.lastIndexOf('.');
        return lastDotIndex == -1 ? null // It was a simple
                // name
                : new AmbiguousName(name.line(), qualifiedName.substring(0,
                lastDotIndex));
    }

    /**
     * Report a syntax error.
     *
     * @param message message identifying the error.
     * @param args    related values.
     */

    private void reportParserError(String message, Object... args) {
        isInError = true;
        isRecovered = false;
        System.err
                .printf("%s:%d: ", scanner.fileName(), scanner.token().line());
        System.err.printf(message, args);
        System.err.println();
    }

    // ////////////////////////////////////////////////
    // Lookahead /////////////////////////////////////
    // ////////////////////////////////////////////////

    /**
     * Are we looking at an IDENTIFIER followed by a LPAREN? Look ahead to find
     * out.
     *
     * @return true iff we're looking at IDENTIFIER LPAREN; false otherwise.
     */

    private boolean seeIdentLParen() {
        scanner.recordPosition();
        boolean result = have(IDENTIFIER) && see(LPAREN);
        scanner.returnToPosition();
        return result;
    }

    /**
     * Are we looking at a cast? ie.
     * <p>
     * <pre>
     *   LPAREN type RPAREN ...
     * </pre>
     * <p>
     * Look ahead to find out.
     *
     * @return true iff we're looking at a cast; false otherwise.
     */

    private boolean seeCast() {
        scanner.recordPosition();
        if (!have(LPAREN)) {
            scanner.returnToPosition();
            return false;
        }
        if (seeBasicType()) {
            scanner.returnToPosition();
            return true;
        }
        if (!see(IDENTIFIER)) {
            scanner.returnToPosition();
            return false;
        } else {
            scanner.next(); // Scan the IDENTIFIER
            // A qualified identifier is ok
            while (have(DOT)) {
                if (!have(IDENTIFIER)) {
                    scanner.returnToPosition();
                    return false;
                }
            }
        }
        while (have(LBRACK)) {
            if (!have(RBRACK)) {
                scanner.returnToPosition();
                return false;
            }
        }
        if (!have(RPAREN)) {
            scanner.returnToPosition();
            return false;
        }
        scanner.returnToPosition();
        return true;
    }

    /**
     * Are we looking at a local variable declaration? ie.
     * <p>
     * <pre>
     *   type IDENTIFIER {LBRACK RBRACK} ...
     * </pre>
     * <p>
     * Look ahead to determine.
     *
     * @return true iff we are looking at local variable declaration; false
     * otherwise.
     */

    private boolean seeLocalVariableDeclaration() {
        scanner.recordPosition();
        if (have(IDENTIFIER)) {
            // A qualified identifier is ok
            while (have(DOT)) {
                if (!have(IDENTIFIER)) {
                    scanner.returnToPosition();
                    return false;
                }
            }
        } else if (seeBasicType()) {
            scanner.next();
        } else {
            scanner.returnToPosition();
            return false;
        }
        while (have(LBRACK)) {
            if (!have(RBRACK)) {
                scanner.returnToPosition();
                return false;
            }
        }
        if (!have(IDENTIFIER)) {
            scanner.returnToPosition();
            return false;
        }
        while (have(LBRACK)) {
            if (!have(RBRACK)) {
                scanner.returnToPosition();
                return false;
            }
        }
        scanner.returnToPosition();
        return true;
    }

    /**
     * Are we looking at a basic type? ie.
     * <p>
     * <pre>
     * BOOLEAN | CHAR | INT
     * </pre>
     *
     * @return true iff we're looking at a basic type; false otherwise.
     */
    //Modified Exercise 3.21 3.22
    private boolean seeBasicType() {
        return see(BOOLEAN) || see(CHAR) || see(INT) || see(DOUBLE)
                || see(FLOAT) || see(LONG);
    }

    /**
     * Are we looking at a reference type? ie.
     * <p>
     * <pre>
     *   referenceType ::= basicType LBRACK RBRACK {LBRACK RBRACK}
     *                   | qualifiedIdentifier {LBRACK RBRACK}
     * </pre>
     *
     * @return true iff we're looking at a reference type; false otherwise.
     */

    private boolean seeReferenceType() {
        if (see(IDENTIFIER)) {
            return true;
        } else {
            scanner.recordPosition();
            if (have(BOOLEAN) || have(CHAR) || have(INT) || have(DOUBLE)
                    || have(FLOAT) || have(LONG)) {
                if (have(LBRACK) && see(RBRACK)) {
                    scanner.returnToPosition();
                    return true;
                }
            }
            scanner.returnToPosition();
        }
        return false;
    }

    /**
     * Are we looking at []?
     *
     * @return true iff we're looking at a [] pair; false otherwise.
     */

    private boolean seeDims() {
        scanner.recordPosition();
        boolean result = have(LBRACK) && see(RBRACK);
        scanner.returnToPosition();
        return result;
    }

    // ////////////////////////////////////////////////
    // Parser Proper /////////////////////////////////
    // ////////////////////////////////////////////////

    /**
     * Parse a compilation unit (a program file) and construct an AST for it.
     * After constructing the Parser, this is its entry point.
     * <p>
     * <pre>
     *   compilationUnit ::= [PACKAGE qualifiedIdentifier SEMI]
     *                       {IMPORT  qualifiedIdentifier SEMI}
     *                       {typeDeclaration}
     *                       EOF
     * </pre>
     *
     * @return an AST for a compilationUnit.
     */

    public JCompilationUnit compilationUnit() {
        int line = scanner.token().line();
        TypeName packageName = null; // Default
        if (have(PACKAGE)) {
            packageName = qualifiedIdentifier();
            mustBe(SEMI);
        }
        ArrayList<TypeName> imports = new ArrayList<>();
        while (have(IMPORT)) {
            imports.add(qualifiedIdentifier());
            mustBe(SEMI);
        }
        ArrayList<JAST> typeDeclarations = new ArrayList<>();
        while (!see(EOF)) {
            JAST typeDeclaration = typeDeclaration();
            if (typeDeclaration != null) {
                typeDeclarations.add(typeDeclaration);
            }
        }
        mustBe(EOF);
        return new JCompilationUnit(scanner.fileName(), line, packageName,
                imports, typeDeclarations);
    }

    /**
     * Parse a qualified identifier.
     * <p>
     * <pre>
     *   qualifiedIdentifier ::= IDENTIFIER {DOT IDENTIFIER}
     * </pre>
     *
     * @return an instance of TypeName.
     */

    private TypeName qualifiedIdentifier() {
        int line = scanner.token().line();
        mustBe(IDENTIFIER);
        StringBuilder qualifiedIdentifier = new StringBuilder(scanner.previousToken().image());
        while (have(DOT)) {
            mustBe(IDENTIFIER);
            qualifiedIdentifier.append(".").append(scanner.previousToken().image());
        }
        return new TypeName(line, qualifiedIdentifier.toString());
    }

    /**
     * Parse a type declaration.
     * <p>
     * <pre>
     *   typeDeclaration ::= modifiers classDeclaration
     * </pre>
     *
     * @return an AST for a typeDeclaration.
     */

    private JAST typeDeclaration() {
        ArrayList<String> mods = modifiers();
        return classDeclaration(mods);
    }

    /**
     * Parse modifiers.
     * <p>
     * <pre>
     *   modifiers ::= {PUBLIC | PROTECTED | PRIVATE | STATIC |
     *                  ABSTRACT}
     * </pre>
     * <p>
     * Check for duplicates, and conflicts among access modifiers (public,
     * protected, and private). Otherwise, no checks.
     *
     * @return a list of modifiers.
     */

    private ArrayList<String> modifiers() {
        ArrayList<String> mods = new ArrayList<>();
        boolean scannedPUBLIC = false;
        boolean scannedPROTECTED = false;
        boolean scannedPRIVATE = false;
        boolean scannedSTATIC = false;
        boolean scannedABSTRACT = false;
        boolean scannedTHROWS = false;
        boolean more = true;
        while (more)
            if (have(PUBLIC)) {
                mods.add("public");
                if (scannedPUBLIC) {
                    reportParserError("Repeated modifier: public");
                }
                if (scannedPROTECTED || scannedPRIVATE) {
                    reportParserError("Access conflict in modifiers");
                }
                scannedPUBLIC = true;
            } else if (have(PROTECTED)) {
                mods.add("protected");
                if (scannedPROTECTED) {
                    reportParserError("Repeated modifier: protected");
                }
                if (scannedPUBLIC || scannedPRIVATE) {
                    reportParserError("Access conflict in modifiers");
                }
                scannedPROTECTED = true;
            } else if (have(PRIVATE)) {
                mods.add("private");
                if (scannedPRIVATE) {
                    reportParserError("Repeated modifier: private");
                }
                if (scannedPUBLIC || scannedPROTECTED) {
                    reportParserError("Access conflict in modifiers");
                }
                scannedPRIVATE = true;
            } else if (have(STATIC)) {
                mods.add("static");
                if (scannedSTATIC) {
                    reportParserError("Repeated modifier: static");
                }
                scannedSTATIC = true;
            } else if (have(ABSTRACT)) {
                mods.add("abstract");
                if (scannedABSTRACT) {
                    reportParserError("Repeated modifier: abstract");
                }
                scannedABSTRACT = true;
            } else if (have(THROWS)) {
                mods.add("throws");
                if (scannedTHROWS) {
                    reportParserError("Repeated modifier: throws");
                }
            } else {
                more = false;
            }
        return mods;
    }

    /**
     * Parse a class declaration.
     * <p>
     * <pre>
     *   classDeclaration ::= CLASS IDENTIFIER
     *                        [EXTENDS qualifiedIdentifier]
     *                        classBody
     * </pre>
     * <p>
     * A class which doesn't explicitly extend another (super) class implicitly
     * extends the superclass java.lang.Object.
     *
     * @param mods the class modifiers.
     * @return an AST for a classDeclaration.
     */

    private JClassDeclaration classDeclaration(ArrayList<String> mods) {
        int line = scanner.token().line();
        mustBe(CLASS);
        mustBe(IDENTIFIER);
        String name = scanner.previousToken().image();
        Type superClass;
        if (have(EXTENDS)) {
            superClass = qualifiedIdentifier();
        } else {
            superClass = Type.OBJECT;
        }
        return new JClassDeclaration(line, mods, name, superClass, classBody());
    }

    /**
     * Parse a class body.
     * <p>
     * <pre>
     *   classBody ::= LCURLY
     *                   {modifiers memberDecl}
     *                 RCURLY
     * </pre>
     *
     * @return list of members in the class body.
     */

    private ArrayList<JMember> classBody() {
        ArrayList<JMember> members = new ArrayList<JMember>();
        mustBe(LCURLY);
        while (!see(RCURLY) && !see(EOF)) {
            members.add(memberDecl(modifiers()));
        }
        mustBe(RCURLY);
        return members;
    }

    /**
     * Parse a member declaration.
     * <p>
     * <pre>
     *   memberDecl ::= IDENTIFIER            // constructor
     *                    formalParameters [THROWS IDENTIFIER {COMMA IDENTIFIER}]
     *                    block
     *                | (VOID | type) IDENTIFIER  // method
     *                    formalParameters [THROWS IDENTIFIER {COMMA IDENTIFIER}]
     *                    (block | SEMI)
     *                | type variableDeclarators SEMI
     * </pre>
     *
     * @param mods the class member modifiers.
     * @return an AST for a memberDecl.
     */

    private JMember memberDecl(ArrayList<String> mods) {
        int line = scanner.token().line();
        JMember memberDecl = null;
        if (seeIdentLParen()) {
            // A constructor
            mustBe(IDENTIFIER);
            String name = scanner.previousToken().image();
            ArrayList<JFormalParameter> params = formalParameters();
                        
            ArrayList<String> exceptions = new ArrayList<String>();
            if (have(THROWS)) {
            	do {
            		mustBe(IDENTIFIER);
            		exceptions.add(scanner.previousToken().image());
            	} while (have(COMMA));
            }
            
            JBlock body = block();
            memberDecl = new JConstructorDeclaration(line, mods, name, params,
                    body, exceptions);
        } else {
            Type type = null;
            if (have(VOID)) {
                // void method
                type = Type.VOID;
                mustBe(IDENTIFIER);
                String name = scanner.previousToken().image();
                ArrayList<JFormalParameter> params = formalParameters();
                
                ArrayList<String> exceptions = new ArrayList<String>();
                if (have(THROWS)) {
                	do {
                		mustBe(IDENTIFIER);
                		exceptions.add(scanner.previousToken().image());
                	} while (have(COMMA));
                }
                JBlock body = have(SEMI) ? null : block();
                memberDecl = new JMethodDeclaration(line, mods, name, type,
                        params, body, exceptions);
            } else {
                type = type();
                if (seeIdentLParen()) {
                    // Non void method
                    mustBe(IDENTIFIER);
                    String name = scanner.previousToken().image();
                    ArrayList<JFormalParameter> params = formalParameters();
                    
                    ArrayList<String> exceptions = new ArrayList<String>();
                    if (have(THROWS)) {
                    	do {
                    		mustBe(IDENTIFIER);
                    		exceptions.add(scanner.previousToken().image());
                    	} while (have(COMMA));
                    }
                    JBlock body = have(SEMI) ? null : block();
                    memberDecl = new JMethodDeclaration(line, mods, name, type,
                            params, body, exceptions);
                } else {
                    // Field
                    memberDecl = new JFieldDeclaration(line, mods,
                            variableDeclarators(type));
                    mustBe(SEMI);
                }
            }
        }
        return memberDecl;
    }

    /**
     * Parse a block.
     * <p>
     * <pre>
     *   block ::= LCURLY {blockStatement} RCURLY
     * </pre>
     *
     * @return an AST for a block.
     */

    private JBlock block() {
        int line = scanner.token().line();
        boolean containsThrow = false;
        String throwName = null;
        ArrayList<JStatement> statements = new ArrayList<JStatement>();
        mustBe(LCURLY);
        while (!see(RCURLY) && !see(EOF)) {
            statements.add(blockStatement());
        }
        mustBe(RCURLY);
        return new JBlock(line, statements);
    }

    /**
     * Parse a block statement.
     * <p>
     * <pre>
     *   blockStatement ::= localVariableDeclarationStatement
     *                    | statement
     * </pre>
     *
     * @return an AST for a blockStatement.
     */

    private JStatement blockStatement() {
        if (seeLocalVariableDeclaration()) {
            return localVariableDeclarationStatement();
        } else {
            return statement();
        }
    }

    /**
     * Parse a statement.
     * <p>
     * <pre>
     *   statement ::= block
     *               | IF parExpression statement [ELSE statement]
     *               | WHILE parExpression statement
     *               | RETURN [expression] SEMI
     *               | SEMI 
     *               | FOR forParExpression statement
     *               | THROW primary [NEW] expression
     *               | TRY statement (CATCH LPAREN formalParameter RPAREN)+
     *               	[FINALLY statement]
     *               | SWITCH parExpression LCURLY {CASE TERNARY_ASK {statement}}
     *               	[DEFAULT TERNARY_ASK {statement}] {CASE TERNARY_ASK {statement}}
     *               | statementExpression SEMI
     * </pre>
     *
     * @return an AST for a statement.
     */

    private JStatement statement() {
        int line = scanner.token().line();
        if (see(LCURLY)) {
            return block();
        } else if (have(IF)) {
            JExpression test = parExpression();
            JStatement consequent = statement();
            JStatement alternate = have(ELSE) ? statement() : null;
            return new JIfStatement(line, test, consequent, alternate);
        } else if (have(WHILE)) {
            JExpression test = parExpression();
            JStatement statement = statement();
            return new JWhileStatement(line, test, statement);
        } else if (have(DO)) {
        	JStatement statement = statement();
        	if (have(WHILE)) {
        		JExpression test = parExpression();
        		mustBe(SEMI);
        		return new JDoWhileStatement(line, test, statement);
        	} else if (have(UNTIL)) {
            	JExpression test = parExpression();
            	mustBe(SEMI);
            	return new JDoUntilStatement(line, test, statement);
        	}
        } else if (have(FOR)) {
            Type type = null;
            mustBe(LPAREN);
            type = type();
            JVariableDeclarator initializer = variableDeclarator(type); 
            if (have(COLON)) { // enhanced-for
                type = type();
                String name = scanner.previousToken().image();
                mustBe(RPAREN);
                JStatement stmnt = statement();
                JVariableDeclarator jvd = new JVariableDeclarator(line, name, type, null);
                return new JForEachStatement(line, initializer, jvd, stmnt);
            } else { // basic for-statement
                mustBe(SEMI);
                JExpression test = expression();
                mustBe(SEMI);
                JStatement increment = statementExpression();
                mustBe(RPAREN);
                JStatement statement = statement();
                return new JForStatement(line, initializer, test, increment, statement);
            }
          } else if (have(RETURN)) {
            if (have(SEMI)) {
                return new JReturnStatement(line, null);
            } else {
                JExpression expr = expression();
                mustBe(SEMI);
                return new JReturnStatement(line, expr);
            }
        } else if (have(BREAK)) {
        	mustBe(SEMI);
        	return new JBreakStatement(line);
        } else if (have(SEMI)) {
            return new JEmptyStatement(line);
        } else if (have(THROW)) {
        	JExpression thrownExpression = primary();
        	if (see(NEW)) {
        		return new JThrowStatement(line, thrownExpression, true);
        	} else {
        		return new JThrowStatement(line, thrownExpression, false);
        	}
        } else if (have(TRY)) {
        	JStatement tryStatement = statement();
        	HashMap<JFormalParameter, JStatement> catches = new HashMap<JFormalParameter, JStatement>();
        	mustBe(CATCH);
        	do {
        		mustBe(LPAREN);
        		JFormalParameter identifier = formalParameter();
        		mustBe(RPAREN);
        		JStatement block = statement();
        		catches.put(identifier, block);
        	} while (have(CATCH));
        	JStatement finallyStatement = null;
        	if (have(FINALLY)) {
        		finallyStatement = statement();
        	}
        	return new JTryStatement(line, tryStatement, catches, finallyStatement);
        } else if (have(SWITCH)) {
        	JExpression clause = parExpression();        	
        	JSwitchStatement switchStatement = new JSwitchStatement(line, clause);
        	
        	mustBe(LCURLY);
        	List<JExpression> caseList = new ArrayList<JExpression>();
        	
        	while (see(CASE) || see(DEFAULT)) {
        		//New case found. Check if it is default and add it to the case list.
        		if (have(DEFAULT)) {
        			caseList.add(null);
        		} else if (have(CASE)) {
        			caseList.add(expression());
        		}
        		mustBe(COLON);
        		
        		if (!see(CASE) && !see(DEFAULT)) {
        			//Case series has ended. Add statements and restart the case list.
        			List<JStatement> statementList = new ArrayList<JStatement>();
        			while(!see(CASE) && !see(DEFAULT) && !see(RCURLY)) {
        				statementList.add(statement());
        			}
        			
        			boolean success = switchStatement.addSwitchPair(caseList, statementList);
        			if (!success) {
        				reportParserError("Switch Syntax Error: No duplicate cases are allowed.");
        			}
        			caseList = new ArrayList<JExpression>();
        		}
        	}
        	
        	mustBe(RCURLY);
        	return switchStatement;
        } else { // Must be a statementExpression
            JStatement statement = statementExpression();
            mustBe(SEMI);
            return statement;
        }
    }

    /**
     * Parse formal parameters.
     * <p>
     * <pre>
     *   formalParameters ::= LPAREN
     *                          [formalParameter
     *                            {COMMA  formalParameter}]
     *                        RPAREN
     * </pre>
     *
     * @return a list of formal parameters.
     */

    private ArrayList<JFormalParameter> formalParameters() {
        ArrayList<JFormalParameter> parameters = new ArrayList<JFormalParameter>();
        boolean varargFound = false;
        mustBe(LPAREN);
        if (have(RPAREN))
            return parameters;
        do {
        	if (varargFound) {
        		reportParserError("Vararg parameters must be the last parameter "
        				+ "of a member declaration.");
        	}
        	JFormalParameter newParam = formalParameter();
        	
            parameters.add(newParam);            
        } while (have(COMMA));
        mustBe(RPAREN);
        return parameters;
    }

    /**
     * Parse a formal parameter.
     * <p>
     * <pre>
     *   formalParameter ::= type IDENTIFIER
     * </pre>
     *
     * @return an AST for a formalParameter.
     */

    private JFormalParameter formalParameter() {
        int line = scanner.token().line();
        Type type = type();
        boolean isVararg = false;
        if (have(ELLIPSIS)) {
        	isVararg = true;
        }
        mustBe(IDENTIFIER);
        String name = scanner.previousToken().image();
        return new JFormalParameter(line, name, type, isVararg);
    }

    /**
     * Parse a parenthesized expression.
     * <p>
     * <pre>
     *   parExpression ::= LPAREN expression RPAREN
     * </pre>
     *
     * @return an AST for a parExpression.
     */

    private JExpression parExpression() {
        mustBe(LPAREN);
        JExpression expr = expression();
        mustBe(RPAREN);
        return expr;
    }


    /**
     * Parse a local variable declaration statement.
     * <p>
     * <pre>
     *   localVariableDeclarationStatement ::= type
     *                                           variableDeclarators
     *                                             SEMI
     * </pre>
     *
     * @return an AST for a variableDeclaration.
     */

    private JVariableDeclaration localVariableDeclarationStatement() {
        int line = scanner.token().line();
        ArrayList<String> mods = new ArrayList<String>();
        ArrayList<JVariableDeclarator> vdecls = variableDeclarators(type());
        mustBe(SEMI);
        return new JVariableDeclaration(line, mods, vdecls);
    }

    /**
     * Parse variable declarators.
     * <p>
     * <pre>
     *   variableDeclarators ::= variableDeclarator
     *                             {COMMA variableDeclarator}
     * </pre>
     *
     * @param type type of the variables.
     * @return a list of variable declarators.
     */

    private ArrayList<JVariableDeclarator> variableDeclarators(Type type) {
        ArrayList<JVariableDeclarator> variableDeclarators = new ArrayList<JVariableDeclarator>();
        do {
            variableDeclarators.add(variableDeclarator(type));
        } while (have(COMMA));
        return variableDeclarators;
    }

    /**
     * Parse a variable declarator.
     * <p>
     * <pre>
     *   variableDeclarator ::= IDENTIFIER
     *                          [ASSIGN variableInitializer]
     * </pre>
     *
     * @param type type of the variable.
     * @return an AST for a variableDeclarator.
     */

    private JVariableDeclarator variableDeclarator(Type type) {
        int line = scanner.token().line();
        mustBe(IDENTIFIER);
        String name = scanner.previousToken().image();
        JExpression initial = have(ASSIGN) ? variableInitializer(type) : null;
        return new JVariableDeclarator(line, name, type, initial);
    }

    /**
     * Parse a variable initializer.
     * <p>
     * <pre>
     *   variableInitializer ::= arrayInitializer
     *                         | expression
     * </pre>
     *
     * @param type type of the variable.
     * @return an AST for a variableInitializer.
     */

    private JExpression variableInitializer(Type type) {
        if (see(LCURLY)) {
            return arrayInitializer(type);
        }
        return expression();
    }

    /**
     * Parse an array initializer.
     * <p>
     * <pre>
     *   arrayInitializer ::= LCURLY
     *                          [variableInitializer
     *                            {COMMA variableInitializer} [COMMA]]
     *                        RCURLY
     * </pre>
     *
     * @param type type of the array.
     * @return an AST for an arrayInitializer.
     */

    private JArrayInitializer arrayInitializer(Type type) {
        int line = scanner.token().line();
        ArrayList<JExpression> initials = new ArrayList<JExpression>();
        mustBe(LCURLY);
        if (have(RCURLY)) {
            return new JArrayInitializer(line, type, initials);
        }
        initials.add(variableInitializer(type.componentType()));
        while (have(COMMA)) {
            initials.add(see(RCURLY) ? null : variableInitializer(type
                    .componentType()));
        }
        mustBe(RCURLY);
        return new JArrayInitializer(line, type, initials);
    }

    /**
     * Parse arguments.
     * <p>
     * <pre>
     *   arguments ::= LPAREN [expression {COMMA expression}] RPAREN
     * </pre>
     *
     * @return a list of expressions.
     */

    private ArrayList<JExpression> arguments() {
        ArrayList<JExpression> args = new ArrayList<JExpression>();
        mustBe(LPAREN);
        if (have(RPAREN)) {
            return args;
        }
        do {
            args.add(expression());
        } while (have(COMMA));
        mustBe(RPAREN);
        return args;
    }

    /**
     * Parse a type.
     * <p>
     * <pre>
     *   type ::= referenceType
     *          | basicType
     * </pre>
     *
     * @return an instance of Type.
     */

    private Type type() {
        if (seeReferenceType()) {
            return referenceType();
        }
        return basicType();
    }

    /**
     * Parse a basic type.
     * <p>
     * <pre>
     *   basicType ::= BOOLEAN | CHAR | INT | FLOAT | LONG | DOUBLE
     * </pre>
     *
     * @return an instance of Type.
     */

    private Type basicType() {
        if (have(BOOLEAN)) {
            return Type.BOOLEAN;
        } else if (have(CHAR)) {
            return Type.CHAR;
        } else if (have(INT)) {
            return Type.INT;
        } else if (have(DOUBLE)) {
            return Type.DOUBLE;
        } else if (have(FLOAT)) {
            return Type.FLOAT;
        } else if (have(LONG)) {
            return Type.LONG;
        } else {
            reportParserError("Type sought where %s found", scanner.token()
                    .image());
            return Type.ANY;
        }
    }

    /**
     * Parse a reference type.
     * <p>
     * <pre>
     *   referenceType ::= basicType LBRACK RBRACK {LBRACK RBRACK}
     *                   | qualifiedIdentifier {LBRACK RBRACK}
     * </pre>
     *
     * @return an instance of Type.
     */

    private Type referenceType() {
        Type type = null;
        if (!see(IDENTIFIER)) {
            type = basicType();
            mustBe(LBRACK);
            mustBe(RBRACK);
            type = new ArrayTypeName(type);
        } else {
            type = qualifiedIdentifier();
        }
        while (seeDims()) {
            mustBe(LBRACK);
            mustBe(RBRACK);
            type = new ArrayTypeName(type);
        }
        return type;
    }

    /**
     * Parse a statement expression.
     * <p>
     * <pre>
     *   statementExpression ::= expression // but must have
     *                                      // side-effect, eg i++
     * </pre>
     *
     * @return an AST for a statementExpression.
     */

    private JStatement statementExpression() {
        int line = scanner.token().line();
        JExpression expr = expression();
        if (expr instanceof JAssignment || expr instanceof JPreIncrementOp
                || expr instanceof JPostDecrementOp
                || expr instanceof JPreDecrementOp
                || expr instanceof JPostIncrementOp
                || expr instanceof JMessageExpression
                || expr instanceof JSuperConstruction
                || expr instanceof JTernaryExpression
                || expr instanceof JThisConstruction || expr instanceof JNewOp
                || expr instanceof JNewArrayOp) {
            // So as not to save on stack
            expr.isStatementExpression = true;
        } else {
            reportParserError("Invalid statement expression; "
                    + "it does not have a side-effect");
        }
        return new JStatementExpression(line, expr);
    }

    /**
     * An expression.
     * <p>
     * <pre>
     *   expression ::= assignmentExpression
     * </pre>
     *
     * @return an AST for an expression.
     */

    private JExpression expression() {
        return assignmentExpression();
    }

    /**
     * Parse an assignment expression.
     * <p>
     * <pre>
     *   assignmentExpression ::=
     *       conditionalAndExpression // level 13
     *           [( ASSIGN  // conditionalExpression
     *            | PLUS_ASSIGN // must be valid lhs
     *            )
     *            assignmentExpression]
     * </pre>
     *
     * @return an AST for an assignmentExpression.
     */

    private JExpression assignmentExpression() {
        int line = scanner.token().line();
        JExpression lhs = ternaryExpression();
        if (have(ASSIGN)) {
            return new JAssignOp(line, lhs, assignmentExpression());
        } else if (have(PLUS_ASSIGN)) {
            return new JPlusAssignOp(line, lhs, assignmentExpression());
        } else if (have(MINUS_ASSIGN)) {
        	return new JMinusAssignOp(line, lhs, assignmentExpression());
        }else if (have(STAR_ASSIGN)) {
        	return new JMultiplyAssignOp(line, lhs, assignmentExpression());
        } else if (have(DIV_ASSIGN)) {
        	return new JDivideAssignOp(line, lhs, assignmentExpression());
        } else if (have(MOD_ASSIGN)) {
        	return new JModuloAssignOp(line, lhs, assignmentExpression());
        } else if (have(LSHIFT_ASSIGN)) {
        	return new JLShiftAssignOp(line, lhs, assignmentExpression());
        } else if (have(RSHIFT_ASSIGN)) {
        	return new JRShiftAssignOp(line, lhs, assignmentExpression());
        } else if (have(ZSHIFT_ASSIGN)) {
        	return new JUSRShiftAssignOp(line, lhs, assignmentExpression());
        } else if (have(AND_ASSIGN)) {
        	return new JBitAndAssignOp(line, lhs, assignmentExpression());
        } else if (have(OR_ASSIGN)) {
        	return new JBitOrAssignOp(line, lhs, assignmentExpression());
        } else if (have(XOR_ASSIGN)) {
        	return new JBitXorAssignOp(line, lhs, assignmentExpression());
        } else {
            return lhs;
        }
    }
    
    /**
     * Parse a ternary expression.
     * 
     * <pre>
     *   ternaryExpression ::= conditionalOrExpression // level 12
     *                                  [TERNARY_ASK conditionalOrExpression 
     *                                      TERNARY_OTHER conditionalOrExpression]
     * </pre>
     * 
     * @return an AST for a ternaryExpression.
     */
    
	private JExpression ternaryExpression() {
		int line = scanner.token().line();
		JExpression lhs = conditionalOrExpression();
		if (have(TERNARY)) {
        	JExpression whenTrue = assignmentExpression();
        	mustBe(COLON);
        	return new JTernaryExpression(line, lhs, whenTrue, ternaryExpression());
        } else {
        	return lhs;
        }
	}
    
    /**
     * Parse a conditional-or expression.
     * 
     * <pre>
     *   conditionalOrExpression ::= conditionalOrExpression // level 11
     *                                  {LOR conditionalOrExpression}
     * </pre>
     * 
     * @return an AST for a conditionalOrExpression.
     */

    private JExpression conditionalOrExpression() {
        int line = scanner.token().line();
        boolean more = true;
        JExpression lhs = conditionalAndExpression();
        while (more) {
            if (have(LOR)) {
                lhs = new JLogicalOrOp(line, lhs, conditionalAndExpression());
            } else {
                more = false;
            }
        }
        return lhs;
    }

    /**
     * Parse a conditional-and expression.
     * <p>
     * <pre>
     *   conditionalAndExpression ::= equalityExpression // level 10
     *                                  {LAND equalityExpression}
     * </pre>
     *
     * @return an AST for a conditionalExpression.
     */

    private JExpression conditionalAndExpression() {
        int line = scanner.token().line();
        boolean more = true;
        JExpression lhs = bitwiseIOrExpression();
        while (more) {
            if (have(LAND)) {
                lhs = new JLogicalAndOp(line, lhs, equalityExpression());
            } else {
                more = false;
            }
        }
        return lhs;
    }
    /**
     * Parse a bitwise-or expression.
     * 
     * <pre>
     *   bitwiseOrExpression ::= bitwiseXorExpression // level 9
     *                                  {BW_OR bitwiseXorExpression}
     * </pre>
     * 
     * @return an AST for a bitwiseOrExpression.
     */

    private JExpression bitwiseIOrExpression() {
        int line = scanner.token().line();
        boolean more = true;
        JExpression lhs = bitwiseXorExpression();
        while (more) {
            if (have(BIOR)) {
                lhs = new JBitOrOp(line, lhs, bitwiseXorExpression());
            } else {
                more = false;
            }
        }
        return lhs;
    }
    
    /**
     * Parse a bitwise-xor expression.
     * 
     * <pre>
     *   bitwiseXorExpression ::= bitwiseAndExpression // level 8
     *                                  {BW_XOR bitwiseAndExpression}
     * </pre>
     * 
     * @return an AST for a bitwiseXorExpression.
     */

    private JExpression bitwiseXorExpression() {
        int line = scanner.token().line();
        boolean more = true;
        JExpression lhs = bitwiseAndExpression();
        while (more) {
            if (have(BEOR)) {
                lhs = new JBitXorOp(line, lhs, bitwiseAndExpression());
            } else {
                more = false;
            }
        }
        return lhs;
    }
    /**
     * Parse a bitwise-and expression.
     * 
     * <pre>
     *   bitwiseAndExpression ::= equalityExpression // level 7
     *                                  {BW_AND equalityExpression}
     * </pre>
     * 
     * @return an AST for a bitwiseAndExpression.
     */    

    private JExpression bitwiseAndExpression() {
        int line = scanner.token().line();
        boolean more = true;
        JExpression lhs = equalityExpression();
        while (more) {
            if (have(BITAND)) {
                lhs = new JBitAndOp(line, lhs, equalityExpression());
            } else {
                more = false;
            }
        }
        return lhs;
    }

    /**
     * Parse an equality expression.
     * <p>
     * <pre>
     *   equalityExpression ::= relationalExpression  // level 6
     *                            {EQUAL relationalExpression}
     * </pre>
     *
     * @return an AST for an equalityExpression.
     */

    private JExpression equalityExpression() {
        int line = scanner.token().line();
        boolean more = true;
        JExpression lhs = relationalExpression();
        while (more) {
            if (have(EQUAL)) {
                lhs = new JEqualOp(line, lhs, relationalExpression());
            } else if (have(NEQUAL)) {
            	lhs = new JNotEqualsOp(line, lhs, relationalExpression());
            } else {
                more = false;
            }
        }
        return lhs;
    }

    /**
     * Parse a relational expression.
     * <p>
     * <pre>
     *   relationalExpression ::= additiveExpression  // level 5
     *                              [(GT | LE) additiveExpression
     *                              | INSTANCEOF referenceType]
     *                              | (GE | LT) // added
     * </pre>
     *
     * @return an AST for a relationalExpression.
     */

    private JExpression relationalExpression() {
        int line = scanner.token().line();
        JExpression lhs = shiftExpression();
        if (have(GT)) {
            return new JGreaterThanOp(line, lhs, shiftExpression());
        } else if (have(LE)) {
            return new JLessEqualOp(line, lhs, shiftExpression());
        } else if (have(GE)) {
        	return new JGreaterEqualOp(line, lhs, shiftExpression());
        } else if (have(LT)) {
        	return new JLessThanOp(line, lhs, shiftExpression());
        } else if (have(INSTANCEOF)) {
            return new JInstanceOfOp(line, lhs, referenceType());
        } else {
            return lhs;
        }
    }
    
    /**
     * Parse an shift expression.
     * 
     * <pre>
     *   shiftExpression ::= additiveExpression  // level 4
     *                            {(L_SHIFT|R_SHIFT|ZFR_SHIFT) additiveExpression}
     * </pre>
     * 
     * @return an AST for an equalityExpression.
     */

    private JExpression shiftExpression() {
    	int line = scanner.token().line();
        boolean more = true;
        JExpression lhs = additiveExpression();
        while (more) {
            if (have(LSHIFT)) {
                lhs = new JLeftShiftOp(line, lhs, additiveExpression());
            } else if (have(RSHIFT)) {
                lhs = new JRightShiftOp(line, lhs, additiveExpression());
            } else if (have(ZSHIFT)) {
            	lhs = new JUnsignedRightShiftOp(line, lhs, additiveExpression());
            } else {
                more = false;
            }
        }
        return lhs;
    }

    /**
     * Parse an additive expression.
     * <p>
     * <pre>
     *   additiveExpression ::= multiplicativeExpression // level 3
     *                            {MINUS|PLUS multiplicativeExpression}
     * </pre>
     *
     * @return an AST for an additiveExpression.
     */

    private JExpression additiveExpression() {
        int line = scanner.token().line();
        boolean more = true;
        JExpression lhs = multiplicativeExpression();
        while (more) {
            if (have(MINUS)) {
                lhs = new JSubtractOp(line, lhs, multiplicativeExpression());
            } else if (have(PLUS)) {
                lhs = new JPlusOp(line, lhs, multiplicativeExpression());
            } else {
                more = false;
            }
        }
        return lhs;
    }

    /**
     * Parse a multiplicative expression.
     * <p>
     * <pre>
     *   multiplicativeExpression ::= unaryExpression  // level 2
     *                                  {STAR|DIVIDE|MOD unaryExpression}
     * </pre>
     *
     * @return an AST for a multiplicativeExpression.
     */

    private JExpression multiplicativeExpression() {
        int line = scanner.token().line();
        boolean more = true;
        JExpression lhs = unaryExpression();
        while (more) {
            if (have(STAR)) {
                lhs = new JMultiplyOp(line, lhs, unaryExpression());
            } else if (have(DIVIDE)) {
                lhs = new JDivideOp(line, lhs, unaryExpression());
            } else if (have(MODULO)) {
                lhs = new JRemOp(line, lhs, unaryExpression());
            } else {
                more = false;
            }
        }
        return lhs;
    }

    /**
     * Parse an unary expression.
     * <p>
     * <pre>
     *   unaryExpression ::= INC unaryExpression // level 1
     *                     | MINUS unaryExpression
     *                     | simpleUnaryExpression
     *                     | DEC unaryExpression
     *                     | PLUS unaryExpression
     * </pre>
     *
     * @return an AST for an unaryExpression.
     */

    private JExpression unaryExpression() {
        int line = scanner.token().line();
        if (have(INC)) {
            return new JPreIncrementOp(line, unaryExpression());
        } else if (have(DEC)) {
        	return new JPreDecrementOp(line, unaryExpression());
        } else if (have(PLUS)) {
        	return new JPositiveOp(line, unaryExpression());
        } else if (have(MINUS)) {
            return new JNegateOp(line, unaryExpression());
        } else {
            return simpleUnaryExpression();
        }
    }

    /**
     * Parse a simple unary expression.
     * <p>
     * <pre>
     *   simpleUnaryExpression ::= LNOT unaryExpression
     *                           | LPAREN basicType RPAREN
     *                               unaryExpression
     *                           | LPAREN
     *                               referenceType
     *                             RPAREN simpleUnaryExpression
     *                           | postfixExpression
     * </pre>
     *
     * @return an AST for a simpleUnaryExpression.
     */

    private JExpression simpleUnaryExpression() {
        int line = scanner.token().line();
        if (have(LNOT)) {
            return new JLogicalNotOp(line, unaryExpression());
        } else if (have(UBC)) {
        	return new JComplementOp(line, unaryExpression());
        } else if (seeCast()) {
            mustBe(LPAREN);
            boolean isBasicType = seeBasicType();
            Type type = type();
            mustBe(RPAREN);
            JExpression expr = isBasicType ? unaryExpression()
                    : simpleUnaryExpression();
            return new JCastOp(line, type, expr);
        } else {
            return postfixExpression();
        }
    }

    /**
     * Parse a postfix expression.
     * <p>
     * <pre>
     *   postfixExpression ::= primary {selector} {DEC}
     * </pre>
     *
     * @return an AST for a postfixExpression.
     */

    private JExpression postfixExpression() {
        int line = scanner.token().line();
        JExpression primaryExpr = primary();
        while (see(DOT) || see(LBRACK)) {
            primaryExpr = selector(primaryExpr);
        }
        while (see(DEC) || see(INC)) {
        	if (have(INC)) {
        		primaryExpr = new JPostIncrementOp(line, primaryExpr);
        	} else if (have(DEC)) {
        		primaryExpr = new JPostDecrementOp(line, primaryExpr);
        	}
        }
        return primaryExpr;
    }


    /**
     * Parse a selector.
     * <p>
     * <pre>
     *   selector ::= DOT qualifiedIdentifier [arguments]
     *              | LBRACK expression RBRACK
     * </pre>
     *
     * @param target the target expression for this selector.
     * @return an AST for a selector.
     */

    private JExpression selector(JExpression target) {
        int line = scanner.token().line();
        if (have(DOT)) {
            // Target . selector
            mustBe(IDENTIFIER);
            String name = scanner.previousToken().image();
            if (see(LPAREN)) {
                ArrayList<JExpression> args = arguments();
                return new JMessageExpression(line, target, name, args);
            } else {
                return new JFieldSelection(line, target, name);
            }
        } else {
            mustBe(LBRACK);
            JExpression index = expression();
            mustBe(RBRACK);
            return new JArrayExpression(line, target, index);
        }
    }

    /**
     * Parse a primary expression.
     * <p>
     * <pre>
     *   primary ::= parExpression
     *             | THIS [arguments]
     *             | SUPER ( arguments
     *                     | DOT IDENTIFIER [arguments]
     *                     )
     *             | literal
     *             | NEW creator
     *             | qualifiedIdentifier [arguments]
     * </pre>
     *
     * @return an AST for a primary.
     */

    private JExpression primary() {
        int line = scanner.token().line();
        if (see(LPAREN)) {
            return parExpression();
        } else if (have(THIS)) {
            if (see(LPAREN)) {
                return new JThisConstruction(line, arguments());
            } else {
                return new JThis(line);
            }
        } else if (have(SUPER)) {
            if (!have(DOT)) {
                return new JSuperConstruction(line, arguments());
            } else {
                mustBe(IDENTIFIER);
                String name = scanner.previousToken().image();
                JExpression newTarget = new JSuper(line);
                if (see(LPAREN)) {
                    return new JMessageExpression(line, newTarget, null, name,
                            arguments());
                } else {
                    return new JFieldSelection(line, newTarget, name);
                }
            }
        } else if (have(NEW)) {
            return creator();
        } else if (see(IDENTIFIER)) {
            TypeName id = qualifiedIdentifier();
            if (see(LPAREN)) {
                return new JMessageExpression(line, null, ambiguousPart(id), id
                        .simpleName(), arguments());
            } else if (ambiguousPart(id) == null) {
                // A simple name
                return new JVariable(line, id.simpleName());
            } else {
                // ambiguousPart.fieldName
                return new JFieldSelection(line, ambiguousPart(id), null, id
                        .simpleName());
            }
        } else {
            return literal();
        }
    }

    /**
     * Parse a creator.
     * <p>
     * <pre>
     *   creator ::= (basicType | qualifiedIdentifier)
     *                 ( arguments
     *                 | LBRACK RBRACK {LBRACK RBRACK}
     *                     [arrayInitializer]
     *                 | newArrayDeclarator
     *                 )
     * </pre>
     *
     * @return an AST for a creator.
     */

    private JExpression creator() {
        int line = scanner.token().line();
        Type type = seeBasicType() ? basicType() : qualifiedIdentifier();
        if (see(LPAREN)) {
            ArrayList<JExpression> args = arguments();
            return new JNewOp(line, type, args);
        } else if (see(LBRACK)) {
            if (seeDims()) {
                Type expected = type;
                while (have(LBRACK)) {
                    mustBe(RBRACK);
                    expected = new ArrayTypeName(expected);
                }
                return arrayInitializer(expected);
            } else
                return newArrayDeclarator(line, type);
        } else {
            reportParserError("( or [ sought where %s found", scanner.token()
                    .image());
            return new JWildExpression(line);
        }
    }

    /**
     * Parse a new array declarator.
     * <p>
     * <pre>
     *   newArrayDeclarator ::= LBRACK expression RBRACK
     *                            {LBRACK expression RBRACK}
     *                            {LBRACK RBRACK}
     * </pre>
     *
     * @param line line in which the declarator occurred.
     * @param type type of the array.
     * @return an AST for a newArrayDeclarator.
     */

    private JNewArrayOp newArrayDeclarator(int line, Type type) {
        ArrayList<JExpression> dimensions = new ArrayList<JExpression>();
        mustBe(LBRACK);
        dimensions.add(expression());
        mustBe(RBRACK);
        type = new ArrayTypeName(type);
        while (have(LBRACK)) {
            if (have(RBRACK)) {
                // We're done with dimension expressions
                type = new ArrayTypeName(type);
                while (have(LBRACK)) {
                    mustBe(RBRACK);
                    type = new ArrayTypeName(type);
                }
                return new JNewArrayOp(line, type, dimensions);
            } else {
                dimensions.add(expression());
                type = new ArrayTypeName(type);
                mustBe(RBRACK);
            }
        }
        return new JNewArrayOp(line, type, dimensions);
    }

    /**
     * Parse a literal.
     * <p>
     * <pre>
     *   literal ::= INT_LITERAL | CHAR_LITERAL | STRING_LITERAL
     *             | TRUE        | FALSE        | NULL
     * </pre>
     *
     * @return an AST for a literal.
     */

    private JExpression literal() {
        int line = scanner.token().line();
        if (have(INT_LITERAL)) {
            return new JLiteralInt(line, scanner.previousToken().image());
        } else if (have(DOUBLE_LITERAL)) {
            return new JLiteralDouble(line, scanner.previousToken().image());
        } else if (have(FLOAT_LITERAL)) {
            return new JLiteralFloat(line, scanner.previousToken().image());
        } else if (have(LONG_LITERAL)) {
            return new JLiteralLong(line, scanner.previousToken().image());
        } else if (have(CHAR_LITERAL)) {
            return new JLiteralChar(line, scanner.previousToken().image());
        } else if (have(STRING_LITERAL)) {
            return new JLiteralString(line, scanner.previousToken().image());
        } else if (have(TRUE)) {
            return new JLiteralTrue(line);
        } else if (have(FALSE)) {
            return new JLiteralFalse(line);
        } else if (have(NULL)) {
            return new JLiteralNull(line);
        } else {
            reportParserError("Literal sought where %s found", scanner.token()
                    .image());
            return new JWildExpression(line);
        }
    }

    // A tracing aid. Invoke to debug the parser at various

    private void trace( String message )
    {
    System.err.println( "["
    + scanner.token().line()
    + ": "
    + message
    + ", looking at a: "
    + scanner.token().tokenRep()
    + " = " + scanner.token().image() + "]" );
    }
}
