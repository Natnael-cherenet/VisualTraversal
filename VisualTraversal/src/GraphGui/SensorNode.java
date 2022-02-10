/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GraphGui;

import java.util.ArrayList;
import javafx.geometry.Point3D;
import javafx.scene.control.Label;
import javafx.scene.shape.Sphere;

/**
 *
 * @author natec
 */
public class SensorNode extends Sphere
{
    Point3D                   location;
    Label                     nodeTag;
    SensorNode                parentNode;
    
    boolean             isVisited;

    public SensorNode(Point3D location, SensorNode parentNode, boolean isVisited, double radius)
    {
        super(radius);
        super.setTranslateX(-location.getX()-10);
        super.setTranslateY(location.getY()+10);
        super.setTranslateZ(-location.getZ()); 
        
        
        this.parentNode = parentNode;
        
        this.isVisited = isVisited;
    }
    
    
    
    public Point3D getLocation()
    {
        return location;
    }

    public void setLocation(Point3D location)
    {
        this.location = location;
    }

    
    
    
    
}
