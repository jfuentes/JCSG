package eu.mihosoft.vrl.v3d;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by joel on 1/23/17.
 */
public class NodeTask implements Runnable{
    private Node currentNode;
    List<Polygon> polygons;
    private ThreadPoolExecutor executor;
    private AtomicInteger count;

    public NodeTask(ThreadPoolExecutor executor, List<Polygon> polygons, Node currentNode, AtomicInteger count){
        this.currentNode = currentNode;
        this.executor = executor;
        this.polygons = polygons;
        this.count = count;
    }

    public void run(){
        if (polygons.isEmpty()){
            count.decrementAndGet();
            return;
        }

        if (currentNode.plane == null) {
            currentNode.plane = polygons.get(0).plane.clone();
        }

        List<Polygon> frontP = new ArrayList<>();
        List<Polygon> backP = new ArrayList<>();

        // parellel version does not work here
        polygons.forEach((polygon) -> {
            currentNode.plane.splitPolygon(
                    polygon, currentNode.polygons, currentNode.polygons, frontP, backP);
        });

        if (frontP.size() > 0) {
            if (currentNode.front == null) {
                currentNode.front = new Node();
            }
            NodeTask task1 = new NodeTask(executor, frontP, currentNode.front, count);
            count.addAndGet(1);
            executor.execute(task1);

            //this.front.build(frontP);
        }
        if (backP.size() > 0) {
            if (currentNode.back == null) {
                currentNode.back = new Node();
            }
            NodeTask task2 = new NodeTask(executor, backP, currentNode.back, count);
            count.addAndGet(1);
            executor.execute(task2);

            //this.back.build(backP);
        }

        count.decrementAndGet();


    }
}
