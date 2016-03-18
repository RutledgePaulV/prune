package com.github.rutledgepaulv.prune.trees;

import com.github.rutledgepaulv.prune.Tree;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;

public class TreeTest {


    @Test
    public void breadthFirst() {

        Tree.Node<Integer> root = new Tree.Node<>(1);

        Tree.Node<Integer> child1 = new Tree.Node<>(2);
        Tree.Node<Integer> child2 = new Tree.Node<>(2);
        Tree.Node<Integer> child3 = new Tree.Node<>(2);

        root.addChildrenNodes(child1, child2, child3);

        child1.addChildren(3, 3);
        child2.addChildren(4, 4);
        child3.addChildren(5, 5);

        Tree<Integer> tree = root.asTree();

        final int[] timesExecuted = new int[]{0};

        Optional<Integer> optional = tree.breadthFirstSearchData(num -> {
            timesExecuted[0]++;
            return num.equals(4);
        });

        assertTrue(optional.isPresent());
        assertEquals(7, timesExecuted[0]);
    }


    @Test
    public void depthFirst() {


        Tree.Node<Integer> root = new Tree.Node<>(1);

        Tree.Node<Integer> child1 = new Tree.Node<>(2);
        Tree.Node<Integer> child2 = new Tree.Node<>(2);
        Tree.Node<Integer> child3 = new Tree.Node<>(2);

        root.addChildrenNodes(child1, child2, child3);

        child1.addChildren(3, 3);
        child2.addChildren(4, 4);
        child3.addChildren(5, 5);

        Tree<Integer> tree = root.asTree();

        final int[] timesExecuted = new int[]{0};

        Optional<Integer> optional = tree.depthFirstSearchData(num -> {
            timesExecuted[0]++;
            return num.equals(4);
        });

        assertTrue(optional.isPresent());
        assertEquals(6, timesExecuted[0]);
    }
}
