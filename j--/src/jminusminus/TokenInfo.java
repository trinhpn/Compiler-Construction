// Copyright 2013 Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

/**
 * An enum of token kinds. Each entry in this enum represents the kind of a
 * token along with its image (string representation).
 *
 * When you add a new token to the scanner, you must also add an entry to this
 * enum specifying the kind and image of the new token.
 * 
 * @version November 8, 2018
 * @author Bill Campbell, Swami Iyer and Bahar Akbal-Delibas
 * @author Gurchetan Singh and Mai Pham
 */

enum TokenKind {
    // ReservedWords [existing]
      EOF("<EOF>")
    , ABSTRACT("abstract")
    , BOOLEAN("boolean")
    , CHAR("char")
    , CLASS("class")
    , ELSE("else")
    , EXTENDS("extends")
    , FALSE("false")
    , IF("if")
    , IMPORT("import")
    , INSTANCEOF("instanceof")
    , INT("int")
    , NEW("new")
    , NULL("null")
    , PACKAGE("package")
    , PRIVATE("private")
    , PROTECTED("protected")
    , PUBLIC("public")
    , RETURN("return")
    , STATIC("static")
    , SUPER("super")
    , THIS("this")
    , TRUE("true")
    , VOID("void")
    , WHILE("while")

    // ReservedWords [added]
    , ASSERT("assert")
    , BREAK("break")
    , BYTE("byte")
    , CASE("case")
    , CATCH("catch")
    , CONST("const")
    , CONTINUE("continue")
    , DEFAULT("default")
    , DO("do")
    , DOUBLE("double")
    , ENUM("enum")
    , FINAL("final")
    , FINALLY("finally")
    , FLOAT("float")
    , FOR("for")
    , GOTO("goto")
    , IMPLEMENTS("implements")
    , INTERFACE("interface")
    , LONG("long")
    , NATIVE("native")
    , SHORT("short")
    , STRICTFP("strictfp")
    , SWITCH("switch")
    , SYNCHRONIZED("synchronized")
    , THROW("throw")
    , THROWS("throws")
    , TRANSIENT("transient")
    , TRY("try")
    , VOLATILE("volatile")
    , UNTIL("until")

    // Operators [existing]
    , PLUS("+") // done
    , ASSIGN("=")
    , DEC("--") // done
    , EQUAL("==")
    , GT(">")
    , INC("++") // done
    , LAND("&&") // existing
    , LE("<=")
    , LNOT("!")
    , MINUS("-")
    , PLUS_ASSIGN("+=")
    , STAR("*")
    , LPAREN("(")
    , RPAREN(")")
    , LCURLY("{")
    , RCURLY("}")
    , LBRACK("[")
    , RBRACK("]")
    , SEMI(";")
    , COLON(":")
    , COMMA(",")
    , DOT(".")

    // Operators [added]
    , ELLIPSIS("...")
    , BITAND("&")
    , TERNARY("?")
    , DIV_ASSIGN("/=")
    , DIVIDE("/")
    , MODULO("%")
    , MOD_ASSIGN("%=")
    , BEOR("^")
    , BIOR("|")
    , LOR("||")
    , LT("<") // done
    , GE(">=") // done
    , LSHIFT("<<")
    , RSHIFT(">>")
    , ZSHIFT(">>>")
    , UBC("~") // done
    , NEQUAL("!=")
    , AND_ASSIGN("&=")
    , LSHIFT_ASSIGN("<<=")
    , RSHIFT_ASSIGN(">>=")
    , ZSHIFT_ASSIGN(">>>=")
    , OR_ASSIGN("|=")
    
    , MINUS_ASSIGN("-=")
    , STAR_ASSIGN("*=")
    , XOR_ASSIGN("^=")
    // Literals [existing]
    , IDENTIFIER("<IDENTIFIER>")
    , INT_LITERAL("<INT_LITERAL>")
    , CHAR_LITERAL("<CHAR_LITERAL>")
    , STRING_LITERAL("<STRING_LITERAL>")

    // Literals [added]
    , HEX_LITERAL("<HEX_LITERAL>")
    , OCTAL_LITERAL("<OCTAL_LITERAL>")
    , BINARY_LITERAL("<BINARY_LITERAL>")
    , BOOL_LITERAL("<BOOLEAN_LITERAL>")
    , FLOAT_LITERAL("<FLOAT_LITERAL>")
    , DOUBLE_LITERAL("<DOUBLE_LITERAL>")
    , LONG_LITERAL("<LONG_LITERAL>");

    /** The token's string representation. */
    private String image;

    /**
     * Construct an instance TokenKind given its string representation.
     *
     * @param image
     *            string representation of the token.
     */

    private TokenKind(String image) {
        this.image = image;
    }

    /**
     * Return the image of the token.
     *
     * @return the token's image.
     */

    public String image() {
        return image;
    }

    /**
     * Return the string representation of the token.
     *
     * @return the token's string representation.
     */

    public String toString() {
        return image;
    }

}

/**
 * A representation of tokens returned by the lexical analyzer method,
 * getNextToken(). A token has a kind identifying what kind of token it is, an
 * image for providing any semantic text, and the line in which it occurred in
 * the source file.
 */

class TokenInfo {

    /** Token kind. */
    private TokenKind kind;

    /**
     * Semantic text (if any). For example, the identifier name when the token
     * kind is IDENTIFIER. For tokens without a semantic text, it is simply its
     * string representation. For example, "+=" when the token kind is
     * PLUS_ASSIGN.
     */
    private String image;

    /** Line in which the token occurs in the source file. */
    private int line;

    /**
     * Construct a TokenInfo from its kind, the semantic text forming the token,
     * and its line number.
     *
     * @param kind
     *            the token's kind.
     * @param image
     *            the semantic text comprising the token.
     * @param line
     *            the line in which the token occurs in the source file.
     */

    public TokenInfo(TokenKind kind, String image, int line) {
        this.kind = kind;
        this.image = image;
        this.line = line;
    }

    /**
     * Construct a TokenInfo from its kind, and its line number. Its image is
     * simply its string representation.
     *
     * @param kind
     *            the token's identifying number.
     * @param line
     *            identifying the line on which the token was found.
     */

    public TokenInfo(TokenKind kind, int line) {
        this(kind, kind.toString(), line);
    }

    /**
     * Return the token's string representation.
     *
     * @return the string representation.
     */

    public String tokenRep() {
        return kind.toString();
    }

    /**
     * Return the semantic text associated with the token.
     *
     * @return the semantic text.
     */

    public String image() {
        return image;
    }

    /**
     * Return the line number associated with the token.
     *
     * @return the line number.
     */

    public int line() {
        return line;
    }

    /**
     * Return the token's kind.
     *
     * @return the kind.
     */

    public TokenKind kind() {
        return kind;
    }

    /**
     * Return the token's image.
     *
     * @return the image.
     */

    public String toString() {
        return image;
    }

}
