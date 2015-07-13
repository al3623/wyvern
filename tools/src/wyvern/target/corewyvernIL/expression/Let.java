package wyvern.target.corewyvernIL.expression;

import wyvern.target.corewyvernIL.Environment;
import wyvern.target.corewyvernIL.astvisitor.ASTVisitor;
import wyvern.target.corewyvernIL.astvisitor.EmitOIRVisitor;
import wyvern.target.corewyvernIL.type.ValueType;
import wyvern.target.oir.OIRAST;
import wyvern.target.oir.OIREnvironment;

public class Let extends Expression{

	private String varName;
	private Expression toReplace;
	private Expression inExpr;
	
	public Let(String varName, Expression toReplace, Expression inExpr) {
		super();
		this.varName = varName;
		this.toReplace = toReplace;
		this.inExpr = inExpr;
	}

	public String getVarName() {
		return varName;
	}
	
	public void setVarName(String varName) {
		this.varName = varName;
	}
	
	public Expression getToReplace() {
		return toReplace;
	}
	
	public void setToReplace(Expression toReplace) {
		this.toReplace = toReplace;
	}
	
	public Expression getInExpr() {
		return inExpr;
	}
	
	public void setInExpr(Expression inExpr) {
		this.inExpr = inExpr;
	}

	@Override
	public ValueType typeCheck(wyvern.tools.types.Environment env) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T acceptVisitor(ASTVisitor <T> emitILVisitor,
			Environment env, OIREnvironment oirenv) {
		return emitILVisitor.visit(env, oirenv, this);
	}
}
