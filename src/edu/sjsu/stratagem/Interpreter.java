package edu.sjsu.stratagem;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import edu.sjsu.stratagem.parser.StratagemLexer;
import edu.sjsu.stratagem.parser.StratagemParser;

import java.io.FileInputStream;
import java.io.InputStream;

public class Interpreter {

    public static void main(String[] args) throws Exception {
        String inputFile = null;
        if (args.length > 0) {
            inputFile = args[0];
        }
        InputStream is = System.in;
        if (inputFile != null) {
            is = new FileInputStream(inputFile);
        }

        ANTLRInputStream input = new ANTLRInputStream(is);
        StratagemLexer lexer = new StratagemLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        StratagemParser parser = new StratagemParser(tokens);
        ParseTree tree = parser.prog();  // parse

        ExpressionBuilderVisitor builder = new ExpressionBuilderVisitor();
        Expression prog = builder.visit(tree);

        Type t = prog.typecheck(new TypeEnvironment());
        Value v = prog.evaluate(new ValueEnvironment());

        System.out.println(v);
    }

}
