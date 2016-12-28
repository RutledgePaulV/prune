package com.github.rutledgepaulv.prune;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;

/**
 * A generic tree structure that supports
 * various traversals, cloning, mapping, getting
 * the depth of a node, getting the global and local
 * order of a node, and more.
 *
 * @param <T>
 */
@SuppressWarnings({"unchecked", "unused", "StatementWithEmptyBody"})
public final class Tree<T> {

    /**
     * Creates a tree of one node with null value.
     *
     * @param <S> The type contained within the nodes.
     * @return The new tree.
     */
    public static <S> Tree<S> empty() {
        return of((S)null);
    }

    /**
     * Creates a tree of one node with the provided value.
     *
     * @param value The value contained by the root node.
     * @param <S> The type contained within the nodes.
     * @return The new tree.
     */
    public static <S> Tree<S> of(S value) {
        return new Node<>(value).asTree();
    }

    /**
     * Creates a tree of one node using the provided node
     * as that one node (the root).
     *
     * @param node The node to make the root.
     * @param <S> The type contained within the nodes.
     * @return The new tree.
     */
    public static <S> Tree<S> of(Node<S> node) {
        return node.asTree();
    }

    /**
     * Creates a tree by pairing up nodes with their children / parents according
     * to a predicate function that should return true when a parent - child
     * relationship is found. Multiple passes will be made so the matching function
     * can even base its criteria off of things like the number of other children
     * a parent or child has, etc. Once a pass through the remaining orphaned nodes
     * results in no changes then the attempts to build will stop and any remaining
     * orphaned nodes just won't be a part of the tree.
     *
     * @param collection The collection of values to aggregate into a tree format.
     *
     * @param parentChildMatcher The predicate that returns true only if the first
     *                           argument is known to be the direct parent of the
     *                           second argument.
     *
     * @param <S> The type contained by the nodes in the resulting trees.
     *
     * @return All of the parentless nodes after a changeless pass of the predicate turned into trees.
     */
    public static <S> List<Tree<S>> of(Collection<S> collection, BiPredicate<Node<S>,Node<S>> parentChildMatcher) {
        List<Node<S>> nodes = collection.stream().map(Node::new).collect(toList());
        while(runPassAndReportIfProgress(nodes, parentChildMatcher));
        return nodes.stream().filter(node -> !node.getParent().isPresent())
                .map(Node::asTree).collect(toList());
    }

    /**
     * Creates a tree by pairing up nodes with their children / parents according
     * to a predicate function that should return true when a parent - child
     * relationship is found. Multiple passes will be made so the matching function
     * can even base its criteria off of things like the number of other children
     * a parent or child has, etc. Once a pass through the remaining orphaned nodes
     * results in no changes then the attempts to build will stop and any remaining
     * orphaned nodes just won't be a part of the tree.
     *
     * @param root The anticipated root value. Only a tree at the first node found with
     *             that value will be returned after all the matching is done.
     *
     * @param tail The collection of values to aggregate into a tree format.
     *
     * @param parentChildMatcher The predicate that returns true only if the first
     *                           argument is known to be the direct parent of the
     *                           second argument.
     *
     * @param <S> The type contained by the nodes in the resulting trees.
     *
     * @return All of the parentless nodes after a changeless pass of the predicate turned into trees.
     */
    public static <S> Tree<S> of(Node<S> root, Collection<S> tail, BiPredicate<Node<S>,Node<S>> parentChildMatcher) {
        List<Node<S>> nodes = Stream.concat(Stream.of(root), tail.stream().map(Node::new)).collect(toList());
        while(runPassAndReportIfProgress(nodes, parentChildMatcher));
        return nodes.stream().filter(node -> node == root).findFirst().map(Node::asTree).orElseGet(root::asTree);
    }

    /**
     * A shorthand mechanism for creating tree nodes.
     *
     * e.g.
     *
     * Tree tree = node("Test", node("child1"), node("child2")).asTree()
     *
     * @param value The value to use for the node.
     * @param children The children to give to the node.
     * @param <S> The type of values the nodes contain.
     * @return The root node.
     */
    @SafeVarargs
    public static <S> Node<S> node(S value, Node<S>... children) {
        Node<S> result = new Node<>(value);
        Arrays.stream(children).forEachOrdered(result::addChildNode);
        return result;
    }

    private final Node<T> root;

    private Tree(Node<T> root, boolean cloneStructure) {
        this.root = cloneStructure ? root.map(identity()) : root;
        this.root.parent = cloneStructure ? null : root.parent;
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
     * Returns the count of all nodes in the tree.
     *
     * @return The cardinality of the tree.
     */
    public final long cardinality() {
        return depthFirstStream().count();
    }

    /**
     * Copies the tree by mapping each node's data into
     * a new node containing exactly the same data and
     * new nodes wrapping the corresponding children
     * and parents.
     *
     * @return A new tree made up of new nodes but containing
     *         the original data references.
     */
    public final Tree<T> shallowClone() {
        return deepClone(identity());
    }

    /**
     * Clones the tree by mapping each node's data into
     * a new node containing the cloned data according
     * to the cloning function.
     *
     *
     * @param dataCloningFunction The function to use to clone
     *                            each data entry. If this function
     *                            doesn't do a true deep clone, then
     *                            neither does this method.
     *
     * @return A new tree made up of new nodes but containing
     *         the original data references.
     */
    public final Tree<T> deepClone(Function<T, T> dataCloningFunction) {
        return new Tree<>(asNode().map(dataCloningFunction), false);
    }

    /**
     * Sorts a tree (in place) by sorting every node's children
     * if the node has children.
     *
     * @param sort A function to sort nodes at each depth according
     *             to the data attached to each node.
     *
     * @return The same tree, but now with sorted levels.
     */
    public final Tree<T> sort(Comparator<T> sort) {
        return sortAsNodes(Comparator.comparing(Node::getData, sort));
    }

    /**
     * Sorts a tree (in place) by sorting every node's children
     * if the node has children.
     *
     * @param sort A function to sort nodes at each depth..
     *
     * @return The same tree, but now with sorted levels.
     */
    public final Tree<T> sortAsNodes(Comparator<Node<T>> sort) {
        breadthFirstStreamNodes().filter(node -> !node.getChildren().isEmpty())
                .forEach(nodeWithChildren -> Collections.sort(nodeWithChildren.children, sort));
        return this;
    }

    /**
     * Transforms a tree by applying a map function to every node.
     *
     * @param func The function to apply.
     * @param <S> The type of data for each resulting node.
     * @return The tree with new nodes.
     */
    public final <S> Tree<S> map(Function<T,S> func) {
        return new Tree<>(root.map(func), false);
    }

    /**
     * Transforms a tree by applying a flatMap function to every node.
     *
     * @param func The function to apply.
     * @param <S> The type of data for each resulting node.
     * @return The tree of new nodes.
     */
    public final <S> Tree<S> flatMap(Function<T,Node<S>> func) {
        return new Tree<>(root.flatMap(func), false);
    }

    /**
     * Transforms a tree by applying a map function to every node.
     *
     * @param func The function to apply.
     * @param <S> The type of data for each resulting node.
     * @return The tree with new nodes.
     */
    public final <S> Tree<S> mapAsNodes(Function<Node<T>,S> func) {
        return new Tree<>(root.mapAsNode(func), false);
    }

    /**
     * Transforms a tree by applying a flatMap function to every node.
     *
     * @param func The function to apply.
     * @param <S> The type of data for each resulting node.
     * @return The tree of new nodes.
     */
    public final <S> Tree<S> flatMapAsNodes(Function<Node<T>,Node<S>> func) {
        return new Tree<>(root.flatMapAsNode(func), false);
    }

    /**
     * Get a particular depth within the tree.
     *
     * @param depth The depth to get.
     * @return A stream of the data that exists at that depth within the tree.
     */
    public final Stream<T> getDepth(int depth) {
        return getDepthAsNodes(depth).map(Node::getData);
    }

    /**
     * Get a particular depth within the tree.
     *
     * @param depth The depth to get.
     * @return A stream of the nodes that exists at that depth within the tree.
     */
    public final Stream<Node<T>> getDepthAsNodes(int depth) {
        return depthFirstStreamNodes().filter(node -> node.getDepth() == depth);
    }

    /**
     * Used to filter out nodes from the tree based on their data.
     * If a node that has children is trimmed, then all of it's children
     * are moved to be children of the node's parent.
     *
     * @param predicate The predicate to identify nodes that should be removed.
     */
    public final Tree<T> filter(Predicate<T> predicate) {
        return filterAsNodes(n -> predicate.test(n.getData()));
    }

    /**
     * Used to filter out nodes from the tree based on their data.
     * If a node that has children is trimmed, then all of it's children
     * are moved to be children of the node's parent.
     *
     * @param predicate The predicate to identify nodes that should be removed.
     */
    public final Tree<T> filterAsNodes(Predicate<Node<T>> predicate) {
        depthFirstStreamNodes()
                .filter(node -> node != root && predicate.test(node))
                .forEachOrdered(Node::filter);
        return this;
    }

    /**
     * Used to trim off any nodes from the tree based on their data.
     * If a node that has children is trimmed, then all of it's children
     * are nixed as well.
     *
     * @param predicate The predicate to identify nodes that should be removed.
     */
    public final Tree<T> prune(Predicate<T> predicate) {
        return pruneAsNodes(n -> predicate.test(n.getData()));
    }

    /**
     * Used to trim off any nodes from the tree. If a node that has
     * children is trimmed, then all of it's children are nixed as
     * well.
     *
     * @param predicate The predicate to identify nodes that should be removed.
     */
    public final Tree<T> pruneAsNodes(Predicate<Node<T>> predicate) {
        depthFirstStreamNodes()
                .filter(node -> node != root && predicate.test(node))
                .forEachOrdered(Node::prune);
        return this;
    }

    /**
     * Searches the data stored by each node and returns the first data object that matches, if any.
     * Performs a depth first search against the tree.
     *
     * @param predicate The predicate to test against each piece of data.
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
        depthFirstStream().allMatch(visitor::apply);
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
        breadthFirstStream().allMatch(visitor::apply);
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
        return fromIterator(depthFirstIter());
    }

    /**
     * Returns a stream of the nodes starting with the current root and traversing
     * the tree in a breadth first order.
     *
     * @return The stream of nodes.
     */
    public final Stream<Node<T>> breadthFirstStreamNodes() {
        return fromIterator(breadthFirstIter());
    }

    /**
     * Gets all the leaf node values of the tree. A leaf node is a node with no children.
     * Since they have no children, they must exist at the fringe/frontier of the graph.
     *
     * @return A stream of the leaves.
     */
    public final Stream<T> getLeaves() {
        return getLeavesAsNodes().map(Node::getData);
    }

    /**
     * Gets all the leaf nodes of the tree. A leaf node is a node with no children.
     * Since they have no children, they must exist at the fringe/frontier of the graph.
     *
     * @return A stream of the leaves.
     */
    public final Stream<Node<T>> getLeavesAsNodes() {
        return depthFirstStreamNodes().filter(node -> node.getChildren().isEmpty());
    }

    /**
     * Gets all of the unique "strands" from the root of the tree down to the leaves.
     * This represents all of the forward walks towards the leaves from the root.
     *
     * @return The strands
     */
    public final Stream<Stream<T>> getStrands() {
        return getStrandsAsNodes().map(items -> items.map(Node::getData));
    }

    /**
     * Gets all of the unique "strands" from the root of the tree down to the leaves.
     * This represents all of the forward walks towards the leaves from the root.
     *
     * @return The strands as the nodes involved in the walks
     */
    public final Stream<Stream<Node<T>>> getStrandsAsNodes() {
        return getLeavesAsNodes().map(node ->
                Stream.concat(node.getAncestry(), Stream.of(node)));
    }

    /**
     * Get the maximum depth that occurs in this tree.
     *
     * @return The max depth.
     */
    public final int getMaxDepth() {
        return depthFirstStreamNodes().mapToInt(Node::getDepth).max().orElse(0);
    }

    /**
     * Get the maximum global order that occurs in this tree.
     *
     * @return The max global order.
     */
    public final int getMaxGlobalOrder() {
        return depthFirstStreamNodes().mapToInt(Node::getGlobalOrder).max().orElse(0);
    }

    /**
     * Get the maximum local order that occurs in this tree.
     *
     * @return The max local order.
     */
    public final int getMaxLocalOrder() {
        return depthFirstStreamNodes().mapToInt(Node::getLocalOrder).max().orElse(0);
    }

    /**
     * Collects the tree into a map format where the keys are the various depths that
     * existed in the tree and the values are all the node values from the nodes at that
     * depth.
     *
     * @return The map of depth to node values at that depth.
     */
    public final Map<Integer, List<T>> byDepth() {
        return byDepthAsNodes().entrySet().stream().collect(toMap(Map.Entry::getKey,
                e -> e.getValue().stream().map(Node::getData).collect(toList())));
    }

    /**
     * Collects the tree into a map format where the keys are the local orders that exist
     * in the map. A local order is an index of a child node amongst its siblings. The values
     * are all the node values in the map whose nodes share that same local order.
     *
     * @return The map of local order to node values at that order.
     */
    public final Map<Integer, List<T>> byLocalOrder() {
        return byLocalOrderAsNodes().entrySet().stream().collect(toMap(Map.Entry::getKey,
                e -> e.getValue().stream().map(Node::getData).collect(toList())));
    }

    /**
     * Collects the tree into a map format where the keys are the global orders that exist
     * in the map. A global order is an index of a node amongst all other nodes at the same
     * depth. The values are all the node values in the map whose nodes share that same global order.
     *
     * @return The map of global order to node values at that order.
     */
    public final Map<Integer, List<T>> byGlobalOrder() {
        return byGlobalOrderAsNodes().entrySet().stream().collect(toMap(Map.Entry::getKey,
                e -> e.getValue().stream().map(Node::getData).collect(toList())));
    }

    /**
     * Collects the tree into a map format where the keys are the various depths that
     * existed in the tree and the values are all the nodes from the nodes at that
     * depth.
     *
     * @return The map of depth to node values at that depth.
     */
    public final Map<Integer, List<Node<T>>> byDepthAsNodes() {
        return breadthFirstStreamNodes().collect(groupingBy(Node::getDepth));
    }

    /**
     * Collects the tree into a map format where the keys are the local orders that exist
     * in the map. A local order is an index of a child node amongst its siblings. The values
     * are all the nodes in the map whom share that same local order.
     *
     * @return The map of local order to nodes at that order.
     */
    public final Map<Integer, List<Node<T>>> byLocalOrderAsNodes() {
        return breadthFirstStreamNodes().collect(groupingBy(Node::getLocalOrder));
    }

    /**
     * Collects the tree into a map format where the keys are the global orders that exist
     * in the map. A global order is an index of a node amongst all other nodes at the same
     * depth. The values are all the nodes in the map whom share that same global order.
     *
     * @return The map of global order to nodes at that order.
     */
    public final Map<Integer, List<Node<T>>> byGlobalOrderAsNodes() {
        return breadthFirstStreamNodes().collect(groupingBy(node ->
                indexOfByRef(getDepthAsNodes(node.getDepth()).collect(toList()), node)));
    }

    /**
     * Returns a new iterator through the tree nodes that iterates through
     * the nodes in a depth first traversal.
     *
     * @return The iterator.
     */
    public Iterator<Node<T>> depthFirstIter() {
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

    /**
     * Returns a new iterator through the tree nodes that iterates through
     * the nodes in a breadth first traversal.
     *
     * @return The iterator.
     */
    public Iterator<Node<T>> breadthFirstIter() {
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


    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Tree)) {
            return false;
        }

        Tree<T> tree = (Tree<T>) o;

        return recursiveTreeNodeEquals(root, tree.root);
    }

    @Override
    public final int hashCode() {
        return recursiveTreeNodeHashCode(root);
    }

    @Override
    public final String toString() {
        final StringBuilder buffer = new StringBuilder();
        recursiveTreeNodeToString(this.root, buffer, new LinkedList<>());
        return buffer.toString();
    }

    /**
     * Represents one node of a tree. A node can either be a leaf or a joint
     * between nodes.
     *
     * @param <T> The type of data maintained by this node and its children.
     */
    public static final class Node<T> {

        private T data;
        private Node<T> parent;
        private final List<Node<T>> children = new LinkedList<>();

        private Node(T data) {
            this.data = data;
        }

        /**
         * Gets the data associated with a node.
         *
         * @return The node's data.
         */
        public final T getData() {
            return data;
        }

        /**
         * Set the data associated with a node.
         *
         * @param data The data to set onto the node.
         */
        public final void setData(T data) {
            this.data = data;
        }

        /**
         * Get an optional of this node's parent, if any.
         *
         * @return An optional containing the parent node of this node, if any.
         */
        public final Optional<Node<T>> getParent() {
            return Optional.ofNullable(parent);
        }

        /**
         * Get an unmodifiable list of this nodes children.
         * If you need to add children, use the "addChildren" methods.
         *
         * @return The list of child nodes.
         */
        public final List<Node<T>> getChildren() {
            return Collections.unmodifiableList(children);
        }

        /**
         * Get all the siblings of this node.
         *
         * @return The list of sibling nodes.
         */
        public final List<Node<T>> getSiblings() {
            return getParent().map(parent -> parent.getChildren().stream()
                    .filter(node -> node != this))
                    .orElse(Stream.empty()).collect(toList());
        }

        public final Node<T> addChild(T data) {
            Node<T> child = new Node<>(data);
            children.add(child);
            child.parent = this;
            return this;
        }

        public final Node<T> addChildNode(Node<T> child) {
            children.add(child);
            child.parent = this;
            return this;
        }

        @SafeVarargs
        public final Node<T> addChildren(T... data) {
            Arrays.stream(data).map((Function<T, Node<T>>) Node::new).map(child -> {
                child.parent = this;
                return child;
            }).forEachOrdered(children::add);
            return this;
        }

        @SafeVarargs
        public final Node<T> addChildrenNodes(Node<T>... data) {
            Arrays.stream(data).map(child -> {
                child.parent = this;
                return child;
            }).forEachOrdered(children::add);
            return this;
        }

        public final Node<T> addChildren(Collection<T> data) {
            data.stream().map((Function<T, Node<T>>) Node::new).map(child -> {
                child.parent = this;
                return child;
            }).forEachOrdered(children::add);
            return this;
        }

        public final Node<T> addChildrenNodes(Collection<Node<T>> data) {
            data.stream().map(child -> {
                child.parent = this;
                return child;
            }).forEachOrdered(children::add);
            return this;
        }

        /**
         * Gets a new tree with this acting as the root.
         * Note that the root node of the new tree will
         * be parentless even if this node has a parent.
         * This is important for the recursive operations
         * for determining depth and order. This node instance
         * and all of its children will go unmodified because
         * an entire structural copy is made.
         *
         * @return A tree view of a structural clone. Any
         *         structural changes made on the new tree
         *         will not impact the structure to which this
         *         node currently belongs. Any changes to a nodes
         *         data *can* impact the data contained in this instances
         *         nodes. If you need a complete (deep) clone, then please
         *         call the deep clone function on the new tree along with
         *         a function describing how to clone each piece of data in
         *         the tree.
         */
        public final Tree<T> asTree() {
            return new Tree<>(this, true);
        }

        /**
         * Gets the root node of the current structure by traversing up
         * the chain of parents. If a node has no parent, then that node
         * will be returned.
         *
         * @return The root node of the current tree structure of which
         *         this node is a part.
         */
        public final Node<T> getRootOfTree() {
            return getParent().map(Node::getRootOfTree).orElse(this);
        }

        /**
         * Gets the depth within the tree at which this node lives.
         * Depth is zero indexed, meaning that the depth of the root
         * node is 0.
         *
         * @return The depth.
         */
        public final int getDepth() {
            return getParent().map(parent -> parent.getDepth() + 1).orElse(0);
        }

        /**
         * Gets the degree of this node (the number of outbound edges).
         *
         * @return The number of outbound edges (eq to the number of children).
         */
        public final int getDegree() {
            return getChildren().size();
        }

        /**
         * Gets the global order within the tree at which this node lives.
         * Global order is determined by how far (from left to right) this
         * node lives amongst other nodes at the same depth. Order is zero
         * indexed meaning that left-most nodes for any depth have a global
         * order of 0.
         *
         * @return The global order.
         */
        public final int getGlobalOrder() {
            return indexOfByRef(new Tree<>(getRootOfTree(), false)
                    .getDepthAsNodes(getDepth())
                    .collect(toList()), this);
        }

        /**
         * Gets the local order within the tree at which this node lives.
         * Local order is determined by how far (from left to right) this
         * node lives amongst its siblings (underneath its parent). Order is zero
         * indexed meaning that left-most nodes under a parent have a local
         * index of 0.
         *
         * @return The local order.
         */
        public final int getLocalOrder() {
            return getParent().map(Node::getChildren)
                    .map(list -> indexOfByRef(list, this))
                    .orElse(0);
        }

        /**
         * Filters this node out of the tree context in which it is in.
         * filtering out a node means transferring all of its children
         * to be siblings of itself and then removing itself from its
         * parent.
         */
        private void filter() {
            getParent().ifPresent(parent -> {
                parent.addChildrenNodes(this.getChildren());
                parent.children.remove(this);
            });
        }

        /**
         * Prunes this node out of the tree context in which it is in.
         * Pruning out a node means removing it and, by extension, all
         * of its children.
         */
        private void prune() {
            getParent().ifPresent(parent -> parent.children.remove(this));
        }

        private <S> Node<S> map(Function<T, S> func) {
            return mapAsNode(n -> func.apply(n.getData()));
        }

        private <S> Node<S> flatMap(Function<T, Node<S>> func) {
            return flatMapAsNode(n -> func.apply(n.getData()));
        }

        private <S> Node<S> mapAsNode(Function<Node<T>, S> func) {
            Node<S> node = new Node<>(func.apply(this));
            children.stream().map(child -> child.mapAsNode(func))
                    .forEachOrdered(node::addChildNode);
            return node;
        }

        private <S> Node<S> flatMapAsNode(Function<Node<T>, Node<S>> func) {
            Node<S> node = func.apply(this);
            children.stream().map(child -> child.flatMapAsNode(func))
                    .forEachOrdered(node::addChildNode);
            return node;
        }

        private Stream<Node<T>> getAncestry() {
            return getParent().map(parent -> Stream.concat(parent.getAncestry(), Stream.of(parent)))
                    .orElse(Stream.empty());
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
            return Objects.equals(data, treeNode.data);
        }

        @Override
        public final int hashCode() {
            return Objects.hash(data);
        }

        @Override
        public final String toString() {
            return Objects.toString(data);
        }

    }

    // adapted from http://www.connorgarvey.com/blog/?p=82#codesyntax_1
    private static <S> void recursiveTreeNodeToString(Node<S> node, StringBuilder buffer,
            List<Iterator<Node<S>>> parentIterators) {
        if (!parentIterators.isEmpty()) {
            boolean amLast = !parentIterators.get(parentIterators.size() - 1).hasNext();
            buffer.append("\n");
            StringBuilder result = new StringBuilder();
            Iterator<Iterator<Node<S>>> it = parentIterators.iterator();
            while (it.hasNext()) {
                Iterator<Node<S>> anIt = it.next();
                if (anIt.hasNext() || (!it.hasNext() && amLast)) {
                    result.append("   |");
                } else {
                    result.append("    ");
                }
            }
            String lines = result.toString();
            buffer.append(lines);
            buffer.append("\n");
            buffer.append(lines);
            buffer.append("- ");
        }
        buffer.append(Objects.toString(node).replaceAll("[\\n\\r]+", "<newline>"));
        if (!node.children.isEmpty()) {
            Iterator<Node<S>> it = node.children.iterator();
            parentIterators.add(it);
            while (it.hasNext()) {
                recursiveTreeNodeToString(it.next(), buffer, parentIterators);
            }
            parentIterators.remove(it);
        }
    }

    private static <S> boolean recursiveTreeNodeEquals(Node<S> node1, Node<S> node2) {
        // check that the root nodes are equal
        return Objects.equals(node1, node2) &&

                // check that they have the same number of children, short circuiting protects
                // against a size mismatch on the children in the next step that compares piecewise
                Objects.equals(node1.children.size(), node2.children.size()) &&

                // verify that each of the children match up
                IntStream.range(0, node1.children.size()).allMatch(index ->

                        // compare each child node by checking equality of that node as a tree
                        recursiveTreeNodeEquals(node1.children.get(index), node2.children.get(index)));
    }


    private static <S> int recursiveTreeNodeHashCode(Node<S> node) {
        return Arrays.hashCode(IntStream.concat(IntStream.of(Objects.hash(node)),
                node.children.stream().mapToInt(Tree::recursiveTreeNodeHashCode)).toArray());
    }


    private static <S> boolean runPassAndReportIfProgress(Collection<Node<S>> nodes, BiPredicate<Node<S>,Node<S>> parentChildMatcher) {
        return nodes.stream().filter(node -> !node.getParent().isPresent()).anyMatch(node ->
                nodes.stream().filter(p -> p != node).filter(p -> parentChildMatcher.test(p, node))
                        .findFirst().map(p -> {p.addChildNode(node); return p;}).isPresent());
    }


    private static <T> Stream<T> fromIterator(Iterator<T> iterator) {
        Iterable<T> iterable = () -> iterator;
        return StreamSupport.stream(iterable.spliterator(), false);
    }

    private static <S> int indexOfByRef(List<S> list, S item) {
        for (int i = 0; i < list.size(); i++) {
            if(list.get(i) == item) {
                return i;
            }
        }
        return -1;
    }

}