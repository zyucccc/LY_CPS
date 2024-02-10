package request.ast.interfaces;


import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import request.ast.Base;
import request.ast.Bexp;
import request.ast.Cexp;
import request.ast.Cont;
import request.ast.Dir;
import request.ast.Dirs;
import request.ast.Gather;
import request.ast.Query;
import request.ast.Rand;
import request.ast.astBase.ABase;
import request.ast.astBase.RBase;
import request.ast.astBexp.AndBExp;
import request.ast.astBexp.CExpBExp;
import request.ast.astBexp.NotBExp;
import request.ast.astBexp.OrBExp;
import request.ast.astBexp.SBExp;
import request.ast.astCexp.EQCExp;
import request.ast.astCexp.GCExp;
import request.ast.astCexp.GEQCExp;
import request.ast.astCexp.LCExp;
import request.ast.astCexp.LEQCExp;
import request.ast.astCont.DCont;
import request.ast.astCont.ECont;
import request.ast.astCont.FCont;
import request.ast.astDirs.FDirs;
import request.ast.astDirs.RDirs;
import request.ast.astGather.FGather;
import request.ast.astGather.RGather;
import request.ast.astQuery.BQuery;
import request.ast.astQuery.GQuery;
import request.ast.astRand.CRand;
import request.ast.astRand.SRand;

public interface IASTvisitor<Result, Data, Anomaly extends Throwable> {
	Result visit(Query<?> ast, Data data) throws Anomaly;
	Result visit(BQuery ast, Data data) throws Anomaly;
	Result visit(GQuery ast, Data data) throws Anomaly;
    Result visit(Base ast, Data data) throws Anomaly;
    Result visit(ABase ast, Data data) throws Anomaly;
    Result visit(RBase ast, Data data) throws Anomaly;
    Result visit(Bexp ast, Data data) throws Anomaly;
    Result visit(AndBExp ast, Data data) throws Anomaly;
    Result visit(CExpBExp ast, Data data) throws Anomaly;
    Result visit(NotBExp ast, Data data) throws Anomaly;
    Result visit(OrBExp ast, Data data) throws Anomaly;
    Result visit(SBExp ast, Data data) throws Anomaly;
    Result visit(Cexp ast, Data data) throws Anomaly;
    Result visit(EQCExp ast, Data data) throws Anomaly;
    Result visit(GCExp ast, Data data) throws Anomaly;
    Result visit(GEQCExp ast, Data data) throws Anomaly;
    Result visit(LCExp ast, Data data) throws Anomaly;
    Result visit(LEQCExp ast, Data data) throws Anomaly;
    Result visit(Cont ast, Data data) throws Anomaly;
    Result visit(DCont ast, Data data) throws Anomaly;
    Result visit(ECont ast, Data data) throws Anomaly;
    Result visit(FCont ast, Data data) throws Anomaly;
    Result visit(Dir ast, Data data) throws Anomaly;
    Result visit(Dirs ast, Data data) throws Anomaly;
    Result visit(FDirs ast, Data data) throws Anomaly;
    Result visit(RDirs ast, Data data) throws Anomaly;
    Result visit(Gather ast, Data data) throws Anomaly;
    Result visit(FGather ast, Data data) throws Anomaly;
    Result visit(RGather ast, Data data) throws Anomaly;
    Result visit(Rand ast, Data data) throws Anomaly;
    Result visit(CRand ast, Data data) throws Anomaly;
    Result visit(SRand ast, Data data) throws Anomaly;


}
