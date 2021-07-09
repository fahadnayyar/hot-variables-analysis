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

public class AdditionalQueriesAnalysis extends BackwardFlowAnalysis {
    
	private FlowSet inVal, outVal;
	private Set<String> localsSet;
	private Map< Stmt, Pair < FlowSet , FlowSet > > finalFlowSets;
	private Set<String> inputSet;
	private Set<String> paramSet;
    
    public AdditionalQueriesAnalysis(UnitGraph g) {
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
                	Map<String, Integer> inValMap = (Map<String, Integer>)inSetItr.next();
                	assert(inValMap.size()==localsSet.size());
                	print1("[");
                    for (String localStr : localsSet) {
                    	print1(localStr); print1(": ");
                    	print1(inValMap.get(localStr)); print1(", ");
                    }
                    print1("]");
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
                	Map<String, Integer> outValMap = (Map<String, Integer>)outSetItr.next();
                	assert(outValMap.size()==localsSet.size());
                	print1("[");
                    for (String localStr : localsSet) {
                    	print1(localStr); print1(": ");
                    	print1(outValMap.get(localStr)); print1(", ");
                    }
                    print1("]");
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
        Map<String, Integer> inValMap = (Map<String, Integer>)inValItr.next();
        Iterator outValItr = outVal.iterator();
        Map<String, Integer> outValMap = (Map<String, Integer>)outValItr.next();
        assert(inValMap.size()==localsSet.size());
        assert(outValMap.size()==localsSet.size());
        
        print("BEFORE TRANSFER FUNCTION: ");
        print1("inVal: ");
        print1("[");
        for (String localStr : localsSet) {
        	print1(localStr); print1(": ");
        	print1(inValMap.get(localStr)); print1(", ");
        }
        print("]");
        print1("outVal: ");
        print1("[");
        for (String localStr : localsSet) {
        	print1(localStr); print1(": ");
        	print1(inValMap.get(localStr)); print1(", ");
        }
        print("]");
        
        for (String localStr : localsSet) {
        	int inValInt = inValMap.get(localStr);
        	int outValInt = outValMap.get(localStr);
        	outValInt = inValInt;
        	outValMap.put(localStr, outValInt);
        }    
        
        Stmt u = (Stmt) unit;

        print("current statement is: ");
        print(u);
        print1("class of statpement is: ");
        print(u.getClass());
        
        //* Transfer function for JIfStmt
        if (u instanceof JIfStmt) {
        	JIfStmt ifStmt = (JIfStmt) u;
        	print("this is an if statement!!");     	
        	
        	List useBoxes = ifStmt.getUseBoxes();
        	print("used variables in if: ");
        	for (int i = 0; i<useBoxes.size(); i++) {
        		Local var = null;
        		ImmediateBox immBox = null;
        		try { 
        			immBox = (ImmediateBox) useBoxes.get(i);
        			try {
        				var = (Local) immBox.getValue();
        			} catch (Exception e) {
        				print(immBox.getValue().getClass());
        				print("Exception: used var in if is not a Local");
        			}
//        			print("immBox data:");
//        			print(immBox.getValue());
//        			print(immBox.getValue().getClass());
        		} catch (Exception e) {
        			print(useBoxes.get(i).getClass());
        			print("Exception: box is not an immediate box");
        		}
        		if (var!=null) {
        			print(var);
        			assert(outValMap.containsKey(var.toString()));
        			outValMap.put(var.toString(), outValMap.get(var.toString())+1);
        		}
        	}
        	print("");        	
        } 
       
        //* Transfer function for JGotoStmt
//        else if (u instanceof JGotoStmt) {
//        	JGotoStmt gotoStmt = (JGotoStmt)u;
//        	print("this is a goto statement!!");	
//        	
//        	List useBoxes = ifStmt.getUseBoxes();
//        	print("used variables in if: ");
//        	for (int i = 0; i<useBoxes.size(); i++) {
//        		Local var = null;
//        		ImmediateBox immBox = null;
//        		try { 
//        			immBox = (ImmediateBox) useBoxes.get(i);
//        			try {
//        				var = (Local) immBox.getValue();
//        			} catch (Exception e) {
//        				print(immBox.getValue().getClass());
//        				print("Exception: used var in if is not a Local");
//        			}
////        			print("immBox data:");
////        			print(immBox.getValue());
////        			print(immBox.getValue().getClass());
//        		} catch (Exception e) {
//        			print(useBoxes.get(i).getClass());
//        			print("Exception: box is not an immediate box");
//        		}
//        		if (var!=null) {
//        			print(var);
//        			assert(outValMap.containsKey(var.toString()));
//        			outValMap.put(var.toString(), outValMap.get(var.toString())+1);
//        		}
//        	}
//        	print(""); 
//        }
        
        outVal.clear();
        outVal.add(outValMap);

        print("AFTER  TRANSFER FUNCTION: ");
        print1("inVal: ");
        print1("[");
        for (String localStr : localsSet) {
        	print1(localStr); print1(": ");
        	print1(inValMap.get(localStr)); print1(", ");
        }
        print("]");
        print1("outVal: ");
        print1("[");
        for (String localStr : localsSet) {
        	print1(localStr); print1(": ");
        	print1(inValMap.get(localStr)); print1(", ");
        }
        print("]");
        print("");
        
        
        //* updating the final flowsets for this statement
        if (!finalFlowSets.containsKey(u)) {
            finalFlowSets.put(u,new Pair<FlowSet, FlowSet>());
        }
        finalFlowSets.get(u).setO1(inVal);
        finalFlowSets.get(u).setO2(outVal);
//        print("niklo");
//        print(inval);
//        print(u);
//        print(outval);
//        print("niklo1");
    }

    @Override
    protected Object newInitialFlow() {
        ArraySparseSet nif = new ArraySparseSet();
        Map<String, Integer> flowMap = new HashMap<String, Integer>();
        for (String localStr : localsSet) {
        	if (!flowMap.containsKey(localStr)) {
                flowMap.put(localStr, 0);
        	}
        }
        nif.add(flowMap);
        return nif;
    }

    @Override
    protected Object entryInitialFlow() {
    	ArraySparseSet nif = new ArraySparseSet();
        Map<String, Integer> flowMap = new HashMap<String, Integer>();
        for (String localStr : localsSet) {
        	if (!flowMap.containsKey(localStr)) {
                flowMap.put(localStr, 0);
        	}
        }
        nif.add(flowMap);
        return nif;
    }

    @Override
    protected void merge(Object in1, Object in2, Object out) {
        FlowSet inVal1 = (FlowSet) in1;
        FlowSet inVal2 = (FlowSet) in2;
        FlowSet outVal = (FlowSet) out;
        assert(inVal1.size()==1); assert(inVal2.size()==1); assert(outVal.size()==1);
        Iterator inValItr1 = inVal1.iterator();
        Map<String, Integer> inValMap1 = (Map<String, Integer>)inValItr1.next();
        Iterator invalItr2 = inVal2.iterator();
        Map<String, Integer> inValMap2 = (Map<String, Integer>)invalItr2.next();
        Iterator outValItr = outVal.iterator();
        Map<String, Integer> outValMap = (Map<String, Integer>)outValItr.next();
        assert(inValMap1.size()==localsSet.size());
        assert(inValMap2.size()==localsSet.size());
        assert(outValMap.size()==localsSet.size());
        for (String localStr : localsSet) { 
        	int inValInt1 = (int)inValMap1.get(localStr);
        	int inValInt2 = (int)inValMap1.get(localStr);
        	int outValInt = inValInt1 + inValInt2;        	
//        	if (inValInt1 > inValInt2) {
//        		outValInt = inValInt1;
//        	}else {
//        		outValInt = inValInt2;
//        	}
        	outValMap.put(localStr, outValInt);
        }
        outVal.clear();
        outVal.add(outValMap);
    }

//    TODO: remove unnecessary setters and getters. 
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
    
    public FlowSet getInVal() {
		return inVal;
	}
	public void setInVal(FlowSet inVal) {
		this.inVal = inVal;
	}
	public FlowSet getOutVal() {
		return outVal;
	}
	public void setOutVal(FlowSet outVal) {
		this.outVal = outVal;
	}
	public Set<String> getLocalsSet() {
		return localsSet;
	}
	public void setLocalsSet(Set<String> localsSet) {
		this.localsSet = localsSet;
	}
	public Map<Stmt, Pair<FlowSet, FlowSet>> getFinalFlowSets() {
		return finalFlowSets;
	}
	public void setFinalFlowSets(Map<Stmt, Pair<FlowSet, FlowSet>> finalFlowSets) {
		this.finalFlowSets = finalFlowSets;
	}
	public Set<String> getInputSet() {
		return inputSet;
	}
	public void setInputSet(Set<String> inputSet) {
		this.inputSet = inputSet;
	}
	public Set<String> getParamSet() {
		return paramSet;
	}
	public void setParamSet(Set<String> paramSet) {
		this.paramSet = paramSet;
	}
    
}
