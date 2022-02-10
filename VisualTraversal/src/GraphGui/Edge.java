/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GraphGui;

import javafx.beans.binding.Bindings;
import javafx.geometry.Point3D;
import javafx.scene.Node;
import javafx.scene.shape.Cylinder;

/**
 *
 * @author natec
 */
public class Edge extends Cylinder
{
    double cost;
    private Edge edge;
    public Edge()
    {
        
    }
    public Edge(double lineThickness, Node node1, Node node2,double rc,double dp,double tc)
    {
        super.setRadius(lineThickness);
        Point3D node1Location = new Point3D(node1.getTranslateX(), node1.getTranslateY(), node1.getTranslateZ());
        Point3D node2Location = new Point3D(node2.getTranslateX(), node2.getTranslateY(), node2.getTranslateZ());
        double distance = node1Location.distance(node2Location);
        super.setHeight(distance);
        super.setTranslateX(node2Location.midpoint(node1Location).getX());
        super.setTranslateY(node2Location.midpoint(node1Location).getY());
        super.setTranslateZ(node2Location.midpoint(node1Location).getZ());
        
       
        super.setRotationAxis(node2Location.subtract(node1Location).crossProduct(new Point3D(0, 1, 0)));       
       
        
        double angleBetweenNodes = Math.acos(node2Location.subtract(node1Location).normalize().dotProduct(new Point3D(0, 1, 0)));
        super.setRotate(-Math.toDegrees(angleBetweenNodes));
        this.cost= (2*rc*dp)+(tc*dp*(distance*distance));
        super.setId(String.format("%.2f",cost));
    }
    
    public void adjustEdge(Edge edge, Node node1, Node node2 ,double rc,double dp,double tc)
    {
        Point3D node1Location = new Point3D(node1.getTranslateX(), node1.getTranslateY(), node1.getTranslateZ());
        Point3D node2Location = new Point3D(node2.getTranslateX(), node2.getTranslateY(), node2.getTranslateZ());
        double distance = node1Location.distance(node2Location);
        edge.setHeight(distance);
        edge.setTranslateX(node2Location.midpoint(node1Location).getX());
        edge.setTranslateY(node2Location.midpoint(node1Location).getY());
        edge.setTranslateZ(node2Location.midpoint(node1Location).getZ());

        edge.setRotationAxis(node2Location.subtract(node1Location).crossProduct(new Point3D(0, 1, 0)));

        double angleBetweenNodes = Math.acos(node2Location.subtract(node1Location).normalize().dotProduct(new Point3D(0, 1, 0)));
        edge.setRotate(-Math.toDegrees(angleBetweenNodes));
        edge.cost = (2 * rc * dp) + (tc * dp * (distance * distance));
        edge.setId(String.format("%.2f", cost));
        
    }

   
    
    
}
// edge.translateXProperty().bind(Bindings.createDoubleBinding(
//                    () -> node2Location.midpoint(node1Location).getX(),
//                    node2.translateXProperty(), node1.translateXProperty()));
//            edge.translateYProperty().bind(Bindings.createDoubleBinding(
//                    () -> node2Location.midpoint(node1Location).getY(),
//                    node2.translateYProperty(), node1.translateYProperty()));
//            edge.translateZProperty().bind(Bindings.createDoubleBinding(
//                    () -> node2Location.midpoint(node1Location).getZ(),
//                    node2.translateZProperty(), node1.translateZProperty()));
//            edge.setRotationAxis(node2Location.subtract(node1Location).crossProduct(new Point3D(0, 1, 0)));
//            edge.rotationAxisProperty().bind(Bindings.createObjectBinding(
//                    () -> node2Location.subtract(node1Location).crossProduct(new Point3D(0, 1, 0)),
//                    node2.translateXProperty(), node1.translateXProperty(),
//                    node2.translateYProperty(), node1.translateYProperty(),
//                    node2.translateZProperty(), node1.translateZProperty()));
//
//            edge.rotateProperty().bind(Bindings.createDoubleBinding(
//                    () -> -Math.toDegrees(
//                            Math.acos(node2Location.subtract(node1Location).
//                                    normalize().dotProduct(new Point3D(0, 1, 0)))),
//                    node2.translateXProperty(), node1.translateXProperty(),
//                    node2.translateYProperty(), node1.translateYProperty(),
//                    node2.translateZProperty(), node1.translateZProperty()));
//            edge.heightProperty().bind(Bindings.createDoubleBinding(
//                    () ->node1Location.distance(node2Location),
//                    node2.translateXProperty(), node1.translateXProperty(),
//                    node2.translateYProperty(), node1.translateYProperty(),
//                    node2.translateZProperty(), node1.translateZProperty()));