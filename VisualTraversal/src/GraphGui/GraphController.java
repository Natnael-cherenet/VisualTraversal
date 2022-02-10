package GraphGui;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.net.URL;
import java.util.Arrays;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.Scanner;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;

import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.paint.Color;
import javafx.scene.Node;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.effect.Glow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Material;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.util.Duration;
import javafx.util.Pair;

/**
 * FXML Controller class
 *
 * @author natec
 */
public class GraphController implements Initializable
{

    final NodeNetwork randomNetwork   = new NodeNetwork();
    Scanner keyboard = new Scanner(System.in);

    @FXML
    private TextField nodeXLoc;
    @FXML
    private TextField nodeYLoc;
    @FXML
    private TextField nodeZLoc;
    @FXML
    private TextField nodeID;
    @FXML
    private TextField distance;
    @FXML
    private TextField kEdges;
    @FXML
    private TextField width;
    @FXML
    private TextField height;
    @FXML
    private TextField depth;
    @FXML
    private TextField numOfNodes;
    @FXML
    private TextField transmissionRange;
    @FXML
    private TextField numOfDataNodes;
    @FXML
    private TextField numOfPackets;
    @FXML
    private TextField capacity;
    @FXML
    private TextField networkStatus;
    @FXML
    private Label startNode;
    @FXML
    private Label endNode;

    @FXML
    private TabPane tab;
    @FXML
    private StackPane threeDimensions;
    @FXML
    private StackPane dpTable;
    @FXML
    private AnchorPane twoContainer;
    @FXML
    private AnchorPane appPane;
    @FXML
    private ListView nodesToVisit;
    @FXML
    private ListView adjacentNodes;

    @FXML
    private Button clear;
    @FXML
    private Button kEdgesButton;
    @FXML
    private Button generate;
    @FXML
    private Button play;
    @FXML
    private Button replay;
    @FXML
    private Button reset;
    @FXML
    private Button resetCam;
    @FXML
    private Button updateNodeLocation;
    @FXML
    private RadioButton bfs;
    @FXML
    private RadioButton dfs;
    @FXML
    private RadioButton prim;
    @FXML
    private RadioButton dijkstra;
    @FXML
    private RadioButton bellManFord;

    @FXML
    private MenuButton samples;

    @FXML
    private MenuItem grid;

    @FXML
    private MenuItem network;
    @FXML
    private Slider animationSpeed;
    @FXML
    private ColorPicker bgColor;
    private Node u = null,v=null, selectedNode = null;
   

    private SubScene sub;
    
    final Group                     root = new Group();
    final Xform                     axisGroup = new Xform();
    final Xform                    graphSpace = new Xform();
    final Xform                    mainContainer = new Xform();
    final Xform                    mainContainer2 = new Xform();

    final PerspectiveCamera         camera = new PerspectiveCamera(true);
    final PerspectiveCamera         camera2 = new PerspectiveCamera(true);
    final Xform                     x_y_Rotations = new Xform();
    final Xform                     x_y_Translations = new Xform();
    final Xform                     z_Rotations = new Xform();

    private ObservableList<Node>    gridNetwork=FXCollections.observableArrayList();
    
    private ObservableList<Node>    manhatGrid;

    private final ObservableList<Label>   tickMarks = FXCollections.observableArrayList();
    

    
    

    private static final double     CAMERA_INITIAL_DISTANCE = -650;
    private static final double     CAMERA_INITIAL_X_ANGLE = 70.0;
    private static final double     CAMERA_INITIAL_Y_ANGLE = 320.0;
    private static final double     CAMERA_NEAR_CLIP = 1;
    private static final double     CAMERA_FAR_CLIP = 10000000.0;
    private static final double     CONTROL_MULTIPLIER = 0.5;
    private static final double     SHIFT_MULTIPLIER = 1.5;
    private static final double     MOUSE_SPEED = 0.3;
    private static final double     ROTATION_SPEED = 1.25;
    private static final double     TRACK_SPEED = 0.3;

    double                          mousePosX;
    double                          mousePosY;
    double                          mouseOldX;
    double                          mouseOldY;
    double                          mouseDeltaX;
    double                          mouseDeltaY;
    int                             currentFrame = 0;
    
    boolean                         startState = false;
    boolean                         gridLoaded = false;
    boolean                         networkLoaded = false;
    boolean                         firstRun= true;
    boolean                         success= true;

    private void buildCamera()
    {
        System.out.println("buildCamera()");
        root.getChildren().add(x_y_Rotations);
        x_y_Rotations.getChildren().add(x_y_Translations);
        x_y_Translations.getChildren().add(z_Rotations);
        z_Rotations.getChildren().add(camera);
        z_Rotations.setRotateZ(180.0);

        camera.setNearClip(CAMERA_NEAR_CLIP);
        camera.setFarClip(CAMERA_FAR_CLIP);
        camera.setTranslateZ(-150);
        camera.setTranslateY(0);
        camera.setTranslateX(100);
//        camera2.setTranslateZ(-60);
//        camera2.setTranslateY(0);
//        camera2.setTranslateX(100);

        camera.setFieldOfView(120);
//        camera2.setFieldOfView(120);
        //2D
//        x_y_Rotations.ry.setAngle(180);
//        x_y_Rotations.rx.setAngle(30);
        x_y_Rotations.ry.setAngle(0);
        x_y_Rotations.rx.setAngle(0);
    }

    private void buildAxes(int width, int height, int depth)
    {

        final Box xAxis = new Box(4000, 1, 1);
        final Box yAxis = new Box(1, 4000, 1);
        final Box zAxis = new Box(1, 1, 4000);
        xAxis.setMaterial(new PhongMaterial(Color.RED));
        yAxis.setMaterial(new PhongMaterial(Color.GREEN));
        zAxis.setMaterial(new PhongMaterial(Color.BLUE));
        int tickMarksRange = Math.max(depth, Math.max(height, width));
        for (int tickValue = -tickMarksRange; tickValue <= tickMarksRange; tickValue += tickMarksRange / 10)
        {
            Label xTicks = new Label();
            xTicks.setText(String.valueOf(tickValue * -1));
            xTicks.setTextFill(Color.BLACK);
            xTicks.setFont(Font.font("Arial", FontPosture.REGULAR, 12));
            xTicks.setCache(true);
            xTicks.setCacheHint(CacheHint.QUALITY);
            xTicks.setTranslateX(tickValue);

            xTicks.setRotate(180);

            Label yTicks = new Label();
            yTicks.setText(String.valueOf(tickValue));
            yTicks.setTextFill(Color.BLACK);
            yTicks.setFont(Font.font("Arial", FontPosture.REGULAR, 12));
            yTicks.setCache(true);
            yTicks.setCacheHint(CacheHint.QUALITY);
            yTicks.setTranslateY(tickValue);
            yTicks.setRotate(180);

            Label zTicks = new Label();
            zTicks.setText(String.valueOf(tickValue));
            zTicks.setTextFill(Color.BLACK);
            zTicks.setFont(Font.font("Arial", FontPosture.REGULAR, 12));
            zTicks.setCache(true);
            zTicks.setCacheHint(CacheHint.QUALITY);
            zTicks.setTranslateZ(tickValue);
//                    
            zTicks.setRotationAxis(Rotate.Z_AXIS);
            zTicks.setRotate(180);

            tickMarks.addAll(Arrays.asList(xTicks, yTicks, zTicks));
        }

        axisGroup.getChildren().addAll(xAxis, yAxis, zAxis);

//       
        mainContainer.getChildren().addAll(axisGroup);
        mainContainer.getChildren().addAll(tickMarks);
    }

    public void handleMouse(SubScene scene, PerspectiveCamera camera)
    {
        scene.setOnMousePressed(me ->
        {
            me.consume();
            mousePosX = me.getSceneX();
            mousePosY = me.getSceneY();
            mouseOldX = me.getSceneX();
            mouseOldY = me.getSceneY();

        });
        scene.setOnMouseDragged(me ->
        {
            me.consume();
            mouseOldX = mousePosX;
            mouseOldY = mousePosY;
            mousePosX = me.getSceneX();
            mousePosY = me.getSceneY();
            mouseDeltaX = (mousePosX - mouseOldX);
            mouseDeltaY = (mousePosY - mouseOldY);

            double modifier = 2.0;

            if (me.isControlDown())
            {
                modifier = CONTROL_MULTIPLIER * 2;
            }
            if (me.isShiftDown())
            {
                modifier = SHIFT_MULTIPLIER * 2;
            }
//            Rotation
            if (me.isPrimaryButtonDown() && tab.getSelectionModel().getSelectedIndex() != 1)
            {
                x_y_Rotations.ry.setAngle(x_y_Rotations.ry.getAngle() - mouseDeltaX * MOUSE_SPEED * modifier * ROTATION_SPEED);
                x_y_Rotations.rx.setAngle(x_y_Rotations.rx.getAngle() + mouseDeltaY * MOUSE_SPEED * modifier * ROTATION_SPEED);
            } //            Zoom   
            else if (me.isSecondaryButtonDown())
            {
                double z = camera.getTranslateZ();
                
                
                double newZ = z + mouseDeltaX * MOUSE_SPEED * modifier;
                camera.setTranslateZ(newZ);
            } //Pan  
            else if (me.isMiddleButtonDown())
            {
                if(tab.getSelectionModel().getSelectedIndex() == 1)
                {
                    camera2.setTranslateX(camera.getTranslateX()-mouseDeltaX);
                    camera2.setTranslateY(camera.getTranslateY()- mouseDeltaY );
                }
                else
                {
                    x_y_Translations.t.setX(x_y_Translations.t.getX() + mouseDeltaX * MOUSE_SPEED * modifier * TRACK_SPEED);
                    x_y_Translations.t.setY(x_y_Translations.t.getY() + mouseDeltaY * MOUSE_SPEED * modifier * TRACK_SPEED);
                }
            }
        }
        );
    }

    public void play()
    {

        if (currentFrame >= randomNetwork.getFrames().size())
        {
            randomNetwork.getTimeline2().play();
            return;

        }
        if (randomNetwork.getFrames().get(currentFrame) != null)
        {

            Timeline temp = new Timeline();
            temp.getKeyFrames().add(randomNetwork.getFrames().get(currentFrame));

            temp.play();

            currentFrame++;
        }

    }
    public void setUp(int width, int height, int depth,int n, int tr,int DN, double DP, double SC)
    {
        
        int choice = 0;
        graphSpace.getChildren().removeAll(randomNetwork.getEdgeBetweenNodes());
        graphSpace.getChildren().removeAll(randomNetwork.getSensorNetwork());
        
        randomNetwork.getEdgeToNodePairing().clear();
        
        if(firstRun)
        {            
            System.out.println("\n=====================================================================\n"
                    + "\tHello,\n\tThis Program Calculates the shortest path between\n"
                    + "\ttwo nodes using dijkstra's or prims algorithm.\n"
                    + "=====================================================================\n");
            System.out.printf("Enter Width (Integer): ");
            width = keyboard.nextInt();
            System.out.printf("Enter Height (Integer): ");
            height = keyboard.nextInt();
            System.out.printf("Enter Depth (Integer): ");
            depth = keyboard.nextInt();
            System.out.printf("Enter number of Nodes (Integer): ");
            n = keyboard.nextInt();
            System.out.printf("Enter Threshhold (Integer): ");
            tr = keyboard.nextInt();
            System.out.printf("Enter number of  Data Nodes (Integer): ");
            DN = keyboard.nextInt();
            while(DN>n)
            {
                System.out.println("Data Nodes greater than total number of nodes.\n"
                        + "Number of Data Nodes needs to be <= "+n+"\n");
                System.out.printf("Enter number of  Data Nodes (Integer): ");
                DN = keyboard.nextInt();
            }
            System.out.printf("Enter number of Data Packets for each Data Node: ");
            DP = keyboard.nextInt();
            System.out.printf("Enter Storage Capacity of each Storage Node: ");
            SC= keyboard.nextInt();
            
            double dataPackets = DN * DP;
            double storageSpace = (n - DN) * SC;
            while (dataPackets > storageSpace)
            {
                System.out.println("There is not enough storage in the network.\n"
                        + "Please provide new data for:\n Number of Data Nodes...\n"
                        + " Number of Data Packets...\n and Storage Capcity of each Storage Node...\n");
                System.out.printf("Enter number of  Data Nodes (Integer): ");
                DN = keyboard.nextInt();
                while (DN > n)
                {
                    System.out.println("Data Nodes greater than total number of nodes.\n"
                            + "Number of Data Nodes needs to be <= " + n + "\n");
                    System.out.printf("Enter number of  Data Nodes (Integer): ");
                    DN = keyboard.nextInt();
                }
                System.out.printf("Enter number of Data Packets for each Data Node: ");
                DP = keyboard.nextInt();
                System.out.printf("Enter Storage Capacity of each Storage Node: ");
                SC = keyboard.nextInt();       
                dataPackets = DN * DP;
                storageSpace = (n - DN) * SC;
                
            }
            
            firstRun=false;
        }

//        Task<Void> startTask = new Task<Void>()
//            {
//                @Override
//                protected Void call() throws Exception
//                {
        randomNetwork.make3DGraph(width, height, depth, n, tr, DN, DP, SC);
        randomNetwork.setStart((randomNetwork.getSensorNetwork().isEmpty() ?null:
                                randomNetwork.getSensorNetwork().get(randomNetwork.getSensorNetwork().size()-1)));
        if (randomNetwork.dfs("network")==false)
        {
            
            System.out.println("===================================");
            System.out.println("Graph is not connected, please input new values."
                    + " Aim for a tranmission range around 80% of\n"
                    + " the maximum between graph width,height, and depth");
            System.out.println("Retrying......");
            System.out.printf("Enter Width (Integer): ");
            width = keyboard.nextInt();
            System.out.printf("Enter Height (Integer): ");
            height = keyboard.nextInt();
            System.out.printf("Enter Depth (Integer): ");
            depth = keyboard.nextInt();
            System.out.printf("Enter number of Nodes (Integer): ");
            n = keyboard.nextInt();
            System.out.printf("Enter Threshhold (Integer): ");
            tr = keyboard.nextInt();
            System.out.printf("Enter number of  Data Nodes (Integer): ");
            DN = keyboard.nextInt();
            while(DN>n)
            {
                System.out.println("Data Nodes greater than total number of nodes.\n"
                        + "Number of Data Nodes needs to be <= "+n+"\n");
                System.out.printf("Enter number of  Data Nodes (Integer): ");
                DN = keyboard.nextInt();
            }
            System.out.printf("Enter number of Data Packets for each Data Node: ");
            DP = keyboard.nextInt();
            System.out.printf("Enter Storage Capacity of each Storage Node: ");
            SC= keyboard.nextInt();
            
            double dataPackets = DN * DP;
            double storageSpace = (n - DN) * SC;
            while (dataPackets > storageSpace)
            {
                System.out.println("There is not enough storage in the network.\n"
                        + "Please provide new data for Number of Data Nodes,"
                        + "Number of Data Packets, and Storage Capcity of each Storage Node.\n");
                
                System.out.printf("Enter number of  Data Nodes (Integer): ");
                DN = keyboard.nextInt();
                while (DN > n)
                {
                    System.out.println("Data Nodes greater than total number of nodes.\n"
                            + "Number of Data Nodes needs to be <= " + n + "\n");
                    System.out.printf("Enter number of  Data Nodes (Integer): ");
                    DN = keyboard.nextInt();
                }
                System.out.printf("Enter number of Data Packets for each Data Node: ");
                DP = keyboard.nextInt();
                System.out.printf("Enter Storage Capacity of each Storage Node: ");
                SC = keyboard.nextInt();    
                dataPackets = DN * DP;
                storageSpace = (n - DN) * SC;
                
            }
           
            setUp(width,height,depth,n,tr,DN,DP,SC);            

        }
        else
        { 
            System.out.println("\n###############################################################################################################");
            System.out.println("Network Successfully Generated...\n"
                    + "\tEach node has a random x and y location. The list of nodes will be organized based on Node type.\n"
                    + "\tIf there are any Data Nodes, they will be listed first for clarity.");
            System.out.println("\n###############################################################################################################\n\n");
            System.out.println("\n-------------------------------\n"
                    + "CONNECTION STATUS: CONNECTED\n"
                    + "-------------------------------\n");
            
            
            System.out.printf("\n\nEnter 0 for Dijkstra.\n"
                    + "Enter 1 for Bellman-Ford.\n"
                    + "Enter 2 for Prim"
                    + "\t->choice: ");
            choice = keyboard.nextInt();
            
            
            animationSpeed.setMin(0.01);
            animationSpeed.setMax(3);
            animationSpeed.setMajorTickUnit(animationSpeed.getMax() / 10);

            randomNetwork.getTimeline().rateProperty().bind(animationSpeed.valueProperty());
            randomNetwork.getTimeline2().rateProperty().bind(animationSpeed.valueProperty());
            randomNetwork.getTimeline().setOnFinished(e2 -> randomNetwork.getTimeline2().play());

//            graphSpace.getChildren().addAll(randomNetwork.getSensorNetwork());
//            graphSpace.getChildren().addAll(randomNetwork.getEdgeBetweenNodes());            
//            nodesToVisit.getItems().addAll(randomNetwork.getToExplore());
            randomNetwork.getSensorNetwork().forEach(node ->
            {

                node.setOnMouseClicked(e ->
                {
                    e.consume();
                     
                    if (e.getButton().equals(MouseButton.MIDDLE))
                    {
                        randomNetwork.setStart(null);
                        randomNetwork.setStart((Sphere) e.getSource());
                        startState = true;
                        System.out.println(randomNetwork.getStart().getId());
                        ((Sphere) randomNetwork.getStart()).setMaterial(new PhongMaterial(Color.YELLOW));
                        startNode.setText(randomNetwork.getStart().getId());
                        dfs.setDisable(false);
                        bfs.setDisable(false);
                        dijkstra.setDisable(false);
                        prim.setDisable(false);

                    }
                    if (e.isPrimaryButtonDown())
                    {
                        randomNetwork.getAdjacentNodes().get(node).forEach(neighbor -> 
                        {
                            Timeline temp = new Timeline();
                            Material m = ((Sphere) neighbor).getMaterial();
                            KeyFrame kf2 = new KeyFrame(Duration.millis(1000),
                                    es ->
                                    {
                                        ((Sphere) neighbor).setMaterial(new PhongMaterial(Color.BLUE));
                                    }, new KeyValue(((Sphere) neighbor).radiusProperty(), 1.5, Interpolator.EASE_OUT));
                            temp.getKeyFrames().add(kf2);
                            
                            KeyFrame kf3 = new KeyFrame(Duration.millis(1000),
                                    es ->
                                    {
                                        ((Sphere) neighbor).setMaterial(m);
                                    },
                                    new KeyValue(((Sphere) neighbor).radiusProperty(), 0.8, Interpolator.EASE_OUT));
                            temp.getKeyFrames().add(kf3);
                        });
                    }
                    if (e.getButton().equals(MouseButton.SECONDARY))
                    {

                        randomNetwork.setEnd(null);
                        if (startState)
                        {
                            randomNetwork.setEnd((Sphere) e.getSource());
                            ((Sphere) randomNetwork.getEnd()).setMaterial(new PhongMaterial(Color.RED));
                            endNode.setText(randomNetwork.getEnd().getId());
//                                
                            startState = false;

                        }

                        

                    }

                });
            });
            
//            randomNetwork.getEdgeBetweenNodes().forEach(edge ->
//            {
////                edge.materialProperty().bind(Bindings.createObjectBinding(
////                        ()-> new PhongMaterial(bgColor.getValue()),bgColor.valueProperty()));
////                l.startYProperty().bind(Bindings.createDoubleBinding(
////                () -> startBox.getBoundsInParent().getMaxY()-startBox.getHeight()/2,
////                startBox.boundsInParentProperty()));
//               
//                edge.setOnMousePressed(e ->
//                {
//                    if (e.getButton().equals(MouseButton.PRIMARY))
//                    {
//
//                        if (e.getClickCount() == 2)
//                        {
//                            distance.setDisable(false);
//                            distance.setOnKeyPressed(k ->
//                            {
//                                if (k.getCode().equals(KeyCode.ENTER))
//                                {
//                                    edge.setId(String.format("%s", distance.getText()));
//                                    distance.clear();
//                                    distance.setPromptText("Distance");
//                                    distance.setDisable(true);
//                                }
//                            });
//
//                        } else
//                        {
//                            distance.setPromptText(edge.getId());
//                        }
//
//                    }
//
//                });
//
//            });

            if (choice == 0 && randomNetwork.getSensorNetwork().size()>0)
            {
                System.out.println("Please choose Starting Node and Destination Node.\n"
                    + "^^^^Refer to the above printout for Node ID^^^^\n");
                System.out.printf("Starting Node ID: ");
                int startingNode = keyboard.nextInt();
                System.out.printf("Destination Node ID: ");
                int destinationNode = keyboard.nextInt();
                randomNetwork.setStart(randomNetwork.getSensorNetwork().get(startingNode));
                randomNetwork.setEnd(randomNetwork.getSensorNetwork().get(destinationNode));
                
                ((Sphere) randomNetwork.getStart()).setMaterial(new PhongMaterial(Color.YELLOW));
                startNode.setText(randomNetwork.getStart().getId());
                graphSpace.getChildren().addAll(randomNetwork.getSensorNetwork());

                graphSpace.getChildren().addAll(randomNetwork.getEdgeBetweenNodes());
                nodesToVisit.getItems().clear();
                nodesToVisit.getItems().addAll(randomNetwork.getToExplore());
                randomNetwork.dijkstra(randomNetwork.getStart(),"network");
                

            }

            if (choice == 1 && randomNetwork.getSensorNetwork().size()>0)
            {
                System.out.println("Please choose Starting Node and Destination Node.\n"
                    + "^^^^Refer to the above printout for Node ID^^^^\n");
                System.out.printf("Starting Node ID: ");
                int startingNode = keyboard.nextInt();
                System.out.printf("Destination Node ID: ");
                int destinationNode = keyboard.nextInt();
                randomNetwork.setStart(randomNetwork.getSensorNetwork().get(startingNode));
                randomNetwork.setEnd(randomNetwork.getSensorNetwork().get(destinationNode));
                
                ((Sphere) randomNetwork.getStart()).setMaterial(new PhongMaterial(Color.YELLOW));
                startNode.setText(randomNetwork.getStart().getId());
                graphSpace.getChildren().addAll(randomNetwork.getSensorNetwork());

                graphSpace.getChildren().addAll(randomNetwork.getEdgeBetweenNodes());
                nodesToVisit.getItems().clear();
                nodesToVisit.getItems().addAll(randomNetwork.getToExplore());
                randomNetwork.bellmanFord("network");
                

            }
            if (choice == 2)
            {
                randomNetwork.setStart(randomNetwork.getSensorNetwork().get(new Random().nextInt(randomNetwork.getSensorNetwork().size())));
                
                ((Sphere) randomNetwork.getStart()).setMaterial(new PhongMaterial(Color.YELLOW));
                startNode.setText(randomNetwork.getStart().getId());
                Task<Void> primTask = new Task<Void>()
                {
                    @Override
                    protected Void call() throws Exception
                    {
                        randomNetwork.prim(randomNetwork.getStart(),"network");

                        return null;
                    }
                };

                nodesToVisit.getItems().clear();
                nodesToVisit.getItems().addAll(randomNetwork.getToExplore());
                primTask.setOnSucceeded(ss ->
                {

                    graphSpace.getChildren().addAll(randomNetwork.getSensorNetwork());
                    graphSpace.getChildren().addAll(randomNetwork.getEdgeBetweenNodes());

                });
                primTask.run();

            }
                

            
        }
        
        
        currentFrame = 0;
    }
    public void generateNetwork(int width, int height, int depth,int n, int tr,int DN, double DP, double SC)
    {
        
        
        appPane.setCursor(Cursor.WAIT);
        appPane.setDisable(true);
//        Task<Void> dijkstraTask = new Task<Void>()
//        {
//            @Override
//            protected Void call() throws Exception
//            {
                if (DN > n)
                {
                    networkStatus.setText("DN > NUM OF NODES");
                    networkStatus.setStyle("-fx-background-color: RED;");
                    success = false;
//                    return null;
                }

                double dataPackets = DN * DP;
                double storageSpace = (n - DN) * SC;
                if (dataPackets > storageSpace)
                {
                    networkStatus.setText("NOT ENOUGH STORAGE");
                    success = false;
//                    return null;
                }

                randomNetwork.make3DGraph(width, height, depth, n, tr, DN, DP, SC);
                randomNetwork.setStart((randomNetwork.getSensorNetwork().isEmpty() ? null
                        : randomNetwork.getSensorNetwork().get(randomNetwork.getSensorNetwork().size() - 1)));
                if (randomNetwork.dfs("network") == false)
                {
                    networkStatus.setText("UNCONNECTED");
                    networkStatus.setStyle("-fx-background-color: RED;");
                    randomNetwork.setStart(null);
                } else
                {
                    networkStatus.setText("CONNECTED");
                    networkStatus.setStyle("-fx-background-color: GREEN;");
                    randomNetwork.setStart(null);
                }

                    animationSpeed.setMin(0.01);
                    animationSpeed.setMax(3);
                    animationSpeed.setMajorTickUnit(animationSpeed.getMax() / 10);

                    randomNetwork.getTimeline().rateProperty().bind(animationSpeed.valueProperty());
                    randomNetwork.getTimeline2().rateProperty().bind(animationSpeed.valueProperty());
                    randomNetwork.getTimeline().setOnFinished(e2 -> randomNetwork.getTimeline2().play());

//            graphSpace.getChildren().addAll(randomNetwork.getSensorNetwork());
//            graphSpace.getChildren().addAll(randomNetwork.getEdgeBetweenNodes());            
//            nodesToVisit.getItems().addAll(randomNetwork.getToExplore());
                    randomNetwork.getSensorNetwork().forEach(node ->
                    {
                        node.setOnMouseClicked(e ->
                        {
                            
                            
                            e.consume();

                            if (e.getButton().equals(MouseButton.MIDDLE) && !e.isAltDown())
                            {
                                randomNetwork.setStart(null);
                                randomNetwork.setStart((Sphere) e.getSource());
                                startState = true;
                                System.out.println(randomNetwork.getStart().getId());
                                ((Sphere) randomNetwork.getStart()).setMaterial(new PhongMaterial(Color.YELLOW));
                                startNode.setText(randomNetwork.getStart().getId());
                                dfs.setDisable(false);
                                bfs.setDisable(false);
                                dijkstra.setDisable(false);
                                prim.setDisable(false);
                                bellManFord.setDisable(false);

                            }
                            if (e.getButton().equals(MouseButton.PRIMARY) && e.isShiftDown() )
                            {
                                u = node;
                                v=null;
                                System.out.println(u.getId());
                            }
                            if (e.getButton().equals(MouseButton.SECONDARY) && e.isShiftDown() )
                            {
                                if(u == null)
                                {
                                    networkStatus.setText("INVALID INPUT");
                                    return;
                                }
                                else
                                {
                                    v = node;
                                    if(randomNetwork.getEdgeToNodePairing().get(new Pair<>(u,v)) !=null)
                                    {
                                        distance.setDisable(false);
                                        Timeline temp = new Timeline();
                                        Edge chosenEdge = randomNetwork.getEdgeToNodePairing().get(new Pair<>(u,v));
                                        Material m = chosenEdge.getMaterial();
                                        
                                        KeyFrame kf = new KeyFrame(Duration.millis(100), es ->
                                        {
                                            chosenEdge.setMaterial(new PhongMaterial(Color.BLUE));

                                        }, new KeyValue((chosenEdge).radiusProperty(), 1.5));
                                        KeyFrame kf2 = new KeyFrame(Duration.millis(600), new KeyValue((chosenEdge).radiusProperty(), 0.15));
                                        temp.getKeyFrames().addAll(kf, kf2);
                                        temp.setCycleCount(1);
                                        temp.play();
                                        distance.setText(String.format("%.2f",chosenEdge.cost));
                                        distance.setOnKeyPressed(k ->
                                        {
                                            if (k.getCode().equals(KeyCode.ENTER))
                                            {
                                                try
                                                {
                                                    chosenEdge.cost = Double.valueOf(distance.getText());
                                                    chosenEdge.setMaterial(m);
                                                    distance.clear();
                                                    distance.setPromptText("Distance");
                                                    distance.setDisable(true);
                                                } catch (NumberFormatException ex)
                                                {
                                                    networkStatus.setText("INVALID INPUT");
                                                    distance.clear();
                                                    distance.setPromptText("Distance");
                                                    distance.setDisable(true);
                                                    return;
                                                }
                                                                                               
                                            }
                                        });
                                    }
                                    else
                                    {
                                        networkStatus.setText("NO EDGE BETWEEN U & V");
                                        return;
                                    }
                                    
                                }
                            }
                            if (e.getButton().equals(MouseButton.MIDDLE) && e.isAltDown())
                            {
                                selectedNode = node;
//                                nodeID.clear();
//                                nodeXLoc.clear();
//                                nodeYLoc.clear();
//                                nodeZLoc.clear();                                
                                adjacentNodes.getItems().clear();
                                nodeID.setText(selectedNode.getId());
                                nodeXLoc.setText(String.format("%.2f", selectedNode.getTranslateX()));
                                nodeYLoc.setText(String.format("%.2f", selectedNode.getTranslateY()));
                                nodeZLoc.setText(String.format("%.2f", selectedNode.getTranslateZ()));
                                randomNetwork.getAdjacentNodes().get(node).forEach(neighbor ->
                                {
                                    Timeline temp = new Timeline();
                                    Button b = new Button(neighbor.getId());
                                    
                                    

                                    b.setOnAction(act ->
                                    {
                                        Material m = ((Sphere) neighbor).getMaterial();
                                        
                                        KeyFrame kf = new KeyFrame(Duration.millis(100), es ->
                                        {
                                            ((Sphere) neighbor).setMaterial(new PhongMaterial(Color.BLUE));

                                        }, new KeyValue(((Sphere) neighbor).radiusProperty(), 3));
                                        KeyFrame kf2 = new KeyFrame(Duration.millis(600), es ->
                                        {
                                            ((Sphere) neighbor).setMaterial(m);

                                        }, new KeyValue(((Sphere) neighbor).radiusProperty(), 0.8));
                                        temp.getKeyFrames().addAll(kf, kf2);
                                        temp.setCycleCount(1);
                                        temp.play();
                                    });
                                   
                                    adjacentNodes.getItems().add(b);
                                });
                                
                            }
                            if (e.getButton().equals(MouseButton.SECONDARY))
                            {

                                randomNetwork.setEnd(null);
                                if (startState)
                                {
                                    randomNetwork.setEnd((Sphere) e.getSource());
                                    ((Sphere) randomNetwork.getEnd()).setMaterial(new PhongMaterial(Color.RED));
                                    endNode.setText(randomNetwork.getEnd().getId());
//                                
                                    startState = false;

                                }

                                

                            }

                        });
                    });
                    
            if(success == true)
            {
                appPane.setCursor(Cursor.DEFAULT);
                appPane.setDisable(false);
                graphSpace.getChildren().clear();
                graphSpace.getChildren().addAll(randomNetwork.getSensorNetwork());
                graphSpace.getChildren().addAll(randomNetwork.getEdgeBetweenNodes());
                
                graphSpace.getChildren().addAll(randomNetwork.getNodeTags());
                
                nodesToVisit.getItems().addAll(randomNetwork.getToExplore());
            }
//        });
//        dijkstraTask.run();

        
//        Thread t= new Thread(dijkstraTask);
//        t.setDaemon(true);
//        t.start();
//        

        
        currentFrame = 0;
    }

    public void network()
    {
        graphSpace.getChildren().clear();

        animationSpeed.setMin(0.01);
        animationSpeed.setMax(3);
        animationSpeed.setMajorTickUnit(animationSpeed.getMax() / 10);

        randomNetwork.getTimeline().rateProperty().bind(animationSpeed.valueProperty());
        randomNetwork.getTimeline2().rateProperty().bind(animationSpeed.valueProperty());
        randomNetwork.getTimeline().setOnFinished(e2 -> randomNetwork.getTimeline2().play());

        gridNetwork = randomNetwork.testNetwork();
        gridNetwork.forEach(node ->
        {

            node.setOnMouseClicked(e ->
            {

                if (e.getButton().equals(MouseButton.MIDDLE))
                {
                    randomNetwork.setStart(null);
                    randomNetwork.setStart((Sphere) e.getSource());
                    startState = true;
                    System.out.println(randomNetwork.getStart().getId());
                    ((Sphere) randomNetwork.getStart()).setMaterial(new PhongMaterial(Color.YELLOW));
                    startNode.setText(randomNetwork.getStart().getId());
                    dfs.setDisable(false);
                    bfs.setDisable(false);
                    dijkstra.setDisable(false);
                    prim.setDisable(false);

                }
                if (e.isPrimaryButtonDown())
                {
                    randomNetwork.getAdjacentNodes().get(node).forEach(neighbor -> ((Sphere) neighbor).setMaterial(new PhongMaterial(Color.GREEN)));
                }
                if (e.getButton().equals(MouseButton.SECONDARY))
                {

                    randomNetwork.setEnd(null);
                    if (startState)
                    {
                        randomNetwork.setEnd((Sphere) e.getSource());
                        ((Sphere) randomNetwork.getEnd()).setMaterial(new PhongMaterial(Color.GREEN));
                        endNode.setText(randomNetwork.getEnd().getId());
                        //                                
                        startState = false;

                    }

                    randomNetwork.getTimeline().setCycleCount(1);

                    randomNetwork.getTimeline().setOnFinished(e2 -> randomNetwork.getTimeline2().play());

                }

            });
        });
       
        graphSpace.getChildren().addAll(randomNetwork.getSensorNetwork());
        graphSpace.getChildren().addAll(randomNetwork.getEdgeBetweenNodes());
        graphSpace.getChildren().addAll(gridNetwork);
        
        nodesToVisit.getItems().clear();
        nodesToVisit.getItems().addAll(randomNetwork.getToExplore());
    }
    public void grid()
    {
        animationSpeed.setMin(0.01);
        animationSpeed.setMax(3);
        animationSpeed.setMajorTickUnit(animationSpeed.getMax() / 10);

        randomNetwork.getTimeline().rateProperty().bind(animationSpeed.valueProperty());
        randomNetwork.getTimeline2().rateProperty().bind(animationSpeed.valueProperty());
        randomNetwork.getTimeline().setOnFinished(e2 -> randomNetwork.getTimeline2().play());

        manhatGrid.forEach(cubey ->
        {
            cubey.setOnMouseEntered(e ->
            {

                if (e.isControlDown())
                {
                    if (!cubey.getId().contains("barrier"))
                    {
                        ((Box) cubey).setMaterial(new PhongMaterial(Color.BLACK));
                        cubey.setId(String.format("%s barrier", cubey.getId()));
                    }

                }
                if (e.isShiftDown())
                {
                    if (cubey.getId().contains("barrier"))
                    {
                        ((Box) cubey).setMaterial(new PhongMaterial(Color.CYAN));
                        cubey.setId(cubey.getId().substring(0, cubey.getId().indexOf("barrier")));
                    }

                }

            });

            cubey.setOnMouseClicked(e ->
            {

                if (e.getButton().equals(MouseButton.MIDDLE))
                {
                    startState = true;
                    randomNetwork.setStart(null);
                    randomNetwork.setStart((Box) e.getSource());
                    startState = true;
                    System.out.println(randomNetwork.getStart().getId());
                    ((Box) randomNetwork.getStart()).setMaterial(new PhongMaterial(Color.YELLOW));
                    startNode.setText(randomNetwork.getStart().getId());
                    dfs.setDisable(false);
                    bfs.setDisable(false);
                    dijkstra.setDisable(false);

                }
                if (e.isPrimaryButtonDown())
                {
                    randomNetwork.getAdjacentNodes().get(cubey).forEach(neighbor -> ((Box) neighbor).setMaterial(new PhongMaterial(Color.GREEN)));
                }
                if (e.getButton().equals(MouseButton.SECONDARY))
                {
                    randomNetwork.setEnd(null);
                    if (startState)
                    {
                        randomNetwork.setEnd((Box) e.getSource());
                        ((Box) randomNetwork.getEnd()).setMaterial(new PhongMaterial(Color.RED));
                        endNode.setText(randomNetwork.getEnd().getId());
//                                
                        startState = false;

                    }

                    

                }
            });

        });
        graphSpace.getChildren().addAll(manhatGrid);

        nodesToVisit.getItems().clear();
        nodesToVisit.getItems().addAll(randomNetwork.getToExplore());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        
        System.out.println("start()");
        setUp(0,0,0,0,0,0,0.0,0.0);        
        root.setDepthTest(DepthTest.ENABLE);  
        buildCamera();
        
        buildAxes(500, 500, 500);
        distance.setStyle("-fx-background-color: #851D2D;"
                     + "-fx-Text-fill: #ffffff;");
//        height.setStyle("-fx-background-color:  #003B1B;"
//                     + "-fx-Text-fill: #ffffff;");
//        depth.setStyle("-fx-background-color:  #0723DB;"
//                     + "-fx-Text-fill: #ffffff;");
//        numOfNodes.setStyle("-fx-background-color:  #C710AF;"
//                     + "-fx-Text-fill: #ffffff;");
//        transmissionRange.setStyle("-fx-background-color:  #1F0036;"
//                     + "-fx-Text-fill: #ffffff;");
//        numOfDataNodes.setStyle("-fx-background-color:  #2F3600;"
//                     + "-fx-Text-fill: #ffffff;");
//        numOfPackets.setStyle("-fx-background-color:  #086C96;"
//                     + "-fx-Text-fill: #ffffff;");
//        capacity.setStyle("-fx-background-color:  #96534B;"
//                     + "-fx-Text-fill: #ffffff;");
//        
        
        
        grid.setOnAction(act ->
        {
            gridLoaded = true;
            networkLoaded = false;
            reset.fire();
            grid();
        });
        network.setOnAction(act ->
        {
            act.consume();
            System.out.println("woop");
            gridLoaded = false;
            networkLoaded = true;

            reset.fire();
            network();
        });
        generate.setOnAction(act ->
        {
            act.consume();
           
            

            reset.fire();
            try
            {
                int w = Integer.valueOf(width.getText());
                int h = Integer.valueOf(height.getText());
                int d = Integer.valueOf(depth.getText());
                int n = Integer.valueOf(numOfNodes.getText());
                int tr = Integer.valueOf(transmissionRange.getText());
                int DN = Integer.valueOf(numOfDataNodes.getText());
                int DP = Integer.valueOf(numOfPackets.getText());
                int SC = Integer.valueOf(capacity.getText());
                generateNetwork(w,h,d,n,tr,DN,DP,SC);
//                generateNetwork(200,200,0,10,90,0,1,1);
            }
            catch (Exception ex)
            {
                networkStatus.setText("INVALID DATA");
                
            }
        });
            
        dfs.setOnAction(a ->
        {
            nodesToVisit.getItems().clear();
            nodesToVisit.getItems().addAll(randomNetwork.getToExplore());
            bfs.setSelected(false);
            dijkstra.setSelected(false);
            prim.setSelected(false);
            bellManFord.setSelected(false);
            String networkType = gridLoaded ? "grid" :(networkLoaded?"gridNetwork": "network");
            System.out.println(networkType);
            Task<Void> dfsTask = new Task<Void>()
            {
                @Override
                protected Void call() throws Exception
                {
                    randomNetwork.dfs(networkType);
//                    randomNetwork.testing(randomNetwork.getStart(), randomNetwork.getEnd(),"gridNetwork");
                    randomNetwork.getTimeline().setCycleCount(1);
                    
                    
                    return null;
                }
            };
            dfsTask.setOnSucceeded(s->play.setStyle("-fx-background-color: GREEN;"));
            dfsTask.run();
            
            dfs.disarm();
            
            dijkstra.setDisable(true);
            
            prim.setDisable(true);
            dfs.setDisable(true);
            bfs.setDisable(true);
            bellManFord.setDisable(true);
        });
        bfs.setOnAction(a ->
        {
            nodesToVisit.getItems().clear();
            nodesToVisit.getItems().addAll(randomNetwork.getToExplore());
            dfs.setSelected(false);
            dijkstra.setSelected(false);
            prim.setSelected(false);
            bellManFord.setSelected(false);
            String networkType = gridLoaded==true ? "grid" :(networkLoaded==true?"gridNetwork": "network");
            System.out.println(networkType);

            Task<Void> bfsTask = new Task<Void>()
            {
                @Override
                protected Void call() throws Exception
                {
                    
                    randomNetwork.bfs(networkType);
                    
                    
                    randomNetwork.getTimeline().setCycleCount(1);
                    
                    return null;
                }
            };
            bfsTask.setOnSucceeded(s->play.setStyle("-fx-background-color: GREEN;"));
            bfsTask.run();
            bfs.disarm();
//            bfs.setSelected(false);

            dijkstra.setDisable(true);
            prim.setDisable(true);
            dfs.setDisable(true);
            bfs.setDisable(true);
            bellManFord.setDisable(true);
        });
        prim.setOnAction(a ->
        {
            nodesToVisit.getItems().clear();
            nodesToVisit.getItems().addAll(randomNetwork.getToExplore());
            String networkType = gridLoaded ? "grid" :(networkLoaded?"gridNetwork": "network");
            dfs.setSelected(false);
            bfs.setSelected(false);
            dijkstra.setSelected(false);
            bellManFord.setSelected(false);
            System.out.println(networkType);
            Task<Void> primTask = new Task<Void>()
            {
                @Override
                protected Void call() throws Exception
                {
                    randomNetwork.prim(randomNetwork.getStart(),networkType);
                    randomNetwork.getTimeline().setCycleCount(1);
                    
                    
                    return null;
                }
            };
            primTask.setOnSucceeded(s->play.setStyle("-fx-background-color: GREEN;"));
            primTask.run();
            
            prim.disarm();
            prim.setSelected(false);
            dijkstra.setDisable(true);
            prim.setDisable(true);
            dfs.setDisable(true);
            bfs.setDisable(true);
            bellManFord.setDisable(true);
        });
        dijkstra.setOnAction(a ->
        {
            
            randomNetwork.getToExplore().add(new Button());
            String networkType = gridLoaded ? "grid" :(networkLoaded?"gridNetwork": "network");
            dfs.setSelected(false);
            bfs.setSelected(false);
            prim.setSelected(false);
            bellManFord.setSelected(false);
//            System.out.println(networkType);
            Task<Void> dijkstraTask = new Task<Void>()
            {
                @Override
                protected Void call() throws Exception
                {
//                    if(gridLoaded || networkLoaded)
//                    {
//                        randomNetwork.dijkstraForGrid(networkType);
//                        
//                    }
                    if(gridLoaded )
                    {
                        randomNetwork.dijkstraForGrid(networkType);
                        
                    }
                    else
                    {
                        randomNetwork.dijkstra(randomNetwork.getStart(), networkType);
                    }
                    randomNetwork.getTimeline().setCycleCount(1);
                    
                    
                    return null;
                }
            };
            dijkstraTask.setOnSucceeded(s->
            {
                play.setStyle("-fx-background-color: GREEN;");
                nodesToVisit.getItems().clear();
                nodesToVisit.getItems().addAll(randomNetwork.getToExplore());
            });
            dijkstraTask.run();
            
            dijkstra.disarm();
            dijkstra.setSelected(false);
            dijkstra.setDisable(true);
            prim.setDisable(true);
            dfs.setDisable(true);
            bfs.setDisable(true);
            bellManFord.setDisable(true);
        });
        bellManFord.setOnAction(a ->
        {
            
            
            String networkType = gridLoaded ? "grid" :(networkLoaded?"gridNetwork": "network");
            dfs.setSelected(false);
            bfs.setSelected(false);
            prim.setSelected(false);
            dijkstra.setSelected(false);
//            System.out.println(networkType);
            Task<Void> BellmanFord = new Task<Void>()
            {
                @Override
                protected Void call() throws Exception
                {
                    randomNetwork.bellmanFord(networkType);
                    randomNetwork.getTimeline().setCycleCount(1);
                    
                    
                    return null;
                }
            };
            BellmanFord.setOnSucceeded(s->
            {
                play.setStyle("-fx-background-color: GREEN;");
                dpTable.getChildren().clear();
                dpTable.getChildren().add(randomNetwork.populateTable());
            });
            BellmanFord.run();
            
            bellManFord.disarm();
            dijkstra.setSelected(false);
            bellManFord.setDisable(true);
            dijkstra.setDisable(true);
            prim.setDisable(true);
            dfs.setDisable(true);
            bfs.setDisable(true);
        });
//        kEdgesButton.setOnAction(act->
//        {
//            try
//            {
//                int k = Integer.valueOf(kEdges.getText());
//                kEdges.clear();
//                String networkType = gridLoaded ? "grid" : (networkLoaded ? "gridNetwork" : "network");
//                dfs.setSelected(false);
//                bfs.setSelected(false);
//                prim.setSelected(false);
//                dijkstra.setSelected(false);
////            System.out.println(networkType);
//                Task<Void> BellmanFord = new Task<Void>()
//                {
//                    @Override
//                    protected Void call() throws Exception
//                    {
//                        randomNetwork.BellmanFord(networkType,k);
//                        randomNetwork.getTimeline().setCycleCount(1);
//                        
//                        return null;
//                    }
//                };
//                BellmanFord.setOnSucceeded(s ->
//                {
//                    play.setStyle("-fx-background-color: GREEN;");
//                    kEdges.setText(NodeNetwork.kEdges+"");
//                    dpTable.getChildren().clear();
//                    dpTable.getChildren().add(randomNetwork.populateTable());
//                    
//                });
//                BellmanFord.run();
//                
//                bellManFord.disarm();
//                dijkstra.setSelected(false);
//                bellManFord.setDisable(true);
//                dijkstra.setDisable(true);
//                prim.setDisable(true);
//                dfs.setDisable(true);
//                bfs.setDisable(true);
//                
//            } catch (Exception e)
//            {
//                
//                kEdges.setStyle("-fx-background-color:   RED;"
//                        + "-fx-Text-fill: #ffffff;");
//                kEdges.setText("INVALID");
//                
//            }
//        });
        
        reset.setOnAction(action ->
        {
            play.setStyle("-fx-background-color: RED;");
            randomNetwork.resetSensorNetwork();
            randomNetwork.getTimeline().stop();
            startNode.setText("Start Node");
            endNode.setText("End Node");
            nodesToVisit.getItems().clear();
            currentFrame= 0;
            dfs.setSelected(false);
            bfs.setSelected(false);
            dijkstra.setSelected(false);
            prim.setSelected(false);
            bellManFord.setSelected(false);

        });
        resetCam.setOnAction(ac->
        {
            
            camera.setTranslateZ(-150);
            camera.setTranslateY(0);
            camera.setTranslateX(0);
            camera.setFieldOfView(120);
            x_y_Rotations.ry.setAngle(0);
            x_y_Rotations.rx.setAngle(0);
            x_y_Rotations.setTranslate(0.0,0.0,0.0);
            
        });
        replay.setOnAction(ac->
        {
          
            randomNetwork.getSensorNetwork().forEach(node -> ((Sphere) node).setMaterial(new PhongMaterial(Color.CYAN)));
            randomNetwork.getEdgeBetweenNodes().forEach(node ->
            {
                ((Cylinder) node).setMaterial(new PhongMaterial(Color.RED));
                ((Cylinder) node).setRadius(0.15);
            });
            
            randomNetwork.getTimeline().rateProperty().unbind();
            randomNetwork.getTimeline().playFromStart();
            randomNetwork.getTimeline().rateProperty().bind(animationSpeed.valueProperty());
//            
        });
        play.setOnMouseClicked(e ->
        {
            if (e.getButton().equals(MouseButton.PRIMARY))
            {
                play();
            }
            if (e.getButton().equals(MouseButton.SECONDARY))
            {
                if (randomNetwork.getTimeline() != null)
                {
                    randomNetwork.getTimeline().play();
                }
            }

        });
        updateNodeLocation.setOnAction(act ->
        {
            try
            {
                selectedNode.setTranslateX(Double.valueOf(nodeXLoc.getText()));
                selectedNode.setTranslateY(Double.valueOf(nodeYLoc.getText()));
                selectedNode.setTranslateZ(Double.valueOf(nodeZLoc.getText()));
                if(randomNetwork.getAdjacentNodes().get(selectedNode) != null)
                {
                    for (int i = 0; i < randomNetwork.getAdjacentNodes().get(selectedNode).size(); i++) 
                    {
                        Node neighbor = randomNetwork.getAdjacentNodes().get(selectedNode).get(i);
                        Edge edge = randomNetwork.getEdgeToNodePairing().get(new Pair<>(selectedNode,neighbor));
                        Edge edge2 = randomNetwork.getEdgeToNodePairing().get(new Pair<>(neighbor,selectedNode));
                        edge.adjustEdge(edge, selectedNode, neighbor, 0, 1, 1);
                        edge2.adjustEdge(edge2, selectedNode, neighbor, 0, 1, 1);
                       
                        
                        
                    }
                }
                
            }
            catch(Exception ex)
            {
                
            }
            
        });
        clear.setOnAction(act ->
        {
            nodeID.clear();
            nodeXLoc.clear();
            nodeYLoc.clear();
            nodeZLoc.clear();
            adjacentNodes.getItems().clear();
        });

        
//        
        mainContainer.getChildren().add(graphSpace);
        SubScene sub = new SubScene(mainContainer, threeDimensions.getWidth(), threeDimensions.getHeight(), true, SceneAntialiasing.BALANCED);
        

        sub.setCamera(camera);

 
        
        sub.setFill(Color.valueOf("#838F9C"));
        handleMouse(sub,camera);
//        camera.translateZProperty().bind(Bindings.createDoubleBinding(()->-threeDimensions.getWidth()/2, threeDimensions.widthProperty()));
        
        sub.heightProperty().bind(threeDimensions.heightProperty());
        sub.widthProperty().bind(threeDimensions.widthProperty());
        threeDimensions.getChildren().add(sub);
        threeDimensions.setCache(true);
        threeDimensions.setCacheHint(CacheHint.SPEED);
        
       
//        sub2.setCamera(camera2);
        sub.fillProperty().bind(bgColor.valueProperty());
        bgColor.setValue(Color.valueOf("#838F9C"));        
//        

    }

}
