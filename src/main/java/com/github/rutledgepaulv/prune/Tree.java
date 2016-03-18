package com.github.rutledgepaulv.prune;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Simply wraps a node of a tree with some additional methods that act on the
 * tree created by the node in focus and that node's children.
 */
public class Tree<T> {

    private Node<T> root;

    /**
     * Constructs a new tree that is rooted at the provided node.
     *
     * @param root The root node.
     */
    Tree(T root) {
        this(new Node<>(root));
    }

    /**
     * Constructs a new tree that is rooted at the provided node.
     *
     * @param root The root node.
     */
    Tree(Node<T> root) {
        Objects.requireNonNull(root);
        this.root = root;
    }

    /**
     * Returns the root node so you can operate on it as a node rather
     * than as a tree.
     *
     * @return The root node.
     */
    public final Node<T> asNode() {
        return this.root;
    }

    /**
     * Searches the data stored by each node and returns the first data object that matches, if any.
     * Performs a depth first search against the tree.
     *
     * @param predicate The predicate to test against each piece of data.
     *
     * @return The first data object that satisfies the provided predicate. Empty optional if none matched.
     */
    public final Optional<T> depthFirstSearchData(Predicate<T> predicate) {
        return depthFirstDataStream().filter(predicate).findFirst();
    }

    /**
     * Searches the data stored by each node and returns the first data object that matches, if any.
     * Performs a breadth first search against the tree.
     *
     * @param predicate The predicate to test against each piece of data.
     *
     * @return The first data object that satisfies the provided predicate. Empty optional if none matched.
     */
    public final Optional<T> breadthFirstSearchData(Predicate<T> predicate) {
        return breadthFirstDataStream().filter(predicate).findFirst();
    }

    /**
     * Searches the tree nodes and returns the first node that matches, if any.
     * Performs a depth first search against the tree.
     *
     * @param predicate The predicate to test against each node.
     *
     * @return The first node that satisfies the provided predicate. Empty optional if none matched.
     */
    public final Optional<Node<T>> depthFirstSearchNode(Predicate<Node<T>> predicate) {
        return depthFirstStream().filter(predicate).findFirst();
    }

    /**
     * Searches the tree nodes and returns the first node that matches, if any.
     * Performs a breadth first search against the tree.
     *
     * @param predicate The predicate to test against each node.
     *
     * @return The first node that satisfies the provided predicate. Empty optional if none matched.
     */
    public final Optional<Node<T>> breadthFirstSearchNode(Predicate<Node<T>> predicate) {
       return breadthFirstStream().filter(predicate).findFirst();
    }

    /**
     * Visits the data objects in the tree in a depth first manner.
     * Returning true from the visitor means it should progress on
     * to the next node. Returning false means to stop visiting any
     * additional nodes.
     *
     * @param visitor The visiting function.
     */
    public final void visitDataDepthFirst(Function<T, Boolean> visitor) {
        visitNodesDepthFirst(node -> visitor.apply(node.getData()));
    }

    /**
     * Visits the data objects in the tree in a breadth first manner.
     * Returning true from the visitor means it should progress on
     * to the next node. Returning false means to stop visiting any
     * additional nodes.
     *
     * @param visitor The visiting function.
     */
    public final void visitDataBreadthFirst(Function<T, Boolean> visitor) {
        visitNodesBreadthFirst(node -> visitor.apply(node.getData()));
    }

    /**
     * Visits the tree nodes in the tree in a depth first manner.
     * Returning true from the visitor means it should progress on
     * to the next node. Returning false means to stop visiting any
     * additional nodes.
     *
     * @param visitor The visiting function.
     */
    public final void visitNodesDepthFirst(Function<Node<T>, Boolean> visitor) {
        depthFirstStream().allMatch(visitor::apply);
    }

    /**
     * Visits the tree nodes in the tree in a breadth first manner.
     * Returning true from the visitor means it should progress on
     * to the next node. Returning false means to stop visiting any
     * additional nodes.
     *
     * @param visitor The visiting function.
     */
    public final void visitNodesBreadthFirst(Function<Node<T>, Boolean> visitor) {
        breadthFirstStream().allMatch(visitor::apply);
    }

    /**
     * Returns a stream of the nodes starting with the current root and traversing
     * the tree in a depth first order.
     *
     * @return The stream of nodes.
     */
    public Stream<T> depthFirstDataStream() {
        return stream(depthFirstIter()).map(Node::getData);
    }

    /**
     * Returns a stream of the nodes starting with the current root and traversing
     * the tree in a breadth first order.
     *
     * @return The stream of nodes.
     */
    public Stream<T> breadthFirstDataStream() {
        return stream(breadthFirstIter()).map(Node::getData);
    }

    /**
     * Returns a stream of the nodes starting with the current root and traversing
     * the tree in a depth first order.
     *
     * @return The stream of nodes.
     */
    public Stream<Node<T>> depthFirstStream() {
        return stream(depthFirstIter());
    }

    /**
     * Returns a stream of the nodes starting with the current root and traversing
     * the tree in a breadth first order.
     *
     * @return The stream of nodes.
     */
    public Stream<Node<T>> breadthFirstStream() {
        return stream(breadthFirstIter());
    }




    private Iterator<Node<T>> depthFirstIter() {
        final Stack<Node<T>> stack = new Stack<>();
        stack.push(root);

        return new Iterator<Node<T>>() {
            @Override
            public boolean hasNext() {
                return !stack.isEmpty();
            }

            @Override
            public Node<T> next() {
                Node<T> node = stack.pop();
                node.getChildren().forEach(stack::push);
                return node;
            }
        };
    }

    private Iterator<Node<T>> breadthFirstIter() {
        final List<Node<T>> queue = new LinkedList<>();
        queue.add(root);

        return new Iterator<Node<T>>() {
            @Override
            public boolean hasNext() {
                return !queue.isEmpty();
            }

            @Override
            public Node<T> next() {
                Node<T> node = queue.remove(0);
                queue.addAll(node.getChildren());
                return node;
            }
        };
    }

    private static <S> Stream<S> stream(Iterator<S> iterator) {
        Iterable<S> iterable = () -> iterator;
        return StreamSupport.stream(iterable.spliterator(), false);
    }


    /**
     * Represents one node of a tree. A node can either be a leaf or a joint
     * between two branches.
     *
     * @param <T> The type of data maintained by this node and its children.
     */
    public static class Node<T> {

        private T data;
        private List<Node<T>> children = new LinkedList<>();

        public Node(T data) {
            this.data = data;
        }

        public T getData() {
            return data;
        }

        public List<Node<T>> getChildren() {
            return children;
        }

        public Node<T> addChild(T data) {
            Node<T> child = new Node<>(data);
            children.add(child);
            return child;
        }

        public Node<T> addChildNode(Node<T> child) {
            children.add(child);
            return child;
        }

        @SafeVarargs
        public final void addChildren(T... data) {
            Arrays.stream(data)
                  .map((Function<T, Node<T>>) Node::new)
                  .forEachOrdered(children::add);
        }

        public final void addChildrenNodes(Node<T>... data) {
            Arrays.stream(data).forEachOrdered(children::add);
        }

        public void addChildren(Collection<T> data) {
            data.stream()
                .map((Function<T, Node<T>>) Node::new)
                .forEachOrdered(children::add);
        }

        public void addChildrenNodes(Collection<Node<T>> data) {
            data.stream().forEachOrdered(children::add);
        }

        public Tree<T> asTree() {
            return new Tree<>(this);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Node)) {
                return false;
            }
            Node<?> treeNode = (Node<?>) o;
            return Objects.equals(data, treeNode.data) &&
                    Objects.equals(children, treeNode.children);
        }

        @Override
        public int hashCode() {
            return Objects.hash(data, children);
        }

    }
}
