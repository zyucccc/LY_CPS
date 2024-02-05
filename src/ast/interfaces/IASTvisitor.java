package ast.interfaces;

import ast.Base;
import ast.Bexp;
import ast.Cexp;
import ast.Cont;
import ast.Dir;
import ast.Dirs;
import ast.Gather;
import ast.Query;
import ast.Rand;

public interface IASTvisitor<Result, Data, Anomaly extends Throwable> {
	Result visit(Query<?> ast, Data data) throws Anomaly;
    Result visit(Base ast, Data data) throws Anomaly;
    Result visit(Bexp ast, Data data) throws Anomaly;
    Result visit(Cexp ast, Data data) throws Anomaly;
    Result visit(Cont ast, Data data) throws Anomaly;
    Result visit(Dir ast, Data data) throws Anomaly;
    Result visit(Dirs ast, Data data) throws Anomaly;
    Result visit(Gather ast, Data data) throws Anomaly;
    Result visit(Rand ast, Data data) throws Anomaly;


}
