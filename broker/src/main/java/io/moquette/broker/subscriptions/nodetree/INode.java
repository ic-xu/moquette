package io.moquette.broker.subscriptions.nodetree;

import java.util.concurrent.atomic.AtomicReference;

public class INode {
    private AtomicReference<CNode> mainNode = new AtomicReference<>();

    public INode(CNode mainNode) {
        this.mainNode.set(mainNode);
        if (mainNode instanceof TNode) { // this should never happen
            throw new IllegalStateException("TNode should not be set on mainNnode");
        }
    }

    boolean compareAndSet(CNode old, CNode newNode) {
        return mainNode.compareAndSet(old, newNode);
    }

    boolean compareAndSet(CNode old, TNode newNode) {
        return mainNode.compareAndSet(old, newNode);
    }

    public CNode mainNode() {
        return this.mainNode.get();
    }

    boolean isTombed() {
        return this.mainNode() instanceof TNode;
    }

//
//    @Override
//    public int hashCode() {
//        return mainNode.get().hashCode();
//    }
//
//    @Override
//    public boolean equals(Object obj) {
//        try{
//            INode iNode = (INode)obj;
//            if(iNode.mainNode().token.equals(this.mainNode().token)){
//                return true;
//            }else return false;
//
//        }catch (Exception e){
//            return false;
//        }
//    }
}
