package GraphGui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.ThreadLocalRandom;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point3D;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.effect.Bloom;
import javafx.scene.effect.Glow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.util.Duration;
import javafx.util.Pair;

/**
 *
 * @author natec
 */
public class NodeNetwork
{
    

    //For grid newtork, WIP

    public class Cube extends Box
    {

        private Node above;
        private Node below;
        private Node left;
        private Node right;

        public void setAbove(Node above)
        {
            this.above = above;
        }

        public void setBelow(Node below)
        {
            this.below = below;
        }

        public void setLeft(Node left)
        {
            this.left = left;
        }

        public void setRight(Node right)
        {
            this.right = right;
        }

        public Node getAbove()
        {
            return above;
        }

        public Node getBelow()
        {
            return below;
        }

        public Node getLeft()
        {
            return left;
        }

        public Node getRight()
        {
            return right;
        }

    }

    
    public Node currentNode;
    public Node prevNode;
    private Node start;
    private Node end;
    private HashMap<Node, ArrayList<Node>> adjacentNodes = new HashMap<Node, ArrayList<Node>>();
    private HashMap<Pair<Node, Node>, Edge> edgeToNodePairing = new HashMap<>();
    private HashMap<Node,HashSet<Node>> connected= new HashMap<>();
     private HashMap<Node, Boolean> visited = new HashMap<>();
     private HashMap<Node, ArrayList<Pair<Pair<Node,Node>,Edge>>> APSP = new HashMap<>();
    private HashMap<Node, Node> nodeParent = new HashMap<Node, Node>();
    private HashMap<Node,Double> distanceToNode= new HashMap<>();
    private HashMap<Pair<Node,Node>,Node> SPT= new HashMap<>();
    private ArrayList<KeyFrame> frames = new ArrayList<>();
    private ObservableList<ObservableList<String>> rt = FXCollections.observableArrayList();

    private ObservableList<Node> sensorNetwork = FXCollections.observableArrayList();
    private ObservableList<Node> gridNetwork = FXCollections.observableArrayList();
    private ObservableList<Node> grid = FXCollections.observableArrayList();
    private ObservableList<Node> toExplore = FXCollections.observableArrayList();
    private ObservableList<Node> edgeBetweenNodes = FXCollections.observableArrayList();
    private ObservableList<Node> nodeTags = FXCollections.observableArrayList();

    private Timeline timeline = new Timeline();
    private Timeline timeline2 = new Timeline();
    private TableView<ObservableList<String>> table;

    int frameCount = 0;
    int counter = 0;
    private boolean startState = false;
    boolean allVisited=false;
    static final double transmissionCost= 100;
    static final double receivingCost= 100;
    static int kEdges=0;
    double dataPackets=0;
    boolean firstRun = true;

   public void resetSensorNetwork()
    {
        firstRun = true;
        if(!grid.isEmpty())
        {
            grid.forEach(node ->
            {
                
                ((Cube) node).setMaterial(new PhongMaterial(Color.CYAN));
                ((Cube) node).setScaleX(1);
                if (node.getId().endsWith("barrier"))
                {
                    System.out.println(node.getId());
                    node.setId(node.getId().substring(0, node.getId().indexOf("barrier")));
                    System.out.println(node.getId());
                }
            });
        }
        if(!sensorNetwork.isEmpty())
        {
            
            edgeToNodePairing.forEach((vertices,edge)->
            {
                if(vertices.getKey() instanceof Sphere&&vertices.getValue() instanceof Sphere)
                {
                    if(vertices.getKey().getId().startsWith("DN"))
                    {
                        ((Sphere)vertices.getKey()).setMaterial(new PhongMaterial(Color.BLACK));
                    }
                    else if(vertices.getKey().getId().startsWith("SN"))
                    {
                        ((Sphere)vertices.getKey()).setMaterial(new PhongMaterial(Color.CYAN));
                    }                    
                    ((Sphere) vertices.getKey()).setRadius(1);
                    
                }
                if(vertices.getKey() instanceof Cube &&vertices.getValue() instanceof Cube)
                {
                    ((Cube)vertices.getKey()).setMaterial(new PhongMaterial(Color.CYAN));
                    ((Cube)vertices.getValue()).setMaterial(new PhongMaterial(Color.CYAN));
                }
                edge.setMaterial(new PhongMaterial(Color.RED));
                Point3D node1Location = new Point3D(vertices.getKey().getTranslateX(),
                                             vertices.getKey().getTranslateY(), 
                                             vertices.getKey().getTranslateZ());
                Point3D node2Location = new Point3D(vertices.getValue().getTranslateX(), 
                                             vertices.getValue().getTranslateY(), 
                                             vertices.getValue().getTranslateZ());
                double distance = node1Location.subtract(node2Location).magnitude();
                distance = (2 * receivingCost * dataPackets) + (transmissionCost * dataPackets * (distance * distance));
                edge.setMaterial(new PhongMaterial(Color.RED));
                edge.setRadius(0.15);
                
                edge.cost = distance;
                edge.setRadius(0.15);                
                
                visited.put(vertices.getKey(), false);
                distanceToNode.put(vertices.getKey(), Double.POSITIVE_INFINITY);
                
            });
        }
        if(!gridNetwork.isEmpty())
        {
            gridNetwork.forEach(node -> 
            {
                ((Sphere) node).setMaterial(new PhongMaterial(Color.BLACK));
                ((Sphere) node).setRadius(1);
            });
            edgeBetweenNodes.forEach(node ->
            {
                ((Edge) node).setMaterial(new PhongMaterial(Color.RED));
                ((Edge) node).setRadius(0.15);
            });
            edgeToNodePairing.forEach((vertices,edge)->
            {
                if(vertices.getKey() instanceof Sphere&&vertices.getValue() instanceof Sphere)
                {
                    ((Sphere)vertices.getKey()).setMaterial(new PhongMaterial(Color.CYAN));
                    ((Sphere)vertices.getValue()).setMaterial(new PhongMaterial(Color.CYAN));
                }
                if(vertices.getKey() instanceof Cube &&vertices.getValue() instanceof Cube)
                {
                    ((Cube)vertices.getKey()).setMaterial(new PhongMaterial(Color.CYAN));
                    ((Cube)vertices.getValue()).setMaterial(new PhongMaterial(Color.CYAN));
                }
                Point3D node1Location = new Point3D(vertices.getKey().getTranslateX(), vertices.getKey().getTranslateY(), vertices.getKey().getTranslateZ());
                Point3D node2Location = new Point3D(vertices.getValue().getTranslateX(), vertices.getValue().getTranslateY(), vertices.getValue().getTranslateZ());
                edge.setMaterial(new PhongMaterial(Color.RED));
                edge.setRadius(0.15);
                edge.setId(String.format("%.2f", node1Location.distance(node2Location)));
                
                visited.put(vertices.getKey(), false);
                distanceToNode.put(vertices.getKey(), Double.POSITIVE_INFINITY);
            });
        }

        setStart(null);
        setEnd(null);
        timeline.getKeyFrames().clear();
        timeline2.getKeyFrames().clear();
        frames.clear();
        toExplore.clear();
        
        frameCount = 0;

    }
//    Grid Newtork, WIP
    public ObservableList<Node> makeGrid(int rows, int columns)
    {
        if(!sensorNetwork.isEmpty())
        {
            sensorNetwork.clear();
        }
        if(!gridNetwork.isEmpty())
        {
            gridNetwork.clear();
        }
        if(!adjacentNodes.isEmpty())
        {
            adjacentNodes.clear();
        }
        if(!edgeToNodePairing.isEmpty())
        {
            edgeToNodePairing.clear();
        }
        if(!edgeBetweenNodes.isEmpty())
        {
            edgeBetweenNodes.clear();
        }
        if(!grid.isEmpty())
        {
            return grid;
        }
        int cubeCount = 0;
        for (int i = 0; i < rows; i++)
        {
            for (int j = 0; j < columns; j++)
            {
                Cube cubey = new Cube();

                cubey.setWidth(1);
                cubey.setHeight(1);
                cubey.setDepth(1);

                cubey.setMaterial(new PhongMaterial(Color.CYAN));
                cubey.setTranslateX((-j * 1.3) - 3);
                cubey.setTranslateY((i * 1.3) + 15);
                cubey.setTranslateZ(cubey.getDepth() / 2);

                cubey.setId(String.format("Cube %d ", cubeCount));

                grid.add(cubey);
                cubeCount++;
                cubey.setCache(true);
                cubey.setCacheHint(CacheHint.QUALITY);
                    
                               

            }
        }
             

        grid.sort(Comparator
                .comparingDouble(cube -> ((Cube) cube).getTranslateX())
                .thenComparingDouble(cube -> ((Cube) cube).getTranslateY())
                .thenComparing(cube -> ((Cube) cube).getTranslateZ()));
        return grid;
    }

//    public ObservableList<Node>  make3DGrid(int n, int tr)
//    {
//        
//        int nodeCount = 0;
//        
//        for (int y = 0; y < n; y++)
//        {
//            for (int x = 0; x < n; x++)
//            {
//
//                Sphere node = new Sphere(0.8);
//
//                ((Sphere) node).setMaterial(new PhongMaterial(Color.CYAN));
//
//                //                  
//                node.setTranslateX((-x * 10)-10);
//
//                node.setTranslateY((y * 10)+10);
//
//                node.setId(String.format("Node %d ", nodeCount));
//                node.setCache(true);
//                gridNetwork.add(node);
//                nodeCount++;
//
//            }
//
//        }
//        /*
//         Sort Based on the x,y,z coordinates of each sphere.
//         This will help speed up the pairing of the nodes by decreasing the number of nodes to check distance to.
//         For each node, check the node location + |threshold|, and if a node is with in that distance, pair it, otherwise 
//         do not bother with computing.
//         */
//        gridNetwork.sort(Comparator
//                .comparingDouble(sphere -> ((Sphere) sphere).getTranslateX())
//                .thenComparingDouble(sphere -> ((Sphere) sphere).getTranslateY())
//                .thenComparing(sphere -> ((Sphere) sphere).getTranslateZ()));
//
//        /*
//         Populate Adjacency list based on chosen condition. In this case, 
//         if the distance between any two nodes is less than or equal to the threshold,
//         add each node to eachother's adjacency list. This is a bidirectional approach.                    
//
//         */
//        
//        for (int currentElement = 0; currentElement < gridNetwork.size(); currentElement++)
//        {
//            Sphere node1 = (Sphere) gridNetwork.get(currentElement);
//            adjacentNodes.put(node1, new ArrayList<Node>());
//            for (int elementToCompareTo = 0; elementToCompareTo < gridNetwork.size(); elementToCompareTo++)
//            {
//                Sphere node2 = (Sphere)gridNetwork.get(elementToCompareTo);
//                Point3D origin = new Point3D(node1.getTranslateX(), node1.getTranslateY(), node1.getTranslateZ());
//                Point3D target = new Point3D(node2.getTranslateX(), node2.getTranslateY(), node2.getTranslateZ());
//                if (origin.distance(target) > tr)
//                {
//                    continue;
//                }
//                if (adjacentNodes.get(node1).contains(node2) && adjacentNodes.get(node2).contains(node1))
//                {
//                    continue;
//                } else
//                {
//                    if (!node1.equals(node2) && origin.distance(target) <= tr && adjacentNodes.get(node1) != null && adjacentNodes.get(node2) != null)
//                    {
//
//                        adjacentNodes.get(node1).add(node2);
//
//                        adjacentNodes.get(node2).add(node1);
//
//                        System.out.println(origin);
//
//                        Cylinder edge = bindNodes(0.15,
//                                new Point3D(node1.getTranslateX(), node1.getTranslateY(), node1.getTranslateZ()),
//                                new Point3D(node2.getTranslateX(), node2.getTranslateY(), node2.getTranslateZ())
//                        );
//                        edge.setMaterial(new PhongMaterial(Color.RED));
//                        edgeBetweenNodes.add(edge);
//                        edge.setId(null);
//                        edgeToNodePairing.put(new Pair<>(node1, node2), edge);
//                        edgeToNodePairing.put(new Pair<>(node2, node1), edge);
//
//                    }
//                }
//
//            }
//        }
//
//        return gridNetwork;
//
//    }
    public ObservableList<Node> testNetwork()
    {
        sensorNetwork.clear();
        gridNetwork.clear();
        adjacentNodes.clear();
        edgeToNodePairing.clear();
        edgeBetweenNodes.clear();
        Sphere node1 = new Sphere(0.8);
        Sphere node2 = new Sphere(0.8);
        Sphere node3 = new Sphere(0.8);
        Sphere node4 = new Sphere(0.8);
        Sphere node5 = new Sphere(0.8);
        Sphere node6 = new Sphere(0.8);
        Sphere node7 = new Sphere(0.8);

        ((Sphere) node1).setMaterial(new PhongMaterial(Color.CYAN));
        ((Sphere) node2).setMaterial(new PhongMaterial(Color.CYAN));
        ((Sphere) node3).setMaterial(new PhongMaterial(Color.CYAN));
        ((Sphere) node4).setMaterial(new PhongMaterial(Color.CYAN));
        ((Sphere) node5).setMaterial(new PhongMaterial(Color.CYAN));
        ((Sphere) node6).setMaterial(new PhongMaterial(Color.CYAN));
        ((Sphere) node7).setMaterial(new PhongMaterial(Color.CYAN));
        

        node1.setTranslateX(-40);
        node1.setTranslateY(100);
        node1.setTranslateZ(0);
        node1.setId(String.format("Node-%d ", 1));
        node1.setCache(true);
        gridNetwork.add(node1);
        node2.setTranslateX(-80);
        node2.setTranslateY(100);
        node2.setTranslateZ(0);
        node2.setId(String.format("Node-%d ", 2));
        node2.setCache(true);
        gridNetwork.add(node2);
        node3.setTranslateX(-20);
        node3.setTranslateY(50);
        node3.setTranslateZ(0);
        node3.setId(String.format("Node-%d ", 3));
        node3.setCache(true);
        gridNetwork.add(node3);
        node4.setTranslateX(-60);
        node4.setTranslateY(50);
        node4.setTranslateZ(0);
        node4.setId(String.format("Node-%d ", 4));
        node4.setCache(true);
        gridNetwork.add(node4);
        node5.setTranslateX(-100);
        node5.setTranslateY(50);
        node5.setTranslateZ(0);
        node5.setId(String.format("Node-%d ", 5));
        node5.setCache(true);
        gridNetwork.add(node5);
        node6.setTranslateX(-40);
        node6.setTranslateY(10);
        node6.setTranslateZ(0);
        node6.setId(String.format("Node-%d ", 6));
        node6.setCache(true);
        gridNetwork.add(node6);
        node7.setTranslateX(-80);
        node7.setTranslateY(10);
        node7.setTranslateZ(0);
        node7.setId(String.format("Node-%d ", 7));
        node7.setCache(true);
        gridNetwork.add(node7);
        for (int i = 0; i < 7; i++)
        {
            adjacentNodes.put(gridNetwork.get(i), new ArrayList<>());
                
        }
        adjacentNodes.get(node1).addAll(Arrays.asList(node2, node3,node4,node6));
        adjacentNodes.get(node2).addAll(Arrays.asList(node1, node4,node5,node7));
        adjacentNodes.get(node3).addAll(Arrays.asList(node1, node6));
        adjacentNodes.get(node4).addAll(Arrays.asList(node1, node2,node6, node7));
        adjacentNodes.get(node5).addAll(Arrays.asList(node2,node7));
        adjacentNodes.get(node6).addAll(Arrays.asList(node1,node3, node4,node7));
        adjacentNodes.get(node7).addAll(Arrays.asList(node2,node4, node5,node6));
        adjacentNodes.forEach((node,neighbors)->
        {
            
            neighbors.forEach(neighbor->
            {
                Edge edge = new Edge(0.15, node, neighbor, 1, 1, 1);

                edge.setMaterial(new PhongMaterial(Color.RED));
                edgeBetweenNodes.add(edge);

                edgeToNodePairing.put(new Pair<>((Sphere) node, (Sphere) neighbor), edge);
                edgeToNodePairing.put(new Pair<>((Sphere) neighbor, (Sphere) node), edge);

                
            });

                        
        });
        
        return gridNetwork;
            
    }
    public void make3DGraph(int width, int height, int depth,int n, int tr,int DN, double DP, double SC)
    {
        dataPackets = DP;
        
        sensorNetwork.clear();
        gridNetwork.clear();
        adjacentNodes.clear();
        edgeToNodePairing.clear();
        edgeBetweenNodes.clear();
        nodeTags.clear();
        if(n<=0)
        {
            System.out.println("Network has zero nodes");
            return;
        }
        
        System.out.println("---------------------------------------------------------------------");
        System.out.printf("\n%-15s%-15s%-15s\n","Node ID", "Node Type","Location");
        System.out.println("---------------------------------------------------------------------");
        for (int i = 0; i < n; i++)
        {
            Point3D location = new Point3D(width == 0 ? 0 : ThreadLocalRandom.current().nextDouble(0, width),
                                           height == 0 ? 0 : ThreadLocalRandom.current().nextDouble(0, height),
                                           depth == 0 ? 0 : ThreadLocalRandom.current().nextDouble(0, depth));
            SensorNode node = new SensorNode(location, null, false, 0.8);
            
            ((Sphere) node).setMaterial(new PhongMaterial(Color.CYAN));

            if(i<=DN && DN >0)
            {
                node.setId(String.format("DN_%d ", i));
                System.out.printf("%-15d%-15s(%.2f,%.2f,%.2f)\n",i, "Data Node",
                        location.getX(),location.getY(),location.getZ());
                ((Sphere) node).setMaterial(new PhongMaterial(Color.BLACK));
            }
            else
            {
                node.setId(String.format("SNode_%d ", i));
                System.out.printf("%-15d%-15s(%.2f,%.2f,%.2f)\n",i, "Sensor Node",
                        location.getX(),location.getY(),location.getZ());
                
            }
            double scaleFactor = 0.25;
            Label nodeTag = new Label();
            nodeTag.setText(node.getId());            
            nodeTag.setFont(Font.font("Calibri", FontPosture.REGULAR, 8));
            nodeTag.setCache(true);
            nodeTag.setCacheHint(CacheHint.QUALITY);
            nodeTag.setStyle("-fx-background-color: BLACK;"
                     + "-fx-Text-fill: #ffffff;"
                     + "-fx-border-color: #ffffff;");
            nodeTag.setPrefWidth(38);            
            nodeTag.setTranslateZ(-node.getRadius());
            nodeTag.setRotate(180);
            nodeTag.translateXProperty().bind(Bindings.createDoubleBinding(()->node.getTranslateX()-20, node.translateXProperty()));
            nodeTag.translateYProperty().bind(Bindings.createDoubleBinding(()->node.getTranslateY()-2.5, node.translateYProperty()));
            nodeTag.translateZProperty().bind(Bindings.createDoubleBinding(()->node.getTranslateZ()-node.getRadius(), node.translateZProperty()));            
            nodeTag.setMouseTransparent(true);
            nodeTags.add(nodeTag);
            nodeTag.setScaleX(scaleFactor);
            nodeTag.setScaleY(scaleFactor);
            
            
            node.setCache(true);
            node.setCacheHint(CacheHint.QUALITY);
            sensorNetwork.add(node);
            visited.put(node, false);
            distanceToNode.put(node, Double.POSITIVE_INFINITY);
        }
        /*
         Sort Based on the x,y,z coordinates of each sphere.
         This will help speed up the pairing of the nodes by decreasing the number of nodes to check distance to.
         For each node, check the node location + |threshold|, and if a node is with in that distance, pair it, otherwise 
         do not bother with computing.
         */
//        sensorNetwork.sort(Comparator
//                .comparingDouble(sphere -> ((Sphere) sphere).getTranslateX())
//                .thenComparingDouble(sphere -> ((Sphere) sphere).getTranslateY())
//                .thenComparing(sphere -> ((Sphere) sphere).getTranslateZ()));

        /*
         Populate Adjacency list based on chosen condition. In this case, 
         if the distance between any two nodes is less than or equal to the threshold,
         add each node to eachother's adjacency list. This is a bidirectional approach.                    
            
         */
        
          for (int currentElement = 0; currentElement < sensorNetwork.size(); currentElement++)
          {
              
            Sphere node1 = (Sphere) sensorNetwork.get(currentElement);
            adjacentNodes.put(node1, new ArrayList<Node>());
            for (int elementToCompareTo = 0; elementToCompareTo < sensorNetwork.size(); elementToCompareTo++)
            {
                Sphere node2 = (Sphere) sensorNetwork.get(elementToCompareTo);
                Point3D node1Location = new Point3D(node1.getTranslateX(), node1.getTranslateY(), node1.getTranslateZ());
                Point3D node2Location = new Point3D(node2.getTranslateX(), node2.getTranslateY(), node2.getTranslateZ());
                
//                if (edgeToNodePairing.containsKey(new Pair<>((Sphere)node1, (Sphere)node2))||edgeToNodePairing.containsKey(new Pair<>((Sphere)node2, (Sphere)node1)))
//                {
//                    System.out.println("edge already seen");
//                    continue;
//                } else
//                if (adjacentNodes.get(node1).contains(node2))
//                {
//                    
//                    continue;
//                } 
//                else
                {
                    if (!node1.equals(node2) && node1Location.distance(node2Location) <= tr)
                    {
                        
                        adjacentNodes.get(node1).add(node2);

//                        adjacentNodes.get(node2).add(node1);

                        

                        Edge edge = new Edge(0.15, node1, node2, receivingCost, DP, transmissionCost);
                        
//                        Edge edge = bindNodes(DP,0.15,
//                                new Point3D(node1.getTranslateX(), node1.getTranslateY(), node1.getTranslateZ()),
//                                new Point3D(node2.getTranslateX(), node2.getTranslateY(), node2.getTranslateZ())
//                        );
                        edge.setMaterial(new PhongMaterial(Color.RED));
                        edgeBetweenNodes.add(edge);
                        edge.setCache(true);
                        edge.setCacheHint(CacheHint.QUALITY);
                        
                        

                        edgeToNodePairing.put(new Pair<>(node1, node2), edge);
//                        edgeToNodePairing.put(new Pair<>(node2, node1), edge);

                    }
                }

            }

        }      

    }

    public void bfs(String graphType)
    {
        timeline.getKeyFrames().clear();
        timeline2.getKeyFrames().clear();        
        if(graphType.equalsIgnoreCase("network"))
        {
            for (int i = 0; i < sensorNetwork.size(); i++)
            {
                visited.put(sensorNetwork.get(i), false);

            }
        }
        else if(graphType.equalsIgnoreCase("gridNetwork"))
        {
            for (int i = 0; i < gridNetwork.size(); i++)
            {
                visited.put(gridNetwork.get(i), false);

            }
        }
        else if (graphType.equalsIgnoreCase("grid"))
        {
            for (int i = 0; i < grid.size(); i++)
            {
                visited.put(grid.get(i), false);

            }
            for (int currentElement = 0; currentElement < grid.size(); currentElement++)
            {
                Cube node1 = (Cube) grid.get(currentElement);
                adjacentNodes.put(node1, new ArrayList<Node>());
                if (node1.getId().endsWith("barrier"))
                {
                    continue;
                }
                for (int elementToCompareTo = 0; elementToCompareTo < grid.size(); elementToCompareTo++)
                {
                    Cube node2 = (Cube) grid.get(elementToCompareTo);
                    if (node2.getId().endsWith("barrier"))
                    {
                        continue;
                    }
                    Point3D node1Location = new Point3D(node1.getTranslateX(), node1.getTranslateY(), node1.getTranslateZ());
                    Point3D node2Location = new Point3D(node2.getTranslateX(), node2.getTranslateY(), node2.getTranslateZ());
                    if(node1Location.distance(node2Location) > node1.getWidth() + 0.55)
                    {
                        continue;
                    }
                    if (!node1.equals(node2) && node1Location.distance(node2Location) <= node1.getWidth() + 0.55 && adjacentNodes.get(node1) != null && adjacentNodes.get(node2) != null && !grid.get(currentElement).getId().endsWith("barrier"))
                    {

                        adjacentNodes.get(node1).add(node2);

                        adjacentNodes.get(node2).add(node1);

                    }

                }

            }
        }
            
//        adjacentNodes.forEach((k, v) ->
//        {
//            System.out.printf("%s", k.getId());
//            for (int i = 0; i < v.size(); i++)
//            {
//                System.out.printf("-> %s", v.get(i).getId());
//
//            }
//            System.out.println();
//        });

        Node currentNode = null;

        Queue<Node> queue = new LinkedList();

        queue.add(getStart());
        nodeParent.put(getStart(), null);
        toExplore.clear();

        visited.put(getStart(), true);
        int firstCounter = 0;
        int counter=0;
        
        while (!queue.isEmpty())
        {
           Node temps = currentNode = queue.poll();
           Point3D nodeLocation = new Point3D(currentNode.getTranslateX(), currentNode.getTranslateY(), currentNode.getTranslateZ());
           
           
           if (temps instanceof Cube)
            {

                KeyFrame kf2 = new KeyFrame(Duration.millis(firstCounter == 0 ? 30 : firstCounter * 20), e ->
                {
                    ((Cube) temps).setMaterial(new PhongMaterial(Color.color(Math.abs(nodeLocation.normalize().getY()),Math.abs(nodeLocation.normalize().getX()),0)));

                });

                KeyFrame kf4 = new KeyFrame(Duration.millis(firstCounter == 0 ? 32 : firstCounter * 22),
                        new KeyValue(temps.scaleXProperty(), 1));

                timeline.getKeyFrames().addAll(kf2, kf4);
                frames.add(kf2);
                frames.add(kf4);
            }
//            if (temps instanceof Sphere)
//            {
//                KeyFrame kf2 = new KeyFrame(Duration.millis(firstCounter == 0 ? 50 : firstCounter * 50),
//                        e -> ((Sphere) temps).setMaterial(new PhongMaterial(Color.GREEN)));
//                timeline.getKeyFrames().add(kf2);
//                frames.add(kf2);
//            }
           

            if (currentNode.equals(getEnd()))
            {
                
                frameCount=0;
                while (nodeParent.get(currentNode) != null)
                {
                    KeyFrame kf = null;
                    Node temp = nodeParent.get(currentNode);
                    if (temp instanceof Sphere)
                    {
                        Edge cyl = (Edge) edgeToNodePairing.get(new Pair<>(currentNode, temp));
//                        cyl.materialProperty().bind(Bindings.createObjectBinding(
//                        ()-> new PhongMaterial(path.getValue()),path.valueProperty()));

                        kf = new KeyFrame(Duration.millis(frameCount == 0 ? 10 : frameCount * 10),
                                e ->
                                {
                                    Glow glow = new Glow();
                                    glow.setLevel(0.9);

                                    cyl.setMaterial(new PhongMaterial(Color.BLACK));
                                    cyl.setRadius(3);
                                    cyl.setEffect(glow);
                                });
                        timeline2.getKeyFrames().add(kf);
                        frames.add(kf);

                    } else if (temp instanceof Cube)
                    {
                        kf = new KeyFrame(Duration.millis(frameCount == 0 ? 10 : frameCount * 10),
                                e ->
                                {
                                    Bloom glow = new Bloom();
                                    glow.setThreshold(0.1);

                                    ((Cube) temp).setMaterial(new PhongMaterial(Color.AQUA));

                                    ((Cube) temp).setEffect(glow);
                                });
                        timeline2.getKeyFrames().add(kf);
                        frames.add(kf);
                    }
                    currentNode = temp;

                    frameCount++;
                }

//                    
//                return;
            }

//            System.out.printf("%s", currentNode.getId());

            if (adjacentNodes.get(currentNode).isEmpty())
            {
                continue;
            }
            
            firstCounter++;
            
            
            for (int i = 0; i < adjacentNodes.get(currentNode).size(); i++)
            {
             
                Node neighbor = adjacentNodes.get(currentNode).get(i);

                if (!visited.get(neighbor))
                {
                    nodeParent.put(neighbor, currentNode);
                    visited.put(neighbor, true);
//                    System.out.printf("->%s", neighbor.getId());
                    queue.add(neighbor);
                    
                    Button b= new Button(neighbor.getId());
                    if(graphType.equalsIgnoreCase("network"))
                    {
                        b.setOnAction(act ->
                        {
                            Material m= ((Sphere) neighbor).getMaterial();
                            Timeline temp =  new Timeline();
                            KeyFrame kf= new KeyFrame(Duration.millis(100),e ->
                                    {
                                        ((Sphere) neighbor).setMaterial(new PhongMaterial(Color.CYAN));

                                    }, new KeyValue(((Sphere) neighbor).radiusProperty(), 3));
                            KeyFrame kf2= new KeyFrame(Duration.millis(600),e ->
                                    {
                                        ((Sphere) neighbor).setMaterial(m);

                                    }, new KeyValue(((Sphere) neighbor).radiusProperty(), 1));
                            temp.getKeyFrames().addAll(kf,kf2);
                            temp.setCycleCount(1);
                            temp.play();
                        });
                    }
                    else if (graphType.equalsIgnoreCase("grid"))
                    {
                        b.setOnAction(act ->
                        {
                            Material m= ((Cube) neighbor).getMaterial();
                            Timeline temp =  new Timeline();
                            KeyFrame kf= new KeyFrame(Duration.millis(100),e ->
                                    {
                                        ((Cube) neighbor).setMaterial(new PhongMaterial(Color.CYAN));

                                    }, new KeyValue(((Cube) neighbor).widthProperty(), 3));
                            KeyFrame kf2= new KeyFrame(Duration.millis(600),e ->
                                    {
                                        ((Sphere) neighbor).setMaterial(m);

                                    }, new KeyValue(((Cube) neighbor).widthProperty(), 2));
                            temp.getKeyFrames().addAll(kf,kf2);
                            temp.setCycleCount(1);
                            temp.play();
                        });
                    }
                    toExplore.add(b);
                        
                    
                    if (neighbor instanceof Cube)
                    {
                        if (neighbor.equals(end))
                        {
                            KeyFrame kf = new KeyFrame(Duration.millis(counter == 0 ? 10 : counter * 10), e
                                    -> ((Cube) neighbor).setMaterial(new PhongMaterial(Color.AQUA)));
                            timeline2.getKeyFrames().add(0, kf);
                            frames.add(kf);
                        }

                        
                    }
                    else if (neighbor instanceof Sphere)
                    {

                        Edge cyls = (Edge) edgeToNodePairing.get(new Pair<>(currentNode, neighbor));
                        
                        if (neighbor.equals(end))
                        {
                            KeyFrame kf = new KeyFrame(Duration.millis(counter == 0 ? 10 : counter * 10), e
                                    -> ((Sphere) neighbor).setMaterial(new PhongMaterial(Color.RED)));
                            timeline2.getKeyFrames().add(0, kf);
                        }
//                        cyls.materialProperty().bind(Bindings.createObjectBinding(
//                        ()-> new PhongMaterial(path.getValue()),path.valueProperty()));
                       
                        
                        KeyFrame kf5 = new KeyFrame(Duration.millis(counter == 0 ? 1.75 : counter ),
                                e -> ((Sphere) neighbor).setMaterial(new PhongMaterial(Color.BLUE)),
                                new KeyValue(cyls.materialProperty(), new PhongMaterial(Color.GREEN)),
                                new KeyValue(cyls.radiusProperty(), 0.3));
                        
                        timeline.getKeyFrames().addAll(kf5);
                        frames.add(kf5);
                        

                    }

                    
//                   
                }
                counter+=2;
            
            }
                
            System.out.println();
        }
       
        queue.clear();
        

        start = null;
        end = null;

    }
//======== Depth first search algorithm using stack====================//
    public boolean dfs(String graphType)
    {
        if(getStart() == null)
        {
            return false;
        }
        connected.clear();
        timeline.getKeyFrames().clear();
        timeline2.getKeyFrames().clear();
        Stack stack = new Stack();
        toExplore.clear();//Clear the list of nodes to visit
        stack.add(getStart());//Add the start node onto stack
        
        visited = new HashMap<Node, Boolean>();
        nodeParent = new HashMap<Node, Node>();
        if(graphType.equalsIgnoreCase("network"))
        {
            for (int i = 0; i < sensorNetwork.size(); i++)
            {
                visited.put(sensorNetwork.get(i), false);

            }
        }
        else if(graphType.equalsIgnoreCase("gridNetwork"))
        {
            for (int i = 0; i < gridNetwork.size(); i++)
            {
                visited.put(gridNetwork.get(i), false);

            }
            
        }
        else if (graphType.equalsIgnoreCase("grid"))
        {
            for (int i = 0; i < grid.size(); i++)
            {
                visited.put(grid.get(i), false);

            }
            for (int currentElement = 0; currentElement < grid.size(); currentElement++)
            {
                Cube node1 = (Cube) grid.get(currentElement);
                adjacentNodes.put(node1, new ArrayList<Node>());
                if (node1.getId().endsWith("barrier"))
                {
                    continue;
                }
                for (int elementToCompareTo = 0; elementToCompareTo < grid.size(); elementToCompareTo++)
                {
                    Cube node2 = (Cube) grid.get(elementToCompareTo);
                    if (node2.getId().endsWith("barrier"))
                    {
                        continue;
                    }
                    Point3D origin = new Point3D(node1.getTranslateX(), node1.getTranslateY(), node1.getTranslateZ());
                    Point3D target = new Point3D(node2.getTranslateX(), node2.getTranslateY(), node2.getTranslateZ());
                    if(origin.distance(target) > node1.getWidth() + 0.55)
                    {
                        continue;
                    }
                    if (!node1.equals(node2) && origin.distance(target) <= node1.getWidth() + 0.55 && adjacentNodes.get(node1) != null && adjacentNodes.get(node2) != null && !grid.get(currentElement).getId().endsWith("barrier"))
                    {

                        adjacentNodes.get(node1).add(node2);

                        adjacentNodes.get(node2).add(node1);

                    }

                }

            }
        }
       
        
        counter= 0;
        while (!stack.isEmpty())
        {
            
            Node currentNode = (Node) stack.pop();
            Node temps = currentNode;
            visited.put(currentNode, true);
            Button b= new Button(currentNode.getId());
            if(graphType.equalsIgnoreCase("network"))
                    {
                        b.setOnAction(act ->
                        {
                            Material m= ((Sphere) temps).getMaterial();
                            Timeline temp =  new Timeline();
                            KeyFrame kf= new KeyFrame(Duration.millis(100),e ->
                                    {
                                        ((Sphere) temps).setMaterial(new PhongMaterial(Color.CYAN));

                                    }, new KeyValue(((Sphere) temps).radiusProperty(), 3));
                            KeyFrame kf2= new KeyFrame(Duration.millis(600),e ->
                                    {
                                        ((Sphere) temps).setMaterial(m);

                                    }, new KeyValue(((Sphere) temps).radiusProperty(), 1));
                            temp.getKeyFrames().addAll(kf,kf2);
                            temp.setCycleCount(1);
                            temp.play();
                        });
                    } 
                    else if (graphType.equalsIgnoreCase("grid"))
                    {
                        b.setOnAction(act ->
                        {
                            Material m= ((Cube) temps).getMaterial();
                            Timeline temp =  new Timeline();
                            KeyFrame kf= new KeyFrame(Duration.millis(100),e ->
                                    {
                                        ((Cube) temps).setMaterial(new PhongMaterial(Color.CYAN));

                                    }, new KeyValue(((Cube) temps).widthProperty(), 3));
                            KeyFrame kf2= new KeyFrame(Duration.millis(600),e ->
                                    {
                                        ((Cube) temps).setMaterial(m);

                                    }, new KeyValue(((Cube) temps).widthProperty(), 2));
                            temp.getKeyFrames().addAll(kf,kf2);
                            temp.setCycleCount(1);
                            temp.play();
                        });
                    }
                        
            
            toExplore.add(b);

            if (currentNode.equals(getEnd()))
            {
                if (currentNode instanceof Cube)
                {
                    KeyFrame kf = new KeyFrame(Duration.millis(10), e
                            -> ((Cube) temps).setMaterial(new PhongMaterial(Color.valueOf("#C29959"))));
                    timeline2.getKeyFrames().add(0, kf);
                }
                if (currentNode instanceof Sphere)
                {
                    KeyFrame kf = new KeyFrame(Duration.millis(counter == 0 ? 1.25 : counter), e
                            -> ((Sphere) temps).setMaterial(new PhongMaterial(Color.RED)));
                    timeline2.getKeyFrames().add(0, kf);
                }
                KeyFrame kf = null;
                visited.put(currentNode, true);
                frameCount=0;
                while (nodeParent.get(currentNode) != null)
                {

                    Node temp = nodeParent.get(currentNode);
                    if (temp instanceof Sphere)
                    {
                        Cylinder cyl = (Cylinder) edgeToNodePairing.get(new Pair<>(currentNode, temp));
                        kf = new KeyFrame(Duration.millis(frameCount == 0 ? 1.25 : frameCount ),
                                e ->
                                {
                                    Glow glow = new Glow();
                                    glow.setLevel(0.9);

                                    cyl.setMaterial(new PhongMaterial(Color.valueOf("#000000")));
                                    cyl.setRadius(0.35);
                                    cyl.setEffect(glow);
                                });
                        timeline2.getKeyFrames().add(kf);
                        frames.add(kf);

                    } else if (temp instanceof Cube)
                    {
                        kf = new KeyFrame(Duration.millis(frameCount == 0 ? 5 : frameCount),
                                e ->
                                {
                                    Bloom glow = new Bloom();
                                    glow.setThreshold(0.1);

                                    ((Cube) temp).setMaterial(new PhongMaterial(Color.valueOf("#C29959")));

                                    ((Cube) temp).setEffect(glow);
                                });
                        timeline2.getKeyFrames().add(kf);
                        frames.add(kf);
                    }
                    currentNode = temp;

                    frameCount++;
                }

//                break;

            }
            if (temps instanceof Cube)
            {

                KeyFrame kf2 = new KeyFrame(Duration.millis(counter == 0 ? 30 : counter ), e ->
                {
                    ((Cube) temps).setMaterial(new PhongMaterial(Color.valueOf("#a405ed")));

                });

               

                timeline.getKeyFrames().addAll(kf2);
                frames.add(kf2);
                
            }
            if (temps instanceof Sphere)
            {
               KeyFrame kf2 = new KeyFrame(Duration.millis(counter == 0 ? 1.25 : counter ), e ->
                {
                    ((Sphere) temps).setMaterial(new PhongMaterial(Color.valueOf("#a405ed")));

                });

                timeline.getKeyFrames().addAll(kf2);
                frames.add(kf2);
                
            }

            
//            System.out.printf(currentNode.getId());
            
            connected.put(currentNode, new HashSet<Node>());
            
            for (int i = 0; i < adjacentNodes.get(currentNode).size(); i++)
            {
                
                Node temp = adjacentNodes.get(currentNode).get(i);
                if (!visited.get(temp) && !stack.contains(temp))
                {
                    stack.push(temp);
                    
                    
                    visited.put(temp, true);
                    connected.get(currentNode).add(temp);

                    nodeParent.put(temp, currentNode);
                    if (temp instanceof Sphere)
                    {
                        Node temp3= temp;
                        
                        
                        Edge cyls = (Edge) edgeToNodePairing.get(new Pair<>(currentNode, temp));
                        Edge cylsw = (Edge) edgeToNodePairing.get(new Pair<>(temp,currentNode));
                        KeyFrame kf5 = new KeyFrame(Duration.millis(counter == 0 ? 1.75 : counter ),
                                e -> ((Sphere) temp3).setMaterial(new PhongMaterial(Color.BLUE)),
                                new KeyValue(cyls.materialProperty(), new PhongMaterial(Color.YELLOW)),
                                new KeyValue(cyls.radiusProperty(), 0.3));
                        
                        timeline.getKeyFrames().addAll(kf5);
                        frames.add(kf5);
                    }
                    
                }
                
                 counter+=2;
            }
            

           
        }
//        System.out.println("===================================================================================================================================");
//        System.out.println("Adjacent Nodes");
//        System.out.println("==============");
//        adjacentNodes.forEach((key,value)->
//        {
//            System.out.printf("%s", key.getId());
//            for (Node v1 : value)
//            {
//                System.out.printf("->%s", v1.getId());
//            }
//            System.out.println();
//            
//        });
//        System.out.println("===================================================================================================================================");
        allVisited=true;
        visited.forEach((node,visitStatus)->
        {
            if (visitStatus.booleanValue() == false)
            {
                stack.clear();
                stack.add(node);
                counter = 0;
                while (!stack.isEmpty())
                {

                    Node currentNode = (Node) stack.pop();
                    visited.put(currentNode, true);

                    connected.put(currentNode, new HashSet<Node>());

                    if (currentNode instanceof Cube)
                    {

                        KeyFrame kf2 = new KeyFrame(Duration.millis(counter == 0 ? 5 : counter ), e ->
                        {
                            ((Cube) currentNode).setMaterial(new PhongMaterial(Color.valueOf("#a405ed")));

                        });

                        timeline.getKeyFrames().addAll(kf2);
                        frames.add(kf2);

                    }
                    if (currentNode instanceof Sphere)
                    {
                        KeyFrame kf2 = new KeyFrame(Duration.millis(counter == 0 ? 5 : counter ), e ->
                        {
                            ((Sphere) currentNode).setMaterial(new PhongMaterial(Color.valueOf("#a405ed")));

                        });

                        timeline.getKeyFrames().addAll(kf2);
                        frames.add(kf2);

                    }

//            System.out.printf(currentNode.getId());
                    connected.put(currentNode, new HashSet<Node>());

                    for (int i = 0; i < adjacentNodes.get(currentNode).size(); i++)
                    {

                        Node temp = adjacentNodes.get(currentNode).get(i);
                        if (!visited.get(temp) && !stack.contains(temp))
                        {
                            stack.push(temp);

                            visited.put(temp, true);
                            connected.get(currentNode).add(temp);

                            nodeParent.put(temp, currentNode);
                            if (temp instanceof Sphere)
                            {
                                Node temp3 = temp;

                                Edge cyls = (Edge) edgeToNodePairing.get(new Pair<>(currentNode, temp));
                                KeyFrame kf5 = new KeyFrame(Duration.millis(counter == 0 ? 1.25 : counter ),
                                        e -> ((Sphere) temp3).setMaterial(new PhongMaterial(Color.BLUE)),
                                        new KeyValue(cyls.materialProperty(), new PhongMaterial(Color.YELLOW)));

                                timeline.getKeyFrames().addAll(kf5);
                                frames.add(kf5);
                            }

                        }

                    }

                    counter++;

                }
                
                allVisited=false;
                
                
            }
            
            
        }); 
        
            
        stack.clear();

//         if(allVisited)
//        {
//            System.out.println("Graph is fully connected, start has path to every other node thru its neighbors");
//            System.out.println("-------------------------------------------------------------");
//            System.out.printf("[");
//            for (int i=0; i<sensorNetwork.size(); i++)
//            {
//                if(i<sensorNetwork.size()-1)
//                {
//                    System.out.printf("%s, ",sensorNetwork.get(i).getId());
//                }
//                else if( i== sensorNetwork.size()-1)
//                {
//                    System.out.printf("%s]\n",sensorNetwork.get(i).getId());
//                }
//            }
//            return allVisited;
//        }
//        else if(!allVisited)
//        {
//            System.out.printf("Graph is not fully connected\n");
//            System.out.printf("-----------------------------\n");
//            System.out.printf("There are %d connected components\n", connected.size());
//            connected.forEach((key, value) ->
//            {
//                System.out.printf("%s", key.getId());
//                for (Node v1 : value)
//                {
//                    System.out.printf("->%s", v1.getId());
//                }
//                System.out.println();
//            });
//        }
         return allVisited;
        

    }
    public void dijkstraForGrid( String graphType)
    {
        timeline.getKeyFrames().clear();
        timeline2.getKeyFrames().clear();
        visited = new HashMap<Node, Boolean>();
        nodeParent = new HashMap<Node, Node>();
        HashMap<Node,Double> distanceToNode= new HashMap<>();
        if (graphType.equalsIgnoreCase("network"))
        {
            
            for (int i = 0; i < sensorNetwork.size(); i++)
            {
                visited.put(sensorNetwork.get(i),false);
                distanceToNode.put(sensorNetwork.get(i), Double.POSITIVE_INFINITY);

            }
        }
         else if(graphType.equalsIgnoreCase("gridNetwork"))
        {
            
            for (int i = 0; i < gridNetwork.size(); i++)
            {
                visited.put(gridNetwork.get(i), false);
                distanceToNode.put(gridNetwork.get(i), Double.POSITIVE_INFINITY);

            }
           
        }
         else if (graphType.equalsIgnoreCase("grid"))
        {
            
            for (int i = 0; i < grid.size(); i++)
            {
                visited.put(grid.get(i), false);
                distanceToNode.put(grid.get(i), Double.POSITIVE_INFINITY);

            }
            for (int currentElement = 0; currentElement < grid.size(); currentElement++)
            {
                Cube node1 = (Cube) grid.get(currentElement);
                adjacentNodes.put(node1, new ArrayList<Node>());
                if (node1.getId().endsWith("barrier"))
                {
                    continue;
                }
                for (int elementToCompareTo = 0; elementToCompareTo < grid.size(); elementToCompareTo++)
                {
                    Cube node2 = (Cube) grid.get(elementToCompareTo);
                    if (node2.getId().endsWith("barrier"))
                    {
                        continue;
                    }
                    Point3D node1Location = new Point3D(node1.getTranslateX(), node1.getTranslateY(), node1.getTranslateZ());
                    Point3D node2Location = new Point3D(node2.getTranslateX(), node2.getTranslateY(), node2.getTranslateZ());
                    if(node1Location.distance(node2Location) > node1.getWidth() + 0.55)
                    {
                        continue;
                    }
                    if (!node1.equals(node2) && node1Location.distance(node2Location) <= node1.getWidth() + 0.55 && adjacentNodes.get(node1) != null && adjacentNodes.get(node2) != null && !grid.get(currentElement).getId().endsWith("barrier"))
                    {

                        adjacentNodes.get(node1).add(node2);

                        adjacentNodes.get(node2).add(node1);
                        Edge edge = new Edge(0.15,node1,node2,1,1,transmissionCost);
                        edge.setMaterial(new PhongMaterial(Color.RED));
                        edgeBetweenNodes.add(edge);

                        edgeToNodePairing.put(new Pair<>(node1, node2), edge);
                        edgeToNodePairing.put(new Pair<>(node2, node1), edge);

                    }

                }

            }
        }
        CostSortedQueue bst= new CostSortedQueue();
//        System.out.println(bst.getSize());
//        System.out.println(bst.isEmpty());
        counter = 0;
        Edge startEdge= new Edge();
        startEdge.setId("0");
        distanceToNode.put(start,0.0);
        
        bst.insert(new Pair<>(null,start), startEdge);
        int step=0;
        counter=0;
        HashMap<Pair<Node,Node>,Node> SPT= new HashMap<>();
        while(!bst.isEmpty())
        {
            
            Node parentPrev= bst.lowestCost(bst.getRoot()).edgeVertices.getKey();
            Node parent= bst.lowestCost(bst.getRoot()).edgeVertices.getValue();
            Cylinder edge= bst.lowestCost(bst.getRoot()).e;
            bst.deleteNetworkNode(bst.lowestCost(bst.getRoot()));             
//            System.out.printf("Previous node: %s, Current node: %s, Edge: %s\n",
//                               parentPrev==null? "null": parentPrev.getId(),
//                               parent==null? "null": parent.getId(),
//                               edge==null? "null": edge.getId());
//            System.out.println(adjacentNodes.get(parent));
            visited.put(parent, true);
            nodeParent.put(parent, parentPrev);
            Node currentNode = parent;
            if(parent.equals(getEnd()))
                {
                    
                    frameCount=0;
                    while (nodeParent.get(parent) != null)
                    {

                        Node temp = nodeParent.get(parent);
                        if (temp instanceof Sphere)
                        {
                            Cylinder cyl = (Cylinder) edgeToNodePairing.get(new Pair<>(parent, temp));
                            KeyFrame kf = new KeyFrame(Duration.millis(frameCount == 0 ? 10 : frameCount * 10),
                                    e ->
                                    {
                                        Glow glow = new Glow();
                                        glow.setLevel(0.9);

                                        cyl.setMaterial(new PhongMaterial(Color.valueOf("#000000")));
                                        cyl.setRadius(0.5);
                                        cyl.setEffect(glow);
                                    });
                            timeline2.getKeyFrames().add(kf);
                            frames.add(kf);

                        }
                         else if (parent instanceof Cube)
                        {
                           KeyFrame kf = new KeyFrame(Duration.millis(frameCount == 0 ? 5 : frameCount * 5),
                                    e ->
                                    {
                                        Bloom glow = new Bloom();
                                        glow.setThreshold(0.1);

                                        ((Cube) temp).setMaterial(new PhongMaterial(Color.valueOf("#C29959")));

                                        ((Cube) temp).setEffect(glow);
                                    });
                            timeline2.getKeyFrames().add(kf);
                            frames.add(kf);
                        }

                        parent = temp;

                        frameCount++;
                    }

                    break;
                }
                if(step>0)
                {
                    if(currentNode instanceof Sphere)
                    {
                        KeyFrame kf2 = new KeyFrame(Duration.millis(counter == 0 ? 1.25 : counter+20),
                                e ->
                                {
        //                            Cylinder cyl = edgeToNodePairing.get(new Pair<>(parentPrev, parent));
                                    ((Sphere)currentNode).setMaterial(new PhongMaterial(Color.GREEN));
                                    edge.setMaterial(new PhongMaterial(Color.GREEN));
                                }, new KeyValue(edge.radiusProperty(), 0.5, Interpolator.EASE_BOTH)
                                 ,new KeyValue(((Sphere)currentNode).radiusProperty(), 1.5, Interpolator.EASE_IN));
                        KeyFrame kf3 = new KeyFrame(Duration.millis(counter == 0 ? 1.25 : counter+22),
                                new KeyValue(((Sphere)currentNode).radiusProperty(), 1.25,Interpolator.EASE_OUT));
                        timeline.getKeyFrames().addAll(kf2,kf3);
                        frames.add(kf2);
                        frames.add(kf3);
                        SPT.put(new Pair<>(parentPrev,currentNode), edge);
                    }
                    if(currentNode instanceof Cube)
                    {
                        KeyFrame kf2 = new KeyFrame(Duration.millis(counter == 0 ? 1.25 : counter),
                                e ->
                                {
        //                            Cylinder cyl = edgeToNodePairing.get(new Pair<>(parentPrev, parent));
                                    ((Cube)currentNode).setMaterial(new PhongMaterial(Color.GREEN));

                                });
                        timeline.getKeyFrames().add(kf2);
                        frames.add(kf2);
                    }

                }
                step++;
            
            for (int i = 0; i < adjacentNodes.get(parent).size(); i++)
            {
                
                Node child = adjacentNodes.get(parent).get(i);
//                System.out.println(child.getId());                
                Edge edgeToChild= (Edge) edgeToNodePairing.get(new Pair<>(parent,child));
                if(child.getId().startsWith("Data"))
                {
                    KeyFrame kf2 = new KeyFrame(Duration.millis(counter == 0 ? 1.25 : counter),
                                e ->
                                {
                                    ((Sphere) child).setMaterial(new PhongMaterial(Color.BLACK));
                                    edgeToChild.setMaterial(new PhongMaterial(Color.ORANGE));

                                });

                        

                        //                       
                        timeline.getKeyFrames().addAll(kf2);
                        frames.add(kf2);
                        visited.put(child, true);
                        continue;
                }
                if(!visited.get(child))
                {
                    nodeParent.put(child, parent);
                    visited.put(child, true);
                    
                    if (graphType.equalsIgnoreCase("grid") || graphType.equalsIgnoreCase("gridNetwork"))
                    {
                        Point3D node1Location = new Point3D(getEnd().getTranslateX(), getEnd().getTranslateY(), getEnd().getTranslateZ());
                        Point3D node2Location = new Point3D(child.getTranslateX(), child.getTranslateY(), child.getTranslateZ());
                        edgeToChild.setId(String.format("%.2f",node1Location.distance(node2Location)));
                    }
                    double distanceToNeighbor = Double.valueOf(edgeToChild.getId());
                    double updateDistance = distanceToNode.get(parent)+distanceToNeighbor;
                    if (child instanceof Sphere)
                    {
                        
                        double height = edgeToChild.getHeight();

                        KeyFrame kf2 = new KeyFrame(Duration.millis(counter == 0 ? 1.25 : counter),
                                e ->
                                {
                                    ((Sphere) child).setMaterial(new PhongMaterial(Color.BLUE));
                                    edgeToChild.setMaterial(new PhongMaterial(Color.YELLOW));

                                });

                        

                        //                       
                        timeline.getKeyFrames().addAll(kf2);
                        frames.add(kf2);
//                                frames.add(kf3);
                    }
                    
                    if(child instanceof Cube)
                    {
                        
                        KeyFrame kf2 = new KeyFrame(Duration.millis(counter == 0 ? 1.25 : counter),
                                e ->
                                {
                                    ((Cube) child).setMaterial(new PhongMaterial(Color.GREEN));
                                    
                                });
                        timeline.getKeyFrames().add(kf2);
                        frames.add(kf2);
                    }
                        
                        if (updateDistance < distanceToNode.get(child))
                        {
//                            System.out.printf("Distance to neighbor before update %s: %.2f", child.getId(), distanceToNode.get(child));
                            distanceToNode.put(child, updateDistance);
//                            System.out.printf("Distance to neighbor after update %s: %.2f", child.getId(), distanceToNode.get(child));

                        }

                        bst.insert(new Pair<>(parent, child), edgeToChild);
                    
                }
                else
                {
                    if (child instanceof Sphere)
                    {
                        KeyFrame kf2 = new KeyFrame(Duration.millis(counter == 0 ? 1.25 : counter),
                                e ->
                                {
                                    ((Sphere) child).setMaterial(new PhongMaterial(Color.MAGENTA));
                                }, new KeyValue(((Sphere) child).radiusProperty(), 1.5, Interpolator.EASE_OUT));
                        timeline.getKeyFrames().add(kf2);
                        frames.add(kf2);
                        KeyFrame kf3 = new KeyFrame(Duration.millis(counter == 0 ? 1.25 : counter + 2),
                                new KeyValue(((Sphere) child).radiusProperty(), 0.8, Interpolator.EASE_OUT));
                        timeline.getKeyFrames().add(kf3);
                        frames.add(kf3);
                    }
                    if (child instanceof Cube)
                    {
                        KeyFrame kf2 = new KeyFrame(Duration.millis(counter == 0 ? 1.25 : counter),
                                e ->
                                {
                                    ((Cube) child).setMaterial(new PhongMaterial(Color.MAGENTA));
                                }, new KeyValue(((Cube) child).scaleXProperty(), 1.09, Interpolator.EASE_OUT),
                                   new KeyValue(((Cube) child).scaleYProperty(), 1.09, Interpolator.EASE_OUT));
                        timeline.getKeyFrames().add(kf2);
                        frames.add(kf2);
                        KeyFrame kf3 = new KeyFrame(Duration.millis(counter == 0 ? 1.25 : counter + 2),
                                       
                                       new KeyValue(((Cube) child).scaleXProperty(), 1, Interpolator.EASE_OUT),
                                       new KeyValue(((Cube) child).scaleYProperty(), 1, Interpolator.EASE_OUT));
                        timeline.getKeyFrames().add(kf3);
                        frames.add(kf3);
                    }

                }
                counter++;
            }

        }
        System.out.println("FINISHED");
        

        
    }
    //======== Dijkstra algorithm using stack====================//
     public void dijkstra(Node start, String graphType)
    {
//        timeline.getKeyFrames().clear();
//        timeline2.getKeyFrames().clear();
        
        
        toExplore.clear();
        
        nodeParent.clear();
        ArrayList<String> path = new ArrayList<>();
//        if(firstRun)
////        {
//            if (graphType.equalsIgnoreCase("network"))
////            {
                for (int i = 0; i < sensorNetwork.size(); i++)
                {
                    visited.put(sensorNetwork.get(i), false);
                    distanceToNode.put(sensorNetwork.get(i), Double.POSITIVE_INFINITY);

                }
//////C
//            }
//             else if(graphType.equalsIgnoreCase("gridNetwork"))
//            {
//
//                for (int i = 0; i < gridNetwork.size(); i++)
//                {
//                    visited.put(gridNetwork.get(i), false);
//                    distanceToNode.put(gridNetwork.get(i), Double.POSITIVE_INFINITY);
//
//                }
//
//            }
//            firstRun = false;
//        }
         
        CostSortedQueue bst= new CostSortedQueue();
//        System.out.println(bst.getSize());
//        System.out.println(bst.isEmpty());
//       
        
        Edge startEdge= new Edge();
        startEdge.setId("0");
        
        distanceToNode.put(start,0.0);
        
        bst.insert(new Pair<>(null,start), startEdge);
        
        int step=0;
        double counter=0;
        
        while(!bst.isEmpty())
        {
            
            Node parentPrev= bst.lowestCost(bst.getRoot()).edgeVertices.getKey();
            Node parent= bst.lowestCost(bst.getRoot()).edgeVertices.getValue();
            Edge edge= bst.lowestCost(bst.getRoot()).e;
            bst.deleteNetworkNode(bst.lowestCost(bst.getRoot()));      
            Button b= new Button(parent.getId());
//            System.out.printf("Previous node: %s, Current node: %s, Edge: %s\n",
//                               parentPrev==null? "null": parentPrev.getId(),
//                               parent==null? "null": parent.getId(),
//                               edge==null? "null": edge.getId());
//            System.out.println(adjacentNodes.get(parent));
            
            if(!visited.get(parent))
            {
                visited.put(parent, true);
                nodeParent.put(parent, parentPrev);
                Node currentNode = parent;
                
                b.setOnAction(act ->
                {
                    Material m = ((Sphere) currentNode).getMaterial();
                    Timeline temp = new Timeline();
                    KeyFrame kf = new KeyFrame(Duration.millis(100), e ->
                    {
                        ((Sphere) currentNode).setMaterial(new PhongMaterial(Color.CYAN));

                    }, new KeyValue(((Sphere) currentNode).radiusProperty(), 3));
                    KeyFrame kf2 = new KeyFrame(Duration.millis(600), e ->
                    {
                        ((Sphere) currentNode).setMaterial(m);

                    }, new KeyValue(((Sphere) currentNode).radiusProperty(), 1));
                    temp.getKeyFrames().addAll(kf, kf2);
                    temp.setCycleCount(1);
                    temp.play();
                });
                toExplore.addAll(b);
                
                if(parent.equals(getEnd()))
                {
                    
                    SPT.put(new Pair<>(parentPrev,parent), edge);
                    
                    System.out.println("###############################################################################################################");
                    System.out.println("Destination Node Found.....");
                    System.out.println("------------------------------");
                    System.out.printf("\t\tShortest Path To %s from %s\n",start.getId(), getEnd().getId());
                    System.out.println("======================================================================");
                    System.out.printf("%s%40s\n","\tVertices","Edge Cost");
                    System.out.println("======================================================================");
                   
                    
                    while (nodeParent.get(parent) != null)
                    {
                        
                        Node temp = nodeParent.get(parent);
                        
                        
                        if (temp instanceof Sphere)
                        {
                            Edge edgeToTemp = (Edge) edgeToNodePairing.get(new Pair<>(temp, parent));
                            
//                            System.out.printf("[%s,%s]\t\t%s\n",temp.getId()== null? "null":temp.getId(),
//                                    parent.getId()==null?"null":parent.getId(),
//                                    edgeToTemp.getId()==null?"null":edge.getId());
                            if (temp.getId() != null && parent.getId() != null && edgeToTemp.getId() != null)
                            {
                                path.add(String.format("%-47s%s\n",
                                        "[" + temp.getId() + "," + parent.getId() + "]",
                                        edgeToTemp.getId()));
                            }
                            KeyFrame kf2 = new KeyFrame(Duration.millis(frameCount == 0 ? 1.25 : frameCount),
                                    e ->
                                    {

                                        ((Sphere) temp).setMaterial(new PhongMaterial(Color.WHITE));
                                        edgeToTemp.setMaterial(new PhongMaterial(Color.MAGENTA));
                                    }
                                    ,new KeyValue(edgeToTemp.radiusProperty(), 0.5, Interpolator.EASE_BOTH), 
                                     new KeyValue(((Sphere) temp).radiusProperty(), 1.5, Interpolator.EASE_IN));
                            
                            KeyFrame kf3 = new KeyFrame(Duration.millis(frameCount+5),
                                           new KeyValue(((Sphere) temp).radiusProperty(), 1.25, Interpolator.EASE_OUT));

                            timeline2.getKeyFrames().addAll(kf2, kf3);
                            frames.add(kf2);
                            frames.add(kf3);
                            

                        }
                        
                         
                        parent = temp;

                        frameCount+=10;
                    }
                    
                    
                    
                    Collections.reverse(timeline2.getKeyFrames());
                    KeyFrame kf = new KeyFrame(Duration.millis(frameCount),
                            e ->
                            {
                                ((Sphere) getEnd()).setMaterial(new PhongMaterial(Color.WHITE));
                            },
                            new KeyValue(((Sphere) getEnd()).radiusProperty(), 1.5, Interpolator.EASE_IN));
                    timeline2.getKeyFrames().add(kf);
                    frames.add(kf);
                    Collections.reverse(path);
                    
                    path.forEach(index->System.out.println(index));
                    
                    System.out.println("TOTAL ENERGY COST: "+distanceToNode.get(getEnd()));
                    break;
//                    
                }
                if(step>0)
                {
                    if(currentNode instanceof Sphere)
                    {
                        
                        KeyFrame kf2 = new KeyFrame(Duration.millis(counter == 0 ? 1.25 : counter),
                                e ->
                                {
        //                          
                                    ((Sphere)currentNode).setMaterial(new PhongMaterial(Color.GREEN));
                                    edge.setMaterial(new PhongMaterial(Color.GREEN));
                                }, new KeyValue(edge.radiusProperty(), 0.5, Interpolator.EASE_BOTH)
                                 ,new KeyValue(((Sphere)currentNode).radiusProperty(), 1.5, Interpolator.EASE_IN));
                        KeyFrame kf3 = new KeyFrame(Duration.millis(counter == 0 ? 1.25 : counter+6),
                                new KeyValue(((Sphere)currentNode).radiusProperty(), 1.25,Interpolator.EASE_OUT));
                        timeline.getKeyFrames().addAll(kf2,kf3);
                        frames.add(kf2);
                        frames.add(kf3);
                        SPT.put(new Pair<>(parentPrev,currentNode), edge);
//                        visited.put(edge,true);
                        
                    }
                    

                }
                step++;

                for (int i = 0; i < adjacentNodes.get(parent).size(); i++)
                {

                    Node child = adjacentNodes.get(parent).get(i);
//                    System.out.println(child.getId());
                    Edge edgeToChild= (Edge) edgeToNodePairing.get(new Pair<>(parent,child));
                    double distanceToNeighbor = edgeToChild.cost;
                    double updateDistance = distanceToNode.get(parent) + distanceToNeighbor;
                    
                    
                    if (child.getId().startsWith("DN"))
                    {
                        System.out.println(child.getId()+"'s STORAGE IS FULL\n"
                                + "RETRYING ANOTHER NODE.......\n");
                        KeyFrame kf2 = new KeyFrame(Duration.millis(counter == 0 ? 1.25 : counter),
                                e ->
                                {
                                    ((Sphere) child).setMaterial(new PhongMaterial(Color.YELLOW));
                                    edgeToChild.setMaterial(new PhongMaterial(Color.BLACK));

                                });

                        //                       
                        timeline.getKeyFrames().addAll(kf2);
                        frames.add(kf2);
                        
                        continue;
                    }
                    if (!visited.get(child))
                    {
                        

//                        if (graphType.equalsIgnoreCase("grid"))
//                        {
//                            Point3D node1Location = new Point3D(getEnd().getTranslateX(), getEnd().getTranslateY(), getEnd().getTranslateZ());
//                            Point3D node2Location = new Point3D(child.getTranslateX(), child.getTranslateY(), child.getTranslateZ());
//                            edgeToChild.setId(String.format("%.2f",node1Location.distance(node2Location)));
//                        }
                        
                        if (updateDistance < distanceToNode.get(child))
                        {
//                            System.out.printf("Distance to neighbor before update %s: %.2f\n", child.getId(), distanceToNode.get(child));

                            distanceToNode.put(child, updateDistance);
                            nodeParent.put(child, parent);
                            bst.insert(new Pair<>(parent, child), edgeToChild);
                        }

//                            System.out.printf("Distance to neighbor after update %s: %.2f\n", child.getId(), distanceToNode.get(child));
                        edgeToChild.setId(String.format("%.2f", distanceToNode.get(child)));
                        if (child instanceof Sphere)
                        {
                            Material m = ((Sphere) currentNode).getMaterial();
                            KeyFrame kf2 = new KeyFrame(Duration.millis(counter == 0 ? 1.25 : counter),
                                    e ->
                                    {
                                        ((Sphere) child).setMaterial(new PhongMaterial(Color.BLUE));
                                    }, new KeyValue(((Sphere) child).radiusProperty(), 1.5, Interpolator.EASE_OUT));
                            timeline.getKeyFrames().add(kf2);
                            frames.add(kf2);
                            KeyFrame kf3 = new KeyFrame(Duration.millis(counter == 0 ? 1.25 : counter + 3),
                                    e ->
                                    {
                                        ((Sphere) child).setMaterial(m);
                                    },
                                    new KeyValue(((Sphere) child).radiusProperty(), 1, Interpolator.EASE_OUT));
                            timeline.getKeyFrames().add(kf3);
                            frames.add(kf3);
                        }

                    } else if (!child.getId().startsWith("DNode"))
                    {
                        Material m = ((Sphere) currentNode).getMaterial();
                        KeyFrame kf2 = new KeyFrame(Duration.millis(counter == 0 ? 1.25 : counter),
                                e ->
                                {
                                    ((Sphere) child).setMaterial(new PhongMaterial(Color.MAGENTA));
                                }, new KeyValue(((Sphere) child).radiusProperty(), 1.5, Interpolator.EASE_OUT));
                        timeline.getKeyFrames().add(kf2);
                        frames.add(kf2);
                        KeyFrame kf3 = new KeyFrame(Duration.millis(counter == 0 ? 1.25 : counter + 3),
                                e ->
                                {
                                    ((Sphere) child).setMaterial(m);
                                },
                                new KeyValue(((Sphere) child).radiusProperty(), 1, Interpolator.EASE_OUT));
                        timeline.getKeyFrames().add(kf3);
                        frames.add(kf3);

                    }

                    counter += 10;




                    


                }
            }
            else
            {
//                System.out.println("()()()()()()()()()()()()()()()()()()()()()()()()()()()()()()()()");
//                System.out.println(distanceToNode.get(parent));
            }

        }
//        if(getEnd() == null)
//        {
            System.out.println("\n###############################################################################################################"
                    + "\n\tAll  VISITED EDGES ");
            System.out.println("======================================================================");
            System.out.printf("%s%40s\n", "\tVertices", "Edge Cost");
            System.out.println("======================================================================\n");
            path.clear();

            SPT.forEach((pair,edge)->
            {                
                path.add(String.format("%-47s%s\n", "["+pair.getKey().getId()+","+pair.getValue().getId()+"]",edge.getId()));
            });
            Collections.reverse(path);
            
            path.forEach(index->System.out.println(index));
        
    }
    public void testing(Node start,Node end,String graphType)
    {

//        timeline.getKeyFrames().clear();
//        timeline2.getKeyFrames().clear();
        int k=0;
        int K=1;
        Queue<Node> q= new LinkedList();
        q.add(start);
        Node a= end;
        ArrayList<Node> s = new ArrayList<>();
        
        rt.clear();
        System.out.printf("\t");
        nodeParent.clear();
        for (int i = 0; i < sensorNetwork.size(); i++)
        {
            System.out.printf("%15d", i);
        }
        System.out.println();
        while(!q.isEmpty())
        {
            Node u= q.poll();
                         
                dijkstra(u, "network");
                s.add(u);
                

                if(s.size()==sensorNetwork.size())
                {
                    

                    break;
                }
//                System.out.printf("Start Node: %s\n", u.getId());


                for (int i = 0; i < adjacentNodes.get(u).size(); i++)
                {
                    Node v = adjacentNodes.get(u).get(i);
                    if(s.contains(v))
                    {
//                        System.out.println("Woop");
                        continue;
                    }
//                    if(v.equals(a))
//                    {
//                        System.out.println("Encountered END");
//                        setEnd(v);
//                        dijkstra(u, v, "network");
//                       
//                        break;
//                    }


                    
                    if(!q.contains(v) )
                    {
                        q.add(v);
                        


//                        setEnd(v);
//    //                System.out.println(v.getId());
//                        dijkstra(u, v, "network");

                    }


                    k++;
                
            }
                
        }
        q.clear();

        System.out.println("========================================================================");
        System.out.println("FINISHED VISITING ALL NODES");
        System.out.println("========================================================================");
        System.out.println(a.getId());
//        nodeParent.forEach((node,parent)->System.out.println(node.getId()+":"+parent.getId()));
        System.out.println("WOOP");
        while (nodeParent.get(a) != null)
        {
//            currentNode = null;
            
//            if(nodeParent.get(a)!=null)
//            {
                currentNode = nodeParent.get(a);
//            }
            
            if (currentNode instanceof Sphere)
            {
                Edge edgeToTemp = (Edge) edgeToNodePairing.get(new Pair<>(a, currentNode));
                System.out.printf("[%s,%s]\t\t%s\n", a.getId() == null ? "null" : a.getId(),
                        currentNode.getId() == null ? "null" : currentNode.getId(),
                        edgeToTemp.getId() == null ? "null" : edgeToTemp.getId());
//                if (temp.getId() != null && a.getId() != null && edgeToTemp.getId() != null)
//                {
//                    path.add(String.format("%-47s%s\n",
//                            "[" + temp.getId() + "," + a.getId() + "]",
//                            edgeToTemp.getId()));
//                }
                KeyFrame kf2 = new KeyFrame(Duration.millis(frameCount == 0 ? 1.25 : frameCount),
                        e ->
                        {

                            ((Sphere) currentNode).setMaterial(new PhongMaterial(Color.WHITE));
                            edgeToTemp.setMaterial(new PhongMaterial(Color.MAGENTA));
                        }, new KeyValue(edgeToTemp.radiusProperty(), 0.5, Interpolator.EASE_BOTH),
                        new KeyValue(((Sphere) currentNode).radiusProperty(), 1.5, Interpolator.EASE_IN));

                KeyFrame kf3 = new KeyFrame(Duration.millis(frameCount + 5),
                        new KeyValue(((Sphere) currentNode).radiusProperty(), 1.25, Interpolator.EASE_OUT));

                timeline2.getKeyFrames().addAll(kf2, kf3);
                frames.add(kf2);
                frames.add(kf3);

            }

//            System.out.println(a.getId());
            a = currentNode;

            frameCount += 10;
        }
//        System.out.println(edgeToNodePairing.get(new Pair<>(getStart(),a)).getId());
        
//        APSP.forEach((node, shortestPath)->
//        {
//            System.out.printf("Shortest Path To end From %s\n",node.getId());
//            Collections.reverse(shortestPath);
//            shortestPath.forEach(pair->
//            {
//                System.out.printf("%-47s%s\n", "["+pair.getKey().getKey().getId()+","+pair.getKey().getValue().getId()+"]",pair.getValue().getId());
//            });
//            
//        });

//        Collections.reverse(timeline2.getKeyFrames());
//        KeyFrame kf = new KeyFrame(Duration.millis(frameCount),
//                e ->
//                {
//                    ((Sphere) getEnd()).setMaterial(new PhongMaterial(Color.WHITE));
//                },
//                new KeyValue(((Sphere) getEnd()).radiusProperty(), 1.5, Interpolator.EASE_IN));
//        timeline2.getKeyFrames().add(kf);
//        frames.add(kf);
             
    }
    public void prim(Node start,String graphType)
    {
        timeline.getKeyFrames().clear();
        timeline2.getKeyFrames().clear();
        visited = new HashMap<Node, Boolean>();
        nodeParent = new HashMap<Node, Node>();
        HashMap<Node,Double> distanceToNode= new HashMap<>();
        if (graphType.equalsIgnoreCase("network"))
        {
            
            for (int i = 0; i < sensorNetwork.size(); i++)
            {
                visited.put(sensorNetwork.get(i),false);
                distanceToNode.put(sensorNetwork.get(i), Double.POSITIVE_INFINITY);

            }
        }
         else if(graphType.equalsIgnoreCase("gridNetwork"))
        {
            
            for (int i = 0; i < gridNetwork.size(); i++)
            {
                visited.put(gridNetwork.get(i), false);
                distanceToNode.put(gridNetwork.get(i), Double.POSITIVE_INFINITY);

            }
           
        }
         
        CostSortedQueue bst= new CostSortedQueue();
        System.out.println(bst.getSize());
        System.out.println(bst.isEmpty());
//       
        
        Edge startEdge= new Edge();
        startEdge.setId("0");
        distanceToNode.put(getStart(),0.0);
        
        bst.insert(new Pair<>(null,getStart()), startEdge);
        int step=0;
        counter=0;
        HashMap<Pair<Node,Node>,Node> MST= new HashMap<>();
        while(!bst.isEmpty())
        {
            
            Node parentPrev= bst.lowestCost(bst.getRoot()).edgeVertices.getKey();
            Node parent= bst.lowestCost(bst.getRoot()).edgeVertices.getValue();
            Cylinder edge= bst.lowestCost(bst.getRoot()).e;
            bst.deleteNetworkNode(bst.lowestCost(bst.getRoot()));             
//            System.out.printf("Previous node: %s, Current node: %s, Edge: %s\n",
//                               parentPrev==null? "null": parentPrev.getId(),
//                               parent==null? "null": parent.getId(),
//                               edge==null? "null": edge.getId());
//            System.out.println(adjacentNodes.get(parent));
            
            if(!visited.get(parent))
            {
                visited.put(parent, true);
                nodeParent.put(parent, parentPrev);
                Node currentNode = parent;
                
                if(parent.equals(getEnd()))
                {
                    frameCount=0;
                    System.out.println("Destination Node Found");
                    System.out.println("----------------------");
                    
                    ArrayList<String> path = new ArrayList<>();
                    while (nodeParent.get(parent) != null)
                    {
                        

                        Node temp = nodeParent.get(parent);
                        if (temp instanceof Sphere)
                        {
                            Cylinder edgeToTemp = (Cylinder) edgeToNodePairing.get(new Pair<>(parent, temp));
//                            
                            
                            KeyFrame kf = new KeyFrame(Duration.millis(frameCount == 0 ? 10 : frameCount * 10),
                                    e ->
                                    {
                                        Glow glow = new Glow();
                                        glow.setLevel(0.9);

                                        edgeToTemp.setMaterial(new PhongMaterial(Color.valueOf("#000000")));
                                        edgeToTemp.setRadius(0.5);
                                        edgeToTemp.setEffect(glow);
                                    });
                            timeline2.getKeyFrames().add(kf);
                            frames.add(kf);

                        }
                        

                        parent = temp;

                        frameCount++;
                    }                                        
                     Collections.reverse(timeline2.getKeyFrames());                     
                     

                    break;
                }
                if(step>0)
                {
                    KeyFrame kf2 = new KeyFrame(Duration.millis(counter == 0 ? 1.25 : counter+20),
                                e ->
                                {
        //                            Cylinder cyl = edgeToNodePairing.get(new Pair<>(parentPrev, parent));
                                    Button b = new Button(currentNode.getId());
                                    if (graphType.equalsIgnoreCase("network") || graphType.equalsIgnoreCase("gridNetwork"))
                                    {
                                        b.setOnAction(act ->
                                        {
                                            Material m = ((Sphere) currentNode).getMaterial();
                                            Timeline temp = new Timeline();
                                            KeyFrame kf = new KeyFrame(Duration.millis(100), es ->
                                            {
                                                ((Sphere) currentNode).setMaterial(new PhongMaterial(Color.CYAN));

                                            }, new KeyValue(((Sphere) currentNode).radiusProperty(), 3));
                                            KeyFrame kf5 = new KeyFrame(Duration.millis(600), es ->
                                            {
                                                ((Sphere) currentNode).setMaterial(m);

                                            }, new KeyValue(((Sphere) currentNode).radiusProperty(), 1));
                                            temp.getKeyFrames().addAll(kf, kf5);
                                            temp.setCycleCount(1);
                                            temp.play();
                                        });
                                    }

                                    toExplore.add(b);
                                    ((Sphere)currentNode).setMaterial(new PhongMaterial(Color.GREEN));
                                    edge.setMaterial(new PhongMaterial(Color.GREEN));
                                }, new KeyValue(edge.radiusProperty(), 0.5, Interpolator.EASE_BOTH)
                                 ,new KeyValue(((Sphere)currentNode).radiusProperty(), 1.5, Interpolator.EASE_IN));
                        KeyFrame kf3 = new KeyFrame(Duration.millis(counter == 0 ? 1.25 : counter+22),
                                new KeyValue(((Sphere)currentNode).radiusProperty(), 1.25,Interpolator.EASE_OUT));
                        timeline.getKeyFrames().addAll(kf2,kf3);
                        frames.add(kf2);
                        frames.add(kf3);
                        MST.put(new Pair<>(parentPrev,currentNode), edge);
//                    if(currentNode instanceof Cube)
//                    {
//                        KeyFrame kf2 = new KeyFrame(Duration.millis(counter == 0 ? 1.25 : counter),
//                                e ->
//                                {
//        //                            Cylinder cyl = edgeToNodePairing.get(new Pair<>(parentPrev, parent));
//                                    ((Cube)currentNode).setMaterial(new PhongMaterial(Color.GREEN));
//
//                                });
//                        timeline.getKeyFrames().add(kf2);
//                        frames.add(kf2);
//                    }

                }
                step++;

                for (int i = 0; i < adjacentNodes.get(parent).size(); i++)
                {

                    Node child = adjacentNodes.get(parent).get(i);
//                    System.out.println(child.getId());
                    Edge edgeToChild= (Edge) edgeToNodePairing.get(new Pair<>(parent,child));
                    double distanceToNeighbor = Double.valueOf(edgeToChild.getId());
                    
//                    if (child.getId().startsWith("Data"))
//                    {
//                        System.out.println(child.getId()+"'s STORAGE IS FULL\n"
//                                + "RETRYING ANOTHER NODE.......\n");
//                        KeyFrame kf2 = new KeyFrame(Duration.millis(counter == 0 ? 1.25 : counter),
//                                e ->
//                                {
//                                    ((Sphere) child).setMaterial(new PhongMaterial(Color.YELLOW));
//                                    edgeToChild.setMaterial(new PhongMaterial(Color.BLACK));
//
//                                });
//
//                        //                       
//                        timeline.getKeyFrames().addAll(kf2);
//                        frames.add(kf2);
//                        
//                        continue;
//                    }
                    if(!visited.get(child))
                    {
                        
                        

    //                    if (graphType.equalsIgnoreCase("gridNetwork")|| graphType.equalsIgnoreCase("grid"))
    //                    {
    //                        Point3D origin = new Point3D(getEnd().getTranslateX(), getEnd().getTranslateY(), getEnd().getTranslateZ());
    //                        Point3D target = new Point3D(child.getTranslateX(), child.getTranslateY(), child.getTranslateZ());
    //                        edgeToChild.setId(String.format("%.2f",origin.distance(target)));
    //                    }
                        if (distanceToNeighbor < distanceToNode.get(child))
                        {
//                            nodeParent.put(child, parent);
//                            System.out.printf("Distance to neighbor before update %s: %.2f\n", child.getId(), distanceToNode.get(child));
                            
                            distanceToNode.put(child, distanceToNeighbor);
                            bst.insert(new Pair<>(parent, child), edgeToChild);


//                            System.out.printf("Distance to neighbor after update %s: %.2f\n", child.getId(), distanceToNode.get(child));
                            if (child instanceof Sphere)
                            {
                                double height= edgeToChild.getHeight();

                                KeyFrame kf2 = new KeyFrame(Duration.millis(counter == 0 ? 1.25 : counter),
                                        e ->
                                        {
                                            ((Sphere) child).setMaterial(new PhongMaterial(Color.BLUE));
                                            edgeToChild.setMaterial(new PhongMaterial(Color.YELLOW));

                                        });
                                
                                KeyFrame kf3 = new KeyFrame(Duration.millis(counter == 0 ? 1.25 : counter+0.98),
                                        e ->
                                        {
                                            
                                            edgeToChild.setMaterial(new PhongMaterial(Color.RED));

                                        });
                                
                                edgeToChild.setId(String.format("%.2f", distanceToNode.get(child)));
    //                       
                                timeline.getKeyFrames().addAll(kf2,kf3);
                                frames.add(kf2);
                                frames.add(kf3);
                            }
                            if (child instanceof Cube)
                            {

                                KeyFrame kf2 = new KeyFrame(Duration.millis(counter == 0 ? 1.25 : counter),
                                        e ->
                                        {
                                            ((Cube) child).setMaterial(new PhongMaterial(Color.GREEN));

                                        });
                                timeline.getKeyFrames().add(kf2);
                                frames.add(kf2);
                            }


                            }
                    } else
                    {
                          KeyFrame kf2 = new KeyFrame(Duration.millis(counter == 0 ? 1.25 : counter),
                                    e ->
                                    {
                                        ((Sphere) child).setMaterial(new PhongMaterial(Color.MAGENTA));
                                    }, new KeyValue(((Sphere) child).radiusProperty(), 1.5, Interpolator.EASE_OUT));
                            timeline.getKeyFrames().add(kf2);
                            frames.add(kf2);
                            KeyFrame kf3 = new KeyFrame(Duration.millis(counter == 0 ? 1.25 : counter+2),
                                    new KeyValue(((Sphere) child).radiusProperty(), 0.8, Interpolator.EASE_OUT));
                            timeline.getKeyFrames().add(kf3);
                            frames.add(kf3);
                        
                    }





                    


                    counter++;
                }
            }

        }
        
        System.out.println("\n###############################################################################################################"
                + "\n\tAll  VISITED EDGES ");
        System.out.println("======================================================================");
        System.out.printf("%s%40s\n", "\tVertices", "Edge Cost");
        System.out.println("======================================================================\n");
        ArrayList<String> path = new ArrayList<>();

        MST.forEach((pair, edge) ->
        {
            path.add(String.format("%-47s%s\n", "[" + pair.getKey().getId() + "," + pair.getValue().getId() + "]", edge.getId()));
        });
        Collections.reverse(path);

        path.forEach(index -> System.out.println(index));

        
    }
    

    public void bellmanFord(String graphType)
    {
        timeline.getKeyFrames().clear();
        timeline2.getKeyFrames().clear();
        distanceToNode.clear();
        for (int i = 0; i < sensorNetwork.size(); ++i)
        {
            distanceToNode.put(sensorNetwork.get(i),Double.POSITIVE_INFINITY);
        }
        nodeParent.clear();

        if(getStart()==null)
        {
            System.out.printf("No Source Vertex\n");
            return;
        }
        if(getEnd()==null)
        {
            System.out.printf("No Destination Vertex\n");
            return;
        }
        start = getStart();
        
        
        distanceToNode.put(start, 0.0);

        System.out.println("\t\t\tMATRIX");
        System.out.println("===============================================================================================================================================================\n");
        SPT= new HashMap<>();
        rt.clear();
        
        nodeParent.put(start, null);
        int k=0;
        System.out.printf("\t");
        distanceToNode.forEach((node, cost) ->
        {
            System.out.printf("%-15s",node.getId());
        });
        System.out.println();
        counter=1;
        for (int i = 0; i < distanceToNode.size(); i++)
        {
            System.out.printf("%d\t",i);
            
            ObservableList<String> temp = FXCollections.observableArrayList();
            temp.add(i+"");
            distanceToNode.forEach((node,cost)->
            {
                System.out.printf("%-15.2f",cost);
                temp.add(String.format("%.2f",cost));
                
            });
            rt.add(temp);
            System.out.println();
            for (int v = 0; v < sensorNetwork.size(); v++)
            {
                prevNode = sensorNetwork.get(v);
                
                for (Node currentNode : adjacentNodes.get(prevNode))
                {
                    Edge edgeToCurrentNode = edgeToNodePairing.get(new Pair<>(prevNode,currentNode));
//                    
                    if (currentNode.getId().startsWith("DN"))
                    {
                        System.out.println(currentNode.getId()+"'s STORAGE IS FULL\n"
                                + "RETRYING ANOTHER NODE.......\n");
                        KeyFrame kf2 = new KeyFrame(Duration.millis(counter == 0 ? 1.25 : counter),
                                e ->
                                {
                                    ((Sphere) currentNode).setMaterial(new PhongMaterial(Color.YELLOW));
                                    edgeToCurrentNode.setMaterial(new PhongMaterial(Color.BLACK));

                                });

                        //                       
                        timeline.getKeyFrames().addAll(kf2);
                        frames.add(kf2);
                        
                        continue;
                    }
                    
                    if (distanceToNode.get(currentNode) > distanceToNode.get(prevNode) + edgeToCurrentNode.cost)
                    {
                        distanceToNode.put(currentNode, distanceToNode.get(prevNode) + edgeToCurrentNode.cost);
//                        System.out.printf("%-47s%s\n", "[" + prevNode.getId() + "," + currentNode.getId() + "]", edgeToCurrentNode.getId());
                        nodeParent.put(currentNode, prevNode);
                        KeyFrame kf = new KeyFrame(Duration.millis(counter*20), 
                                      new KeyValue(((Sphere) currentNode).materialProperty(), new PhongMaterial(Color.MAGENTA), Interpolator.EASE_OUT),
                                      new KeyValue(((Sphere) currentNode).radiusProperty(), 1.25, Interpolator.EASE_OUT),                                      
                                      new KeyValue(edgeToCurrentNode.radiusProperty(), 0.5, Interpolator.EASE_OUT),                                        
                                      new KeyValue(edgeToCurrentNode.materialProperty(), new PhongMaterial(Color.BLUE), Interpolator.EASE_OUT));
                        KeyFrame kf2 = new KeyFrame(Duration.millis((counter*20)+10), 
                                       new KeyValue(((Sphere) currentNode).materialProperty(), new PhongMaterial(Color.CYAN), Interpolator.EASE_OUT),
                                       new KeyValue(((Sphere) currentNode).radiusProperty(), 0.8, Interpolator.EASE_BOTH),                                     
                                       new KeyValue(edgeToCurrentNode.radiusProperty(), 0.15, Interpolator.EASE_BOTH),                                        
                                       new KeyValue(edgeToCurrentNode.materialProperty(), new PhongMaterial(Color.RED), Interpolator.EASE_BOTH));
                        timeline.getKeyFrames().addAll(kf,kf2);
                        counter++;
                        SPT.put(new Pair<>(prevNode,currentNode), edgeToCurrentNode);
                    }
                }
            }
            
        }
        //No negative edges in sensorNetwork
//        for (int v = 0; v < sensorNetwork.size(); v++)
//        {
//            prevNode = sensorNetwork.get(v);
//            for (Node currentNode : adjacentNodes.get(prevNode))
//            {
//                Edge edgeToCurrentNode = edgeToNodePairing.get(new Pair<>(prevNode, currentNode));
//
//                if (distanceToNode.get(currentNode) > distanceToNode.get(prevNode) + edgeToCurrentNode.cost)
//                {
//                    System.out.printf("Negative Cycle found\n");
//                    return;
//                }
//            }
//        }
        
        
        System.out.println("===============================================================================================================================================================\n");
        System.out.println("VISITED EDGES");
        System.out.println("---------------------------");
        
        ArrayList<String> path = new ArrayList<>();
        System.out.printf("%s%40s\n", "\tVertices", "Edge Cost");
        System.out.println("=================================================================================");
        SPT.forEach((pair, edge) ->
        {
            path.add(String.format("%-47s%s\n", "[" + pair.getKey().getId() + "," + pair.getValue().getId() + "]", edge.getId()));
        });
        Collections.reverse(path);

        path.forEach(index -> System.out.println(index));
        prevNode = getEnd();
        frameCount=0;
        path.clear();
        while (nodeParent.get(prevNode) != null)
        {
            

            currentNode = nodeParent.get(prevNode);
            if (currentNode instanceof Sphere)
            {
                Cylinder edgeToTemp = (Cylinder) edgeToNodePairing.get(new Pair<>(prevNode, currentNode));
                if (currentNode.getId() != null && prevNode.getId() != null && edgeToTemp.getId() != null)
                {
                    path.add(String.format("%-47s%s\n",
                            "[" + currentNode.getId() + "," + prevNode.getId() + "]",
                            edgeToTemp.getId()));
                }
//                            

                KeyFrame kf = new KeyFrame(Duration.millis(frameCount == 0 ? 10 : frameCount * 10),
                        e ->
                        {
                            Glow glow = new Glow();
                            glow.setLevel(0.9);

                            edgeToTemp.setMaterial(new PhongMaterial(Color.valueOf("#000000")));
                            edgeToTemp.setRadius(0.5);
                            edgeToTemp.setEffect(glow);
                        });
                timeline2.getKeyFrames().add(kf);
                frames.add(kf);

            }

            prevNode = currentNode;
            if(prevNode.equals(getStart()))
            {
                break;
            }

            frameCount++;
        }
        Collections.reverse(path);
        System.out.printf("\t\tShortest Path To %s from %s\n", start.getId(), getEnd().getId());
        System.out.println("======================================================================");
        System.out.printf("%s%40s\n", "\tVertices", "Edge Cost");
        System.out.println("======================================================================");
        path.forEach(index -> System.out.println(index));
        Collections.reverse(timeline2.getKeyFrames());
        return;

        
    }
    
   
    public TableView populateTable()
    {
        table =  new TableView();
        table.getItems().clear();

        table.setEditable(false);
//        insertionField.getChildren().clear();
        counter=0;
        while(true)
        {
            if(counter==0)
            {
                Label label = null;
                label = new Label("K = ");
                final int j = counter;
                TableColumn<ObservableList<String>, String> col = new TableColumn<>();
                col.setCellFactory(TextFieldTableCell.forTableColumn());

                col.setCellValueFactory(cellContent -> new SimpleStringProperty(cellContent.getValue().get(j)));
                col.setPrefWidth(85);

                VBox header = new VBox();


                header.getChildren().addAll(label);
    //            
                header.setAlignment(Pos.CENTER);
                col.setGraphic(header);
                table.getColumns().addAll(col);
                counter++;
                continue;
            }
             distanceToNode.forEach((node, cost) ->
                {
                    final int j = counter;
                    Label label = null;                   
                    label = new Label(node.getId());
                    
                    System.out.printf("%-15s",node.getId());
                     TableColumn<ObservableList<String>, String> col = new TableColumn<>();
                    col.setCellFactory(TextFieldTableCell.forTableColumn());

                    col.setCellValueFactory(cellContent -> new SimpleStringProperty(cellContent.getValue().get(j)));
                    col.setPrefWidth(85);

                    VBox header = new VBox();


                    header.getChildren().addAll(label);
        //            
                    header.setAlignment(Pos.CENTER);
                    col.setGraphic(header);
                    counter++;
                    table.getColumns().addAll(col);
                });
            



            table.getItems().addAll(rt);
            break;
        }
        return table;
    }

    public Timeline getTimeline()
    {
        return timeline;
    }

    public Timeline getTimeline2()
    {
        return timeline2;
    }

    public ArrayList<KeyFrame> getFrames()
    {
        return frames;
    }

    public int getFrameCount()
    {
        return frameCount;
    }

    public void setStart(Node start)
    {
        this.start = start;

    }

    public void setEnd(Node end)
    {
        this.end = end;
    }

    public Node getStart()
    {
        return start;
    }

    public Node getEnd()
    {
        return end;
    }

    public ObservableList<Node> getToExplore()
    {
        return toExplore;
    }
    public HashMap<Node, ArrayList<Node>> getAdjacentNodes()
    {
        return adjacentNodes;
    }

    public HashMap<Pair<Node, Node>, Edge> getEdgeToNodePairing()
    {
        return edgeToNodePairing;
    }

    public HashMap<Node, Boolean> getVisited()
    {
        return visited;
    }

    public ObservableList<Node> getSensorNetwork()
    {
        return sensorNetwork;
    }
    public ObservableList<Node> getGridNetwork()
    {
        return gridNetwork;
    }
    public ObservableList<Node> getGrid()
    {
        return grid;
    }

    public ObservableList<Node> getEdgeBetweenNodes()
    {
        return edgeBetweenNodes;
    }
    public ObservableList<Node> getNodeTags()
    {
        return nodeTags;
    }
    public ObservableList getRT()
    {
        return rt;
    }
    
    public TableView getTable()
    {
        return table;
    }
    

}
