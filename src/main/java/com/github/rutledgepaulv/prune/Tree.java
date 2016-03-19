package com.github.rutledgepaulv.prune;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Objects.requireNonNull;

/**
 * Simply wraps a node of a tree with some additional methods that act on the
 * tree created by the node in focus and that node's children.
 */
public final class Tree<T> {

    private final Node<T> root;

    /**
     * Constructs a new tree that is rooted at the provided node.
     *
     * @param root The root node.
     */
    private Tree(Node<T> root) {
        this.root = requireNonNull(root);
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
     * Used to trim off any nodes from the tree based on their data.
     * If a node that has children is trimmed, then all of it's children
     * are nixed as well.
     *
     * @param predicate The predicate to identify nodes that should be removed.
     */
    public final void pruneDescendants(Predicate<T> predicate) {
        depthFirstStreamNodes().filter(node -> !node.equals(root) && predicate.test(node.getData()))
                               .forEachOrdered(Node::prune);
    }

    /**
     * Used to trim off any nodes from the tree. If a node that has
     * children is trimmed, then all of it's children are nixed as
     * well.
     *
     * @param predicate The predicate to identify nodes that should be removed.
     */
    public final void pruneDescendantsAsNodes(Predicate<Node<T>> predicate) {
        depthFirstStreamNodes().filter(node -> !node.equals(root) && predicate.test(node))
                               .forEachOrdered(Node::prune);
    }

    /**
     * Searches the data stored by each node and returns the first data object that matches, if any.
     * Performs a depth first search against the tree.
     *
     * @param predicate The predicate to test against each piece of data.
     *
     * @return The first data object that satisfies the provided predicate. Empty optional if none matched.
     */
    public final Optional<T> depthFirstSearch(Predicate<T> predicate) {
        return depthFirstStream().filter(predicate).findFirst();
    }

    /**
     * Searches the data stored by each node and returns the first data object that matches, if any.
     * Performs a breadth first search against the tree.
     *
     * @param predicate The predicate to test against each piece of data.
     *
     * @return The first data object that satisfies the provided predicate. Empty optional if none matched.
     */
    public final Optional<T> breadthFirstSearch(Predicate<T> predicate) {
        return breadthFirstStream().filter(predicate).findFirst();
    }

    /**
     * Searches the tree nodes and returns the first node that matches, if any.
     * Performs a depth first search against the tree.
     *
     * @param predicate The predicate to test against each node.
     *
     * @return The first node that satisfies the provided predicate. Empty optional if none matched.
     */
    public final Optional<Node<T>> depthFirstSearchNodes(Predicate<Node<T>> predicate) {
        return depthFirstStreamNodes().filter(predicate).findFirst();
    }

    /**
     * Searches the tree nodes and returns the first node that matches, if any.
     * Performs a breadth first search against the tree.
     *
     * @param predicate The predicate to test against each node.
     *
     * @return The first node that satisfies the provided predicate. Empty optional if none matched.
     */
    public final Optional<Node<T>> breadthFirstSearchNodes(Predicate<Node<T>> predicate) {
       return breadthFirstStreamNodes().filter(predicate).findFirst();
    }

    /**
     * Visits the data objects in the tree in a depth first manner.
     * Returning true from the visitor means it should progress on
     * to the next node. Returning false means to stop visiting any
     * additional nodes.
     *
     * @param visitor The visiting function.
     */
    public final void depthFirstVisit(Function<T, Boolean> visitor) {
        depthFirstStreamNodes().map(Node::getData).allMatch(visitor::apply);
    }

    /**
     * Visits the data objects in the tree in a breadth first manner.
     * Returning true from the visitor means it should progress on
     * to the next node. Returning false means to stop visiting any
     * additional nodes.
     *
     * @param visitor The visiting function.
     */
    public final void breadthFirstVisit(Function<T, Boolean> visitor) {
        breadthFirstStreamNodes().map(Node::getData).allMatch(visitor::apply);
    }

    /**
     * Visits the tree nodes in the tree in a depth first manner.
     * Returning true from the visitor means it should progress on
     * to the next node. Returning false means to stop visiting any
     * additional nodes.
     *
     * @param visitor The visiting function.
     */
    public final void depthFirstVisitNodes(Function<Node<T>, Boolean> visitor) {
        // relying on short circuiting to abort execution when it returns false
        depthFirstStreamNodes().allMatch(visitor::apply);
    }

    /**
     * Visits the tree nodes in the tree in a breadth first manner.
     * Returning true from the visitor means it should progress on
     * to the next node. Returning false means to stop visiting any
     * additional nodes.
     *
     * @param visitor The visiting function.
     */
    public final void breadthFirstVisitNodes(Function<Node<T>, Boolean> visitor) {
        // relying on short circuiting to abort execution when it returns false
        breadthFirstStreamNodes().allMatch(visitor::apply);
    }

    /**
     * Returns a stream of the nodes starting with the current root and traversing
     * the tree in a depth first order.
     *
     * @return The stream of nodes.
     */
    public final Stream<T> depthFirstStream() {
        return depthFirstStreamNodes().map(Node::getData);
    }

    /**
     * Returns a stream of the nodes starting with the current root and traversing
     * the tree in a breadth first order.
     *
     * @return The stream of nodes.
     */
    public final Stream<T> breadthFirstStream() {
        return breadthFirstStreamNodes().map(Node::getData);
    }

    /**
     * Returns a stream of the nodes starting with the current root and traversing
     * the tree in a depth first order.
     *
     * @return The stream of nodes.
     */
    public final Stream<Node<T>> depthFirstStreamNodes() {
        return stream(depthFirstIter());
    }

    /**
     * Returns a stream of the nodes starting with the current root and traversing
     * the tree in a breadth first order.
     *
     * @return The stream of nodes.
     */
    public final Stream<Node<T>> breadthFirstStreamNodes() {
        return stream(breadthFirstIter());
    }




    private Iterator<Node<T>> depthFirstIter() {
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
                queue.addAll(0, node.getChildren());
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

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Tree)) {
            return false;
        }
        Tree<?> tree = (Tree<?>) o;
        return Objects.equals(root, tree.root);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(root);
    }


    /**
     * Represents one node of a tree. A node can either be a leaf or a joint
     * between two branches.
     *
     * @param <T> The type of data maintained by this node and its children.
     */
    public static final class Node<T> {

        private T data;
        private Node<T> parent;
        private List<Node<T>> children = new LinkedList<>();

        public Node(T data) {
            this.data = data;
        }

        public final T getData() {
            return data;
        }

        public final List<Node<T>> getChildren() {
            return Collections.unmodifiableList(children);
        }

        public final Node<T> addChild(T data) {
            Node<T> child = new Node<>(data);
            children.add(child);
            child.parent = this;
            return child;
        }

        public final Node<T> addChildNode(Node<T> child) {
            children.add(child);
            child.parent = this;
            return child;
        }

        @SafeVarargs
        public final void addChildren(T... data) {
            Arrays.stream(data).map((Function<T, Node<T>>) Node::new)
                  .map(child -> {
                      child.parent = this;
                      return child;
                  }).forEachOrdered(children::add);
        }

        @SafeVarargs
        public final void addChildrenNodes(Node<T>... data) {
            Arrays.stream(data).map(child -> {
                child.parent = this;
                return child;
            }).forEachOrdered(children::add);
        }

        public final void addChildren(Collection<T> data) {
            data.stream().map((Function<T, Node<T>>) Node::new)
                .map(child -> {
                    child.parent = this;
                    return child;
                }).forEachOrdered(children::add);
        }

        public final void addChildrenNodes(Collection<Node<T>> data) {
            data.stream().map(child -> {
                child.parent = this;
                return child;
            }).forEachOrdered(children::add);
        }

        public final Tree<T> asTree() {
            return new Tree<>(this);
        }

        private void prune() {
            if(parent != null) {
                parent.children.remove(this);
            }
        }

        @Override
        public final boolean equals(Object o) {
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
        public final int hashCode() {
            return Objects.hash(data, children);
        }

    }
}
