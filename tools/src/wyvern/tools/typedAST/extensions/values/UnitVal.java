package wyvern.tools.typedAST.extensions.values;

import wyvern.tools.errors.FileLocation;
import wyvern.tools.typedAST.AbstractTypedAST;
import wyvern.tools.typedAST.AbstractValue;
import wyvern.tools.typedAST.CoreAST;
import wyvern.tools.typedAST.CoreASTVisitor;
import wyvern.tools.typedAST.Value;
import wyvern.tools.types.Environment;
import wyvern.tools.types.Type;
import wyvern.tools.types.extensions.Unit;
import wyvern.tools.util.TreeWriter;

public class UnitVal extends AbstractValue implements Value, CoreAST {
	private UnitVal(FileLocation location) { this.location = location; }
	// private static UnitVal instance = new UnitVal(); // FIXME: I have to move away from instance to provide line number! :(
	public static UnitVal getInstance(FileLocation fileLocation) {
		return new UnitVal(fileLocation); // instance; 
	}
	
	@Override
	public Type getType() {
		return Unit.getInstance();
	}
	
	@Override
	public void writeArgsToTree(TreeWriter writer) {
		// nothing to write		
	}
	
	@Override
	public String toString() {
		return "()";
	}

	@Override
	public void accept(CoreASTVisitor visitor) {
		visitor.visit(this);
	}

	private FileLocation location;
	public FileLocation getLocation() {
		return this.location;
	}
}
