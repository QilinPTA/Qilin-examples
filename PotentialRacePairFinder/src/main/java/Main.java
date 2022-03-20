import qilin.core.PTA;
import qilin.util.PTAUtils;
import qilin.util.Pair;
import qilin.util.Stopwatch;
import soot.Body;
import soot.Local;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.InstanceFieldRef;

import java.util.HashSet;
import java.util.Set;

/*
* find potential data-race pairs.
* (1) two different statements: a.f = ..., b.f = ...; check whether a aliases with b.
* (2) two different statements: a.f = ..., ... = b.f, check whether a aliases with b.
* (3) a and b should be local variables defined in application code.
* (4) a = b is allowed.
* */
public class Main {

    public static void main(String[] args) {
        Stopwatch stopwatch = Stopwatch.newAndStart("Pointer Analysis");
        PTA pta = driver.Main.run(args);
        stopwatch.stop();
        System.out.println(stopwatch);
        boolean verbose = false;
        run(pta, verbose);
    }

    public static void run(PTA pta, boolean verbose) {
        Set<Unit> writes = new HashSet<>();
        Set<Unit> reads = new HashSet<>();
        pta.getNakedReachableMethods().forEach(m -> {
            if (!m.isConcrete()) {
                return;
            }
            if (!m.getDeclaringClass().isApplicationClass()) {
                return;
            }
            Body body = PTAUtils.getMethodBody(m);
            for(Unit unit: body.getUnits()) {
                if (unit instanceof AssignStmt as) {
                    Value l = as.getLeftOp();
                    Value r = as.getRightOp();
                    if (l instanceof InstanceFieldRef) {
                        writes.add(unit);
                    } else if (r instanceof InstanceFieldRef) {
                        reads.add(unit);
                    }
                }
            }
        });
        System.out.println("#write:" + writes.size());
        System.out.println("#read:" + reads.size());

        Set<Pair<Unit, Unit>> potentialRacePairs = new HashSet<>();
        // check write and write pair
        Unit[] mWrites = writes.toArray(writes.toArray(new Unit[0]));
        for(int i = 0; i < mWrites.length; ++i) {
            Unit u1 = mWrites[i];
            for(int j = i + 1; j < mWrites.length; ++j) {
                Unit u2 = mWrites[j];
                AssignStmt as1 = (AssignStmt) u1;
                AssignStmt as2 = (AssignStmt) u2;
                InstanceFieldRef l1 = (InstanceFieldRef) as1.getLeftOp();
                InstanceFieldRef l2 = (InstanceFieldRef) as2.getLeftOp();
                if (l1.getField() != l2.getField()) {
                    continue;
                }
                Local base1 = (Local) l1.getBase();
                Local base2 = (Local) l2.getBase();
                if (pta.mayAlias(base1, base2)) {
                    potentialRacePairs.add(new Pair<>(u1, u2));
                }
            }
        }
        int wwRace = potentialRacePairs.size();
        System.out.println("#write-write-race:" + wwRace);
        // check read and write pair
        for(Unit u1 : writes) {
            for(Unit u2 : reads) {
                AssignStmt as1 = (AssignStmt) u1;
                AssignStmt as2 = (AssignStmt) u2;
                InstanceFieldRef l1 = (InstanceFieldRef) as1.getLeftOp();
                InstanceFieldRef l2 = (InstanceFieldRef) as2.getRightOp();
                if (l1.getField() != l2.getField()) {
                    continue;
                }
                Local base1 = (Local) l1.getBase();
                Local base2 = (Local) l2.getBase();
                if (pta.mayAlias(base1, base2)) {
                    potentialRacePairs.add(new Pair<>(u1, u2));
                }
            }
        }
        System.out.println("#read-write-race:" + (potentialRacePairs.size() - wwRace));
        if (verbose) {
            System.out.println("Race pairs:");
            for(Pair<Unit, Unit> pair : potentialRacePairs) {
                System.out.println(pair.getFirst() + "\t" + pair.getSecond());
            }
        }
    }
}
