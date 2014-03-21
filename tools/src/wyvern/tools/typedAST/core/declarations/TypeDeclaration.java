package wyvern.tools.typedAST.core.declarations;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import wyvern.tools.errors.FileLocation;
import wyvern.tools.typedAST.abs.Declaration;
import wyvern.tools.typedAST.core.binding.*;
import wyvern.tools.typedAST.core.values.Obj;
import wyvern.tools.typedAST.interfaces.CoreAST;
import wyvern.tools.typedAST.interfaces.CoreASTVisitor;
import wyvern.tools.typedAST.interfaces.TypedAST;
import wyvern.tools.types.Environment;
import wyvern.tools.types.Type;
import wyvern.tools.types.TypeResolver;
import wyvern.tools.types.extensions.ClassType;
import wyvern.tools.types.extensions.TypeType;
import wyvern.tools.util.Reference;
import wyvern.tools.util.TreeWriter;

public class TypeDeclaration extends Declaration implements CoreAST {
	protected DeclSequence decls;
	private NameBinding nameBinding;
	private TypeBinding typeBinding;
	
	private Environment declEvalEnv;
	protected Reference<Obj> attrObj = new Reference<>();
    protected Reference<Environment> declEnv = new Reference<>(Environment.getEmptyEnvironment());
	protected Reference<Environment> attrEnv = new Reference<>(Environment.getEmptyEnvironment());

	public Reference<Obj> getAttrObjRef() {
		return attrObj;
	}

	public Environment getAttrEnv() {
		return attrEnv.get();
	}
	@Override
	public Environment extendType(Environment env) {
		return env.extend(typeBinding);
	}

	private boolean declGuard = false;
	@Override
	public Environment extendName(Environment env, Environment against) {
		if (!declGuard) {
			for (Declaration decl : decls.getDeclIterator()) {
				declEnv.set(decl.extendName(declEnv.get(), against));
				if (decl instanceof AttributeDeclaration) {
					env = env.extend(new NameBindingImpl(getName(), ((AttributeDeclaration) decl).getType()));
					nameBinding = new NameBindingImpl(nameBinding.getName(), decl.getType());
				}
			}
			declGuard = true;
		}
		return env;
	}

	public static class AttributeDeclaration extends Declaration {
		private final TypedAST body;
		private Type rType;

		public AttributeDeclaration(TypedAST body) {
			this.body = body;
		}
		public AttributeDeclaration(TypedAST body, Type result) {
			this.body = body;
			this.rType = result;
		}

		@Override
		public String getName() {
			return "";
		}

		@Override
		protected Type doTypecheck(Environment env) {
			return body.typecheck(env);
		}

		@Override
		protected Environment doExtend(Environment old) {
			return old;
		}

		@Override
		public Environment extendWithValue(Environment old) {
			return old;
		}

		@Override
		public void evalDecl(Environment evalEnv, Environment declEnv) {
		}

		@Override
		public Type getType() {
			return rType;
		}

		@Override
		public Map<String, TypedAST> getChildren() {
			Map<String, TypedAST> childMap = new HashMap<>();
			childMap.put("body", body);
			return childMap;
		}

		@Override
		public TypedAST cloneWithChildren(Map<String, TypedAST> newChildren) {
			return new AttributeDeclaration(newChildren.get("body"));
		}

		@Override
		public FileLocation getLocation() {
			return null;
		}

		@Override
		public void writeArgsToTree(TreeWriter writer) {
		}

		public TypedAST getBody() {
			return body;
		}

		@Override
		public Environment extendType(Environment env) {
			return env;
		}

		@Override
		public Environment extendName(Environment env, Environment against) {
			rType = TypeResolver.resolve(rType, against);
			return env;
		}
	}

    public TypeDeclaration(String name, DeclSequence decls, FileLocation clsNameLine) {
		this.decls = decls;
		nameBinding = new NameBindingImpl(name, null);
		typeBinding = new TypeBinding(name, null);
		Type objectType = new TypeType(this);
		attrEnv.set(attrEnv.get().extend(new TypeDeclBinding("type", this)));
		Type classType = new ClassType(attrEnv, attrEnv, new LinkedList<String>()); // TODO set this to a class type that has the class members
		nameBinding = new NameBindingImpl(nameBinding.getName(), classType);
		typeBinding = new TypeBinding(nameBinding.getName(), objectType);
		this.location = clsNameLine;
	}

	@Override
	public void writeArgsToTree(TreeWriter writer) {
		//TODO: implement me
		//writer.writeArgs(definition);
	}

	@Override
	public void accept(CoreASTVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public Type getType() {
		return this.typeBinding.getType();
	}

	@Override
	public Map<String, TypedAST> getChildren() {
		Map<String, TypedAST> childMap = new HashMap<>();
		childMap.put("decls", decls);
		return childMap;
	}

	@Override
	public TypedAST cloneWithChildren(Map<String, TypedAST> newChildren) {
		return new TypeDeclaration(nameBinding.getName(), (DeclSequence)newChildren.get("decls"), location);
	}

	@Override
	public Type doTypecheck(Environment env) {
		// env = env.extend(new NameBindingImpl("this", nameBinding.getType()));
		Environment eenv = decls.extend(env);
		
		for (Declaration decl : decls.getDeclIterator()) {
			decl.typecheckSelf(eenv);
		}

		return this.typeBinding.getType();
	}	
	
	@Override
	protected Environment doExtend(Environment old) {
		Environment newEnv = old.extend(nameBinding).extend(typeBinding);
		// newEnv = newEnv.extend(new NameBindingImpl("this", nameBinding.getType())); // Why is there "this" in a type (not class)?
		return newEnv;
	}

	@Override
	public Environment extendWithValue(Environment old) {
		Environment newEnv = old.extend(new ValueBinding(nameBinding.getName(), nameBinding.getType()));
		return newEnv;
	}

	@Override
	public void evalDecl(Environment evalEnv, Environment declEnv) {
		declEvalEnv = declEnv;
		Environment thisEnv = decls.extendWithDecls(Environment.getEmptyEnvironment());

		Environment attrEnv = Environment.getEmptyEnvironment();
		ValueBinding vb = (ValueBinding) declEnv.lookup(nameBinding.getName());
		for (Declaration decl : decls.getDeclIterator()) {
			if (decl instanceof AttributeDeclaration) {
				vb.setValue(((AttributeDeclaration) decl).getBody().evaluate(evalEnv));
			}
		}
	}
	
	public DeclSequence getDecls() {
		return decls;
	}

	@Override
	public String getName() {
		return nameBinding.getName();
	}

	private FileLocation location = FileLocation.UNKNOWN;
	
	@Override
	public FileLocation getLocation() {
		return location; 
	}

    public NameBinding lookupDecl(String name) {
        return declEnv.get().lookup(name);
    }


	public Reference<Environment> getDeclEnv() {
		return declEnv;
	}
}