package com.diylisp.model;

import com.diylisp.Evaluator;
import com.diylisp.exception.LispException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.diylisp.model.Bool.bool;
import static com.diylisp.model.Symbol.symbol;

public class SExpression extends AbstractSyntaxTree {

    private List<AbstractSyntaxTree> expressions;

    public SExpression(List<AbstractSyntaxTree> expressions) {
        this.expressions = expressions;
    }

    public SExpression(AbstractSyntaxTree... expressions) {
        this.expressions = Arrays.asList(expressions);
    }

    public static SExpression sexp(AbstractSyntaxTree... expressions) {
        return new SExpression(expressions);
    }

    public static SExpression sexp(List<AbstractSyntaxTree> expressions) {
        return new SExpression(expressions);
    }

    public int size() {
        return expressions.size();
    }

    public List<Symbol> asSymbols() {
        return expressions.stream().map(exp -> (Symbol) exp).collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SExpression that = (SExpression) o;

        if (!expressions.equals(that.expressions)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return expressions.hashCode();
    }

    @Override
    public String toString() {
        if (expressions.size() == 0)
            return "()";

        String first = expressions.get(0).toString();
        String exps = (first.equals("quote")
                ? expressions.subList(1, expressions.size())
                : expressions)
                .stream()
                .map(AbstractSyntaxTree::toString)
                .collect(Collectors.joining(" "));
        return first.equals("quote")
            ? "'" + exps
            : "(" + exps + ")";
    }

    @Override
    public AbstractSyntaxTree evaluate(List<AbstractSyntaxTree> exps, Environment env) {
        exps.set(0, Evaluator.evaluateList(expressions, env));
        return exps.get(0).evaluate(exps, env);
    }

    @Override
    public AbstractSyntaxTree evaluate(Environment env) {
        if (expressions.size() == 0)
            throw new LispException("List was empty");

        return Evaluator.evaluateList(expressions, env);
    }

    @Override
    public AbstractSyntaxTree copy() {
        List<AbstractSyntaxTree> copied = expressions
                .stream()
                .map(AbstractSyntaxTree::copy)
                .collect(Collectors.toCollection(ArrayList::new));
        return sexp(copied);
    }

    public AbstractSyntaxTree cons(AbstractSyntaxTree head, Environment env) {
        expressions.add(0, head);
        return sexp(expressions);
    }

    public AbstractSyntaxTree head(Environment env) {
        if (expressions.get(0).equals(symbol("quote"))) {
            SExpression list = Evaluator.evaluateSexp(expressions.get(1));
            if (list.size() == 0)
                throw new LispException("Cannot call head on an empty list");

            return list.expressions.get(0);
        }

        return Evaluator.evaluate(expressions.get(0), env);
    }

    public AbstractSyntaxTree tail(Environment env) {
        if (expressions.get(0).equals(symbol("quote"))) {
            SExpression list = Evaluator.evaluateSexp(expressions.get(1));
            if (list.size() == 0)
                throw new LispException("Cannot call tail on an empty list");

            return sexp(list.expressions.subList(1, list.expressions.size()));
        }

        return sexp(expressions.subList(1, expressions.size()));
    }

    public AbstractSyntaxTree isEmpty(Environment env) {
        if (expressions.size() == 0)
            return Bool.True;

        if (expressions.get(0).equals(symbol("quote"))) {
            SExpression list = Evaluator.evaluateSexp(expressions.get(1));
            return bool(list.size() == 0);
        }

        return bool(expressions.size() == 0);
    }
}
