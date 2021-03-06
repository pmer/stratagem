package edu.sjsu.stratagem;

import static org.junit.Assert.*;

import edu.sjsu.stratagem.exception.StratagemException;
import org.junit.Test;

public class ExpressionTest {

    @Test
    public void testValueExpr() {
        ValueEnvironment env = new ValueEnvironment();
        ValueExpr ve = new ValueExpr(new IntVal(3));
        IntVal i = (IntVal) ve.evaluate(env);
        assertEquals(3, i.toInt());
    }

    @Test
    public void testVarExpr() {
        ValueEnvironment env = new ValueEnvironment();
        Value v = new IntVal(3);
        env.updateVar("x", v);
        Expression e = new VarExpr("x");
        assertEquals(v, e.evaluate(env));
    }

    @Test(expected=StratagemException.class)
    public void testVarNotFoundExpr() {
        ValueEnvironment env = new ValueEnvironment();
        Value v = new IntVal(3);
        env.updateVar("x", v);
        Expression e = new VarExpr("y");
        e.evaluate(env);
    }

    @Test
    public void testIfTrueExpr() {
        ValueEnvironment env = new ValueEnvironment();
        IfExpr ife = new IfExpr(ValueExpr.trueSingleton,
                new ValueExpr(new IntVal(1)),
                new ValueExpr(new IntVal(2)));
        IntVal iv = (IntVal) ife.evaluate(env);
        assertEquals(1, iv.toInt());
    }

    @Test
    public void testIfFalseExpr() {
        ValueEnvironment env = new ValueEnvironment();
        IfExpr ife = new IfExpr(ValueExpr.falseSingleton,
                new ValueExpr(new IntVal(1)),
                new ValueExpr(new IntVal(2)));
        IntVal iv = (IntVal) ife.evaluate(env);
        assertEquals(2, iv.toInt());
    }

    @Test
    public void testBadIfExpr() {
        ValueEnvironment env = new ValueEnvironment();
        IfExpr ife = new IfExpr(new ValueExpr(new IntVal(0)),
                new ValueExpr(new IntVal(1)),
                new ValueExpr(new IntVal(2)));
        try {
            ife.evaluate(env);
            fail();
        } catch (Exception ignored) {}
    }

    @Test
    public void testBinOpExpr() {
        ValueEnvironment env = new ValueEnvironment();
        BinOpExpr boe = new BinOpExpr(Op.ADD,
                new ValueExpr(new IntVal(1)),
                new ValueExpr(new IntVal(2)));
        IntVal iv = (IntVal) boe.evaluate(env);
        assertEquals(new IntVal(3), iv);
    }

    @Test
    public void testSeqExpr() {
        ValueEnvironment env = new ValueEnvironment();
        SeqExpr se = new SeqExpr(new Expression[] {
                new ValueExpr(new IntVal(2)),
                new BinOpExpr(Op.MULTIPLY,
                        new ValueExpr(new IntVal(2)),
                        new ValueExpr(new IntVal(3)))
        });
        assertEquals(new IntVal(6), se.evaluate(env));
    }

    @Test
    // (function(x) { x; })(321);
    public void testIdFunction() {
        ValueEnvironment env = new ValueEnvironment();
        FunctionDeclExpr f = new FunctionDeclExpr(
                "x",
                AnyType.singleton,
                new VarExpr("x"));
        f.typecheck(new TypeEnvironment());
        Expression arg = new ValueExpr(new IntVal(321));
        FunctionAppExpr app = new FunctionAppExpr(f, arg);
        assertEquals(new IntVal(321), app.evaluate(env));
    }

    @Test
    // fn(name: String) { fn(unused: String) { name }("Bob") }("Alice")
    public void testScope1() {
        StringVal alice = new StringVal("Alice");
        StringVal bob = new StringVal("Bob");

        ValueEnvironment env = new ValueEnvironment();
        FunctionDeclExpr innerDecl = new FunctionDeclExpr(
                "unused",
                StringType.singleton,
                new VarExpr("name"));
        FunctionDeclExpr outerDecl = new FunctionDeclExpr(
                "name",
                StringType.singleton,
                new FunctionAppExpr(innerDecl, new ValueExpr(bob)));
        outerDecl.typecheck(new TypeEnvironment());

        FunctionAppExpr outerApp = new FunctionAppExpr(outerDecl, new ValueExpr(alice));
        Value v = outerApp.evaluate(env);
        assertEquals(v, alice);
    }

    @Test
    // fn(name: String) { fn(name: String) { name }("Bob") }("Alice")
    public void testScope2() {
        StringVal alice = new StringVal("Alice");
        StringVal bob = new StringVal("Bob");

        ValueEnvironment env = new ValueEnvironment();
        FunctionDeclExpr innerDecl = new FunctionDeclExpr(
                "name",
                StringType.singleton,
                new VarExpr("name"));
        FunctionDeclExpr outerDecl = new FunctionDeclExpr(
                "name",
                StringType.singleton,
                new FunctionAppExpr(innerDecl, new ValueExpr(bob)));
        outerDecl.typecheck(new TypeEnvironment());

        FunctionAppExpr outerApp = new FunctionAppExpr(
                outerDecl,
                new ValueExpr(alice));
        Value v = outerApp.evaluate(env);
        assertEquals(v, bob);
    }

    @Test
    // fn(name: String) { fn(name: String) { name }("Bob"); name }("Alice")
    public void testScope3() {
        StringVal alice = new StringVal("Alice");
        StringVal bob = new StringVal("Bob");

        ValueEnvironment env = new ValueEnvironment();
        FunctionDeclExpr innerDecl = new FunctionDeclExpr(
                "name",
                StringType.singleton,
                new VarExpr("name"));
        FunctionDeclExpr outerDecl = new FunctionDeclExpr(
                "name",
                StringType.singleton,
                new SeqExpr(new Expression[] {
                        new FunctionAppExpr(innerDecl, new ValueExpr(bob)),
                        new VarExpr("name")
                }));
        outerDecl.typecheck(new TypeEnvironment());

        FunctionAppExpr outerApp = new FunctionAppExpr(
                outerDecl,
                new ValueExpr(alice));
        Value v = outerApp.evaluate(env);
        assertEquals(v, alice);
    }
}
