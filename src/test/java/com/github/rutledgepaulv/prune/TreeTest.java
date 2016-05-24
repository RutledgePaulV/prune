package com.github.rutledgepaulv.prune;

import org.junit.Test;

import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.*;

public class TreeTest {

    @Test
    public void mapAgainstTree() {
        Tree.Node<String> root = new Tree.Node<>("root");
        Tree.Node<String> child1 = new Tree.Node<>("one");
        Tree.Node<String> child2 = new Tree.Node<>("two");
        Tree.Node<String> child3 = new Tree.Node<>("three");
        Tree.Node<String> subchild1 = new Tree.Node<>("fourteen");
        child1.addChildNode(subchild1);
        root.addChildrenNodes(child1, child2, child3);

        assertEquals("4\n" + "   |\n" + "   |- 3\n" + "   |   |\n" + "   |   |- 8\n" + "   |\n" + "   |- 3\n" +
                        "   |\n" + "   |- 5",
                root.asTree().map(String::length).toString());
    }

    @Test
    public void swapBetweenNodeAndTree() {

        Tree.Node<Integer> root = new Tree.Node<>(100);

        Tree<Integer> tree = root.asTree();

        Tree.Node<Integer> inverse = tree.asNode();

        assertEquals(root, inverse);

        Tree<Integer> treeInverse = inverse.asTree();

        assertEquals(tree, treeInverse);
    }


    @Test
    public void readmeExample() {
        Tree.Node<Integer> root = new Tree.Node<>(1);

        Tree.Node<Integer> child1 = new Tree.Node<>(2);
        Tree.Node<Integer> child2 = new Tree.Node<>(6);
        Tree.Node<Integer> child3 = new Tree.Node<>(2);

        root.addChildrenNodes(child1, child2, child3);

        child1.addChildren(5, 5);
        child2.addChildren(4, 4);
        child3.addChildren(3, 3);

        Tree<Integer> tree = root.asTree();

        Optional<Integer> firstIntegerGreaterThan4DepthFirst = tree.depthFirstSearch(val -> val > 4);
        Optional<Integer> firstIntegerGreaterThan4BreadthFirst = tree.breadthFirstSearch(val -> val > 4);

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

        Optional<Integer> optional = tree.breadthFirstSearch(num -> {
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

        Optional<Integer> optional = tree.depthFirstSearch(num -> {
            timesExecuted[0]++;
            return num.equals(4);
        });

        assertTrue(optional.isPresent());
        assertEquals(6, timesExecuted[0]);
    }


    @Test
    public void depthSearchFindNodeWithMoreThan3Children() {
        Tree.Node<Integer> root = new Tree.Node<>(1);

        Tree.Node<Integer> child1 = new Tree.Node<>(2);
        Tree.Node<Integer> child2 = new Tree.Node<>(2);

        root.addChildrenNodes(child1, child2);

        child1.addChildren(3, 3, 5, 6);
        child2.addChildren(4, 4, 5);

        Optional<Tree.Node<Integer>> optional = root.asTree()
                .depthFirstSearchNodes(node -> node.getChildren().size() > 3);

        assertTrue(optional.isPresent());
        assertEquals((Integer) 2, optional.map(Tree.Node::getData).get());
    }

    @Test
    public void breadthFirstFindNodeWithMoreThan3Children() {
        Tree.Node<Integer> root = new Tree.Node<>(1);

        Tree.Node<Integer> child1 = new Tree.Node<>(2);
        Tree.Node<Integer> child2 = new Tree.Node<>(2);

        root.addChildrenNodes(child1, child2);

        child1.addChildren(3, 3, 5, 6);
        child2.addChildren(4, 4, 5);

        Optional<Tree.Node<Integer>> optional = root.asTree()
                .breadthFirstSearchNodes(node -> node.getChildren().size() > 3);

        assertTrue(optional.isPresent());
        assertEquals((Integer) 2, optional.map(Tree.Node::getData).get());
    }

    @Test
    public void pruneDirectDescendants() {

        Tree.Node<Integer> root = new Tree.Node<>(1);
        root.addChildren(1, 2, 3, 4);

        assertEquals(4, root.getChildren().size());

        root.asTree().pruneDescendants(n -> n < 4);
        assertEquals(1, root.getChildren().size());
    }

    @Test
    public void pruneDescendantsByNodes() {

        Tree.Node<Integer> root = new Tree.Node<>(1);
        Tree.Node<Integer> child = new Tree.Node<>(5);

        root.addChildNode(child);
        child.addChildren(4, 3, 5);
        root.addChildren(1, 2, 3, 4);

        assertEquals(5, root.getChildren().size());
        root.asTree().pruneDescendantsAsNodes(node -> node.getChildren().size() == 3);

        assertEquals(4, root.getChildren().size());
        assertFalse(root.asTree().depthFirstStreamNodes().anyMatch(node -> node.equals(child)));
    }

    @Test
    public void pruneIndirectDescendants() {

        Tree.Node<Integer> root = new Tree.Node<>(1);
        Tree.Node<Integer> child = new Tree.Node<>(5);

        root.addChildNode(child);
        root.addChildren(4, 3, 5);
        child.addChildren(5, 6);
        child.addChildrenNodes(new Tree.Node<>(7));
        child.addChild(8);
        child.addChildren(Collections.singletonList(10));
        child.addChildrenNodes(Collections.singletonList(new Tree.Node<>(9)));

        assertEquals(4, root.getChildren().size());

        root.asTree().pruneDescendants(n -> n == 5);
        assertEquals(2, root.getChildren().size());

        assertFalse(root.asTree().depthFirstStream().anyMatch(val -> val == 5));
        assertFalse(root.asTree().depthFirstStream().anyMatch(val -> val == 6));
        assertFalse(root.asTree().depthFirstStream().anyMatch(val -> val == 7));
        assertFalse(root.asTree().depthFirstStream().anyMatch(val -> val == 8));
        assertFalse(root.asTree().depthFirstStream().anyMatch(val -> val == 9));
        assertFalse(root.asTree().depthFirstStream().anyMatch(val -> val == 10));

        assertFalse(root.asTree().depthFirstStreamNodes().anyMatch(node -> node.equals(child)));
        assertFalse(root.asTree().breadthFirstStreamNodes().anyMatch(node -> node.equals(child)));
    }


    @Test
    public void depthFirstVisit() {

        Tree.Node<Integer> root = new Tree.Node<>(1);

        Tree.Node<Integer> child1 = new Tree.Node<>(2);
        Tree.Node<Integer> child2 = new Tree.Node<>(6);
        Tree.Node<Integer> child3 = new Tree.Node<>(2);

        root.addChildrenNodes(child1, child2, child3);

        child1.addChildren(5, 5);
        child2.addChildren(4, 4);
        child3.addChildren(3, 3);

        Tree<Integer> tree = root.asTree();

        final int[] count = new int[]{0};
        final int[] expected = new int[]{1, 2, 5, 5, 6, 4, 4, 2, 3, 3};

        tree.depthFirstVisit(val -> {
            assertEquals((Integer) expected[count[0]], val);
            count[0] += 1;
            return true;
        });

        assertEquals(expected.length, count[0]);
    }


    @Test
    public void breadthFirstVisit() {

        Tree.Node<Integer> root = new Tree.Node<>(1);

        Tree.Node<Integer> child1 = new Tree.Node<>(2);
        Tree.Node<Integer> child2 = new Tree.Node<>(6);
        Tree.Node<Integer> child3 = new Tree.Node<>(2);

        root.addChildrenNodes(child1, child2, child3);

        child1.addChildren(5, 5);
        child2.addChildren(4, 4);
        child3.addChildren(3, 3);

        Tree<Integer> tree = root.asTree();

        final int[] count = new int[]{0};
        final int[] expected = new int[]{1, 2, 6, 2, 5, 5, 4, 4, 3, 3};

        tree.breadthFirstVisit(val -> {
            assertEquals((Integer) expected[count[0]], val);
            count[0] += 1;
            return true;
        });

        assertEquals(expected.length, count[0]);
    }


    @Test
    public void depthFirstVisitExitsEarly() {

        Tree.Node<Integer> root = new Tree.Node<>(1);

        Tree.Node<Integer> child1 = new Tree.Node<>(2);
        Tree.Node<Integer> child2 = new Tree.Node<>(6);
        Tree.Node<Integer> child3 = new Tree.Node<>(2);

        root.addChildrenNodes(child1, child2, child3);

        child1.addChildren(5, 5);
        child2.addChildren(4, 4);
        child3.addChildren(3, 3);

        Tree<Integer> tree = root.asTree();

        final int[] count = new int[]{0};
        final int[] expected = new int[]{1, 2, 5, 5};

        tree.depthFirstVisit(val -> {
            assertEquals((Integer) expected[count[0]], val);
            count[0] += 1;
            return count[0] != expected.length;
        });

        assertEquals(expected.length, count[0]);
    }


    @Test
    public void breadthFirstVisitExitsEarly() {

        Tree.Node<Integer> root = new Tree.Node<>(1);

        Tree.Node<Integer> child1 = new Tree.Node<>(2);
        Tree.Node<Integer> child2 = new Tree.Node<>(6);
        Tree.Node<Integer> child3 = new Tree.Node<>(2);

        root.addChildrenNodes(child1, child2, child3);

        child1.addChildren(5, 5);
        child2.addChildren(4, 4);
        child3.addChildren(3, 3);

        Tree<Integer> tree = root.asTree();

        final int[] count = new int[]{0};
        final int[] expected = new int[]{1, 2, 6, 2, 5, 5, 4, 4, 3, 3};

        tree.breadthFirstVisit(val -> {
            assertEquals((Integer) expected[count[0]], val);
            count[0] += 1;
            return count[0] != expected.length;
        });

        assertEquals(expected.length, count[0]);
    }


    @Test
    public void depthFirstVisitNodes() {

        Tree.Node<Integer> root = new Tree.Node<>(1);

        Tree.Node<Integer> child1 = new Tree.Node<>(2);
        Tree.Node<Integer> child2 = new Tree.Node<>(6);
        Tree.Node<Integer> child3 = new Tree.Node<>(2);

        root.addChildrenNodes(child1, child2, child3);

        child1.addChildren(5, 5);
        child2.addChildren(4, 4);
        child3.addChildren(3, 3);

        Tree<Integer> tree = root.asTree();

        final int[] count = new int[]{0};
        final int[] expected = new int[]{1, 2, 5, 5, 6, 4, 4, 2, 3, 3};

        tree.depthFirstVisitNodes(val -> {
            assertEquals((Integer) expected[count[0]], val.getData());
            count[0] += 1;
            return true;
        });

        assertEquals(expected.length, count[0]);
    }


    @Test
    public void breadthFirstVisitNodes() {

        Tree.Node<Integer> root = new Tree.Node<>(1);

        Tree.Node<Integer> child1 = new Tree.Node<>(2);
        Tree.Node<Integer> child2 = new Tree.Node<>(6);
        Tree.Node<Integer> child3 = new Tree.Node<>(2);

        root.addChildrenNodes(child1, child2, child3);

        child1.addChildren(5, 5);
        child2.addChildren(4, 4);
        child3.addChildren(3, 3);

        Tree<Integer> tree = root.asTree();

        final int[] count = new int[]{0};
        final int[] expected = new int[]{1, 2, 6, 2, 5, 5, 4, 4, 3, 3};

        tree.breadthFirstVisitNodes(val -> {
            assertEquals((Integer) expected[count[0]], val.getData());
            count[0] += 1;
            return true;
        });

        assertEquals(expected.length, count[0]);
    }


    @Test
    public void depthFirstVisitNodesExitsEarly() {

        Tree.Node<Integer> root = new Tree.Node<>(1);

        Tree.Node<Integer> child1 = new Tree.Node<>(2);
        Tree.Node<Integer> child2 = new Tree.Node<>(6);
        Tree.Node<Integer> child3 = new Tree.Node<>(2);

        root.addChildrenNodes(child1, child2, child3);

        child1.addChildren(5, 5);
        child2.addChildren(4, 4);
        child3.addChildren(3, 3);

        Tree<Integer> tree = root.asTree();

        final int[] count = new int[]{0};
        final int[] expected = new int[]{1, 2, 5, 5};

        tree.depthFirstVisitNodes(val -> {
            assertEquals((Integer) expected[count[0]], val.getData());
            count[0] += 1;
            return count[0] != expected.length;
        });

        assertEquals(expected.length, count[0]);
    }


    @Test
    public void breadthFirstVisitNodesExitsEarly() {

        Tree.Node<Integer> root = new Tree.Node<>(1);

        Tree.Node<Integer> child1 = new Tree.Node<>(2);
        Tree.Node<Integer> child2 = new Tree.Node<>(6);
        Tree.Node<Integer> child3 = new Tree.Node<>(2);

        root.addChildrenNodes(child1, child2, child3);

        child1.addChildren(5, 5);
        child2.addChildren(4, 4);
        child3.addChildren(3, 3);

        Tree<Integer> tree = root.asTree();

        final int[] count = new int[]{0};
        final int[] expected = new int[]{1, 2, 6, 2, 5, 5, 4, 4, 3, 3};

        tree.breadthFirstVisitNodes(val -> {
            assertEquals((Integer) expected[count[0]], val.getData());
            count[0] += 1;
            return count[0] != expected.length;
        });

        assertEquals(expected.length, count[0]);
    }


    @Test
    public void checkBasicEqualsAndHashCode() {
        Tree.Node<Integer> root = new Tree.Node<>(1);

        Tree.Node<Integer> one = new Tree.Node<>(2);
        Tree.Node<Integer> two = new Tree.Node<>(2);

        assertFalse(root.equals("test"));
        assertFalse(root.asTree().equals("test"));

        Tree<Integer> tree = root.asTree();

        assertTrue(tree.equals(tree));

        assertEquals(one.hashCode(), two.hashCode());
        assertEquals(one.asTree().hashCode(), two.asTree().hashCode());

        assertTrue(one.equals(one));
        assertTrue(one.asTree().equals(one.asTree()));
    }

    @Test
    public void checkComplexTreeEqualsAndHashCode() {
        Tree.Node<Integer> tree1_root = new Tree.Node<>(1);
        Tree.Node<Integer> tree1_depth1_a = new Tree.Node<>(2);
        Tree.Node<Integer> tree1_depth1_b = new Tree.Node<>(2);
        Tree.Node<Integer> tree1_depth2_a_a = new Tree.Node<>(5);
        Tree.Node<Integer> tree1_depth2_b_a = new Tree.Node<>(6);

        tree1_root.addChildrenNodes(tree1_depth1_a, tree1_depth1_b);
        tree1_depth1_a.addChildNode(tree1_depth2_a_a);
        tree1_depth1_b.addChildNode(tree1_depth2_b_a);

        Tree.Node<Integer> tree2_root = new Tree.Node<>(1);
        Tree.Node<Integer> tree2_depth1_a = new Tree.Node<>(2);
        Tree.Node<Integer> tree2_depth1_b = new Tree.Node<>(2);
        Tree.Node<Integer> tree2_depth2_a_a = new Tree.Node<>(5);
        Tree.Node<Integer> tree2_depth2_b_a = new Tree.Node<>(6);

        tree2_root.addChildrenNodes(tree2_depth1_a, tree2_depth1_b);
        tree2_depth1_a.addChildNode(tree2_depth2_a_a);
        tree2_depth1_b.addChildNode(tree2_depth2_b_a);

        assertEquals(tree1_root, tree1_root);
        assertEquals(tree2_root, tree2_root);
        assertEquals(tree1_root.hashCode(), tree2_root.hashCode());

        Tree<Integer> tree1 = tree1_root.asTree();
        Tree<Integer> tree2 = tree2_root.asTree();

        assertEquals(tree1, tree2);
        assertEquals(tree1.hashCode(), tree2.hashCode());

        tree2_depth2_b_a.addChild(5);

        assertNotEquals(tree1, tree2);
    }

    @Test
    public void testNodeToString() {
        Tree.Node<Integer> root = new Tree.Node<>(1);
        Tree.Node<Integer> child1 = new Tree.Node<>(2);
        root.addChildNode(child1);
        assertEquals("1", root.toString());
    }


    @Test
    public void testTreeToString() {
        Tree.Node<Integer> root = new Tree.Node<>(1);

        Tree.Node<Integer> child1 = new Tree.Node<>(2);
        Tree.Node<Integer> child2 = new Tree.Node<>(6);
        Tree.Node<Integer> child3 = new Tree.Node<>(2);

        root.addChildrenNodes(child1, child2, child3);

        child1.addChildren(5, 5);
        child2.addChildren(4, 4);
        child3.addChildren(3, 3);

        Tree<Integer> tree = root.asTree();

        assertEquals("1\n" + "   |\n" + "   |- 2\n" + "   |   |\n" + "   |   |- 5\n" + "   |   |\n" + "   |   |- 5\n" +
                "   |\n" + "   |- 6\n" + "   |   |\n" + "   |   |- 4\n" + "   |   |\n" + "   |   |- 4\n" + "   |\n" +
                "   |- 2\n" + "       |\n" + "       |- 3\n" + "       |\n" + "       |- 3", tree.toString());
    }


    @Test
    public void testTreeToStringWhenToStringOfNodeContainsNewLines() {

        Tree.Node<Object> root = new Tree.Node<>(1);
        Tree.Node<Object> child1 = new Tree.Node<>(2);
        Tree.Node<Object> child2 = new Tree.Node<>("testing \n boom");
        Tree.Node<Object> child3 = new Tree.Node<>(2);

        root.addChildrenNodes(child1, child2, child3);

        child1.addChildren(5, 5);
        child2.addChildren(4, 4);
        child3.addChildren(3, 3);

        Tree<Object> tree = root.asTree();

        assertEquals("1\n" + "   |\n" + "   |- 2\n" + "   |   |\n" + "   |   |- 5\n" + "   |   |\n" + "   |   |- 5\n" +
                "   |\n" + "   |- testing <newline> boom\n" + "   |   |\n" + "   |   |- 4\n" + "   |   |\n" +
                "   |   |- 4\n" + "   |\n" + "   |- 2\n" + "       |\n" + "       |- 3\n" + "       |\n" +
                "       |- 3", tree.toString());
    }

}
