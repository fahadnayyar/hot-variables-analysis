package HotVariablesAnalysis;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import soot.Body;
import soot.BodyTransformer;
import soot.SootMethod;
import soot.jimple.Stmt;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.Pair;

public class HotVariableAnalysisWrapper extends BodyTransformer{

    @Override
    protected void internalTransform(Body body, String phase, Map options) {
        // TODO Auto-generated method stub
        SootMethod sootMethod = body.getMethod();
        double alpha = 0.5;
        if(!sootMethod.getName().equals("<init>")) {
            System.out.println("Analysing method " + sootMethod.getName());
            UnitGraph g = new BriefUnitGraph(sootMethod.getActiveBody());
            TotalQueriesAnalysis totalQueriesAnalysis = new TotalQueriesAnalysis(g);
            AdditionalQueriesAnalysis additionalQueriesAnalysis = new AdditionalQueriesAnalysis(g);
            Map< Stmt, Pair < FlowSet , FlowSet > > totalQueriesAnalysisFinalFlowSets = totalQueriesAnalysis.getFinalFlowSets();
            Map< Stmt, Pair < FlowSet , FlowSet > > additionalQueriesAnalysisFinalFlowSets = additionalQueriesAnalysis.getFinalFlowSets();
            Set<String> totallocalsSet = totalQueriesAnalysis.getLocalsSet();
            Set<String> additionalLocalsSet = additionalQueriesAnalysis.getLocalsSet();	
            assert(totalQueriesAnalysisFinalFlowSets.size() == additionalQueriesAnalysisFinalFlowSets.size());
            assert(totallocalsSet.size()==additionalLocalsSet.size());
            
            for (Stmt stmt : additionalQueriesAnalysisFinalFlowSets.keySet()) {
                if (stmt.toString().contains("this")) {
                    continue;
                }
                FlowSet additionalinSet = additionalQueriesAnalysisFinalFlowSets.get(stmt).getO1();           
                FlowSet totalinSet = totalQueriesAnalysisFinalFlowSets.get(stmt).getO1();
                FlowSet additionaloutSet = additionalQueriesAnalysisFinalFlowSets.get(stmt).getO2();
                FlowSet totaloutSet = totalQueriesAnalysisFinalFlowSets.get(stmt).getO2();
                assert(additionalinSet.size()==1); assert(totalinSet.size()==1);
                assert(additionaloutSet.size()==1); assert(totaloutSet.size()==1);
                int qtInInt = (int)totalinSet.iterator().next();
                double qtIn = (double)qtInInt;
                int qtOutInt = (int)totaloutSet.iterator().next();
                double qtOut = (double)qtOutInt;
                Map<String, Integer> qAddInMap = (Map<String, Integer>)additionalinSet.iterator().next();
                Map<String, Integer> qAddOutMap = (Map<String, Integer>)additionaloutSet.iterator().next();               
                Set<String> hotVariablesIn = new HashSet<String>();
                Set<String> hotVariablesOut = new HashSet<String>();
                for (String localStr : additionalLocalsSet) {
                	 int qAddInInt = (int)qAddInMap.get(localStr);
                	 double qAddIn = (double)qAddInInt; 
                	 int qAddOutInt = (int)qAddOutMap.get(localStr);
                	 double qAddOut = (double)qAddOutInt; 
                	 if (qAddIn > alpha*qtIn) {
                		 hotVariablesIn.add(localStr);
                	 }
                	 if (qAddOut > alpha*qtOut) {
                		 hotVariablesOut.add(localStr);
                	 }
                }
                print("Hot Variables before:");
                print1("[");
                for (String hotVarInStr: hotVariablesIn) {
                	print1(hotVarInStr); print1(", ");
                }
                print("]");
           	 	print1("INSTRUCTION: ");print(stmt.toString());
           	 	print("Hot Variables before:");
           	 	print1("[");
           	 	for (String hotVarOutStr: hotVariablesOut) {
           	 		print1(hotVarOutStr); print1(", ");
           	 	}
           	 	print("]");
            }
            
        }
    }
    protected void print(Object s) {
        System.out.println(s.toString());
    }
    protected void print1(Object s) {
        System.out.print(s.toString().toString());
    }
}