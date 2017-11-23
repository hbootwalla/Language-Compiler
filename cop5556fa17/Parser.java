package cop5556fa17;

import static cop5556fa17.Scanner.Kind.BOOLEAN_LITERAL;
import static cop5556fa17.Scanner.Kind.COMMA;
import static cop5556fa17.Scanner.Kind.EOF;
import static cop5556fa17.Scanner.Kind.IDENTIFIER;
import static cop5556fa17.Scanner.Kind.INTEGER_LITERAL;
import static cop5556fa17.Scanner.Kind.KW_A;
import static cop5556fa17.Scanner.Kind.KW_DEF_X;
import static cop5556fa17.Scanner.Kind.KW_DEF_Y;
import static cop5556fa17.Scanner.Kind.KW_R;
import static cop5556fa17.Scanner.Kind.KW_SCREEN;
import static cop5556fa17.Scanner.Kind.KW_X;
import static cop5556fa17.Scanner.Kind.KW_Y;
import static cop5556fa17.Scanner.Kind.KW_Z;
import static cop5556fa17.Scanner.Kind.KW_a;
import static cop5556fa17.Scanner.Kind.KW_abs;
import static cop5556fa17.Scanner.Kind.KW_atan;
import static cop5556fa17.Scanner.Kind.KW_boolean;
import static cop5556fa17.Scanner.Kind.KW_cart_x;
import static cop5556fa17.Scanner.Kind.KW_cart_y;
import static cop5556fa17.Scanner.Kind.KW_cos;
import static cop5556fa17.Scanner.Kind.KW_file;
import static cop5556fa17.Scanner.Kind.KW_image;
import static cop5556fa17.Scanner.Kind.KW_int;
import static cop5556fa17.Scanner.Kind.KW_polar_a;
import static cop5556fa17.Scanner.Kind.KW_polar_r;
import static cop5556fa17.Scanner.Kind.KW_r;
import static cop5556fa17.Scanner.Kind.KW_sin;
import static cop5556fa17.Scanner.Kind.KW_url;
import static cop5556fa17.Scanner.Kind.KW_x;
import static cop5556fa17.Scanner.Kind.KW_y;
import static cop5556fa17.Scanner.Kind.LPAREN;
import static cop5556fa17.Scanner.Kind.LSQUARE;
import static cop5556fa17.Scanner.Kind.OP_AND;
import static cop5556fa17.Scanner.Kind.OP_ASSIGN;
import static cop5556fa17.Scanner.Kind.OP_AT;
import static cop5556fa17.Scanner.Kind.OP_COLON;
import static cop5556fa17.Scanner.Kind.OP_DIV;
import static cop5556fa17.Scanner.Kind.OP_EQ;
import static cop5556fa17.Scanner.Kind.OP_EXCL;
import static cop5556fa17.Scanner.Kind.OP_GE;
import static cop5556fa17.Scanner.Kind.OP_GT;
import static cop5556fa17.Scanner.Kind.OP_LARROW;
import static cop5556fa17.Scanner.Kind.OP_LE;
import static cop5556fa17.Scanner.Kind.OP_LT;
import static cop5556fa17.Scanner.Kind.OP_MINUS;
import static cop5556fa17.Scanner.Kind.OP_MOD;
import static cop5556fa17.Scanner.Kind.OP_NEQ;
import static cop5556fa17.Scanner.Kind.OP_OR;
import static cop5556fa17.Scanner.Kind.OP_PLUS;
import static cop5556fa17.Scanner.Kind.OP_Q;
import static cop5556fa17.Scanner.Kind.OP_RARROW;
import static cop5556fa17.Scanner.Kind.OP_TIMES;
import static cop5556fa17.Scanner.Kind.RSQUARE;
import static cop5556fa17.Scanner.Kind.SEMI;
import static cop5556fa17.Scanner.Kind.STRING_LITERAL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import cop5556fa17.Scanner.Kind;
import cop5556fa17.Scanner.Token;
import cop5556fa17.AST.ASTNode;
import cop5556fa17.AST.Declaration;
import cop5556fa17.AST.Declaration_Image;
import cop5556fa17.AST.Declaration_SourceSink;
import cop5556fa17.AST.Declaration_Variable;
import cop5556fa17.AST.Expression;
import cop5556fa17.AST.Expression_Binary;
import cop5556fa17.AST.Expression_BooleanLit;
import cop5556fa17.AST.Expression_Conditional;
import cop5556fa17.AST.Expression_FunctionAppWithExprArg;
import cop5556fa17.AST.Expression_FunctionAppWithIndexArg;
import cop5556fa17.AST.Expression_Ident;
import cop5556fa17.AST.Expression_IntLit;
import cop5556fa17.AST.Expression_PixelSelector;
import cop5556fa17.AST.Expression_PredefinedName;
import cop5556fa17.AST.Expression_Unary;
import cop5556fa17.AST.Index;
import cop5556fa17.AST.LHS;
import cop5556fa17.AST.Program;
import cop5556fa17.AST.Sink;
import cop5556fa17.AST.Sink_Ident;
import cop5556fa17.AST.Sink_SCREEN;
import cop5556fa17.AST.Source;
import cop5556fa17.AST.Source_CommandLineParam;
import cop5556fa17.AST.Source_Ident;
import cop5556fa17.AST.Source_StringLiteral;
import cop5556fa17.AST.Statement;
import cop5556fa17.AST.Statement_Assign;
import cop5556fa17.AST.Statement_In;
import cop5556fa17.AST.Statement_Out;

class SymbolTable{

	
	HashMap<String, Object> symbolTable = new HashMap<String, Object>();
	
	public Object returnType(String name){
		if(symbolTable.containsKey(name))
			return symbolTable.get(name);
		return null;
	}
}

public class Parser {
	
	@SuppressWarnings("serial")
	public class SyntaxException extends Exception {
		Token t;

		public SyntaxException(Token t, String message) {
			super(message);
			this.t = t;
		}

	}


	Scanner scanner;
	Token t;
	
	HashSet<Kind> functionNames = new HashSet<Kind>(); 
	HashSet<Kind> unaryExpressionKeywords = new HashSet<Kind>();
	HashSet<Kind> declarationKeywords = new HashSet<Kind>();
	
	
	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
		constructHashsets();
		
	}
	
	void constructHashsets(){
		functionNames.addAll(Arrays.asList(KW_sin, KW_cos, KW_atan, KW_abs, KW_cart_x, KW_cart_y, KW_polar_a, KW_polar_r));
		unaryExpressionKeywords.addAll(Arrays.asList(KW_x, KW_y, KW_r, KW_a, KW_X, KW_Y, KW_Z, KW_A, KW_R, KW_DEF_X, KW_DEF_Y));
		declarationKeywords.addAll(Arrays.asList(KW_int, KW_boolean, KW_image, KW_url, KW_file));
	}

	/**
	 * Main method called by compiler to parser input.
	 * Checks for EOF
	 * 
	 * @throws SyntaxException
	 */
	public Program parse() throws SyntaxException {
		Program p = program();
		matchEOF();
		return p;
	}
	
	public void consume(){
		t = scanner.nextToken();
	}
	
	public void match(Kind kind) throws SyntaxException{
		if(t.kind == kind)
			consume();
		else
			throw new SyntaxException(t, "Error at Position: " + t.pos_in_line + " in Line: " + t.line + " Expected Token: " + kind + " but got token: " + t.kind);
	}

	/**
	 * Program ::=  IDENTIFIER   ( Declaration SEMI | Statement SEMI )*   
	 * 
	 * Program is start symbol of our grammar.
	 * 
	 * @throws SyntaxException
	 */
	Program program() throws SyntaxException {
		//TODO  implement this
		Token firstToken = t;
		match(Kind.IDENTIFIER);
		ArrayList<ASTNode> decsAndStatements = new ArrayList<ASTNode>();
		
		while(declarationKeywords.contains(t.kind) || (t.kind == IDENTIFIER)){
			if(declarationKeywords.contains(t.kind)){
				Declaration d = declaration();
				match(SEMI);
				decsAndStatements.add(d);
			}
			else if(t.kind == IDENTIFIER){
				Statement s = statement();
				match(SEMI);
				decsAndStatements.add(s);
			}
		}
		return new Program(firstToken, firstToken, decsAndStatements);
	}
	

	/**
	 * Expression ::=  OrExpression  OP_Q  Expression OP_COLON Expression    | OrExpression
	 * 
	 * Our test cases may invoke this routine directly to support incremental development.
	 * 
	 * @throws SyntaxException
	 */
	public Expression expression() throws SyntaxException {
		Expression condExpr = null;
		Token firstToken = t;
		Expression trueExpr = null;
		Expression falseExpr = null;
		condExpr = orExpression();
		if(t.kind == OP_Q){
			consume();
			trueExpr = expression();
			match(OP_COLON);
			falseExpr = expression();
			return new Expression_Conditional(firstToken, condExpr, trueExpr, falseExpr);
		}
		return condExpr;
	}

	Index xySelector() throws SyntaxException {
			Token firstToken = t;
			Expression e0 = new Expression_PredefinedName(firstToken, t.kind);
			match(Kind.KW_x);
			match(Kind.COMMA);
			Expression e1 = new Expression_PredefinedName(t, t.kind);
			match(Kind.KW_y);
			return new Index(firstToken, e0, e1);
	}
	
	Index raSelector() throws SyntaxException {
			Token firstToken = t;
			Expression e0 = new Expression_PredefinedName(firstToken, t.kind);
			match(Kind.KW_r);
			match(Kind.COMMA);
			Expression e1 = new Expression_PredefinedName(t, t.kind);
			match(Kind.KW_a);
			return new Index(firstToken, e0, e1);
	}
	
	Index selector() throws SyntaxException {
		Token firstToken = t;
		Expression e0 = expression();
		match(Kind.COMMA);
		Expression e1 = expression();
		return new Index(firstToken, e0, e1);
	}
	
	Index lhsSelector() throws SyntaxException {
		Index index = null;
		match(Kind.LSQUARE);
		if(t.kind == Kind.KW_x)
			index = xySelector();
		else if(t.kind == Kind.KW_r)
			index = raSelector();
		else
			throw new SyntaxException(t,"LhsSelector Error. Position: " + t.pos_in_line + " in Line: " + t.line + " for token: " + t);
		match(Kind.RSQUARE);
		return index;
	}
	
	
	void functionName() throws SyntaxException {
		if(functionNames.contains(t.kind))
			consume();
		else
			throw new SyntaxException(t,"FunctionName Error. Position: " + t.pos_in_line + " in Line: " + t.line + " for token: " + t);
	}
	
	Expression functionApplication() throws SyntaxException {
		Token firstToken = t;
		functionName();
		if(t.kind == Kind.LPAREN){
			match(Kind.LPAREN);
			Expression e0 = expression();
			match(Kind.RPAREN);
			return new Expression_FunctionAppWithExprArg(firstToken, firstToken.kind, e0);
		}
		else if(t.kind == Kind.LSQUARE){
			match(Kind.LSQUARE);
			Index index = selector();
			match(Kind.RSQUARE);
			return new Expression_FunctionAppWithIndexArg(firstToken, firstToken.kind, index);
		}
		else
			throw new SyntaxException(t,"FunctionApplication Error. Position: " + t.pos_in_line + " in Line: " + t.line + " for token: " + t);
	}
	
	LHS lhs() throws SyntaxException {
		Token firstToken = t;
		Index index = null;
		match(Kind.IDENTIFIER);
		if(t.kind == Kind.LSQUARE){
			match(Kind.LSQUARE);
			index = lhsSelector();
			match(Kind.RSQUARE);
		}
		return new LHS(firstToken, firstToken, index);
	}
	
	Expression identOrPixelSelectorExpression() throws SyntaxException {
		Token firstToken = t;
		Expression expr = new Expression_Ident(firstToken, firstToken);
		match(Kind.IDENTIFIER);
		if(t.kind == Kind.LSQUARE){
			match(Kind.LSQUARE);
			Index index = selector();
			match(Kind.RSQUARE);
			expr = new Expression_PixelSelector(firstToken, firstToken, index);
		}
		return expr;
	}

	Expression primary() throws SyntaxException{
		Token firstToken = t;
		Expression expr = null;
		if(t.kind == Kind.INTEGER_LITERAL){
			expr = new Expression_IntLit(firstToken, firstToken.intVal());
			consume();
		}
		else if(t.kind == Kind.LPAREN){
			match(Kind.LPAREN);
			expr = expression();
			match(Kind.RPAREN);
		}
		else if(functionNames.contains(t.kind)){
			expr = functionApplication();
		}
		else if(t.kind == BOOLEAN_LITERAL){
			expr = new Expression_BooleanLit(firstToken, Boolean.parseBoolean(t.getText()));
			consume();
		}
		else
			throw new SyntaxException(t,"Primary Error. Position: " + t.pos_in_line + " in Line: " + t.line + " for token: " + t);
		return expr;
	}
	
	
	Expression unaryExpressionNotPlusMinus() throws SyntaxException {
		Expression expr = null;
		Token firstToken = t;
		Token op = null;
		if(t.kind == OP_EXCL){
			op = t;
			match(OP_EXCL);
			Expression e = unaryExpression();
			expr = new Expression_Unary(firstToken,op,e);
		}
		else if(t.kind == INTEGER_LITERAL || t.kind == LPAREN || functionNames.contains(t.kind) || t.kind == BOOLEAN_LITERAL){
			expr = primary();
		}
		else if(t.kind == IDENTIFIER){
			expr = identOrPixelSelectorExpression();
		}
		else if(unaryExpressionKeywords.contains(t.kind)){
			consume();
			expr = new Expression_PredefinedName(firstToken, firstToken.kind);
		}
		else 
			throw new SyntaxException(t,"UnaryExpressionNotPlusMinus Error. Position: " + t.pos_in_line + " in Line: " + t.line + " for token: " + t);
		return expr;
	}
	
	Expression unaryExpression() throws SyntaxException {
		Expression expr = null;
		Token firstToken = t;
		Token op = null;
		if(t.kind == OP_PLUS){
			op = t;
			consume();
			expr = unaryExpression();
		}
		else if(t.kind == OP_MINUS){
			op = t;
			consume();
			expr = unaryExpression();
		}
		else if(t.kind == OP_EXCL || t.kind == INTEGER_LITERAL || t.kind == LPAREN || functionNames.contains(t.kind) || 
				t.kind == IDENTIFIER || unaryExpressionKeywords.contains(t.kind) || t.kind == BOOLEAN_LITERAL){
			return unaryExpressionNotPlusMinus();
		}
		else 
			throw new SyntaxException(t,"UnaryExpression Error. Position: " + t.pos_in_line + " in Line: " + t.line + " for token: " + t);
		return new Expression_Unary(firstToken, op, expr);
	}
	
	Expression multExpression() throws SyntaxException {
		Token firstToken = t;
		Expression e0 = unaryExpression();
		Token op = null;
		Expression e1 = null;
		while((t.kind == OP_TIMES) || (t.kind == OP_DIV) || (t.kind == OP_MOD)){
			op = t;
			consume();
			e1 = unaryExpression();
			e0 = new Expression_Binary(firstToken, e0, op, e1);
		}
		return e0;
	}
	
	Expression addExpression() throws SyntaxException {
		Token firstToken = t;
		Expression e0 = multExpression();
		Token op = null;
		Expression e1 = null;
		while((t.kind == OP_PLUS) || (t.kind == OP_MINUS)){
			op = t;
			consume();
			e1 = multExpression();
			e0 = new Expression_Binary(firstToken, e0, op, e1);
		}
		return e0;
	}
	
	Expression relExpression() throws SyntaxException {
		Token firstToken = t;
		Expression e0 = addExpression();
		Token op = null;
		Expression e1 = null;
		while((t.kind == OP_LT) || (t.kind == OP_GT) || (t.kind == OP_LE) || (t.kind == OP_GE)){
			op = t;
			consume();
			e1 = addExpression();
			e0 = new Expression_Binary(firstToken, e0, op, e1);
		}
		return e0;
	}
	
	Expression eqExpression() throws SyntaxException {
		Token firstToken = t;
		Expression e0 = relExpression();
		Token op = null;
		Expression e1 = null;
		while((t.kind == OP_EQ) || (t.kind == OP_NEQ)){
			op = t;
			consume();
			e1 = relExpression();
			e0 = new Expression_Binary(firstToken, e0, op, e1);
		}
		return e0;
	}
	
	Expression andExpression() throws SyntaxException {
		Token firstToken = t;
		Expression e0 = eqExpression();
		Token op = null;
		Expression e1 = null;
		while(t.kind == OP_AND){
			op = t;
			consume();
			e1 = eqExpression();
			e0 = new Expression_Binary(firstToken, e0, op, e1);
		}
		return e0;
	}
	
	Expression orExpression() throws SyntaxException {
		Token firstToken = t;
		Expression e0 = andExpression();
		Token op = null;
		Expression e1 = null;
		while(t.kind == OP_OR){
			op = t;
			consume();
			e1 = andExpression();
			e0 = new Expression_Binary(firstToken, e0, op, e1);
		}
		return e0;
	}
	
	Statement assignmentStatement() throws SyntaxException {
			Token firstToken = t;
			LHS lhs = lhs();
			match(OP_ASSIGN);
			Expression expr = expression();
			return new Statement_Assign(firstToken,lhs, expr);
	}
	
	Statement imageInStatement() throws SyntaxException {
		Token firstToken = t;
		Source src = null;
		if(t.kind == IDENTIFIER){
			consume();
			match(OP_LARROW);
			src = source();
		}
		else
			throw new SyntaxException(t,"ImageInStatement Error. Position: " + t.pos_in_line + " in Line: " + t.line + " for token: " + t);
		return new Statement_In(firstToken, firstToken, src);
	}
	
	Sink sink() throws SyntaxException {
		Token firstToken = t;
		if(t.kind == IDENTIFIER || t.kind == KW_SCREEN){
			switch(t.kind){
			case IDENTIFIER: consume(); return new Sink_Ident(firstToken,firstToken); 
			case KW_SCREEN:  consume(); return new Sink_SCREEN(firstToken);
			default: return null;
			}			
		}
		else
			throw new SyntaxException(t,"Sink Error. Position: " + t.pos_in_line + " in Line: " + t.line + " for token: " + t);
	}
	
	Statement imageOutStatement() throws SyntaxException {
		Token firstToken = t;
		if(t.kind == IDENTIFIER){
			consume();
			match(OP_RARROW);
			Sink s = sink();
			return new Statement_Out(firstToken, firstToken, s);
		}
		else
			throw new SyntaxException(t,"ImageOutStatement Error. Position: " + t.pos_in_line + " in Line: " + t.line + " for token: " + t);
	}
	
	Statement statement() throws SyntaxException {
		Token firstToken = t;
		Statement stmt = null;
		match(IDENTIFIER);
		if(t.kind == OP_LARROW){
			consume();
			Source src = source();
			stmt = new Statement_In(firstToken, firstToken, src);
		}
		else if(t.kind == OP_RARROW){
			consume();
			Sink sink = sink();
			stmt = new Statement_Out(firstToken, firstToken, sink);
		}
		else if(t.kind == LSQUARE){
			consume();
			Index index = lhsSelector();
			match(RSQUARE);
			match(OP_ASSIGN);
			Expression expr = expression();
			stmt = new Statement_Assign(firstToken, new LHS(firstToken, firstToken, index), expr);
		}
		else if(t.kind == OP_ASSIGN){
			consume();
			Expression expr = expression();
			stmt = new Statement_Assign(firstToken, new LHS(firstToken, firstToken, null), expr);
		}
		else 
			throw new SyntaxException(t,"Statement Error. Position: " + t.pos_in_line + " in Line: " + t.line + " for token: " + t);
		return stmt;
	}
	
	Declaration_Image imageDeclaration() throws SyntaxException {
		Token firstToken = t;
		Expression xSize = null;
		Expression ySize = null;
		Token name = null;
		Source src = null;
		match(KW_image);
		if(t.kind == LSQUARE){
			consume();
			xSize = expression();
			match(COMMA);
			ySize = expression();
			match(RSQUARE);
		}
		name = t;
		match(IDENTIFIER);
		if(t.kind == OP_LARROW){
			consume();
			src = source();
		}
		return new Declaration_Image(firstToken,xSize,ySize,name,src);
	}
	
	void sourceSinkType() throws SyntaxException {
		if(t.kind == KW_url || t.kind == KW_file){
			consume();
		}
		else
			throw new SyntaxException(t,"SourceSinkType Error. Position: " + t.pos_in_line + " in Line: " + t.line + " for token: " + t);
	}
	
	Source source() throws SyntaxException{
		Token firstToken = t;
		Source s = null;
		if(t.kind == STRING_LITERAL || t.kind == IDENTIFIER){
			switch(t.kind){
				case STRING_LITERAL: s = new Source_StringLiteral(firstToken, firstToken.getText());
				break;
				
				case IDENTIFIER: s = new Source_Ident(firstToken, firstToken);
				break;
				
				default:
				break;
			}
			consume();
			return s;
		}
		else if(t.kind == OP_AT){
			consume();
			Expression e = expression();
			s = new Source_CommandLineParam(firstToken, e);
			return s;
		}
		else
			throw new SyntaxException(t,"Source Error. Position: " + t.pos_in_line + " in Line: " + t.line + " for token: " + t);
	}
	
	Declaration_SourceSink sourceSinkDeclaration() throws SyntaxException{
		Token firstToken = t;
		Token type = t;
		Token name = null;
		Source src = null;
		if(t.kind == KW_url || t.kind == KW_file){
			consume();
			name = t;
			match(IDENTIFIER);
			match(OP_ASSIGN);
			src = source();
		}
		else 
			throw new SyntaxException(t,"SourceSinkDeclaration Error. Position: " + t.pos_in_line + " in Line: " + t.line + " for token: " + t);
		return new Declaration_SourceSink(firstToken, type, name, src);
	}
	
	void varType() throws SyntaxException {
		if(t.kind == Kind.KW_int || t.kind == Kind.KW_boolean){
			consume();
		}
		else
			throw new SyntaxException(t,"VarType Error. Position: " + t.pos_in_line + " in Line: " + t.line + " for token: " + t);
	}
	
	Declaration_Variable variableDeclaration() throws SyntaxException {
			Token firstToken = t;
			Expression expr = null;
			varType();
			Token name = t;
			match(IDENTIFIER);
			if(t.kind == OP_ASSIGN){
				consume();
				expr = expression();
			}
			return new Declaration_Variable(firstToken, firstToken, name, expr);
	}
	
	Declaration declaration() throws SyntaxException {
		if(t.kind == KW_int || t.kind == KW_boolean){
			return variableDeclaration();
		}
		else if(t.kind == KW_image){
			return imageDeclaration();
		}
		else if(t.kind == KW_url || t.kind == KW_file){
			return sourceSinkDeclaration();
		}
		else
			throw new SyntaxException(t, "Declaration Error. Position: " + t.pos_in_line + " in Line: " + t.line + " for token: " + t);
	}
	

	
	 
	
	/**
	 * Only for check at end of program. Does not "consume" EOF so no attempt to get
	 * nonexistent next Token.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (t.kind == EOF) {
			return t;
		}
		String message =  "Expected EOL at " + t.line + ":" + t.pos_in_line + " " + t.kind;
		throw new SyntaxException(t, message);
	}
}
