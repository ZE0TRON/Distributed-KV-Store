package de.tum.i13.shared.BST;

import de.tum.i13.shared.Constants;

import java.util.ArrayList;
import java.util.Arrays;

public class BST {
    private RingNode root;

    public BST(RingNode root) {
        this.root = root;
    }

    public BST() {
        this.root = null;
    }

    public RingNode min(){
       RingNode ringNode = root;
       while (ringNode.left != null) {
           ringNode = ringNode.left;
       }
       return ringNode;
    }

    public RingNode max(){
        RingNode ringNode = root;
        while (ringNode.right != null) {
            ringNode = ringNode.right;
        }
        return ringNode;
    }

    public RingNode predecessor(RingNode ringNode) {
        if (ringNode.left != null) {
            return new BST(ringNode.left).max();
        }
        while (ringNode.parent != null && ringNode.parent.left == ringNode) {
            ringNode = ringNode.parent;
        }
        return ringNode.parent;
    }

    public RingNode successor(RingNode ringNode) {
        if (ringNode.right != null) {
            return new BST(ringNode.right).min();
        }
        while (ringNode.parent != null && ringNode.parent.right == ringNode) {
            ringNode = ringNode.parent;
        }
        return ringNode.parent;
    }

    public RingNode search(String key) {
        RingNode current = root;
        while(current != null) {
            if (current.key.compareTo(key) > 0 ) {
                current = current.left;
            } else if (current.key.compareTo(key) < 0) {
                current = current.right;
            } else {
                return current;
            }
        }
        return null;
    }

    public synchronized RingNode insert(RingNode node) {
        RingNode prev = null;
        RingNode current = root;

        if (root == null) {
            root = node;
            return node;
        }

        while (current != null) {
            prev = current;
            if (current.key.compareTo(node.key) >= 0 ) {
                current = current.left;
            } else {
               current = current.right;
            }
        }

        if (prev.key.compareTo(node.key) > 0) {
            prev.left = node;
        } else {
            prev.right = node;
        }
        node.parent = prev;
        return node;
    }

    private RingNode deleteRec(RingNode node, String key) {
        if (node == null)
            return null;

        /* Otherwise, recur down the tree */
        if (node.key.compareTo(key) > 0) {
            node.left = deleteRec(node.left, key);
            if (node.left != null) {
                node.left.parent = node;
            }
        }
        else if (node.key.compareTo(key) < 0) {
            node.right = deleteRec(node.right, key);
            if (node.right != null) {
                node.right.parent = node;
            }
        }
            // if key is same as root's
            // key, then This is the
            // node to be deleted
        else {
            // node with only one child or no child
            if (node.left == null)
                return node.right;
            else if (node.right == null)
                return node.left;

            // node with two children: Get the inorder
            // successor (smallest in the right subtree)
            RingNode successor = successor(node);
            if (node == null) {
                return null;
            }
            node.value = successor.value;
            node.key= successor.key;
            // Delete the inorder successor
            node.right = deleteRec(node.right, node.key);
            if (node.right != null) {
                node.right.parent = node;
            }
        }

        return node;
    }
    public synchronized void delete(RingNode ringNode) {
        root = deleteRec(root, ringNode.key);

    }

    private void serializeRec(RingNode node, StringBuilder sb) {
        if (node != null) {
            sb.append(node.serialize());
            sb.append(",");
            serializeRec(node.left, sb);
            serializeRec(node.right, sb);
        }
        else {
            sb.append("#");
            sb.append(",");
        }
    }

    // Encodes a tree to a single string.
    public String serialize() {
        StringBuilder sb = new StringBuilder();
        serializeRec(root, sb);
        sb.deleteCharAt(sb.length()-1);
        return sb.toString();
    }

    private static RingNode deserializeRec(ArrayList<String> treeData) {
        if(treeData.size() == 0) {
            return null;
        }
        String valStr = treeData.get(0);
        if(valStr.equals("#")) {
            treeData.remove(0);
            return null;
        }
        RingNode node = RingNode.deserialize(valStr);
        treeData.remove(0);
        node.left = deserializeRec(treeData);
        node.right = deserializeRec(treeData);
        return node;
    }

    // Decodes your encoded data to tree.

    public static BST deserialize(String data) {
        ArrayList<String> treeData = new ArrayList<>(Arrays.asList(data.split(",")));
        return new BST(deserializeRec(treeData));
    }

    public RingNode successorOfKey(String key) {
        RingNode current = search(key);
        return ringSuccessor(current);

    }

    public RingNode predecessorOfKey(String key) {
        RingNode current = search(key);
        return ringPredecessor(current);
    }

    private RingNode ringSuccessor(RingNode node) {
       RingNode successor = successor(node);
       return successor == null ? min() : successor;
    }

    private RingNode ringPredecessor(RingNode node) {
        RingNode predecessor = predecessor(node);
        return predecessor == null ? max() : predecessor;
    }

    public ArrayList<RingNode> dfs() {
        ArrayList<RingNode> nodes = new ArrayList<>();
        this.dfsInorder(this.root, nodes);
        return nodes;
    }

    private void dfsInorder(RingNode node,ArrayList<RingNode> nodes) {
        if(node == null) {
            return;
        }
        dfsInorder(node.left, nodes);
        nodes.add(node);
        dfsInorder(node.right, nodes);
    }

    public int nodeCount() {
        ArrayList<RingNode> nodes = dfs();
        return nodes.size();
    }

    public RingNode getRoot() {
        return this.root;
    }

    private RingNode replicaPredecessor(RingNode node) {
        RingNode predecessor = node;
        for (int i = 0; i <= Constants.REPLICA_COUNT; i++)  {
           predecessor = this.ringPredecessor(predecessor);
        }
        return predecessor;
    }

    public String serializeKeyRanges(boolean includeReplicas) {
        ArrayList<RingNode> nodes = dfs();
        StringBuilder sb = new StringBuilder();
        for (RingNode node: nodes) {
            RingNode predecessor = includeReplicas ? replicaPredecessor(node) : ringPredecessor(node);
            sb.append(predecessor.key);
            sb.append(",");
            sb.append(node.key);
            sb.append(",");
            sb.append(node.value.toHashableString());
            sb.append(";");
        }
        sb.deleteCharAt(sb.length() -1 );
        return sb.toString();
    }
}
