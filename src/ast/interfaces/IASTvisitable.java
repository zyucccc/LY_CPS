package ast.interfaces;

public interface IASTvisitable {
    <Result, Data, Anomaly extends Throwable> 
    Result eval(IASTvisitor<Result, Data, Anomaly> visitor,
                  Data data) throws Anomaly;
}
