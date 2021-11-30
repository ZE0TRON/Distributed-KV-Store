package de.tum.i13.ecs.BST;


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

    public RingNode insert(RingNode node) {
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

    public void delete(RingNode ringNode) {
        RingNode parent = ringNode.parent;
        if (parent == null) {
            return;
        }
        boolean leftChild = parent.left == ringNode;
        if(ringNode.left == null && ringNode.right == null) {
            if (leftChild) {
               parent.left = null;
            } else {
                parent.right = null;
            }
        } else if(ringNode.left == null) {
            if (leftChild) {
                parent.left = ringNode.right;
            } else {
                parent.right = ringNode.right;
            }
        } else if (ringNode.right == null) {
            if (leftChild) {
                parent.left = ringNode.left;
            } else {
                parent.right = ringNode.left;
            }
        } else {
            RingNode successor = successor(ringNode);
            if (successor.right != null) {
                successor.right.parent = successor.parent;
                // Successor is always a left child in this case because we will be returned the min of ringNode's subtree
                successor.parent.left = successor.right;
            }
            if (leftChild) {
                parent.left = successor;
            } else {
                parent.right = successor;
            }
            successor.parent = parent;
        }
        ringNode = null;
    }

    public RingNode successorOfKey(String key) {
        RingNode current = root;
        RingNode prev = root;
        while(current != null) {
            prev = current;
            if (current.key.compareTo(key) > 0 ) {
                current = current.left;

            }
            else if (current.key.compareTo(key) < 0) {
                current = current.right;
            }
            else {
                return current;
            }
        }
        if (prev == null) {
            return root;
        }
        if (prev.key.compareTo(key) < 0) {
            return ringSuccessor(prev);
        }
        return prev;
    }

    private RingNode ringSuccessor(RingNode node) {
       RingNode successor = successor(node);
       if (successor == null) {
           successor = min();
       }
       return successor;
    }


}
