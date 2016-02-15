package com.github.rutledgepaulv.prune.trees;

import com.github.rutledgepaulv.prune.nodes.TreeNode;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TreeTest {

    @Test
    public void breadthFirst() {

        TreeNode<Integer> root = new TreeNode<>(1);

        TreeNode<Integer> child1 = new TreeNode<>(2);
        TreeNode<Integer> child2 = new TreeNode<>(2);
        TreeNode<Integer> child3 = new TreeNode<>(2);

        root.getChildren().add(child1);
        root.getChildren().add(child2);
        root.getChildren().add(child3);

        child1.getChildren().add(new TreeNode<>(3));
        child1.getChildren().add(new TreeNode<>(3));

        child2.getChildren().add(new TreeNode<>(4));
        child2.getChildren().add(new TreeNode<>(4));

        child3.getChildren().add(new TreeNode<>(5));
        child3.getChildren().add(new TreeNode<>(5));


        Tree<Integer> tree = new Tree<>(root);

        final int[] timesExecuted = new int[]{0};

        Optional<TreeNode<Integer>> optional = tree.breadthFirstSearch(num -> {
            timesExecuted[0]++;
            return num.equals(4);
        });


        assertTrue(optional.isPresent());
        assertEquals(7, timesExecuted[0]);
    }


    @Test
    public void depthFirst() {

        TreeNode<Integer> root = new TreeNode<>(1);

        TreeNode<Integer> child1 = new TreeNode<>(2);
        TreeNode<Integer> child2 = new TreeNode<>(2);
        TreeNode<Integer> child3 = new TreeNode<>(2);

        root.getChildren().add(child1);
        root.getChildren().add(child2);
        root.getChildren().add(child3);

        child1.getChildren().add(new TreeNode<>(3));
        child1.getChildren().add(new TreeNode<>(3));

        child2.getChildren().add(new TreeNode<>(4));
        child2.getChildren().add(new TreeNode<>(4));

        child3.getChildren().add(new TreeNode<>(5));
        child3.getChildren().add(new TreeNode<>(5));


        Tree<Integer> tree = new Tree<>(root);

        final int[] timesExecuted = new int[]{0};

        Optional<TreeNode<Integer>> optional = tree.depthFirstSearch(num -> {
            timesExecuted[0]++;
            return num.equals(4);
        });


        assertTrue(optional.isPresent());
        assertEquals(6, timesExecuted[0]);
    }
}
