package cop5556fa17;

import java.util.ArrayList;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

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
import cop5556fa17.AST.Source;
import cop5556fa17.AST.Source_CommandLineParam;
import cop5556fa17.AST.Source_Ident;
import cop5556fa17.AST.Source_StringLiteral;
import cop5556fa17.AST.Statement_In;
import cop5556fa17.AST.Statement_Out;
import jdk.nashorn.internal.ir.Flags;
import cop5556fa17.AST.Statement_Assign;
//import cop5556fa17.image.ImageFrame;
//import cop5556fa17.image.ImageSupport;

public class CodeGenVisitor implements ASTVisitor, Opcodes {

	/**
	 * All methods and variable static.
	 */


	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 */
	public CodeGenVisitor(boolean DEVEL, boolean GRADE, String sourceFileName) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
	}

	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;

	MethodVisitor mv; // visitor of method currently under construction

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;
	


	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		className = program.name;  
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object", null);
		cw.visitSource(sourceFileName, null);
		// create main method
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
		// initialize
		mv.visitCode();		
		//add label before first instruction
		Label mainStart = new Label();
		mv.visitLabel(mainStart);		
		// if GRADE, generates code to add string to log
		//CodeGenUtils.genLog(GRADE, mv, "entering main");

		// visit decs and statements to add field to class
		//  and instructions to main method, respectivley
		ArrayList<ASTNode> decsAndStatements = program.decsAndStatements;
		for (ASTNode node : decsAndStatements) {
			node.visit(this, arg);
		}

		//generates code to add string to log
		//CodeGenUtils.genLog(GRADE, mv, "leaving main");
		
		//adds the required (by the JVM) return statement to main
		mv.visitInsn(RETURN);
		
		//adds label at end of code
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		
		//handles parameters and local variables of main. Right now, only args
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);

		//Sets max stack size and number of local vars.
		//Because we use ClassWriter.COMPUTE_FRAMES as a parameter in the constructor,
		//asm will calculate this itself and the parameters are ignored.
		//If you have trouble with failures in this routine, it may be useful
		//to temporarily set the parameter in the ClassWriter constructor to 0.
		//The generated classfile will not be correct, but you will at least be
		//able to see what is in it.
		mv.visitMaxs(0, 0); 
		
		//terminate construction of main method
		mv.visitEnd();
		
		//terminate class construction
		cw.visitEnd();

		//generate classfile as byte array and return
		return cw.toByteArray();
	}

	@Override
	public Object visitDeclaration_Variable(Declaration_Variable declaration_Variable, Object arg) throws Exception {
		// TODO 
		
		switch(declaration_Variable.varType){
			case INTEGER: {
						  cw.visitField(ACC_STATIC, declaration_Variable.name, "I", null, new Integer(0)).visitEnd();
				  		  if(declaration_Variable.e != null){
							 declaration_Variable.e.visit(this, arg);
							 mv.visitFieldInsn(PUTSTATIC, className, declaration_Variable.name, "I");
						  }
						  break;
						  }
			
			case BOOLEAN: {
						  cw.visitField(ACC_STATIC, declaration_Variable.name, "Z",null, new Boolean(false)).visitEnd();
						  if(declaration_Variable.e != null){
							 declaration_Variable.e.visit(this, arg);
							 mv.visitFieldInsn(PUTSTATIC, className, declaration_Variable.name, "Z");
						  }
						  break;
						  }
			
			default:
				throw new UnsupportedOperationException();
		}
		return null;		
	}

	@Override
	public Object visitExpression_Binary(Expression_Binary expression_Binary, Object arg) throws Exception {
		// TODO 
		
		Label trueLabel = new Label();
		Label falseLabel = new Label();
		Label endLabel = new Label();
		
		expression_Binary.e0.visit(this, arg);
		expression_Binary.e1.visit(this, arg);
		
		switch(expression_Binary.op){
			case OP_PLUS:	mv.visitInsn(IADD); mv.visitJumpInsn(GOTO, endLabel);
							break;
							
			case OP_MINUS:	mv.visitInsn(ISUB); mv.visitJumpInsn(GOTO, endLabel);
							break;
							
			case OP_TIMES:	mv.visitInsn(IMUL); mv.visitJumpInsn(GOTO, endLabel);
							break;
							
			case OP_DIV:	mv.visitInsn(IDIV); mv.visitJumpInsn(GOTO, endLabel);
							break;
						 
			case OP_MOD: 	mv.visitInsn(IREM); mv.visitJumpInsn(GOTO, endLabel);
							break;
							
//			case OP_POWER:	mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "pow", "(DD)D");
//							mv.visitInsn(D2I);
//							mv.visitJumpInsn(GOTO, endLabel);
//							break;
			
			case OP_EQ:		mv.visitJumpInsn(IF_ICMPEQ, trueLabel);
							mv.visitJumpInsn(GOTO, falseLabel);
							break;

			case OP_NEQ:	mv.visitJumpInsn(IF_ICMPNE, trueLabel);
							mv.visitJumpInsn(GOTO, falseLabel);
							break;
							
			case OP_LT:		mv.visitJumpInsn(IF_ICMPLT, trueLabel);
							mv.visitJumpInsn(GOTO, falseLabel);
							break;
							
			case OP_LE:		mv.visitJumpInsn(IF_ICMPLE, trueLabel);
							mv.visitJumpInsn(GOTO, falseLabel);
							break;
			
			case OP_GT:		mv.visitJumpInsn(IF_ICMPGT, trueLabel);
							mv.visitJumpInsn(GOTO, falseLabel);
							break;
			
			case OP_GE:		mv.visitJumpInsn(IF_ICMPGE, trueLabel);
							mv.visitJumpInsn(GOTO, falseLabel);
							break;
			
			case OP_OR:		mv.visitInsn(IOR); mv.visitJumpInsn(GOTO, endLabel);
							break;
							
			case OP_AND:	mv.visitInsn(IAND); mv.visitJumpInsn(GOTO, endLabel);
							break;	
							
			default:	throw new UnsupportedOperationException();
					
							
		}
		
		mv.visitLabel(trueLabel);
		mv.visitLdcInsn(new Boolean(true));
		mv.visitJumpInsn(GOTO, endLabel);
		mv.visitLabel(falseLabel);
		mv.visitLdcInsn(new Boolean(false));
		mv.visitLabel(endLabel);
		//CodeGenUtils.genLogTOS(GRADE, mv, expression_Binary.getType());
		return null;
	}

	@Override
	public Object visitExpression_Unary(Expression_Unary expression_Unary, Object arg) throws Exception {
		// TODO
		switch(expression_Unary.op){
			case OP_PLUS:	expression_Unary.e.visit(this, arg);
							break;
				
			case OP_MINUS:	expression_Unary.e.visit(this, arg);
							mv.visitInsn(INEG);
							break;
				
			case OP_EXCL:	expression_Unary.e.visit(this, arg);
							if(expression_Unary.varType == Type.BOOLEAN){
								mv.visitLdcInsn(true);
								mv.visitInsn(IXOR);
							}
							else{
								mv.visitLdcInsn(new Integer(Integer.MAX_VALUE));
								mv.visitInsn(IXOR);
							}
							break;
			
			default:		throw new UnsupportedOperationException();
							
		}
		//CodeGenUtils.genLogTOS(GRADE, mv, expression_Unary.getType());
		return null;
	}

	// generate code to leave the two values on the stack
	@Override
	public Object visitIndex(Index index, Object arg) throws Exception {
		// TODO HW6
		index.e0.visit(this, arg);
		index.e1.visit(this, arg);
		if(!index.isCartesian()){
			mv.visitInsn(DUP2);
			mv.visitFieldInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_x", RuntimeFunctions.cart_xSig);
			mv.visitInsn(DUP_X2);
			mv.visitInsn(POP);
			mv.visitFieldInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_y", RuntimeFunctions.cart_ySig);
		}
		return null;
	}

	@Override
	public Object visitExpression_PixelSelector(Expression_PixelSelector expression_PixelSelector, Object arg)
			throws Exception {
		// TODO HW6
		mv.visitFieldInsn(GETSTATIC, className, expression_PixelSelector.name, ImageSupport.ImageClassName);
		if(expression_PixelSelector.index != null)
			expression_PixelSelector.index.visit(this, arg);
		mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getPixel", ImageSupport.getPixelSig, false);
		return null;
	}

	@Override
	public Object visitExpression_Conditional(Expression_Conditional expression_Conditional, Object arg)
			throws Exception {
		// TODO 
		expression_Conditional.condition.visit(this, arg);
		Label trueExpressionLabel  = new Label();
		mv.visitJumpInsn(IFNE, trueExpressionLabel);
		expression_Conditional.falseExpression.visit(this, arg);
		Label goToEnd = new Label();
		mv.visitJumpInsn(GOTO, goToEnd);
		mv.visitLabel(trueExpressionLabel);
		expression_Conditional.trueExpression.visit(this, arg);
		mv.visitLabel(goToEnd);
		//CodeGenUtils.genLogTOS(GRADE, mv, expression_Conditional.trueExpression.getType());
		return null;
	}


	@Override
	public Object visitDeclaration_Image(Declaration_Image declaration_Image, Object arg) throws Exception {
		cw.visitField(ACC_STATIC, declaration_Image.name, ImageSupport.ImageDesc, null, null);
		if(declaration_Image.source != null){
			declaration_Image.source.visit(this, arg);
		}
		if(declaration_Image.xSize == null && declaration_Image.ySize == null){
			mv.visitFieldInsn(GETSTATIC, ImageSupport.className, "defX", "I");
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
			mv.visitFieldInsn(GETSTATIC, ImageSupport.className, "defY", "I");
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
		}
		else{
			declaration_Image.xSize.visit(this, arg);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
			declaration_Image.ySize.visit(this, arg);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
		}
		if(declaration_Image.source != null){
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "readImage", ImageSupport.readImageSig, false);
			mv.visitFieldInsn(PUTSTATIC, className, declaration_Image.name, ImageSupport.ImageDesc);
		}
		else
		{
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "makeImage", ImageSupport.makeImageSig, false);
			mv.visitFieldInsn(PUTSTATIC, className, declaration_Image.name, ImageSupport.ImageDesc);
		}
		return null;
	}
	
  
	@Override
	public Object visitSource_StringLiteral(Source_StringLiteral source_StringLiteral, Object arg) throws Exception {
		mv.visitLdcInsn(source_StringLiteral.fileOrUrl);
		return null;
	}

	

	@Override
	public Object visitSource_CommandLineParam(Source_CommandLineParam source_CommandLineParam, Object arg)
			throws Exception {
		// TODO 
		mv.visitVarInsn(ALOAD,0);
		source_CommandLineParam.paramNum.visit(this, arg);
		mv.visitInsn(AALOAD);
		return null;
	}

	@Override
	public Object visitSource_Ident(Source_Ident source_Ident, Object arg) throws Exception {
		// TODO HW6
		mv.visitFieldInsn(GETSTATIC, className, source_Ident.name, "Ljava/lang/String;");
		return null;
	}


	@Override
	public Object visitDeclaration_SourceSink(Declaration_SourceSink declaration_SourceSink, Object arg) throws Exception {
		cw.visitField(ACC_STATIC, declaration_SourceSink.name, "Ljava/lang/String;", null, null);
		if(declaration_SourceSink.source != null)
			declaration_SourceSink.source.visit(this, arg);
		mv.visitFieldInsn(PUTSTATIC, className, declaration_SourceSink.name, "Ljava/lang/String;");
		return null;
	}
	


	@Override
	public Object visitExpression_IntLit(Expression_IntLit expression_IntLit, Object arg) throws Exception {
		// TODO 
		mv.visitLdcInsn(new Integer(expression_IntLit.value));
		//CodeGenUtils.genLogTOS(GRADE, mv, Type.INTEGER);
		return null;
	}

	@Override
	public Object visitExpression_FunctionAppWithExprArg(
			Expression_FunctionAppWithExprArg expression_FunctionAppWithExprArg, Object arg) throws Exception {
		expression_FunctionAppWithExprArg.arg.visit(this, arg);
		switch(expression_FunctionAppWithExprArg.function){
		case KW_abs: mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "abs", RuntimeFunctions.absSig, false);
		break;
		
		case KW_log: mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "log", RuntimeFunctions.logSig, false);
		break;
		
		case KW_sin: mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "sin", RuntimeFunctions.sinSig, false);
		break;
		
		case KW_cos: mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cos", RuntimeFunctions.cosSig, false);
		break;
		
		case KW_atan: mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "atan", RuntimeFunctions.atanSig, false);
		break;
		
		default:
			break;
		}
		return null;
	}

	@Override
	public Object visitExpression_FunctionAppWithIndexArg(
			Expression_FunctionAppWithIndexArg expression_FunctionAppWithIndexArg, Object arg) throws Exception {
		expression_FunctionAppWithIndexArg.arg.visit(this, arg);
		switch(expression_FunctionAppWithIndexArg.function){
			case KW_cart_x:
				mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_x", RuntimeFunctions.cart_xSig);
				break;
			case KW_cart_y:
				mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_y", RuntimeFunctions.cart_ySig);
				break;
			case KW_polar_r:
				mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_r", RuntimeFunctions.polar_rSig);
				break;
			case KW_polar_a:
				mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_a", RuntimeFunctions.polar_aSig);
				break;
		}
		return null;
	}

	@Override
	public Object visitExpression_PredefinedName(Expression_PredefinedName expression_PredefinedName, Object arg)
			throws Exception {
		// TODO HW6
		
		throw new UnsupportedOperationException();
	}

	/** For Integers and booleans, the only "sink"is the screen, so generate code to print to console.
	 * For Images, load the Image onto the stack and visit the Sink which will generate the code to handle the image.
	 */
	@Override
	public Object visitStatement_Out(Statement_Out statement_Out, Object arg) throws Exception {
		// TODO in HW5:  only INTEGER and BOOLEAN
		// TODO HW6 remaining cases
		String fieldType; 
		if(statement_Out.getDec().varType == Type.INTEGER)
			fieldType = "I";
		else if(statement_Out.getDec().varType == Type.BOOLEAN)
			fieldType = "Z";
		else if(statement_Out.getDec().varType == Type.IMAGE){
			mv.visitFieldInsn(GETSTATIC, className, statement_Out.name, ImageSupport.ImageDesc);
			statement_Out.sink.visit(this, arg);
			return null;
		}
		else
			throw new UnsupportedOperationException();
		mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
		mv.visitFieldInsn(GETSTATIC, className, statement_Out.name, fieldType);
		CodeGenUtils.genLogTOS(GRADE, mv, statement_Out.getDec().varType);
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(" + fieldType + ")V", false);
		return null;
		
	}

	/**
	 * Visit source to load rhs, which will be a String, onto the stack
	 * 
	 *  In HW5, you only need to handle INTEGER and BOOLEAN
	 *  Use java.lang.Integer.parseInt or java.lang.Boolean.parseBoolean 
	 *  to convert String to actual type. 
	 *  
	 *  TODO HW6 remaining types
	 */
	@Override
	public Object visitStatement_In(Statement_In statement_In, Object arg) throws Exception {
		// TODO (see comment )
		String fieldType;
		String fieldClass;
		String methodName;
		if(statement_In.getDec().varType == Type.INTEGER){
			fieldType = "I";
			fieldClass = "java/lang/Integer";
			methodName = "parseInt";
		}
		else if(statement_In.getDec().varType == Type.BOOLEAN){
			fieldType = "Z";
			fieldClass = "java/lang/Boolean";
			methodName = "parseBoolean";
		}
		else if(statement_In.getDec().varType == Type.IMAGE){
			
			statement_In.source.visit(this, arg);
			
			return null;
		}
		else
			throw new UnsupportedOperationException();
		statement_In.source.visit(this, arg);
		mv.visitMethodInsn(INVOKESTATIC, fieldClass, methodName, "(Ljava/lang/String;)"+fieldType, false);
		mv.visitFieldInsn(PUTSTATIC, className, statement_In.name, fieldType);
		return null;
	}

	
	/**
	 * In HW5, only handle INTEGER and BOOLEAN types.
	 */
	public Object visitStatement_Assign(Statement_Assign statement_Assign, Object arg) throws Exception {
		//TODO  (see comment)
		if(statement_Assign.lhs.varType == Type.IMAGE){
			Label firstLabel = new Label();
			Label secondLabel = new Label();
			Label endLabel1 = new Label();
			Label endLabel2 = new Label();
			mv.visitInsn(ICONST_0);
			mv.visitFieldInsn(GETSTATIC, className, statement_Assign.lhs.name, ImageSupport.ImageDesc);
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getX", ImageSupport.getXSig, false);
			mv.visitLabel(firstLabel);
			mv.visitJumpInsn(IF_ICMPGE, endLabel1);
			mv.visitInsn(ICONST_0);
			mv.visitFieldInsn(GETSTATIC, className, statement_Assign.lhs.name, ImageSupport.ImageDesc);
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getY", ImageSupport.getYSig, false);
			mv.visitLabel(secondLabel);
			mv.visitJumpInsn(IF_ICMPGE, endLabel2);
			statement_Assign.e.visit(this, arg);
			statement_Assign.lhs.visit(this, arg);
			mv.visitInsn(ICONST_1);
			mv.visitInsn(IADD);
			mv.visitJumpInsn(GOTO, secondLabel);
			mv.visitInsn(POP);
			mv.visitInsn(ICONST_1);
			mv.visitInsn(IADD);
			mv.visitJumpInsn(GOTO, firstLabel);
			mv.visitLabel(endLabel2);
			
		}
		else {
			statement_Assign.e.visit(this, arg);
			statement_Assign.lhs.visit(this, arg);
		}
		return null;
	}

	/**
	 * In HW5, only handle INTEGER and BOOLEAN types.
	 */
	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {
		//TODO  (see comment)
		switch(lhs.varType){
			case INTEGER:
				mv.visitFieldInsn(PUTSTATIC, className, lhs.name, "I");
				break;
				
			case BOOLEAN:
				mv.visitFieldInsn(PUTSTATIC, className, lhs.name, "Z");
				break;
				
			case IMAGE:
				mv.visitFieldInsn(GETSTATIC, className, lhs.name, ImageSupport.ImageDesc);
				if(lhs.index != null)
					lhs.index.visit(this, arg);
				mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getPixel", ImageSupport.getPixelSig, false);
				
				break;
			default:
				throw new UnsupportedOperationException();
		}
		return null;
	}
	

	@Override
	public Object visitSink_SCREEN(Sink_SCREEN sink_SCREEN, Object arg) throws Exception {
		//TODO HW6
		mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "makeFrame", ImageSupport.makeFrameSig, false);
		mv.visitInsn(POP);
		return null;
	}

	@Override
	public Object visitSink_Ident(Sink_Ident sink_Ident, Object arg) throws Exception {
		//TODO HW6
		mv.visitFieldInsn(GETSTATIC, className, sink_Ident.name, "Ljava/lang/String;");
		mv.visitMethodInsn(GETSTATIC, ImageSupport.className, "write", ImageSupport.writeSig, false);
		return null;
	}

	@Override
	public Object visitExpression_BooleanLit(Expression_BooleanLit expression_BooleanLit, Object arg) throws Exception {
		//TODO
		mv.visitLdcInsn(new Boolean(expression_BooleanLit.value));
		//CodeGenUtils.genLogTOS(GRADE, mv, Type.BOOLEAN);
		return null;
	}

	@Override
	public Object visitExpression_Ident(Expression_Ident expression_Ident,
			Object arg) throws Exception {
		//TODO
		Type t = expression_Ident.getType();
		switch(t){
			case INTEGER:
				mv.visitFieldInsn(GETSTATIC, className, expression_Ident.name, "I");
				break;
				
			case BOOLEAN:
				mv.visitFieldInsn(GETSTATIC, className, expression_Ident.name, "Z");
				break;
			
			default:
				break;
		}
		//CodeGenUtils.genLogTOS(GRADE, mv, expression_Ident.getType());
		return null;
	}

}
