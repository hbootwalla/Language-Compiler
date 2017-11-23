package cop5556fa17;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import cop5556fa17.Scanner.Kind;
import cop5556fa17.Scanner.Token;
import cop5556fa17.TypeUtils.Type;
import cop5556fa17.AST.ASTNode;
import cop5556fa17.AST.ASTVisitor;
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
import cop5556fa17.AST.Sink_Ident;
import cop5556fa17.AST.Sink_SCREEN;
import cop5556fa17.AST.Source_CommandLineParam;
import cop5556fa17.AST.Source_Ident;
import cop5556fa17.AST.Source_StringLiteral;
import cop5556fa17.AST.Statement_Assign;
import cop5556fa17.AST.Statement_In;
import cop5556fa17.AST.Statement_Out;

public class TypeCheckVisitor implements ASTVisitor {
	
	
		public HashMap<String, Declaration> symbolTable = new HashMap<>();

		@SuppressWarnings("serial")
		public static class SemanticException extends Exception {
			Token t;

			public SemanticException(Token t, String message) {
				super("line " + t.line + " pos " + t.pos_in_line + ": "+  message);
				this.t = t;
			}

		}		
		

	
	/**
	 * The program name is only used for naming the class.  It does not rule out
	 * variables with the same name.  It is returned for convenience.
	 * 
	 * @throws Exception 
	 */
	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		for (ASTNode node: program.decsAndStatements) {
			node.visit(this, arg);
		}
		return program.name;
	}

	@Override
	public Object visitDeclaration_Variable(
			Declaration_Variable declaration_Variable, Object arg)
			throws Exception {
		if(declaration_Variable.e != null){
			declaration_Variable.e.visit(this, arg);
		}
		String name = declaration_Variable.name;
		if(!symbolTable.containsKey(name)){
			symbolTable.put(name, declaration_Variable);
			declaration_Variable.varType = TypeUtils.getType(declaration_Variable.type);
			if(declaration_Variable.e != null && declaration_Variable.e.varType != declaration_Variable.varType)
				throw new SemanticException(declaration_Variable.firstToken, "Expression is not of the same type as variable");
			return null;
		}
		else
			throw new SemanticException(declaration_Variable.firstToken, "Redeclaring Variables is not allowed");
	}

	@Override
	public Object visitExpression_Binary(Expression_Binary expression_Binary,
			Object arg) throws Exception {
		expression_Binary.e0.visit(this, arg);
		expression_Binary.e1.visit(this, arg);
		Kind op = expression_Binary.op;
		if(expression_Binary.e0.varType == expression_Binary.e1.varType){
			if(op == Kind.OP_EQ || op == Kind.OP_NEQ){
				expression_Binary.varType = Type.BOOLEAN;
			}
			else if((op == Kind.OP_GE || op == Kind.OP_GT || op == Kind.OP_LT || op == Kind.OP_LE) && expression_Binary.e0.varType == Type.INTEGER){
				expression_Binary.varType = Type.BOOLEAN;
			}
			else if((op == Kind.OP_AND || op == Kind.OP_OR) && (expression_Binary.e0.varType == Type.INTEGER || expression_Binary.e0.varType == Type.BOOLEAN)){
				expression_Binary.varType = expression_Binary.e0.varType;
			}
			else if((op == Kind.OP_DIV || op == Kind.OP_TIMES || op == Kind.OP_PLUS || op == Kind.OP_MINUS || op == Kind.OP_MOD || op == Kind.OP_POWER)
					&& (expression_Binary.e0.varType == Type.INTEGER)){
				expression_Binary.varType = Type.INTEGER; 
			}
			else{
				expression_Binary.varType = null;
			}
			if(expression_Binary.varType == null)
				throw new SemanticException(expression_Binary.firstToken, "Binary Expression has invalid type");
			
		}else{
			throw new SemanticException(expression_Binary.firstToken, "Both expressions should have the same type");
		}
		return null;
	}

	@Override
	public Object visitExpression_Unary(Expression_Unary expression_Unary,
			Object arg) throws Exception {
		expression_Unary.e.visit(this, arg);
		Type t = expression_Unary.e.varType;
		if(expression_Unary.op == Kind.OP_EXCL && (t == Type.BOOLEAN || t == Type.INTEGER)){
			expression_Unary.varType = t;
		}else if((expression_Unary.op == Kind.OP_PLUS || expression_Unary.op == Kind.OP_MINUS) && t == Type.INTEGER){
			expression_Unary.varType = Type.INTEGER;	
		}else{
			expression_Unary.varType = null;
		}
		if(expression_Unary.varType == null){
			throw new SemanticException(expression_Unary.firstToken, "Unary Expressions can either be Integer or Boolean type");
		}
		return null;
	}

	@Override
	public Object visitIndex(Index index, Object arg) throws Exception {
		index.e0.visit(this, arg);
		index.e1.visit(this, arg);
		if(index.e0.varType == Type.INTEGER && index.e1.varType == Type.INTEGER){
			if(index.e0.getClass() == Expression_PredefinedName.class && index.e1.getClass() == Expression_PredefinedName.class){
				Expression_PredefinedName pd1 = (Expression_PredefinedName)index.e0;
				Expression_PredefinedName pd2 = (Expression_PredefinedName)index.e1;
				
				index.setCartesian(!(pd1.kind == Kind.KW_r && pd2.kind == Kind.KW_a));
			}
			return null;
		}
		else
			throw new SemanticException(index.firstToken, "Index Expressions should be of integer types.");
	}

	@Override
	public Object visitExpression_PixelSelector(
			Expression_PixelSelector expression_PixelSelector, Object arg)
			throws Exception {
		Declaration decl = symbolTable.get(expression_PixelSelector.name);
		if(decl == null){
			throw new SemanticException(expression_PixelSelector.firstToken, "Variables have to be declared before being used.");
		}
		if(expression_PixelSelector.index != null)
			expression_PixelSelector.index.visit(this, arg);
		Type nameType = decl.varType;
		if(nameType == Type.IMAGE){
			expression_PixelSelector.varType = Type.INTEGER;
		}
		else if(expression_PixelSelector.index == null){
			expression_PixelSelector.varType = nameType;
		}
		else{
			expression_PixelSelector.varType = null;
		}
		if(expression_PixelSelector.varType == null){
			throw new SemanticException(expression_PixelSelector.firstToken, "Pixel Selector Type cannot be null");
		}
		return null;
	}

	@Override
	public Object visitExpression_Conditional(
			Expression_Conditional expression_Conditional, Object arg)
			throws Exception {
		expression_Conditional.condition.visit(this, arg);
		expression_Conditional.trueExpression.visit(this, arg);
		expression_Conditional.falseExpression.visit(this, arg);
		if(expression_Conditional.condition.varType != Type.BOOLEAN || (expression_Conditional.trueExpression.varType != expression_Conditional.falseExpression.varType)){
			throw new SemanticException(expression_Conditional.firstToken, "Conditional Expression should be return boolean or true and false expression should be of same type.");
		}
		expression_Conditional.varType = expression_Conditional.trueExpression.varType;
		return null;		
	}

	@Override
	public Object visitDeclaration_Image(Declaration_Image declaration_Image,
			Object arg) throws Exception {
		String name = declaration_Image.name;
		if(declaration_Image.source != null){
			declaration_Image.source.visit(this, arg);
		}
		if(!symbolTable.containsKey(name)){
			symbolTable.put(name, declaration_Image);
			declaration_Image.varType = TypeUtils.Type.IMAGE;
			if(declaration_Image.xSize != null && declaration_Image.ySize != null){
				declaration_Image.xSize.visit(this, arg);
				declaration_Image.ySize.visit(this, arg);
				if(declaration_Image.xSize.varType != Type.INTEGER || declaration_Image.ySize.varType != Type.INTEGER){
					throw new SemanticException(declaration_Image.firstToken, "XSize and YSize have to yield Integers.");
				}
			}
			else if(declaration_Image.xSize != null && declaration_Image.ySize == null){
				throw new SemanticException(declaration_Image.firstToken,"No Y specification for corresponding XSize");
			}
			
			return null;
		}
		else
			throw new SemanticException(declaration_Image.firstToken, "Redeclaring Variables is not allowed");
	}

	@Override
	public Object visitSource_StringLiteral(
			Source_StringLiteral source_StringLiteral, Object arg)
			throws Exception {
		if(isValidURL(source_StringLiteral.fileOrUrl)){
			source_StringLiteral.varType = Type.URL;
		}
		else{
			source_StringLiteral.varType = Type.FILE;
		}
		return null;
	}

	private boolean isValidURL(String fileOrUrl) {
		try{
			URL url = new URL(fileOrUrl);
			return true;
		}
		catch(MalformedURLException mue){
			return false;
		}
		
	}

	@Override
	public Object visitSource_CommandLineParam(
			Source_CommandLineParam source_CommandLineParam, Object arg)
			throws Exception {
		source_CommandLineParam.paramNum.visit(this, arg);
		source_CommandLineParam.varType = source_CommandLineParam.paramNum.varType;
		if(source_CommandLineParam.varType != Type.INTEGER){
			throw new SemanticException(source_CommandLineParam.firstToken, "Command Line Paramater Type should be an Integer");
		}
		return null;
	}

	@Override
	public Object visitSource_Ident(Source_Ident source_Ident, Object arg)
			throws Exception {
		Declaration decl = symbolTable.get(source_Ident.name);
		if(decl == null){
			throw new SemanticException(source_Ident.firstToken, "Variables have to be declared before being used.");
		} 
		source_Ident.varType = decl.varType;
		if(source_Ident.varType != Type.FILE && source_Ident.varType != Type.URL){
			throw new SemanticException(source_Ident.firstToken, "Source Identifier Type has to be of File or Url Type");
		}
		return null;
	}

	@Override
	public Object visitDeclaration_SourceSink(
			Declaration_SourceSink declaration_SourceSink, Object arg)
			throws Exception {
		String name = declaration_SourceSink.name;
		if(declaration_SourceSink.source != null)
			declaration_SourceSink.source.visit(this, arg);
		if(!symbolTable.containsKey(name)){
			symbolTable.put(name, declaration_SourceSink);
			declaration_SourceSink.varType = TypeUtils.getType(declaration_SourceSink.type);
			if(declaration_SourceSink.source.varType != declaration_SourceSink.varType){
				throw new SemanticException(declaration_SourceSink.firstToken,"Source/Sink type should match declaration type.");
			}
			return null;
		}
		else
			throw new SemanticException(declaration_SourceSink.firstToken, "Redeclaring Variables is not allowed");
	}

	@Override
	public Object visitExpression_IntLit(Expression_IntLit expression_IntLit,
			Object arg) throws Exception {
		expression_IntLit.varType = Type.INTEGER;
		return null;
	}

	@Override
	public Object visitExpression_FunctionAppWithExprArg(
			Expression_FunctionAppWithExprArg expression_FunctionAppWithExprArg,
			Object arg) throws Exception {
		expression_FunctionAppWithExprArg.arg.visit(this, arg);
		if(expression_FunctionAppWithExprArg.arg.varType == Type.INTEGER){
			expression_FunctionAppWithExprArg.varType = Type.INTEGER;
			return null;
		}
		else
			throw new SemanticException(expression_FunctionAppWithExprArg.firstToken, "Function Argument has to be of type Integer");
	}

	@Override
	public Object visitExpression_FunctionAppWithIndexArg(
			Expression_FunctionAppWithIndexArg expression_FunctionAppWithIndexArg,
			Object arg) throws Exception {
		if(expression_FunctionAppWithIndexArg.arg != null)
			expression_FunctionAppWithIndexArg.arg.visit(this, arg);
		expression_FunctionAppWithIndexArg.varType = Type.INTEGER;
		return null;
	}

	@Override
	public Object visitExpression_PredefinedName(
			Expression_PredefinedName expression_PredefinedName, Object arg)
			throws Exception {
		expression_PredefinedName.varType = Type.INTEGER;
		return null;
	}

	@Override
	public Object visitStatement_Out(Statement_Out statement_Out, Object arg)
			throws Exception {
		Declaration decl = symbolTable.get(statement_Out.name);
		if(decl == null){
			throw new SemanticException(statement_Out.firstToken, "Variable has to be declared before use");
		}
		statement_Out.setDec(decl);
		statement_Out.sink.visit(this, arg);
		if(((decl.varType == Type.INTEGER || decl.varType==Type.BOOLEAN) && statement_Out.sink.varType==Type.SCREEN) || (decl.varType==Type.IMAGE && (statement_Out.sink.varType ==Type.FILE || statement_Out.sink.varType == Type.SCREEN)))
		{
			
		}
		else{
			throw new SemanticException(statement_Out.firstToken, "Sink and Variable Types do not match");
		}
		return null;
	}

	@Override
	public Object visitStatement_In(Statement_In statement_In, Object arg)
			throws Exception {
		Declaration decl = symbolTable.get(statement_In.name);
		if(decl == null){
			throw new SemanticException(statement_In.firstToken, "Variable has to be declared before use");
		}
		statement_In.setDec(decl);
		statement_In.source.visit(this, arg);
//		if(decl.varType != statement_In.source.varType){
//			throw new SemanticException(statement_In.firstToken, "Variable and Source Types do not match");
//		}
		return null;
	}

	@Override
	public Object visitStatement_Assign(Statement_Assign statement_Assign,
			Object arg) throws Exception {
		LHS lhs = statement_Assign.lhs;
		Expression e = statement_Assign.e;
		if(lhs != null)
			lhs.visit(this, arg);
		if(e != null)
			e.visit(this, arg);
		if(lhs.varType != e.varType){
			throw new SemanticException(statement_Assign.firstToken, "Type of LHS and Expression should be the same.");
		}
		statement_Assign.setCartesian(lhs.isCartesian()); 
		return null;
	}

	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {
		Declaration dec = (Declaration)symbolTable.get(lhs.name);
		if(dec == null)
			throw new SemanticException(lhs.firstToken, "Variable is not declared");
		lhs.declaration = dec;
		lhs.varType = dec.varType;
		if(lhs.index != null){
			lhs.index.visit(this, arg);
			lhs.setCartesian(lhs.index.isCartesian());
		}
		else
			lhs.setCartesian(false);
		return null;
	}

	@Override
	public Object visitSink_SCREEN(Sink_SCREEN sink_SCREEN, Object arg)
			throws Exception {
		sink_SCREEN.varType = Type.SCREEN;
		return null;
	}

	@Override
	public Object visitSink_Ident(Sink_Ident sink_Ident, Object arg)
			throws Exception {
		Declaration decl = symbolTable.get(sink_Ident.name);
		if(decl == null)
			throw new SemanticException(sink_Ident.firstToken, "Variable is not declared");
		sink_Ident.varType = decl.varType;
		if(sink_Ident.varType != Type.FILE){
			throw new SemanticException(sink_Ident.firstToken, "Sink Type has to be of File Type");
		}
		return null;
	}

	@Override
	public Object visitExpression_BooleanLit(
			Expression_BooleanLit expression_BooleanLit, Object arg)
			throws Exception {
		expression_BooleanLit.varType = Type.BOOLEAN;
		return null;
	}

	@Override
	public Object visitExpression_Ident(Expression_Ident expression_Ident,
			Object arg) throws Exception {
		
		Declaration decl = symbolTable.get(expression_Ident.name);
		if(decl == null){
			throw new SemanticException(expression_Ident.firstToken, "Variable has to be declared before being used.");
		}
		expression_Ident.varType = decl.varType;
		return null;
	}

}
