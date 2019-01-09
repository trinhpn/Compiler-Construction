// Copyright 2013 Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Hashtable;
import static jminusminus.TokenKind.*;

/**
 * A lexical analyzer for j--, that has no backtracking mechanism.
 *
 * When you add a new token to the scanner, you must also add an entry in the
 * TokenKind enum in TokenInfo.java specifying the kind and image of the new
 * token.
 * 
 * @version November 8, 2018
 * @author Bill Campbell, Swami Iyer and Bahar Akbal-Delibas
 * @author Gurchetan Singh and Mai Pham
 */

class Scanner {

    /** End of file character. */
    public final static char EOFCH = CharReader.EOFCH;

    /** Keywords in j--. */
    private Hashtable<String, TokenKind> reserved;

    /** Source characters. */
    private CharReader input;

    /** Next unscanned character. */
    private char ch;

    /** Whether a scanner error has been found. */
    private boolean isInError;

    /** Source file name. */
    private String fileName;

    /** Line number of current token. */
    private int line;

    /**
     * Construct a Scanner object.
     *
     * @param fileName
     *            the name of the file containing the source.
     * @exception FileNotFoundException
     *                when the named file cannot be found.
     */

    public Scanner(String fileName) throws FileNotFoundException {
        this.input = new CharReader(fileName);
        this.fileName = fileName;
        isInError = false;

        // Keywords in j-- [existing]
        reserved = new Hashtable<String, TokenKind>();
        reserved.put(ABSTRACT.image(), ABSTRACT);
        reserved.put(BOOLEAN.image(), BOOLEAN);
        reserved.put(CHAR.image(), CHAR);
        reserved.put(CLASS.image(), CLASS);
        reserved.put(ELSE.image(), ELSE);
        reserved.put(EXTENDS.image(), EXTENDS);
        reserved.put(FALSE.image(), FALSE);
        reserved.put(IF.image(), IF);
        reserved.put(IMPORT.image(), IMPORT);
        reserved.put(INSTANCEOF.image(), INSTANCEOF);
        reserved.put(INT.image(), INT);
        reserved.put(NEW.image(), NEW);
        reserved.put(NULL.image(), NULL);
        reserved.put(PACKAGE.image(), PACKAGE);
        reserved.put(PRIVATE.image(), PRIVATE);
        reserved.put(PROTECTED.image(), PROTECTED);
        reserved.put(PUBLIC.image(), PUBLIC);
        reserved.put(RETURN.image(), RETURN);
        reserved.put(STATIC.image(), STATIC);
        reserved.put(SUPER.image(), SUPER);
        reserved.put(THIS.image(), THIS);
        reserved.put(TRUE.image(), TRUE);
        reserved.put(VOID.image(), VOID);
        reserved.put(WHILE.image(), WHILE);

        // Keywords in j-- [added]
        reserved.put(ASSERT.image(), ASSERT);
        reserved.put(BREAK.image(), BREAK);
        reserved.put(BYTE.image(), BYTE);
        reserved.put(CASE.image(), CASE);
        reserved.put(CATCH.image(), CATCH);
        reserved.put(CONST.image(), CONST);
        reserved.put(CONTINUE.image(), CONTINUE);
        reserved.put(DEFAULT.image(), DEFAULT);
        reserved.put(DO.image(), DO);
        reserved.put(DOUBLE.image(), DOUBLE);
        reserved.put(ENUM.image(), ENUM);
        reserved.put(FINAL.image(), FINAL);
        reserved.put(FINALLY.image(), FINALLY);
        reserved.put(FLOAT.image(), FLOAT);
        reserved.put(FOR.image(), FOR);
        reserved.put(GOTO.image(), GOTO);
        reserved.put(IMPLEMENTS.image(), IMPLEMENTS);
        reserved.put(INTERFACE.image(), INTERFACE);
        reserved.put(LONG.image(), LONG);
        reserved.put(NATIVE.image(), NATIVE);
        reserved.put(SHORT.image(), SHORT);
        reserved.put(STRICTFP.image(), STRICTFP);
        reserved.put(SWITCH.image(), SWITCH);
        reserved.put(SYNCHRONIZED.image(), SYNCHRONIZED);
        reserved.put(THROW.image(), THROW);
        reserved.put(THROWS.image(), THROWS);
        reserved.put(TRANSIENT.image(), TRANSIENT);
        reserved.put(TRY.image(), TRY);
        reserved.put(VOLATILE.image(), VOLATILE);
        reserved.put(UNTIL.image(), UNTIL);

        // Prime the pump.
        nextCh();
    }

    /**
     * Scan the next token from input.
     *
     * @return the the next scanned token.
     */

    public TokenInfo getNextToken() {
        StringBuffer buffer;
        boolean moreWhiteSpace = true;
        boolean isDouble = false;
        while (moreWhiteSpace) {
            while (isWhitespace(ch)) {
                nextCh();
            }
            if (ch == '/') {
                //System.out.println(ch);
                nextCh();
                if (ch == '/') {
                    // CharReader maps all new lines to '\n'
                    while (ch != '\n' && ch != EOFCH) {
                        nextCh();
                    }
                } else if (ch == '=') {
                    return new TokenInfo(DIV_ASSIGN, line);
                } else if (ch == '*') {
                    // CharReader maps all chars to "*/"
                    boolean asterisk = false;
                    boolean escComment = false;
                    while (!escComment && ch != EOFCH) {
                        nextCh();
                        if (ch == '*') {
                            asterisk = true;
                            nextCh();
                        } else if (asterisk && ch != '/') {
                            asterisk = false;
                            nextCh();
                        } else if (asterisk) { // && ch == '/'
                            escComment = true;         // exit comment loop
                            nextCh();
                        } else if (ch == EOFCH) {
                            reportScannerError("Unexpected end of file found in multi-line comment");
                        }
                    }
                } else {
                    return new TokenInfo(DIVIDE, line);
                }
            } else {
                moreWhiteSpace = false;
            }
        }
        line = input.line();
        switch (ch) {
        case '(':
            nextCh();
            return new TokenInfo(LPAREN, line);
        case ')':
            nextCh();
            return new TokenInfo(RPAREN, line);
        case '{':
            nextCh();
            return new TokenInfo(LCURLY, line);
        case '}':
            nextCh();
            return new TokenInfo(RCURLY, line);
        case '[':
            nextCh();
            return new TokenInfo(LBRACK, line);
        case ']':
            nextCh();
            return new TokenInfo(RBRACK, line);
        case ';':
            nextCh();
            return new TokenInfo(SEMI, line);
        case ',':
            nextCh();
            return new TokenInfo(COMMA, line);
        case '=':
            nextCh();
            if (ch == '=') {
                nextCh();
                return new TokenInfo(EQUAL, line);
            } else {
                return new TokenInfo(ASSIGN, line);
            }
        case '!':
            nextCh();
            if (ch == '=') {
                return new TokenInfo(NEQUAL, line);
            } else {
                return new TokenInfo(LNOT, line);
            }
        case '*':
            nextCh();
            if (ch == '=') {
            	return new TokenInfo(STAR_ASSIGN, line);
            } else return new TokenInfo(STAR, line);
        case '%':
            nextCh();
            if (ch == '=') {
                return new TokenInfo(MOD_ASSIGN, line);
            } else {
                return new TokenInfo(MODULO, line);
            }
        case '^':
            nextCh();
            return new TokenInfo(BEOR, line);
        case '|':
            nextCh();
            if (ch == '|') {
                nextCh();
                return new TokenInfo(LOR, line);
            } else if (ch == '=') {
                nextCh();
                return new TokenInfo(OR_ASSIGN, line);
            } else {
                return new TokenInfo(BIOR, line);
            }
        case '+':
            nextCh();
            if (ch == '=') {
                nextCh();
                return new TokenInfo(PLUS_ASSIGN, line);
            } else if (ch == '+') {
                nextCh();
                return new TokenInfo(INC, line);
            } else {
                return new TokenInfo(PLUS, line);
            }
        case '-':
            nextCh();
            if (ch == '-') {
                nextCh();
                return new TokenInfo(DEC, line);
            } else if (ch == '=') {
            	nextCh();
            	return new TokenInfo(MINUS_ASSIGN, line);
            } else {
                return new TokenInfo(MINUS, line);
            }
        case '&':
            nextCh();
            if (ch == '&') {
                nextCh();
                return new TokenInfo(LAND, line);
            } else if (ch == '=') {
                nextCh();
                return new TokenInfo(AND_ASSIGN, line);
            } else {
                return new TokenInfo(BITAND, line);
            }
        case '~':
            nextCh();
            return new TokenInfo(UBC, line);
        case '>':
            nextCh();
            if (ch == '=') {
                nextCh();
                return new TokenInfo(GE, line);
            } else if (ch == '>') {
                nextCh();
                if (ch == '>') {
                    nextCh();
                    if (ch == '=') {
                        return new TokenInfo(ZSHIFT_ASSIGN, line);
                    } else {
                        return new TokenInfo(ZSHIFT, line);
                    }
                }
            } else if (ch == '=') {
                nextCh();
                return new TokenInfo(RSHIFT_ASSIGN, line);
            } else {
                return new TokenInfo(GT, line);
            }
        case '<':
            nextCh();
            if (ch == '=') {
                nextCh();
                return new TokenInfo(LE, line);
            } else if (ch == '<') {
                nextCh();
                return new TokenInfo(LSHIFT_ASSIGN, line);
            } else {
                return new TokenInfo(LT, line);
            }
        case '\'':
            buffer = new StringBuffer();
            buffer.append('\'');
            nextCh();
            if (ch == '\\') {
                nextCh();
                buffer.append(escape());
            } else {
                buffer.append(ch);
                nextCh();
            }
            if (ch == '\'') {
                buffer.append('\'');
                nextCh();
                return new TokenInfo(CHAR_LITERAL, buffer.toString(), line);
            } else {
                // Expected a ' ; report error and try to
                // recover.
                reportScannerError(ch
                        + " found by scanner where closing ' was expected.");
                while (ch != '\'' && ch != ';' && ch != '\n') {
                    nextCh();
                }
                return new TokenInfo(CHAR_LITERAL, buffer.toString(), line);
            }
        case '"':
            buffer = new StringBuffer();
            buffer.append("\"");
            nextCh();
            while (ch != '"' && ch != '\n' && ch != EOFCH) {
                if (ch == '\\') {
                    nextCh();
                    buffer.append(escape());
                } else {
                    buffer.append(ch);
                    nextCh();
                }
            }
            if (ch == '\n') {
                reportScannerError("Unexpected end of line found in String");
            } else if (ch == EOFCH) {
                reportScannerError("Unexpected end of file found in String");
            } else {
                // Scan the closing "
                nextCh();
                buffer.append("\"");
            }
            return new TokenInfo(STRING_LITERAL, buffer.toString(), line);
        case '.':
            nextCh();
            if (isDigit(ch)) {
                isDouble = true;
                buffer = new StringBuffer(".");
                return parseDoubleRightSide(buffer);
            } else if (ch == '.') {
            	nextCh();
            	if (ch == '.') {
            		nextCh();
            		return new TokenInfo(ELLIPSIS, line);
            	} else {
            		reportScannerError("Invalid token '..'");
            	}
            }
            else {
            	return new TokenInfo(DOT, line);
            }
        case '?':
            nextCh();
            return new TokenInfo(TERNARY, line);
        case ':':
            nextCh();
            return new TokenInfo(COLON, line);
        case EOFCH:
            return new TokenInfo(EOF, line);
        case '0':
        	nextCh();
        	buffer = new StringBuffer();
        	//For binary, hex, and octal numbers, if the number is empty (e.g. 0b and nothing else),
        	//then a syntax error will be reported here.
        	if (ch == 'b') {
        		nextCh();
        		while (ch >= '0' && ch <= '1') {
        			buffer.append(ch);
        			nextCh();
        		}
        		if (buffer.length() == 0) reportScannerError("Invalid binary literal number.");
        		return new TokenInfo(BIN_INT_LITERAL, buffer.toString(), line);
        	} else if (ch == 'x') {
        		nextCh();
        		while (isDigit(ch) || (ch >= 'a' && ch <= 'f') || (ch >= 'A' && ch <= 'F')) {
        			buffer.append(ch);
        			nextCh();
        		}
        		if (buffer.length() == 0) reportScannerError("Invalid hexadecimal literal number.");
        		return new TokenInfo(HEX_INT_LITERAL, buffer.toString(), line);
        	} else if (ch >= '0' && ch <= '7') {
        		nextCh();
        		while (ch >= '0' && ch <= '7') {
        			buffer.append(ch);
        			nextCh();
        		}
        		if (buffer.length() == 0) reportScannerError("Invalid octal literal number.");
        		return new TokenInfo(OCT_INT_LITERAL, buffer.toString(), line);
        	} else if (ch == '.') {
        		buffer = new StringBuffer();
        		nextCh();
        		buffer.append("0.");
        		return parseDoubleRightSide(buffer);
        	} else {
        		return new TokenInfo(INT_LITERAL, "0", line);
        	}
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9':
        buffer = new StringBuffer();
        while (isDigit(ch)) {
            buffer.append(ch);
            nextCh();
        }
        if(ch == '.' || ch == 'e' || ch == 'E') {
            boolean isExponential = false;
            if (ch == 'e') {
                isExponential = true;
            }
            isDouble = true;   // at least a double, could change later
            buffer.append(ch);
            nextCh();
            if (isExponential) {
                if (ch == '+' || ch == '-') {
                    buffer.append(ch);
                    nextCh();
                }
            }
            while (isDigit(ch)) {
                buffer.append(ch);
                nextCh();
            }
            if(ch == 'f' || ch == 'F') {
                nextCh();
                return new TokenInfo(FLOAT_LITERAL, buffer.toString(), line);
            } else if (ch == 'l' || ch == 'L') {
                nextCh();
                return new TokenInfo(LONG_LITERAL, buffer.toString(), line);
            } else if (isDouble || ch == 'd' || ch == 'D') {
                if (ch == 'd' || ch == 'D') {
                    nextCh();
                }
                return new TokenInfo(DOUBLE_LITERAL, buffer.toString(), line);
            }
            reportScannerError("Undefined literal");
        }

        if (ch == 'f' || ch == 'F') {
            nextCh();
            return new TokenInfo(FLOAT_LITERAL, buffer.toString(), line);
        } else if (ch == 'l' || ch == 'L') {
            nextCh();
            return new TokenInfo(LONG_LITERAL, buffer.toString(), line);
        } else if (isDouble || ch == 'd' || ch == 'D') {
                if (ch == 'd' || ch == 'D') {
                    nextCh();
                }
                return new TokenInfo(DOUBLE_LITERAL, buffer.toString(), line);
        } else {
            return new TokenInfo(INT_LITERAL, buffer.toString(), line);
        }
        default:
            if (isIdentifierStart(ch)) {
                buffer = new StringBuffer();
                while (isIdentifierPart(ch)) {
                    buffer.append(ch);
                    nextCh();
                }
                String identifier = buffer.toString();
                if (reserved.containsKey(identifier)) {
                    return new TokenInfo(reserved.get(identifier), line);
                } else {
                    return new TokenInfo(IDENTIFIER, identifier, line);
                }
            } else {
                reportScannerError("Unidentified input token: '%c'", ch);
                nextCh();
                return getNextToken();
            }
        }
    }

    /**
     * Scan and return an escaped character.
     *
     * @return escaped character.
     */

    private String escape() {
        switch (ch) {
        case 'b':
            nextCh();
            return "\\b";
        case 't':
            nextCh();
            return "\\t";
        case 'n':
            nextCh();
            return "\\n";
        case 'f':
            nextCh();
            return "\\f";
        case 'r':
            nextCh();
            return "\\r";
        case '"':
            nextCh();
            return "\"";
        case '\'':
            nextCh();
            return "\\'";
        case '\\':
            nextCh();
            return "\\\\";
        default:
            reportScannerError("Badly formed escape: \\%c", ch);
            nextCh();
            return "";
        }
    }

    /**
     * Scan and return the remaining part of a double.<br>
     * Pre-Invariant: If a decimal point appears in this double, it must already have been read.<br>
     * A decimal point need not appear in the double at all. For instance, this could be called up to a point where
     * an e or E occurs to finish the parsing.
     * @param buffer A StringBuilder with the number loaded into it up to, and including, the decimal (if present).
     * @return a token representing a double or float
     */
    private TokenInfo parseDoubleRightSide(StringBuffer buffer) {
    	while (isDigit(ch)) {
    		buffer.append(ch);
    		nextCh();
    	}
    	if (ch == 'e' || ch == 'E') {
    		buffer.append(ch);
    		nextCh();
    		if (ch == '-') {
    			buffer.append(ch);
    			nextCh();
    		}
    		if (!isDigit(ch)) reportScannerError("Invalid floating point: " + buffer.toString());
    		while (isDigit(ch)) {
    			buffer.append(ch);
    			nextCh();
    		}
    	}
    	
    	if (ch == 'f' || ch == 'F') {
    		nextCh();
    		return new TokenInfo(FLOAT_LITERAL, buffer.toString(), line);
    	} else if (ch == 'd' || ch == 'D') {
    		nextCh();
    		return new TokenInfo(DOUBLE_LITERAL, buffer.toString(), line);
    	} else {
    		return new TokenInfo(DOUBLE_LITERAL, buffer.toString(), line);
    	}
    }

    /**
     * Advance ch to the next character from input, and update the line number.
     */

    private void nextCh() {
        line = input.line();
        try {
            ch = input.nextChar();
        } catch (Exception e) {
            reportScannerError("Unable to read characters from input");
        }
    }

    /**
     * Report a lexcial error and record the fact that an error has occured.
     * This fact can be ascertained from the Scanner by sending it an
     * errorHasOccurred() message.
     *
     * @param message
     *            message identifying the error.
     * @param args
     *            related values.
     */

    private void reportScannerError(String message, Object... args) {
        isInError = true;
        System.err.printf("%s:%d: ", fileName, line);
        System.err.printf(message, args);
        System.err.println();
    }

    /**
     * Return true if the specified character is a digit (0-9); false otherwise.
     *
     * @param c
     *            character.
     * @return true or false.
     */

    private boolean isDigit(char c) {
        return (c >= '0' && c <= '9');
    }

    /**
     * Return true if the specified character is a whitespace; false otherwise.
     *
     * @param c
     *            character.
     * @return true or false.
     */

    private boolean isWhitespace(char c) {
        switch (c) {
        case ' ':
        case '\t':
        case '\n': // CharReader maps all new lines to '\n'
        case '\f':
            return true;
        }
        return false;
    }

    /**
     * Return true if the specified character can start an identifier name;
     * false otherwise.
     *
     * @param c
     *            character.
     * @return true or false.
     */

    private boolean isIdentifierStart(char c) {
        return (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c == '_' || c == '$');
    }

    /**
     * Return true if the specified character can be part of an identifier name;
     * false otherwise.
     *
     * @param c
     *            character.
     * @return true or false.
     */

    private boolean isIdentifierPart(char c) {
        return (isIdentifierStart(c) || isDigit(c));
    }

    /**
     * Has an error occurred up to now in lexical analysis?
     *
     * @return true or false.
     */

    public boolean errorHasOccurred() {
        return isInError;
    }

    /**
     * The name of the source file.
     *
     * @return name of the source file.
     */

    public String fileName() {
        return fileName;
    }

}

/**
 * A buffered character reader. Abstracts out differences between platforms,
 * mapping all new lines to '\n'. Also, keeps track of line numbers where the
 * first line is numbered 1.
 */

class CharReader {

    /** A representation of the end of file as a character. */
    public final static char EOFCH = (char) -1;

    /** The underlying reader records line numbers. */
    private LineNumberReader lineNumberReader;

    /** Name of the file that is being read. */
    private String fileName;

    /**
     * Construct a CharReader from a file name.
     *
     * @param fileName
     *            the name of the input file.
     * @exception FileNotFoundException
     *                if the file is not found.
     */

    public CharReader(String fileName) throws FileNotFoundException {
        lineNumberReader = new LineNumberReader(new FileReader(fileName));
        this.fileName = fileName;
    }

    /**
     * Scan the next character.
     *
     * @return the character scanned.
     * @exception IOException
     *                if an I/O error occurs.
     */

    public char nextChar() throws IOException {
        return (char) lineNumberReader.read();
    }

    /**
     * The current line number in the source file, starting at 1.
     *
     * @return the current line number.
     */

    public int line() {
        // LineNumberReader counts lines from 0.
        return lineNumberReader.getLineNumber() + 1;
    }

    /**
     * Return the file name.
     *
     * @return the file name.
     */

    public String fileName() {
        return fileName;
    }

    /**
     * Close the file.
     *
     * @exception IOException
     *                if an I/O error occurs.
     */

    public void close() throws IOException {
        lineNumberReader.close();
    }

}
