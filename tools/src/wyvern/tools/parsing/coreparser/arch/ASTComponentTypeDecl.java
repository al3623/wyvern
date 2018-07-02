/* Generated By:JJTree: Do not edit this line. ASTComponentTypeDecl.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package wyvern.tools.parsing.coreparser.arch;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import wyvern.target.corewyvernIL.decl.Declaration;
import wyvern.target.corewyvernIL.decl.DefDeclaration;
import wyvern.target.corewyvernIL.decltype.DeclType;
import wyvern.target.corewyvernIL.decltype.ValDeclType;
import wyvern.target.corewyvernIL.expression.Expression;
import wyvern.target.corewyvernIL.expression.New;
import wyvern.target.corewyvernIL.modules.Module;
import wyvern.target.corewyvernIL.modules.TypedModuleSpec;
import wyvern.target.corewyvernIL.support.InterpreterState;
import wyvern.target.corewyvernIL.type.NominalType;
import wyvern.target.corewyvernIL.type.StructuralType;
import wyvern.tools.errors.ErrorMessage;
import wyvern.tools.errors.ToolError;

public class ASTComponentTypeDecl extends SimpleNode {
  private boolean isExternal = false;
  private String typeName;
  private HashMap<String, String> reqs = new HashMap<>();
  private HashMap<String, String> provs = new HashMap<>();

  public ASTComponentTypeDecl(int id) {
    super(id);
  }

  public ASTComponentTypeDecl(ArchParser p, int id) {
    super(p, id);
  }

  public HashMap<String, String> getReqs() {
    return reqs;
  }

  public HashMap<String, String> getProvs() {
    return provs;
  }

  public boolean isExternal() {
    return isExternal;
  }

  public void setExternal(boolean isExternal) {
    this.isExternal = isExternal;
  }

  public String getTypeName() {
    return typeName;
  }

  public void setTypeName(String t) {
    typeName = t;
  }

  public String toString() {
    return super.toString() + " " + typeName + " : " + isExternal;
  }

  public void collectPorts() {
    for (Node child : children) {
      if (child instanceof ASTPortDecl) {
        String req = ((ASTPortDecl) child).getRequires();
        String prov = ((ASTPortDecl) child).getProvides();
        String name = ((ASTPortDecl) child).getPort();
        if (req != null) {
          reqs.put(name, req);
        } else if (prov != null) {
          provs.put(name, prov);
        }
      }
    }
  }

  public boolean checkModule() {
    if (isExternal) {
      return true;
    }
    /* Check that the .wyv file exists */
    String rootLoc = null, wyvernPath = null;
    Module mod = null;
    int checkCount = 0;

    try {
      rootLoc = System.getenv("WYVERN_ROOT");
      if (rootLoc == null) {
        rootLoc = System.getProperty("user.dir");
      }

      wyvernPath = System.getenv("WYVERN_HOME");
      if (wyvernPath == null) {
        System.err.println(
            "must set WYVERN_HOME environmental variable to wyvern project directory");
        return false;
      }

      wyvernPath += "/stdlib/";
      // sanity check: is the wyvernPath a valid directory?
      if (!Files.isDirectory(Paths.get(wyvernPath))) {
        System.err.println(
            "Error: WYVERN_HOME is not set to a valid Wyvern project directory");
        return false;
      }
    } catch (ToolError e) {
      System.err.println(e.getMessage());
    }

    InterpreterState state = new InterpreterState(
        InterpreterState.PLATFORM_JAVA, new File(rootLoc),
        new File(wyvernPath));

    try {
      mod = state.getResolver().resolveModule(typeName);

      /* Check that it's a module def */
      Expression expr = mod.getExpression();
      if (!expr.getType().toString().contains("apply()")) {
        // throw error saying it's not a module def
        ToolError.reportError(ErrorMessage.MODULE_DEF_NOT_FOUND, location,
            typeName);
      }

      /* Check that it has the correct dependencies */
      List<TypedModuleSpec> deps = mod.getDependencies();
      if (deps.size() != reqs.size()) {
        // throw error saying dependencies don't match
        ToolError.reportError(ErrorMessage.COMPONENT_DEPENDENCY_INCONSISTENCY,
            location, typeName);
      }
      for (TypedModuleSpec dep : deps) {
        String depName = dep.getDefinedTypeName();
        if (!reqs.containsValue(depName)) {
          // throw error saying dependencies don't match
          ToolError.reportError(ErrorMessage.COMPONENT_DEPENDENCY_INCONSISTENCY,
              location, typeName);
        }
      }

      /* Check that it exposes the correct fields */
      List<Declaration> decls = ((New) expr).getDecls().stream()
          .filter(p -> p instanceof DefDeclaration)
          .collect(Collectors.toList());
      for (Declaration d : decls) {
        DefDeclaration dd = (DefDeclaration) d;

        List<DeclType> valdecltypes = ((StructuralType) (dd.getType()))
            .getDeclTypes().stream().filter(p -> p instanceof ValDeclType)
            .collect(Collectors.toList());

        for (DeclType dt : valdecltypes) {
          ValDeclType vdtype = (ValDeclType) dt;
          String name = vdtype.getName();
          String t = ((NominalType) vdtype.getSourceType()).getTypeMember();
          if (provs.get(name) == null || !provs.get(name).equals(t)) {
            // inconsistent fields
            ToolError.reportError(
                ErrorMessage.COMPONENT_DEPENDENCY_INCONSISTENCY, location,
                typeName);
          } else {
            checkCount++;
          }
        }
        if (checkCount != valdecltypes.size()) {
          // inconsistent fields
          ToolError.reportError(ErrorMessage.COMPONENT_DEPENDENCY_INCONSISTENCY,
              location, typeName);
          return false;
        }
        return true;
      }

    } catch (ToolError e) {
      e.printStackTrace();
      return false;
    }
    return true;

  }

  /** Accept the visitor. **/
  public Object jjtAccept(ArchParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
/*
 * JavaCC - OriginalChecksum=f5aec0da9432112df1e15710b91b7b28 (do not edit this
 * line)
 */