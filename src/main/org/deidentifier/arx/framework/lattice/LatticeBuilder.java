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

/**
 * The class LatticeBuilder.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class LatticeBuilder {

    /** The levels. */
    private Node[][] levels    = null;

    /** The maxlevels. */
    private int[]    maxLevels = null;

    /** The minlevels. */
    private int[]    minLevels = null;

    /**
     * Instantiates a new lattice builder.
     *
     * @param maxLevels the maxlevels
     * @param minLevels the minlevels
     * @param maxHeights
     */
    public LatticeBuilder(final int[] maxLevels,
                          final int[] minLevels) {
        this.maxLevels = maxLevels;
        this.minLevels = minLevels;
    }

    /**
     * Builds the.
     * 
     * @return the lattice
     */
    public Lattice build() {
        int[] max = new int[maxLevels.length];
        for (int i = 0; i < max.length; i++) {
            max[i] = maxLevels[i] + 1;
        }

        return new Lattice(max);
        // final int numNodes = buildLevelsAndMap();
        // return new Lattice(levels, numNodes);
    }

}
