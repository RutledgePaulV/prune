package com.github.rutledgepaulv.prune.trees;

import com.github.rutledgepaulv.prune.nodes.TreeNode;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.function.Function;
import java.util.function.Predicate;

public class Tree<T> {

    private TreeNode<T> root;

    public Tree(TreeNode<T> root) {
        this.root = root;
    }

    public final Optional<TreeNode<T>> depthFirstSearch(Predicate<T> predicate) {
        final TreeNode[] result = new TreeNode[1];

        visitDepthFirst((node) -> {
            if (predicate.test(node.getData())) {
                result[0] = node;
                return false;
            } else {
                return true;
            }
        });

        return Optional.ofNullable(result[0]);
    }

    public final Optional<TreeNode<T>> breadthFirstSearch(Predicate<T> predicate) {
        final TreeNode[] result = new TreeNode[1];

        visitBreadthFirst((node) -> {
            if (predicate.test(node.getData())) {
                result[0] = node;
                return false;
            } else {
                return true;
            }
        });

        return Optional.ofNullable(result[0]);
    }


    public final void visitDepthFirst(Function<TreeNode<T>, Boolean> visitor) {
        Stack<TreeNode<T>> stack = new Stack<>();
        stack.push(root);
        visitDepthFirstInternal(visitor, stack);
    }

    public final void visitBreadthFirst(Function<TreeNode<T>, Boolean> visitor) {
        List<TreeNode<T>> queue = new LinkedList<>();
        queue.add(root);
        visitBreadthFirstInternal(visitor, queue);
    }

    private static <T> void visitDepthFirstInternal(Function<TreeNode<T>, Boolean> visitor, Stack<TreeNode<T>> stack) {
        if(stack.empty()){
            return;
        }

        TreeNode<T> node = stack.pop();
        if(visitor.apply(node)){
            node.getChildren().forEach(stack::push);
            visitDepthFirstInternal(visitor, stack);
        }
    }


    private static <T> void visitBreadthFirstInternal(Function<TreeNode<T>, Boolean> visitor, List<TreeNode<T>> queue) {
        if(queue.isEmpty()){
            return;
        }

        TreeNode<T> node = queue.remove(0);

        if(visitor.apply(node)){
            queue.addAll(node.getChildren());
            visitBreadthFirstInternal(visitor, queue);
        }
    }



}
