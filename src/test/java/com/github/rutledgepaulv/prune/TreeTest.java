package com.github.rutledgepaulv.prune;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.github.rutledgepaulv.prune.Tree.node;
import static org.junit.Assert.*;

public class TreeTest {

    @Test
    public void testCardinality() {
        Tree<String> tree = node("root", node("depth1", node("depth2", node("depth3")))).asTree();
        assertEquals(4, tree.cardinality());
    }

    @Test
    public void testDegree() {
        Tree<String> tree = node("root", node("depth1", node("depth2", node("depth3")))).asTree();
        assertEquals(1, tree.asNode().getDegree());

        tree = node("root", node("depth1"), node("depth2"), node("depth3")).asTree();
        assertEquals(3, tree.asNode().getDegree());
    }

    @Test
    public void maxDepth() {
        Tree<String> tree = node("root", node("depth1", node("depth2", node("depth3")))).asTree();
        assertEquals(3, tree.getMaxDepth());
    }

    @Test
    public void maxOrder() {
        Tree<String> tree = node("root", node("depth1", node("depth2", node("depth3"), node("depth3"))),
                node("depth1", node("depth2", node("depth3"), node("depth3")))).asTree();

        assertEquals(3, tree.getMaxGlobalOrder());
        assertEquals(1, tree.getMaxLocalOrder());
    }

    @Test
    public void ofCollection() {

        List<Data> things = Arrays.asList(new Data(0, -1, "Root"), new Data(1, 0, "Child1"), new Data(2, 0, "Child2"));
        List<Tree<Data>> trees = Tree.of(things, (p, c) -> (p.getData().id == c.getData().parentId));
        assertEquals(1, trees.size());
        assertEquals("(parent: -1, body: Root)\n" +
                     "   |\n" +
                     "   |- (parent: 0, body: Child1)\n" +
                     "   |\n" +
                     "   |- (parent: 0, body: Child2)", trees.get(0).toString());
    }

    @Test
    public void ofCollectionWithExplicitRoot() {
        List<Data> things = Arrays.asList(new Data(1, 0, "Child1"), new Data(2, 0, "Child2"));
        Tree<Data> tree = Tree.of(node(new Data(0, -1, "Root")), things, (p, c) -> (p.getData().id == c.getData().parentId));
        assertEquals("(parent: -1, body: Root)\n" +
                     "   |\n" +
                     "   |- (parent: 0, body: Child1)\n" +
                     "   |\n" +
                     "   |- (parent: 0, body: Child2)", tree.toString());
    }


    @Test
    public void collapsingFilter() {

        Tree<String> tree = node("root", node("child1", node("nestedChild1")), node("child2", node("nestedChild2"))).asTree();

        assertEquals("root\n" +
                     "   |\n" +
                     "   |- nestedChild1\n" +
                     "   |\n" +
                     "   |- nestedChild2", tree.filter(s -> s.contains("child")).toString());
    }

    @Test
    public void mapAgainstTree() {

        Tree.Node<String> root = node("root");
        Tree.Node<String> child1 = node("one");
        Tree.Node<String> child2 = node("two");
        Tree.Node<String> child3 = node("three");
        Tree.Node<String> subchild1 = node("fourteen");
        child1.addChildrenNodes(subchild1);
        root.addChildrenNodes(child1, child2, child3);

        assertEquals("4\n" +
                        "   |\n" +
                        "   |- 3\n" +
                        "   |   |\n" +
                        "   |   |- 8\n" +
                        "   |\n" +
                        "   |- 3\n" +
                        "   |\n" +
                        "   |- 5",
                root.asTree().map(String::length).toString());
    }

    @Test
    public void swapBetweenNodeAndTree() {

        Tree.Node<Integer> root = node(100);

        Tree<Integer> tree = root.asTree();

        Tree.Node<Integer> inverse = tree.asNode();

        assertEquals(root, inverse);

        Tree<Integer> treeInverse = inverse.asTree();

        assertEquals(tree, treeInverse);
    }



    @Test
    public void breadthFirst() {

        Tree.Node<Integer> root = node(1);

        Tree.Node<Integer> child1 = node(2);
        Tree.Node<Integer> child2 = node(2);
        Tree.Node<Integer> child3 = node(2);

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


        Tree.Node<Integer> root = node(1);

        Tree.Node<Integer> child1 = node(2);
        Tree.Node<Integer> child2 = node(2);
        Tree.Node<Integer> child3 = node(2);

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
        Tree.Node<Integer> root = node(1);

        Tree.Node<Integer> child1 = node(2);
        Tree.Node<Integer> child2 = node(2);

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
        Tree.Node<Integer> root = node(1);

        Tree.Node<Integer> child1 = node(2);
        Tree.Node<Integer> child2 = node(2);

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

        Tree.Node<Integer> root = node(1);
        root.addChildren(1, 2, 3, 4);

        assertEquals(4, root.getChildren().size());

        root = root.asTree().prune(n -> n < 4).asNode();
        assertEquals(1, root.getChildren().size());
    }

    @Test
    public void pruneDescendantsByNodes() {

        Tree.Node<Integer> root = node(1);
        Tree.Node<Integer> child = node(5);

        root.addChildNode(child);
        child.addChildren(4, 3, 5);
        root.addChildren(1, 2, 3, 4);

        assertEquals(5, root.getChildren().size());
        root = root.asTree().pruneAsNodes(node -> node.getChildren().size() == 3).asNode();

        assertEquals(4, root.getChildren().size());
        assertFalse(root.asTree().depthFirstStreamNodes().anyMatch(node -> node.equals(child)));
    }

    @Test
    public void pruneIndirectDescendants() {

        Tree.Node<Integer> root = node(1);
        Tree.Node<Integer> child = node(5);

        root.addChildNode(child);
        root.addChildren(4, 3, 5);
        child.addChildren(5, 6);
        child.addChildrenNodes(node(7));
        child.addChild(8);
        child.addChildren(Collections.singletonList(10));
        child.addChildrenNodes(Collections.singletonList(node(9)));

        assertEquals(4, root.getChildren().size());

        root = root.asTree().prune(n -> n == 5).asNode();

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

        Tree.Node<Integer> root = node(1);

        Tree.Node<Integer> child1 = node(2);
        Tree.Node<Integer> child2 = node(6);
        Tree.Node<Integer> child3 = node(2);

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

        Tree.Node<Integer> root = node(1);

        Tree.Node<Integer> child1 = node(2);
        Tree.Node<Integer> child2 = node(6);
        Tree.Node<Integer> child3 = node(2);

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

        Tree.Node<Integer> root = node(1);

        Tree.Node<Integer> child1 = node(2);
        Tree.Node<Integer> child2 = node(6);
        Tree.Node<Integer> child3 = node(2);

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

        Tree.Node<Integer> root = node(1);

        Tree.Node<Integer> child1 = node(2);
        Tree.Node<Integer> child2 = node(6);
        Tree.Node<Integer> child3 = node(2);

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

        Tree.Node<Integer> root = node(1);

        Tree.Node<Integer> child1 = node(2);
        Tree.Node<Integer> child2 = node(6);
        Tree.Node<Integer> child3 = node(2);

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

        Tree.Node<Integer> root = node(1);

        Tree.Node<Integer> child1 = node(2);
        Tree.Node<Integer> child2 = node(6);
        Tree.Node<Integer> child3 = node(2);

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

        Tree.Node<Integer> root = node(1);

        Tree.Node<Integer> child1 = node(2);
        Tree.Node<Integer> child2 = node(6);
        Tree.Node<Integer> child3 = node(2);

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

        Tree.Node<Integer> root = node(1);

        Tree.Node<Integer> child1 = node(2);
        Tree.Node<Integer> child2 = node(6);
        Tree.Node<Integer> child3 = node(2);

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
        Tree.Node<Integer> root = node(1);

        Tree.Node<Integer> one = node(2);
        Tree.Node<Integer> two = node(2);

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
        Tree.Node<Integer> tree1_root = node(1);
        Tree.Node<Integer> tree1_depth1_a = node(2);
        Tree.Node<Integer> tree1_depth1_b = node(2);
        Tree.Node<Integer> tree1_depth2_a_a = node(5);
        Tree.Node<Integer> tree1_depth2_b_a = node(6);

        tree1_root.addChildrenNodes(tree1_depth1_a, tree1_depth1_b);
        tree1_depth1_a.addChildNode(tree1_depth2_a_a);
        tree1_depth1_b.addChildNode(tree1_depth2_b_a);

        Tree.Node<Integer> tree2_root = node(1);
        Tree.Node<Integer> tree2_depth1_a = node(2);
        Tree.Node<Integer> tree2_depth1_b = node(2);
        Tree.Node<Integer> tree2_depth2_a_a = node(5);
        Tree.Node<Integer> tree2_depth2_b_a = node(6);

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

        tree2 = tree2_root.asTree();

        assertNotEquals(tree1, tree2);
    }

    @Test
    public void testNodeToString() {
        Tree.Node<Integer> root = node(1);
        Tree.Node<Integer> child1 = node(2);
        root.addChildNode(child1);
        assertEquals("1", root.toString());
    }


    @Test
    public void testTreeToString() {
        Tree.Node<Integer> root = node(1);

        Tree.Node<Integer> child1 = node(2);
        Tree.Node<Integer> child2 = node(6);
        Tree.Node<Integer> child3 = node(2);

        root.addChildrenNodes(child1, child2, child3);

        child1.addChildren(5, 5);
        child2.addChildren(4, 4);
        child3.addChildren(3, 3);

        Tree<Integer> tree = root.asTree();

        assertEquals("1\n" +
                    "   |\n" +
                    "   |- 2\n" +
                    "   |   |\n" +
                    "   |   |- 5\n" +
                    "   |   |\n" +
                    "   |   |- 5\n" +
                    "   |\n" + "   " +
                    "|- 6\n" +
                    "   |   |\n" +
                    "   |   |- 4\n" +
                    "   |   |\n" +
                    "   |   |- 4\n" +
                    "   |\n" +
                    "   |- 2\n" +
                    "       |\n" +
                    "       |- 3\n" +
                    "       |\n" +
                    "       |- 3", tree.toString());
    }


    @Test
    public void testTreeToStringWhenToStringOfNodeContainsNewLines() {

        Tree.Node<Object> root = node(1);
        Tree.Node<Object> child1 = node(2);
        Tree.Node<Object> child2 = node("testing \n boom");
        Tree.Node<Object> child3 = node(2);

        root.addChildrenNodes(child1, child2, child3);

        child1.addChildren(5, 5);
        child2.addChildren(4, 4);
        child3.addChildren(3, 3);

        Tree<Object> tree = root.asTree();

        assertEquals("1\n" +
                    "   |\n" +
                    "   |- 2\n" +
                    "   |   |\n" +
                    "   |   |- 5\n" +
                    "   |   |\n" +
                    "   |   |- 5\n" +
                    "   |\n" +
                    "   |- testing <newline> boom\n" +
                    "   |   |\n" +
                    "   |   |- 4\n" +
                    "   |   |\n" +
                    "   |   |- 4\n" +
                    "   |\n" +
                    "   |- 2\n" +
                    "       |\n" +
                    "       |- 3\n" +
                    "       |\n" +
                    "       |- 3", tree.toString());
    }


    private static class Data {
        private int id;
        private int parentId;
        private String body;
        private Data(int id, int parent, String b) {
            this.id = id;
            this.parentId = parent;
            this.body = b;
        }
        @Override
        public String toString() {
            return "(parent: " + parentId + ", body: " + body + ")";
        }
    }
}
