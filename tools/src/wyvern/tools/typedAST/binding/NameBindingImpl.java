package wyvern.tools.typedAST.binding;

import wyvern.tools.errors.FileLocation;
import wyvern.tools.typedAST.TypedAST;
import wyvern.tools.typedAST.Value;
import wyvern.tools.typedAST.extensions.Variable;
import wyvern.tools.types.Environment;
import wyvern.tools.types.Type;

public class NameBindingImpl extends AbstractBinding implements NameBinding {
	public NameBindingImpl(String name, Type type) {
		super(name, type);
	}

	public TypedAST getUse() {
		// throw new RuntimeException("this method should not be needed!!!");
		return new Variable(this, FileLocation.UNKNOWN); // FIXME: !!! Cannot replicate its use from outside!
	}
	
	public Value getValue(Environment env) {
		// TODO: code smell, leaving in to make sure I don't need it.  Refactor to move down to ValueBinding and eliminate env parameter
		throw new RuntimeException("deprecated - to be removed");
		/*NameBinding bind = env.lookup(getName());
		assert bind instanceof ValueBinding;
		return ((ValueBinding)bind).getValue(env);*/
	}

}