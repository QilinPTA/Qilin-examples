package qilin.core.parm.ctxcons;

import qilin.core.PTAScene;
import qilin.core.context.ContextElement;
import qilin.core.context.ContextElements;
import qilin.core.pag.CallSite;
import qilin.core.pag.ContextAllocNode;
import qilin.parm.ctxcons.CtxConstructor;
import soot.*;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CollectionCtxConstructor implements CtxConstructor {
    private final Set<Type> collections = Stream.of(
        RefType.v("java.util.Collection"),
        RefType.v("java.util.Dictionary"),
        RefType.v("java.util.Map"),
        RefType.v("java.util.Arrays"),
        RefType.v("java.util.Collections")
    ).collect(Collectors.toSet());


    @Override
    public Context constructCtx(MethodOrMethodContext caller, ContextAllocNode receiverNode, CallSite callSite, SootMethod target) {
        boolean flag = false;
        RefType declClassType = target.getDeclaringClass().getType();
        for(Type type : collections) {
            if(PTAScene.v().getOrMakeFastHierarchy().canStoreType(declClassType, type)) {
                flag = true;
                break;
            }
        }
        // if target method is not a method declared in any subclasses of collections, return empty context.
        if (!flag) {
            return emptyContext;
        }
        // otherwise, construct the callee context as kOBJ.
        Context callerContext = caller.context();
        if (receiverNode == null) { // static invoke
            return callerContext;
        }
        Context context = receiverNode.context();
        assert context instanceof ContextElements;
        ContextElements ctxElems = (ContextElements) context;
        int s = ctxElems.size();
        ContextElement[] cxtAllocs = ctxElems.getElements();
        ContextElement[] array = new ContextElement[s + 1];
        array[0] = receiverNode.base();
        System.arraycopy(cxtAllocs, 0, array, 1, s);
        return new ContextElements(array, s + 1);
    }
}
