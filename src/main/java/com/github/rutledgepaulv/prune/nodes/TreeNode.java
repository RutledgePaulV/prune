package com.github.rutledgepaulv.prune.nodes;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;


public class TreeNode<T> {

    private T data;
    private List<TreeNode<T>> children = new LinkedList<>();

    public TreeNode() {}

    public TreeNode(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public List<TreeNode<T>> getChildren() {
        return children;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TreeNode)) {
            return false;
        }
        TreeNode<?> treeNode = (TreeNode<?>) o;
        return Objects.equals(data, treeNode.data) &&
                Objects.equals(children, treeNode.children);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data, children);
    }

}
