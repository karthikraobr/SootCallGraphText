package Test;

import java.util.Iterator;
import java.util.Map;

import soot.G;
import soot.Main;
import soot.PackManager;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootMethod;
import soot.Transform;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Targets;

public class CallGraphTest {

	public static void iteratre(SootMethod source, CallGraph cg) {
		Targets tg = new Targets(cg.edgesOutOf(source));
		tg.forEachRemaining((edge)->{
			SootMethod sm = edge.method();
			System.out.println("Here is reachable methods\t" + sm.getSignature());
			iteratre(sm, cg);
		});
	}

	public static void runAnalysis() {
		G.reset();
		Transform transform = new Transform("wjtp.analysis", new SceneTransformer() {
			@Override
			protected void internalTransform(String arg0, Map arg1) {
				Scene.v().loadNecessaryClasses();
				Scene.v().loadDynamicClasses();
				
				Scene.v().getClasses().forEach((cls)->{
					cls.getMethods().forEach((mtd)->{
						if(mtd.getName().equals("flowThrough")) {
							System.out.println("Inside flowthrough");
							System.out.println(mtd.retrieveActiveBody());
							iteratre(mtd, Scene.v().getCallGraph());
						}
						
					});
				});
			}
		});
		PackManager.v().getPack("wjtp").add(transform);
		String targetDir = "targetsBin";
		Main.main(new String[] { "-cp", targetDir, "-process-dir", targetDir, "-w",
				 "-exclude", "javax",
				"-p", "cg.spark", "verbose", "true", "-allow-phantom-refs",
				 "-no-bodies-for-excluded",
				"-src-prec", "only-class", "-output-format", "J" });
	}

	public static void main(String[] args) {
		runAnalysis();
	}

}
