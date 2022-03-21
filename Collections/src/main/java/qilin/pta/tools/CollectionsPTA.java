package qilin.pta.tools;

import qilin.core.parm.ctxcons.CollectionCtxConstructor;
import qilin.parm.heapabst.AllocSiteAbstractor;
import qilin.parm.heapabst.HeuristicAbstractor;
import qilin.parm.select.CtxSelector;
import qilin.parm.select.HeuristicSelector;
import qilin.parm.select.PipelineSelector;
import qilin.parm.select.UniformSelector;
import qilin.pta.PTAConfig;

public class CollectionsPTA extends BasePTA {
    public CollectionsPTA() {
        this.ctxCons = new CollectionCtxConstructor();
        CtxSelector us = new UniformSelector(3, 2);
        if (PTAConfig.v().getPtaConfig().enforceEmptyCtxForIgnoreTypes) {
            this.ctxSel = new PipelineSelector(new HeuristicSelector(), us);
        } else {
            this.ctxSel = us;
        }
        if (PTAConfig.v().getPtaConfig().mergeHeap) {
            System.out.println(".... Heuristic...");
            this.heapAbst = new HeuristicAbstractor(pag);
        } else {
            this.heapAbst = new AllocSiteAbstractor(pag);
        }
        System.out.println("CollectionsPTA ...");
    }
}
