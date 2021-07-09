package HotVariablesAnalysis;

import soot.Local;
import soot.Value;
import soot.ValueBox;
import soot.JastAddJ.IfStmt;
import soot.jimple.AssignStmt;
import soot.jimple.GotoStmt;
import soot.jimple.Stmt;
import soot.jimple.internal.*;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.BackwardFlowAnalysis;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.Pair;
import soot.util.Chain;

import java.util.*;

public class TotalQueriesAnalysis extends BackwardFlowAnalysis {
    
	FlowSet inVal, outVal;
    Set<String> localsSet;
    Map< Stmt, Pair < FlowSet , FlowSet > > finalFlowSets;
    Set<String> inputSet;
    Set<String> paramSet;
    
    public TotalQueriesAnalysis(UnitGraph g) {
        super(g);
        
        localsSet = new HashSet<String>();
        inputSet = new HashSet<String>();
        paramSet = new HashSet<String>();
        finalFlowSets = new HashMap<Stmt, Pair < FlowSet , FlowSet >>();

        //* Adding all local variables of this method into set localsSet
        Chain<Local> locals =  g.getBody().getLocals();
        for (Local local : locals) {
            String localStr = local.toString();
            if ( !(( localStr.contains("this"))) ) {
                localsSet.add(localStr);
            }
        }
        print("");
        print("All the local variables of this method are: ");
        for (String localStr : localsSet) {
            print1(localStr);
            print1(", ");
        }
        print("");

        doAnalysis();

        print("***----------printing main output of this method------------***");
        //* printing finalFlowsets
        for (Stmt stmt : finalFlowSets.keySet()) {
            if (stmt.toString().contains("this")) {
                continue;
            }

            print("");
            FlowSet inSet = finalFlowSets.get(stmt).getO1();
            FlowSet outSet = finalFlowSets.get(stmt).getO2();
            int flagin = 0;
            int flagout = 0;
            Iterator inSetItr = null;
            if (!inSet.isEmpty()) {
            	inSetItr = inSet.iterator();
            	flagin=1;
            }
            Iterator outSetItr = null;
            if (!outSet.isEmpty()) {
                outSetItr = outSet.iterator();
                flagout = 1;
            }

            //* printing inset:
            assert(inSet.size()==1);
            print1("inset: { ");
            if (flagin==1) {
                while (inSetItr.hasNext()) {
                    String insetVar = inSetItr.next().toString();
                    print1(insetVar);
                }
            }
            print("}");


            //* printing statement:
            print1("Unit: ");
            print(stmt.toString());


            //* printintg outset:
            assert(outSet.size()==1);
            print1("outset: { ");
            if (flagout==1) {
                while (outSetItr.hasNext()) {
                    String outsetVar = outSetItr.next().toString();
                    print1(outsetVar);
                    print1(", ");
                }
            }
            print("}");


            print("");
        }

        print("***----------main output of this method ends here------------***");
        print("");
    }
    @Override
    protected void flowThrough(Object in, Object unit, Object out) {
        inVal = (FlowSet) in;
        outVal = (FlowSet) out;
        assert(inVal.size()==1); assert(outVal.size()==1);
        Iterator inValItr = inVal.iterator();
        int inValInt = (int)inValItr.next();
        Iterator outValItr = outVal.iterator();
        int outValInt = (int)outValItr.next();
        
        print("BEFORE TF");
        print1("inVal: "); print(inValInt);
        print1("outVal: "); print(outValInt);
        
        outValInt = inValInt;
        
        Stmt u = (Stmt) unit;

        //* printint the statememnt:
        print("current statement is: ");
        print(u);
        print1("class of statpement is: ");
        print(u.getClass());
        
        //* Transfer function for JIfStmt
        if (u instanceof JIfStmt) {
        	JIfStmt ifStmt = (JIfStmt) u;
        	print("this is an if statement!!");
        	outValInt++;
        	
        } 
        
        //* Transfer function for JGotoStmt
//        else if (u instanceof JGotoStmt) {
//        	JGotoStmt gotoStmt = (JGotoStmt)u;
//        	print("this is a goto statement!!");
//        	outValInt++;
//        }
        
        outVal.clear();
        outVal.add(outValInt);

        print("AFTER TF");
        print1("inVal: "); print(inValInt);
        print1("outVal: "); print(outValInt);
        print("");
        //* updating the final flowsets for this statement
        if (!finalFlowSets.containsKey(u)) {
            finalFlowSets.put(u,new Pair<FlowSet, FlowSet>());
        }
        finalFlowSets.get(u).setO1(inVal);
        finalFlowSets.get(u).setO2(outVal);
    }

    @Override
    protected Object newInitialFlow() {
        ArraySparseSet nif = new ArraySparseSet();
        nif.add(0);
        return nif;
    }

    @Override
    protected Object entryInitialFlow() {
        ArraySparseSet eif = new ArraySparseSet();
        eif.add(0);
        return eif;
    }

    @Override
    protected void merge(Object in1, Object in2, Object out) {
        FlowSet inVal1 = (FlowSet) in1;
        FlowSet inVal2 = (FlowSet) in2;
        FlowSet outVal = (FlowSet) out;
        assert(inVal1.size()==1); assert(inVal2.size()==1); assert(outVal.size()==1);
        Iterator inValItr1 = inVal1.iterator();
        int inValInt1 = (int)inValItr1.next();
        Iterator invalItr2 = inVal2.iterator();
        int inValInt2 = (int)invalItr2.next();
        Iterator outValItr = outVal.iterator();
        int outValInt = (int)outValItr.next();
        outValInt = inValInt1 + inValInt2;
//        if (inValInt1 > inValInt2) {
//        	outValInt = inValInt1;
//        }else {
//        	outValInt = inValInt2;
//        }
        outVal.clear();
        outVal.add(outValInt);
    }

    @Override
    protected void copy(Object source, Object dest) {
        FlowSet srcSet = (FlowSet) source;
        FlowSet destSet = (FlowSet) dest;
        srcSet.copy(destSet);
    }

    protected void print(Object s) {
        System.out.println(s.toString());
    }
    protected void print1(Object s) {
        System.out.print(s.toString().toString());
    }
}
