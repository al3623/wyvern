package wyvern.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import wyvern.stdlib.Globals;
import wyvern.target.corewyvernIL.decltype.ConcreteTypeMember;
import wyvern.target.corewyvernIL.decltype.DeclType;
import wyvern.target.corewyvernIL.decltype.DefDeclType;
import wyvern.target.corewyvernIL.expression.Invokable;
import wyvern.target.corewyvernIL.expression.SeqExpr;
import wyvern.target.corewyvernIL.expression.Value;
import wyvern.target.corewyvernIL.modules.Module;
import wyvern.target.corewyvernIL.modules.TypedModuleSpec;
import wyvern.target.corewyvernIL.support.EvalContext;
import wyvern.target.corewyvernIL.support.GenContext;
import wyvern.target.corewyvernIL.support.InterpreterState;
import wyvern.target.corewyvernIL.type.StructuralType;
import wyvern.target.corewyvernIL.type.ValueType;
import wyvern.tools.arch.lexing.ArchLexer;
import wyvern.tools.errors.ToolError;
import wyvern.tools.parsing.coreparser.ParseException;
import wyvern.tools.parsing.coreparser.TokenManager;
import wyvern.tools.parsing.coreparser.WyvernTokenManager;
import wyvern.tools.parsing.coreparser.arch.ASTArchDesc;
import wyvern.tools.parsing.coreparser.arch.ArchParser;
import wyvern.tools.parsing.coreparser.arch.ArchParserConstants;
import wyvern.tools.parsing.coreparser.arch.DeclCheckVisitor;
import wyvern.tools.parsing.coreparser.arch.Node;

public class ArchitectureInterpreter {
  public static void main(String[] args) {
    if (args.length != 1) {
      System.err.println("usage: wyvarch <filename>");
      System.exit(1);
    }
    String filename = args[0];
    Path filepath = Paths.get(filename);
    if (!Files.isReadable(filepath)) {
      System.err.println("Cannot read file " + filename);
      System.exit(1);
    }
    try {
      String rootLoc;
      if (wyvernRoot.get() != null) {
        rootLoc = wyvernRoot.get();
      } else {
        rootLoc = System.getProperty("user.dir");
      }
      String wyvernPath = System.getenv("WYVERN_HOME");
      if (wyvernPath == null) {
        if (wyvernHome.get() != null) {
          wyvernPath = wyvernHome.get();
        } else {
          System.err.println(
              "must set WYVERN_HOME environmental variable to wyvern project directory");
          return;
        }
      }
      wyvernPath += "/stdlib/";
      // sanity check: is the wyvernPath a valid directory?
      if (!Files.isDirectory(Paths.get(wyvernPath))) {
        System.err.println(
            "Error: WYVERN_HOME is not set to a valid Wyvern project directory");
        return;
      }

      // Check architecture file
      File f = new File(rootLoc + "/" + filepath.toString());
      BufferedReader source = new BufferedReader(new FileReader(f));
      ArchParser wp = new ArchParser(
          (TokenManager) new WyvernTokenManager<ArchLexer, ArchParserConstants>(
              source, "test", ArchLexer.class, ArchParserConstants.class));
      wp.fname = filename;
      Node start = wp.ArchDesc();
      DeclCheckVisitor visitor = new DeclCheckVisitor();
      visitor.visit((ASTArchDesc) start, null);

      // Construct interpreter state
      InterpreterState state = new InterpreterState(
          InterpreterState.PLATFORM_JAVA, new File(rootLoc),
          new File(wyvernPath));

      // Process connectors and begin generation

      HashMap<String, String> connectors = visitor.getConnectors();
      for (String connectorInstance : connectors.keySet()) {
        String connector = connectors.get(connectorInstance);

        HashSet<String> fullports = visitor.getAttachments()
            .get(connectorInstance);
        HashSet<String> ports = new HashSet<>();
        for (String p : fullports) {
          String[] pair = p.split(".");
          ports.add(pair[1]);
        }

        // Load the connector type module and get context
        Module m = state.getResolver().resolveType(connector + "Properties");
        GenContext genCtx = Globals.getGenContext(state);
        EvalContext evalCtx = Globals.getStandardEvalContext();
        SeqExpr mSeqExpr = state.getResolver().wrapWithCtx(m.getExpression(),
            m.getDependencies(), evalCtx);
        genCtx = mSeqExpr.extendContext(genCtx);
        evalCtx = mSeqExpr.interpretCtx(evalCtx).getSecond();

        // Get AST node
        TypedModuleSpec mSpec = m.getSpec();
        ValueType mType = mSpec.getType();
        StructuralType mSType = mType.getStructuralType(genCtx);
        ConcreteTypeMember connectorDecl = (ConcreteTypeMember) mSType
            .findDecl(connector + "Properties", genCtx);

        // Interpret type and get metadata
        ConcreteTypeMember contype = (ConcreteTypeMember) connectorDecl
            .interpret(evalCtx);
        Value metadata = contype.getMetadataValue();
        ValueType metadataType = metadata.getType();
        StructuralType metadataStructure = metadataType
            .getStructuralType(genCtx);

        // Prepare args for metadata methods
        for (String port : ports) {
          // create objectvalue of list of ASTPortDecls
        }

        // Execute metadata
        Value portCompatibility = null, connectorImpl = null,
            connectorInit = null;
        for (DeclType dt : metadataStructure.getDeclTypes()) {
          if (dt instanceof DefDeclType) {
            DefDeclType defdecl = (DefDeclType) dt;
            String methodName = defdecl.getName();
            List<Value> testArgs = new LinkedList<Value>();

            if (methodName.equals("checkPortCompatibility")) {

            } else if (methodName.equals("generateConnectorImpl")) {

            } else if (methodName.equals("generateConnectorInit")) {

            } else {
              // throw error here for presence of extra methods
            }

            ((Invokable) metadata).invoke(methodName, testArgs)
                .executeIfThunk();
          }
        }
        if (portCompatibility == null || connectorImpl == null
            || connectorInit == null) {
          // throw error for the absence of certain methods
        }
      }

    } catch (ToolError e) {
      e.printStackTrace();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (ParseException e) {
      e.printStackTrace();
    }
    System.out.println("~DONE~");
  }

  // used to set WYVERN_HOME when called programmatically
  public static final ThreadLocal<String> wyvernHome = new ThreadLocal<String>();

  // used to set WYVERN_ROOT when called programmatically
  public static final ThreadLocal<String> wyvernRoot = new ThreadLocal<String>();
}
