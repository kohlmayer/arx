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

import java.util.Arrays;

import org.deidentifier.arx.metric.InformationLoss;

public class Node {

    /** All privacy criteria are fulfilled. */
    public static final int PROPERTY_ANONYMOUS            = 1 << 0;

    /** Not all privacy criteria are fulfilled. */
    public static final int PROPERTY_NOT_ANONYMOUS        = 1 << 1;

    /** A k-anonymity sub-criterion is fulfilled. */
    public static final int PROPERTY_K_ANONYMOUS          = 1 << 2;

    /** A k-anonymity sub-criterion is not fulfilled. */
    public static final int PROPERTY_NOT_K_ANONYMOUS      = 1 << 3;

    /** The transformation results in insufficient utility. */
    public static final int PROPERTY_INSUFFICIENT_UTILITY = 1 << 4;

    /** The transformation has been checked explicitly. */
    public static final int PROPERTY_CHECKED              = 1 << 5;

    /** A snapshot for this transformation must be created if it fits the size limits, regardless of whether it triggers the storage condition. */
    public static final int PROPERTY_FORCE_SNAPSHOT       = 1 << 6;

    /** This node has already been visited during the second phase. */
    public static final int PROPERTY_VISITED              = 1 << 7;

    /** Marks nodes for which the search algorithm guarantees to never check any of its successors. */
    public static final int PROPERTY_SUCCESSORS_PRUNED    = 1 << 8;

    /** We have already fired an event for this node. */
    public static final int PROPERTY_EVENT_FIRED          = 1 << 9;

    private Lattice         lattice                       = null;
    public int              id;

    public Node(Lattice lattice, int index) {
        this.id = index;
        this.lattice = lattice;
    }

    public Node(int id) {
        // TODO: implement correctly
        this.id = id;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Node other = (Node) obj;
        if (id != other.id) {
            return false;
        }
        return true;
    }

    /**
     * Associated data.
     *
     * @return
     */

    public Object getData() {
        return lattice.getData(id);
    }

    /**
     * Returns the information loss.
     *
     * @return
     */

    public InformationLoss<?> getInformationLoss() {
        return lattice.getInformationLoss(id);
    }

    /**
     * Returns the level.
     *
     * @return
     */
    public int getLevel() {
        return lattice.getLevel(id);
    }

    /**
     * @return the lowerBound
     */

    public InformationLoss<?> getLowerBound() {
        return lattice.getLowerBound(id);
    }

    /**
     * Returns the predecessors.
     *
     * @param materialize
     * @return
     */

    private Node[] precs;

    public Node[] getPredecessors() {
        if (precs == null) {
            precs = lattice.getPredecessorsNodes(id);
        }
        return precs;
    }

    /**
     * Returns the successors.
     *
     * @param materialize
     * @return
     */

    private Node[] succs;

    public Node[] getSuccessors() {
        if (succs == null) {
            succs = lattice.getSuccessorsNodes(id);
        }
        return succs;
    }

    /**
     * Returns the transformation.
     *
     * @return
     */

    public int[] getTransformation() {
        return lattice.getTransformation(id);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */

    public int hashCode() {
        return id;
    }

    /**
     * Returns whether the node has the given property.
     *
     * @param property
     * @return
     */
    public boolean hasProperty(int property) {
        return lattice.hasProperty(id, property);
    }

    /**
     * Associated data.
     *
     * @param data
     */
    public void setData(Object data) {
        lattice.setData(id, data);
    }

    public void setTransformation(int[] transformation, int level) {
        throw new UnsupportedOperationException();
    }

    /**
     * Adds a predecessor
     * 
     * @param node
     */
    protected void addPredecessor(Node node) {
        // empty by design
    }

    /**
     * Adds a successor
     * 
     * @param node
     */
    protected void addSuccessor(Node node) {
        // empty by design
    }

    protected void setInformationLoss(final InformationLoss<?> informationLoss) {
        throw new UnsupportedOperationException();
    }

    protected void setLowerBound(final InformationLoss<?> lowerBound) {
        throw new UnsupportedOperationException();
    }

    protected void setPredecessors(Node[] nodes) {
        throw new UnsupportedOperationException();
    }

    protected void setProperty(int property) {
        throw new UnsupportedOperationException();
    }

    protected void setSuccessors(Node[] nodes) {
        throw new UnsupportedOperationException();
    }

    void builderAddPredecessor(Node predecessor) {
        throw new UnsupportedOperationException();
    }

    void builderAddSuccessor(Node successor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return Arrays.toString(getTransformation());
    }

}
