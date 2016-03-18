package com.github.rutledgepaulv.prune;

import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;

public class TreeTest {

    @Test
    public void readmeExample () {
        Tree.Node<Integer> root = new Tree.Node<>(1);

        Tree.Node<Integer> child1 = new Tree.Node<>(2);
        Tree.Node<Integer> child2 = new Tree.Node<>(6);
        Tree.Node<Integer> child3 = new Tree.Node<>(2);

        root.addChildrenNodes(child1, child2, child3);

        child1.addChildren(5, 5);
        child2.addChildren(4, 4);
        child3.addChildren(3, 3);

        Tree<Integer> tree = root.asTree();

        Optional<Integer> firstIntegerGreaterThan4DepthFirst = tree.depthFirstSearchData(val -> val > 4);
        Optional<Integer> firstIntegerGreaterThan4BreadthFirst = tree.breadthFirstSearchData(val -> val > 4);

        assertTrue(firstIntegerGreaterThan4DepthFirst.isPresent());
        assertTrue(firstIntegerGreaterThan4BreadthFirst.isPresent());

        assertEquals((Integer) 5, firstIntegerGreaterThan4DepthFirst.get());
        assertEquals((Integer) 6, firstIntegerGreaterThan4BreadthFirst.get());
    }


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
