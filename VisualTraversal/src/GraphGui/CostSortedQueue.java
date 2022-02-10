package GraphGui;



import java.util.ArrayList;
import GraphGui.Edge;
import javafx.scene.Node;
import javafx.util.Pair;
public  class CostSortedQueue extends Edge
{ 
    ArrayList<Edge> c= new ArrayList<>();
    private int size=0;
    //node class that defines CostSortedQueue node
    class NetworkNode  { 
        double key; 
        NetworkNode left, right;
        Pair<Node,Node> edgeVertices;
        Edge e;
   
        public NetworkNode(Pair<Node,Node> nodes, Edge edge)
        { 
            if(edge==null)
            {
                key=0;
            }
            else
            {
                key = edge.cost;
            } 
            edgeVertices= nodes;
            e= edge;
            left = right = null; 
        } 
    } 

    public void setRoot(NetworkNode root)
    {
        this.root = root;
    }
    public NetworkNode getRoot()
    {
        return root;
    }
    public int getSize()
    {
        return size;
    }
    public boolean isEmpty()
    {
        return root == null;
    }
    // CostSortedQueue root node 
    NetworkNode root; 
   
    public NetworkNode makeNetworkNode(Pair<Node,Node> nodes, Edge edge)
    {
        return new NetworkNode(nodes,edge);
    }
   // Constructor for CostSortedQueue =>initial empty tree
    CostSortedQueue(){ 
        root = null; 
    } 
    //delete a node from CostSortedQueue
    NetworkNode deleteNetworkNode(NetworkNode node) { 
        root = deleteNNRecursive(root, node); 
        this.size--;
        return root;
    } 
   
    
    NetworkNode deleteNNRecursive(NetworkNode root, NetworkNode node)  { 
        
        if (root == null)  return root; 
   
        
        if (node.key < root.key)     
            root.left = deleteNNRecursive(root.left, node); 
        else if (node.key > root.key)  
            root.right = deleteNNRecursive(root.right, node); 
        else  { 
            
            if (root.left == null) 
                return root.right; 
            else if (root.right == null) 
                return root.left; 
   
           
            root =(lowestCost(root.right)); 
   
            
            root.right = deleteNNRecursive(root.right, root); 
        } 
        return root; 
    } 
   
    public NetworkNode lowestCost(NetworkNode root)  { 
        
        NetworkNode minval = root; 
        //find minval
        while (root.left != null)  { 
            minval = root.left; 
            root = root.left; 
        } 
        
        return minval; 
    } 
   
    // insert a node in CostSortedQueue 
    public void insert(Pair<Node,Node> nodes,Edge key)  
    { 
        
        root = insert_Recursive(root,nodes, key); 
        
        
    } 
   
    //recursive insert function
    NetworkNode insert_Recursive(NetworkNode root, Pair<Node,Node> nodes, Edge key) { 
          //tree is empty
        if (root == null) { 
            root = new NetworkNode(nodes,key); 
            return root; 
        } 
        //traverse the tree
        
        if (key.cost < root.key) 
        {
            root.left = insert_Recursive(root.left,nodes, key);
        } 
        else if (key.cost >= root.key) 
        {
            root.right = insert_Recursive(root.right,nodes, key);
        } 
          
        
        return root; 
    } 
 
// method for inorder traversal of CostSortedQueue 
    public ArrayList<Edge> getList()
    {
        return c;
    }
    void inorder()
    {
        inorder_Recursive(root,c);        
    } 
    
   
    // recursively traverse the CostSortedQueue  
    private void inorder_Recursive(NetworkNode root, ArrayList c) 
    {
        
        if (root != null) { 
            inorder_Recursive(root.left,c); 
            System.out.printf("Vertices:%s, %s | Edge: %s\n",
                    root.edgeVertices.getKey().getId(),root.edgeVertices.getValue().getId(),root.e.getId());
            
            size++;
            inorder_Recursive(root.right,c); 
        } 
        
    } 
     
    public boolean search(double key)  { 
        root = search_Recursive(root, key); 
        if (root!= null)
            return true;
        else
            return false;
    } 
   
    //recursive insert function
    private NetworkNode search_Recursive(NetworkNode root, double key)  { 
        // Base Cases: root is null or key is present at root 
        if (root==null || root.key==key) 
            return root; 
        // val is greater than root's key 
        if (root.key > key) 
            return search_Recursive(root.left, key); 
        // val is less than root's key 
        return search_Recursive(root.right, key); 
    } 
}