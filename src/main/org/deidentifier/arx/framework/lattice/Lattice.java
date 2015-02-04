/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2015 Florian Kohlmayer, Fabian Prasser
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.deidentifier.arx.framework.lattice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.deidentifier.arx.ARXListener;
import org.deidentifier.arx.framework.check.INodeChecker;
import org.deidentifier.arx.metric.InformationLoss;

public class Lattice {

    public class LevelbasedLatticeIterator implements Iterator<Node> {

        private final Lattice lattice;
        private final int     maxRow;
        private int           currentRow;
        private int           currentColumn;

        public LevelbasedLatticeIterator(Lattice lattice) {
            this.lattice = lattice;
            currentColumn = 0;
            currentRow = 0;
            maxRow = lattice.levels.length;
        }

        @Override
        public boolean hasNext() {
            if (currentRow < maxRow) {
                return true;
            }
            return false;
        }

        @Override
        public Node next() {

            int[] level = lattice.levels[currentRow];
            Node node = lattice.getNode(level[currentColumn]);

            if (currentColumn < (level.length - 1)) {
                currentColumn++;
            } else {
                currentColumn = 0;
                currentRow++;
            }

            return node;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

    }

    public static void main(String[] args) {
        int[] height = new int[] { 3, 2, 6 };

        Lattice lattice = new Lattice(height);

        System.out.println("size: " + lattice.getSize());
        System.out.println("base: " + Arrays.toString(lattice.basis));
        System.out.println("offsets: " + Arrays.toString(lattice.offsets));

        int cnt = 0;
        Iterator<Node> iterator = lattice.iterator();
        while (iterator.hasNext()) {
            Node node = iterator.next();
            System.out.print(node);
            System.out.print("-");
            cnt++;
        }
        System.out.println(cnt);

    }

    /** A listener */
    private ARXListener                            listener   = null;

    /** Tag trigger */
    private NodeAction                             tagTrigger = null;
    private final int[]                            maxLevels;
    private final int[]                            basis;
    private final int[]                            offsets;
    private final int[]                            nodeProperties;
    private final int                              height;
    private final Map<Integer, Object>             data;
    private final Map<Integer, InformationLoss<?>> informationLoss;

    private final Map<Integer, InformationLoss<?>> lowerBound;

    private final int[][]                          levels;

    Node[][]                                       nodeLevels = null;

    public Lattice(int[] maxLevels) {
        this.maxLevels = new int[maxLevels.length];
        offsets = new int[maxLevels.length];
        basis = Arrays.copyOf(maxLevels, maxLevels.length);

        int temp = 0;
        int size = 1;
        for (int i = maxLevels.length - 1; i >= 0; i--) {
            offsets[i] = size;
            size *= maxLevels[i];
            this.maxLevels[i] = maxLevels[i] - 1;
            temp += this.maxLevels[i];
        }
        nodeProperties = new int[size];
        height = temp + 1;

        data = new HashMap<Integer, Object>();
        informationLoss = new HashMap<Integer, InformationLoss<?>>();
        lowerBound = new HashMap<Integer, InformationLoss<?>>();

        // create levels
        levels = createLevels();

    }

    public Lattice(Node[][] nodes, int levels) {
        // TODO: implement correctly

        Node node = null;
        for (int j = 0; j < nodes.length; j++) {
            Node[] level = nodes[j];
            for (int k = 0; k < level.length; k++) {
                node = level[k];
            }
        }
        int[] transformation = node.getTransformation();
        int[] maxLevels = new int[transformation.length];
        for (int i = 0; i < maxLevels.length; i++) {
            maxLevels[i] = transformation[i] + 1;
        }
        this.maxLevels = new int[maxLevels.length];
        offsets = new int[maxLevels.length];
        basis = Arrays.copyOf(maxLevels, maxLevels.length);

        int temp = 0;
        int size = 1;
        for (int i = maxLevels.length - 1; i >= 0; i--) {
            offsets[i] = size;
            size *= maxLevels[i];
            this.maxLevels[i] = maxLevels[i] - 1;
            temp += this.maxLevels[i];
        }
        nodeProperties = new int[size];
        height = temp + 1;

        data = new HashMap<Integer, Object>();
        informationLoss = new HashMap<Integer, InformationLoss<?>>();
        lowerBound = new HashMap<Integer, InformationLoss<?>>();

        // create levels
        this.levels = createLevels();

        // throw new UnsupportedOperationException();
        System.out.println("Ugly!");

    }

    public Node getBottom() {
        return getNode(0);
    }

    public int getHeight() {
        return height;
    }

    public int getLevel(int index) {
        return getLevel(getTransformation(index));
    }

    public Node[][] getLevels() {

        if (nodeLevels == null) {
            Node[][] levels = new Node[this.levels.length][];
            for (int i = 0; i < levels.length; i++) {
                levels[i] = new Node[this.levels[i].length];
                for (int j = 0; j < levels[i].length; j++) {
                    levels[i][j] = getNode(this.levels[i][j]);
                }
            }
            nodeLevels = levels;
        }

        // Return
        return nodeLevels;
    }

    public int getSize() {
        return nodeProperties.length;
    }

    public Node getTop() {
        return getNode(getSize() - 1);
    }

    public Iterator<Node> iterator() {
        return new LevelbasedLatticeIterator(this);
    }

    public void setChecked(Node node, INodeChecker.Result result) {

        int index = node.id;

        // Set checked
        setProperty(index, Node.PROPERTY_CHECKED);

        // Anonymous
        if (result.anonymous) {
            setProperty(index, Node.PROPERTY_ANONYMOUS);
        } else {
            setProperty(index, Node.PROPERTY_NOT_ANONYMOUS);
        }

        // k-Anonymous
        if (result.kAnonymous) {
            setProperty(index, Node.PROPERTY_K_ANONYMOUS);
        } else {
            setProperty(index, Node.PROPERTY_NOT_K_ANONYMOUS);
        }

        // Infoloss
        informationLoss.put(index, result.informationLoss);
        lowerBound.put(index, result.lowerBound);
    }

    public void setInformationLoss(Node node, InformationLoss<?> informationLoss) {
        this.informationLoss.put(node.id, informationLoss);
    }

    public void setListener(final ARXListener listener) {
        this.listener = listener;
    }

    public void setLowerBound(Node node, InformationLoss<?> lowerBound) {
        this.lowerBound.put(node.id, lowerBound);
    }

    public void setProperty(Node node, int property) {
        int index = node.id;
        if (!hasProperty(index, property)) {
            setProperty(index, property);
            triggerTagged(node);
        }
    }

    public void setPropertyDownwards(int index, boolean include, int property) {

        if (include) {
            setProperty(index, property);
        }

        int tempIndex = index;
        for (int i = maxLevels.length - 1; i >= 0; i--) {
            int state = tempIndex % basis[i];
            if (state != 0) {
                int predecessorIndex = index - offsets[i];
                if (!hasProperty(predecessorIndex, property)) {
                    setPropertyDownwards(predecessorIndex, true, property);
                }
            }
            tempIndex /= basis[i];
        }
    }

    public void setPropertyDownwards(Node node, boolean include, int property) {
        setPropertyDownwards(node.id, include, property);
    }

    public void setPropertyUpwards(int index, boolean include, int property) {

        if (include) {
            setProperty(index, property);
        }

        int tempIndex = index;
        for (int i = maxLevels.length - 1; i >= 0; i--) {
            int state = tempIndex % basis[i];
            if (state < maxLevels[i]) {
                int successorIndex = index + offsets[i];
                if (!hasProperty(successorIndex, property)) {
                    setPropertyUpwards(successorIndex, true, property);
                }
            }
            tempIndex /= basis[i];
        }
    }

    public void setPropertyUpwards(Node node, boolean include, int property) {
        setPropertyUpwards(node.id, include, property);
    }

    public void setTagTrigger(NodeAction trigger) {
        tagTrigger = trigger;
    }

    private int[][] createLevels() {
        // Prepare
        @SuppressWarnings("unchecked")
        List<Integer>[] levels = new List[height];
        for (int i = 0; i < levels.length; i++) {
            levels[i] = new ArrayList<Integer>();
        }

        // Add
        for (int i = 0; i < nodeProperties.length; i++) {
            int level = getLevel(i);
            levels[level].add(i);
        }

        // Pack
        int[][] result = new int[levels.length][];
        for (int i = 0; i < levels.length; i++) {
            result[i] = new int[levels[i].size()];
            for (int j = 0; j < result[i].length; j++) {
                result[i][j] = levels[i].get(j);
            }
        }
        return result;
    }

    private int getLevel(final int[] transformation) {
        // Return the sum of transformation's components
        int sum = 0;
        for (int a : transformation) {
            sum += a;
        }
        return sum;
    }

    private Node getNode(int index) {
        Node node = new Node(this, index);
        return node;
    }

    private int[] getPredecessors(final int index) {
        int[] predessors = new int[maxLevels.length];

        int tempIndex = index;
        int idx = 0;
        for (int i = maxLevels.length - 1; i >= 0; i--) {
            int state = tempIndex % basis[i];
            if (state != 0) {
                int predecessorIndex = index - offsets[i];
                predessors[idx++] = predecessorIndex;
            }
            tempIndex /= basis[i];
        }

        int[] result = new int[idx];
        System.arraycopy(predessors, 0, result, 0, idx);
        return result;
    }

    private int[] getSuccessors(final int index) {
        int[] successors = new int[maxLevels.length];

        int tempIndex = index;
        int idx = 0;
        for (int i = maxLevels.length - 1; i >= 0; i--) {
            int state = tempIndex % basis[i];
            if (state < maxLevels[i]) {
                int successorIndex = index + offsets[i];
                successors[idx++] = successorIndex;
            }
            tempIndex /= basis[i];
        }

        int[] result = new int[idx];
        System.arraycopy(successors, 0, result, 0, idx);
        return result;
    }

    private void setProperty(int index, int property) {
        nodeProperties[index] |= property;
    }

    private void triggerTagged(int index, Node node) {
        if ((listener != null) && !hasProperty(index, Node.PROPERTY_EVENT_FIRED)) {
            if ((tagTrigger == null) || tagTrigger.appliesTo(node)) {
                setProperty(index, Node.PROPERTY_EVENT_FIRED);
                listener.nodeTagged(getSize());
            }
        }
    }

    private void triggerTagged(Node node) {
        triggerTagged(node.id, node);
    }

    protected Object getData(int index) {
        return data.get(index);
    }

    protected int getIndex(int[] transformation) {
        int index = 0;
        for (int i = 0; i < transformation.length; i++) {
            index += offsets[i] * transformation[i];
        }
        return index;
    }

    protected InformationLoss<?> getInformationLoss(int index) {
        return informationLoss.get(index);
    }

    protected InformationLoss<?> getLowerBound(int index) {
        return lowerBound.get(index);
    }

    protected Node[] getPredecessorsNodes(int index) {
        int[] predecessors = getPredecessors(index);
        Node[] predecessorsNodes = new Node[predecessors.length];
        for (int i = 0; i < predecessors.length; i++) {
            Node node = getNode(predecessors[i]);
            predecessorsNodes[i] = node;
        }
        return predecessorsNodes;
    }

    protected Node[] getSuccessorsNodes(int index) {
        int[] successors = getSuccessors(index);
        Node[] successorNodes = new Node[successors.length];
        for (int i = 0; i < successors.length; i++) {
            Node node = getNode(successors[i]);
            successorNodes[i] = node;
        }
        return successorNodes;
    }

    protected int[] getTransformation(int index) {
        int[] transformation = new int[basis.length];
        for (int i = transformation.length - 1; i >= 0; i--) {
            transformation[i] = index % basis[i];
            index /= basis[i];
        }
        return transformation;
    }

    protected boolean hasProperty(int index, int property) {
        return (nodeProperties[index] & property) == property;
    }

    protected void setData(int index, Object data) {
        this.data.put(index, data);
    }
}
