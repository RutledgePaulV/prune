[![Build Status](https://travis-ci.org/RutledgePaulV/prune.svg?branch=master)](https://travis-ci.org/RutledgePaulV/prune)
[![Coverage Status](https://coveralls.io/repos/github/RutledgePaulV/prune/badge.svg?branch=master)](https://coveralls.io/github/RutledgePaulV/prune?branch=master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.rutledgepaulv/prune/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.rutledgepaulv/prune)

## A zero-nonsense tree library for Java 8

No dependencies. One top-level class. Depth-first and breadth-first node streaming, visiting, and searching.
Prune off sections of the tree using predicate filters. ToString of a tree returns an ascii representation of the
tree. Two nodes with the same data are equal. Two trees with the same structure and ordering of nodes with 
the same data are equal.

### Usage
```java
Tree.Node<Integer> root = new Tree.Node<>(1);

Tree.Node<Integer> child1 = new Tree.Node<>(2);
Tree.Node<Integer> child2 = new Tree.Node<>(6);
Tree.Node<Integer> child3 = new Tree.Node<>(2);

root.addChildrenNodes(child1, child2, child3);

child1.addChildren(5, 5);
child2.addChildren(4, 4);
child3.addChildren(3, 3);

Tree<Integer> tree = node(1, 
                            node(2, 
                                node(5), node(5)), 
                            node(6, 
                                node(4), node(4)), 
                            node(2, 
                                node(3), node(3))).asTree();

Optional<Integer> firstIntegerGreaterThan4DepthFirst = tree.depthFirstSearch(val -> val > 4);
Optional<Integer> firstIntegerGreaterThan4BreadthFirst = tree.breadthFirstSearch(val -> val > 4);

assertTrue(firstIntegerGreaterThan4DepthFirst.isPresent());
assertTrue(firstIntegerGreaterThan4BreadthFirst.isPresent());

assertEquals((Integer) 5, firstIntegerGreaterThan4DepthFirst.get());
assertEquals((Integer) 6, firstIntegerGreaterThan4BreadthFirst.get());


System.out.println(tree);
```

```text
1 -
    |
    |- 2
    |   |
    |   |- 5
    |   |
    |   |- 5
    |
    |- 6
    |   |
    |   |- 4
    |   |
    |   |- 4
    |
    |- 2
        |
        |- 3
        |
        |- 3
```

### Install
```xml
<dependencies>
    
    <dependency>
        <groupId>com.github.rutledgepaulv</groupId>
        <artifactId>prune</artifactId>
        <version>1.1</version>
    </dependency>
            
</dependencies>
```


### License
This project is licensed under [MIT license](http://opensource.org/licenses/MIT).
