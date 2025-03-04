/*
 Copyright 2008-2010 Gephi
 Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
 Website : http://www.gephi.org

 This file is part of Gephi.

 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

 Copyright 2011 Gephi Consortium. All rights reserved.

 The contents of this file are subject to the terms of either the GNU
 General Public License Version 3 only ("GPL") or the Common
 Development and Distribution License("CDDL") (collectively, the
 "License"). You may not use this file except in compliance with the
 License. You can obtain a copy of the License at
 http://gephi.org/about/legal/license-notice/
 or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
 specific language governing permissions and limitations under the
 License.  When distributing the software, include this License Header
 Notice in each file and include the License files at
 /cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
 License Header, with the fields enclosed by brackets [] replaced by
 your own identifying information:
 "Portions Copyrighted [year] [name of copyright owner]"

 If you wish your version of this file to be governed by only the CDDL
 or only the GPL Version 3, indicate your decision by adding
 "[Contributor] elects to include this software in this distribution
 under the [CDDL or GPL Version 3] license." If you do not indicate a
 single choice of license, a recipient has the option to distribute
 your version of this file under either the CDDL, the GPL Version 3 or
 to extend the choice of license to its licensees as provided above.
 However, if you add GPL Version 3 code and therefore, elected the GPL
 Version 3 license, then the option applies only if the new code is
 made subject to such option by the copyright holder.

 Contributor(s):

 Portions Copyrighted 2011 Gephi Consortium.
 */

package org.gephi.io.importer.impl;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import org.gephi.graph.api.AttributeUtils;
import org.gephi.graph.api.Interval;
import org.gephi.graph.api.TimeFormat;
import org.gephi.graph.api.TimeRepresentation;
import org.gephi.graph.api.types.TimeSet;
import org.gephi.io.importer.api.ColumnDraft;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.ContainerLoader;
import org.gephi.io.importer.api.ContainerUnloader;
import org.gephi.io.importer.api.EdgeDirection;
import org.gephi.io.importer.api.EdgeDirectionDefault;
import org.gephi.io.importer.api.EdgeDraft;
import org.gephi.io.importer.api.EdgeMergeStrategy;
import org.gephi.io.importer.api.ElementDraft;
import org.gephi.io.importer.api.ElementIdType;
import org.gephi.io.importer.api.Issue;
import org.gephi.io.importer.api.Issue.Level;
import org.gephi.io.importer.api.NodeDraft;
import org.gephi.io.importer.api.Report;
import org.joda.time.DateTimeZone;
import org.openide.util.NbBundle;

/**
 * @author Mathieu Bastian
 */
public class ImportContainerImpl implements Container, ContainerLoader, ContainerUnloader {

    protected static final int NULL_INDEX = -1;
    //Factory
    private final ElementFactoryImpl factory;
    //Parameters
    private final ImportContainerParameters parameters;
    //Maps and Data
    private final ObjectList<NodeDraftImpl> nodeList;
    private final ObjectList<EdgeDraftImpl> edgeList;
    private final Object2IntMap<String> nodeMap;
    private final Object2IntMap<String> edgeMap;
    private final Object2IntMap edgeTypeMap;
    private final Object2ObjectMap<String, ColumnDraft> nodeColumns;
    private final Object2ObjectMap<String, ColumnDraft> edgeColumns;
    //MetaData
    private String source;
    private Class lastEdgeType;
    private Long2ObjectMap<int[]>[] edgeTypeSets;
    private EdgeDirectionDefault edgeDefault = EdgeDirectionDefault.MIXED;
    //Config
    private ElementIdType elementIdType = ElementIdType.STRING;
    //Management
    private boolean dynamicGraph = false;
    private boolean dynamicAttributes = false;
    private Report report;
    //Counting
    private int directedEdgesCount = 0;
    private int undirectedEdgesCount = 0;
    private int selfLoops = 0;
    private int mutualEdgesCount = 0;
    //Dynamic
    private TimeFormat timeFormat = TimeFormat.DOUBLE;
    private TimeRepresentation timeRepresentation = TimeRepresentation.INTERVAL;
    private DateTimeZone timeZone = DateTimeZone.getDefault();
    private Double timestamp;
    private Interval interval;
    //Report flag
    private boolean reportedUnknownNode;
    private boolean reportedParallelEdges;

    public ImportContainerImpl() {
        parameters = new ImportContainerParameters();
        nodeMap = new Object2IntOpenHashMap<>();
        edgeMap = new Object2IntOpenHashMap<>();
        nodeMap.defaultReturnValue(NULL_INDEX);
        edgeMap.defaultReturnValue(NULL_INDEX);
        nodeList = new ObjectArrayList<>();
        edgeList = new ObjectArrayList<>();
        edgeTypeMap = new Object2IntOpenHashMap();
        edgeTypeSets = new Long2ObjectMap[0];
        factory = new ElementFactoryImpl(this);
        nodeColumns = new Object2ObjectLinkedOpenHashMap<>();
        edgeColumns = new Object2ObjectLinkedOpenHashMap<>();
        report = new Report();
    }

    @Override
    public ContainerLoader getLoader() {
        return this;
    }

    @Override
    public synchronized ContainerUnloader getUnloader() {
        return this;
    }

    @Override
    public ElementFactoryImpl factory() {
        return factory;
    }

    @Override
    public String getSource() {
        return source;
    }

    @Override
    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public void addNode(NodeDraft nodeDraft) {
        checkElementDraftImpl(nodeDraft);
        NodeDraftImpl nodeDraftImpl = (NodeDraftImpl) nodeDraft;

        if (nodeMap.containsKey(nodeDraftImpl.getId())) {
            String message = NbBundle
                .getMessage(ImportContainerImpl.class, "ImportContainerException_nodeExist", nodeDraftImpl.getId());
            report.logIssue(new Issue(message, Level.WARNING));
            return;
        }

        int index = nodeList.size();
        nodeList.add(nodeDraftImpl);
        nodeMap.put(nodeDraftImpl.getId(), index);
    }

    @Override
    public NodeDraftImpl getNode(String id) {
        checkId(id);

        int index = nodeMap.getInt(id);
        NodeDraftImpl node = null;
        if (index == NULL_INDEX) {
            if (parameters.isAutoNode()) {
                //Creates the missing node
                node = factory.newNodeDraft(id);
                addNode(node);
                node.setCreatedAuto(true);
                if (!reportedUnknownNode) {
                    String message =
                        NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerException_AutoNodeCreated");
                    report.logIssue(new Issue(message, Level.INFO));
                    reportedUnknownNode = true;
                }
            } else {
                String message =
                    NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerException_UnknowNodeId", id);
                report.logIssue(new Issue(message, Level.SEVERE));
            }
        } else {
            node = nodeList.get(index);
        }
        return node;
    }

    @Override
    public boolean nodeExists(String id) {
        checkId(id);
        return nodeMap.containsKey(id);
    }

    @Override
    public boolean edgeExists(String source, String target) {
        checkId(source);
        checkId(target);
        NodeDraftImpl sourceNode = getNode(source);
        NodeDraftImpl targetNode = getNode(target);
        if (sourceNode != null && targetNode != null) {
            boolean undirected = edgeDefault.equals(EdgeDirectionDefault.UNDIRECTED) ||
                (undirectedEdgesCount > 0 && directedEdgesCount == 0);
            long edgeId = getLongId(sourceNode, targetNode, !undirected);
            for (Long2ObjectMap l : edgeTypeSets) {
                if (l != null) {
                    if (l.containsKey(edgeId)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void addEdge(EdgeDraft edgeDraft) {
        checkElementDraftImpl(edgeDraft);

        EdgeDraftImpl edgeDraftImpl = (EdgeDraftImpl) edgeDraft;
        if (edgeDraftImpl.getSource() == null) {
            String message =
                NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerException_MissingNodeSource");
            report.logIssue(new Issue(message, Level.SEVERE));
            return;
        }
        if (edgeDraftImpl.getTarget() == null) {
            String message =
                NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerException_MissingNodeTarget");
            report.logIssue(new Issue(message, Level.SEVERE));
            return;
        }

        //Check if already exists
        if (edgeMap.containsKey(edgeDraftImpl.getId())) {
            String message = NbBundle
                .getMessage(ImportContainerImpl.class, "ImportContainerException_edgeExist", edgeDraftImpl.getId());
            report.logIssue(new Issue(message, Level.WARNING));
            return;
        }

        //Self loop
        if (edgeDraftImpl.isSelfLoop() && !parameters.isSelfLoops()) {
            String message = NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerException_SelfLoop");
            report.logIssue(new Issue(message, Level.SEVERE));
            return;
        }

        //Check direction and default type
        if (edgeDraftImpl.getDirection() != null) {
            //Test if the given type match with parameters
            switch (edgeDefault) {
                case DIRECTED:
                    if (edgeDraftImpl.getDirection().equals(EdgeDirection.UNDIRECTED)) {
                        report.logIssue(new Issue(NbBundle
                            .getMessage(ImportContainerImpl.class, "ImportContainerException_Bad_Edge_Type",
                                edgeDefault, edgeDraftImpl.getId()), Level.SEVERE));
                        return;
                    }
                    break;
                case UNDIRECTED:
                    if (edgeDraftImpl.getDirection().equals(EdgeDirection.DIRECTED)) {
                        report.logIssue(new Issue(NbBundle
                            .getMessage(ImportContainerImpl.class, "ImportContainerException_Bad_Edge_Type",
                                edgeDefault, edgeDraftImpl.getId()), Level.SEVERE));
                        return;
                    }
                    break;
            }
        }

        //Get index
        int index = edgeList.size();

        //Type
        int edgeType = getEdgeType(edgeDraftImpl.getType());
        long sourceTargetLong = getLongId(edgeDraftImpl);

        ensureLongSetArraySize(edgeType);
        Long2ObjectMap<int[]> edgeTypeSet = edgeTypeSets[edgeType];

        if (edgeTypeSet.containsKey(sourceTargetLong)) {
            if (!parameters.isParallelEdges()) {
                report.logIssue(new Issue(NbBundle
                    .getMessage(ImportContainerImpl.class, "ImportContainerException_Parallel_Edge_Forbidden",
                        edgeDraftImpl.getId()), Level.SEVERE));
                return;
            } else {
                int[] edges = edgeTypeSet.get(sourceTargetLong);
                int[] newEdges = new int[edges.length + 1];
                newEdges[edges.length] = index;
                System.arraycopy(edges, 0, newEdges, 0, edges.length);
                edgeTypeSet.put(sourceTargetLong, newEdges);

                if (!reportedParallelEdges) {
                    report.logIssue(new Issue(NbBundle
                        .getMessage(ImportContainerImpl.class, "ImportContainerException_Parallel_Edge_Merged",
                            edgeDraftImpl.getId()), Level.INFO));
                    reportedParallelEdges = true;
                }
            }
        } else {
            edgeTypeSet.put(sourceTargetLong, new int[] {index});
        }

        //Self loop
        if (edgeDraftImpl.isSelfLoop()) {
            selfLoops++;
        }

        //Direction
        final boolean directed = isEdgeDirected(edgeDraft);

        if (directed) {
            directedEdgesCount++;
        } else {
            undirectedEdgesCount++;
        }

        //Adding
        edgeList.add(edgeDraftImpl);
        edgeMap.put(edgeDraft.getId(), index);
    }

    private void removeNode(NodeDraftImpl node) {
        String id = node.getId();
        if (!nodeMap.containsKey(id)) {
            return;
        }

        int index = nodeMap.removeInt(id);
        nodeList.set(index, null);
    }

    @Override
    public void removeEdge(EdgeDraft edgeDraft) {
        checkElementDraftImpl(edgeDraft);

        EdgeDraftImpl edgeDraftImpl = (EdgeDraftImpl) edgeDraft;
        String id = edgeDraftImpl.getId();

        if (!edgeMap.containsKey(id)) {
            return;
        }

        final boolean directed = isEdgeDirected(edgeDraftImpl);

        if (directed) {
            directedEdgesCount--;
        } else {
            undirectedEdgesCount--;
        }

        if (edgeDraftImpl.isSelfLoop()) {
            selfLoops--;
        }

        int edgeType = getEdgeType(edgeDraftImpl.getType());
        long sourceTargetLong = getLongId(edgeDraftImpl);
        ensureLongSetArraySize(edgeType);
        Long2ObjectMap<int[]> edgeTypeSet = edgeTypeSets[edgeType];

        //Get index
        final int index = edgeMap.removeInt(id);

        //Update edgeType set
        int[] edges = edgeTypeSet.remove(sourceTargetLong);
        if (Arrays.binarySearch(edges, index) >= 0) {
            if (edges.length > 1) {
                int[] newEdges = new int[edges.length - 1];
                int i = 0;
                for (int e : edges) {
                    if (e != index) {
                        newEdges[i++] = e;
                    }
                }
                edgeTypeSet.put(sourceTargetLong, newEdges);
            } else {
                edgeTypeSet.put(sourceTargetLong, new int[0]);
            }
        }

        //Remove edge
        edgeList.set(index, null);
    }

    private boolean isEdgeDirected(EdgeDraft edgeDraft) {
        final EdgeDirection direction = edgeDraft.getDirection();
        return edgeDefault.equals(EdgeDirectionDefault.DIRECTED)
            || (edgeDefault.equals(EdgeDirectionDefault.MIXED) && direction != EdgeDirection.UNDIRECTED);
    }

    @Override
    public boolean edgeExists(String id) {
        checkId(id);

        return edgeMap.containsKey(id);
    }

    @Override
    public EdgeDraft getEdge(String id) {
        checkId(id);

        int index = edgeMap.getInt(id);
        if (index == NULL_INDEX) {
            return null;
        }
        return edgeList.get(index);
    }

    @Override
    public Iterable<NodeDraft> getNodes() {
        return new NullFilterIterable<>(nodeList);
    }

    @Override
    public int getNodeCount() {
        return nodeMap.size();
    }

    @Override
    public Iterable<EdgeDraft> getEdges() {
        return new NullFilterIterable<>(edgeList);
    }

    @Override
    public int getEdgeCount() {
        return edgeMap.size();
    }

    @Override
    public int getMutualEdgeCount() {
        return mutualEdgesCount;
    }

    @Override
    public TimeFormat getTimeFormat() {
        return timeFormat;
    }

    @Override
    public void setTimeFormat(TimeFormat timeFormat) {
        this.timeFormat = timeFormat;
    }

    @Override
    public TimeRepresentation getTimeRepresentation() {
        return timeRepresentation;
    }

    @Override
    public void setTimeRepresentation(TimeRepresentation timeRepresentation) {
        this.timeRepresentation = timeRepresentation;
    }

    @Override
    public DateTimeZone getTimeZone() {
        return timeZone;
    }

    @Override
    public void setTimeZone(DateTimeZone timeZone) {
        this.timeZone = timeZone;
    }

    @Override
    public ColumnDraft addNodeColumn(String key, Class typeClass) {
        if (AttributeUtils.isDynamicType(typeClass)) {
            if (TimeSet.class.isAssignableFrom(typeClass)) {
                return addNodeColumn(key, typeClass, true);
            } else {
                return addNodeColumn(key, AttributeUtils.getStaticType(typeClass), true);
            }
        } else {
            return addNodeColumn(key, typeClass, false);
        }
    }

    @Override
    public ColumnDraft addNodeColumn(String key, Class typeClass, boolean dynamic) {
        key = key.toLowerCase().trim();
        ColumnDraft column = nodeColumns.get(key);
        typeClass = AttributeUtils.getStandardizedType(typeClass);
        if (column == null) {
            int index = nodeColumns.size();
            column = new ColumnDraftImpl(key, index, dynamic, typeClass);
            nodeColumns.put(key, column);
            if (dynamic) {
                report.log(NbBundle
                    .getMessage(ImportContainerImpl.class, "ImportContainerLog.AddDynamicNodeColumn", key,
                        typeClass.getSimpleName()));
            } else {
                report.log(NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerLog.AddNodeColumn", key,
                    typeClass.getSimpleName()));
            }
        } else if (!column.getTypeClass().equals(typeClass)) {
            report.logIssue(new Issue(NbBundle
                .getMessage(ImportContainerImpl.class, "ImportContainerException_Column_Type_Mismatch", key,
                    column.getTypeClass()), Level.SEVERE));
        }
        return column;
    }

    @Override
    public ColumnDraft addEdgeColumn(String key, Class typeClass) {
        if (AttributeUtils.isDynamicType(typeClass)) {
            if (TimeSet.class.isAssignableFrom(typeClass)) {
                return addEdgeColumn(key, typeClass, true);
            } else {
                return addEdgeColumn(key, AttributeUtils.getStaticType(typeClass), true);
            }
        } else {
            return addEdgeColumn(key, typeClass, false);
        }
    }

    @Override
    public ColumnDraft addEdgeColumn(String key, Class typeClass, boolean dynamic) {
        key = key.toLowerCase().trim();
        ColumnDraft column = edgeColumns.get(key);
        typeClass = AttributeUtils.getStandardizedType(typeClass);
        if (column == null) {
            int index = edgeColumns.size();
            column = new ColumnDraftImpl(key, index, dynamic, typeClass);
            edgeColumns.put(key, column);
            if (dynamic) {
                report.log(NbBundle
                    .getMessage(ImportContainerImpl.class, "ImportContainerLog.AddDynamicEdgeColumn", key,
                        typeClass.getSimpleName()));
            } else {
                report.log(NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerLog.AddEdgeColumn", key,
                    typeClass.getSimpleName()));
            }
        } else if (!column.getTypeClass().equals(typeClass)) {
            report.logIssue(new Issue(NbBundle
                .getMessage(ImportContainerImpl.class, "ImportContainerException_Column_Type_Mismatch", key,
                    column.getTypeClass()), Level.SEVERE));
        }
        return column;
    }

    @Override
    public ColumnDraft getNodeColumn(String key) {
        return nodeColumns.get(key.toLowerCase());
    }

    @Override
    public boolean hasNodeColumn(String key) {
        return nodeColumns.containsKey(key.toLowerCase());
    }

    @Override
    public ColumnDraft getEdgeColumn(String key) {
        return edgeColumns.get(key.toLowerCase());
    }

    @Override
    public boolean hasEdgeColumn(String key) {
        return edgeColumns.containsKey(key.toLowerCase());
    }

    @Override
    public Iterable<ColumnDraft> getNodeColumns() {
        return nodeColumns.values();
    }

    @Override
    public Iterable<ColumnDraft> getEdgeColumns() {
        return edgeColumns.values();
    }

    @Override
    public void setInterval(String startDateTime, String endDateTime) {
        try {
            double start, end;
            if (startDateTime == null || startDateTime.trim().isEmpty() || "-inf".equalsIgnoreCase(startDateTime) ||
                "-infinity".equalsIgnoreCase(startDateTime)) {
                start = Double.NEGATIVE_INFINITY;
            } else {
                start = timeFormat.equals(TimeFormat.DOUBLE) ? Double.parseDouble(startDateTime) :
                    AttributeUtils.parseDateTime(startDateTime, getTimeZone());
            }
            if (endDateTime == null || endDateTime.trim().isEmpty() || "inf".equalsIgnoreCase(endDateTime) ||
                "infinity".equalsIgnoreCase(endDateTime)) {
                end = Double.POSITIVE_INFINITY;
            } else {
                end = timeFormat.equals(TimeFormat.DOUBLE) ? Double.parseDouble(endDateTime) :
                    AttributeUtils.parseDateTime(endDateTime, getTimeZone());
            }
            this.interval = new Interval(start, end);
        } catch (Exception e) {
            report.logIssue(new Issue(NbBundle
                .getMessage(ImportContainerImpl.class, "ImportContainerException_Interval_Parse_Error",
                    "[" + startDateTime + "," + endDateTime + "]"), Level.SEVERE));
            return;
        }
        report.log(NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerLog.GraphInterval",
            "[" + startDateTime + "," + endDateTime + "]"));
    }

    @Override
    public Interval getInterval() {
        return interval;
    }

    @Override
    public Double getTimestamp() {
        return timestamp;
    }

    @Override
    public void setTimestamp(String timestamp) {
        try {
            double t = timeFormat.equals(TimeFormat.DOUBLE) ? Double.parseDouble(timestamp) :
                AttributeUtils.parseDateTime(timestamp, getTimeZone());
            this.timestamp = t;
        } catch (Exception e) {
            report.logIssue(new Issue(NbBundle
                .getMessage(ImportContainerImpl.class, "ImportContainerException_Timestamp_Parse_Error", timestamp),
                Level.SEVERE));
            return;
        }
        report.log(NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerLog.GraphTimestamp", timestamp));
    }

    @Override
    public ElementIdType getElementIdType() {
        return elementIdType;
    }

    @Override
    public void setElementIdType(ElementIdType type) {
        if (this.elementIdType != type) {
            this.elementIdType = type;
            report.log(NbBundle
                .getMessage(ImportContainerImpl.class, "ImportContainerLog.ElementIdType", elementIdType.toString()));
        }
    }

    @Override
    public boolean verify() {
        //Edge weight zero or negative
        for (EdgeDraftImpl edge : new NullFilterIterable<EdgeDraftImpl>(edgeList)) {
            String id = edge.getId();
            if (edge.getWeight() < 0f) {
                report.logIssue(new Issue(
                    NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerException_Negative_Weight", id),
                    Level.WARNING));
            } else if (edge.getWeight() == 0) {
                removeEdge(edge);
                report.logIssue(new Issue(
                    NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerException_Weight_Zero_Ignored", id),
                    Level.SEVERE));
            }
        }

        //Graph EdgeDefault
        if (directedEdgesCount > 0 && undirectedEdgesCount == 0) {
            setEdgeDefault(EdgeDirectionDefault.DIRECTED);
        } else if (directedEdgesCount == 0 && undirectedEdgesCount > 0) {
            setEdgeDefault(EdgeDirectionDefault.UNDIRECTED);
        } else if (directedEdgesCount > 0 && undirectedEdgesCount > 0) {
            setEdgeDefault(EdgeDirectionDefault.MIXED);
        }

        //Count mutual edges
        if (directedEdgesCount > 0) {
            for (EdgeDraftImpl edge : new NullFilterIterable<EdgeDraftImpl>(edgeList)) {
                if (isEdgeDirected(edge) && getOpposite(edge) != null) {
                    mutualEdgesCount++;
                }
            }
            mutualEdgesCount /= 2;
        }

        //IdType
        if (elementIdType.equals(ElementIdType.INTEGER) || elementIdType.equals(ElementIdType.LONG)) {
            try {
                for (NodeDraftImpl node : nodeList) {
                    if (elementIdType.equals(ElementIdType.INTEGER)) {
                        Integer.parseInt(node.getId());
                    } else if (elementIdType.equals(ElementIdType.LONG)) {
                        Long.parseLong(node.getId());
                    }
                }
                for (EdgeDraftImpl edge : new NullFilterIterable<EdgeDraftImpl>(edgeList)) {
                    if (elementIdType.equals(ElementIdType.INTEGER)) {
                        Integer.parseInt(edge.getId());
                    } else if (elementIdType.equals(ElementIdType.LONG)) {
                        Long.parseLong(edge.getId());
                    }
                }
            } catch (NumberFormatException e) {
                report.logIssue(new Issue(NbBundle
                    .getMessage(ImportContainerImpl.class, "ImportContainerException_ElementIdType_Parse_Error",
                        elementIdType), Level.WARNING));
                elementIdType = ElementIdType.STRING;
            }
        }

        //Is dynamic graph
        for (NodeDraftImpl node : nodeList) {
            if (node != null) {
                if (node.isDynamic()) {
                    dynamicGraph = true;
                }
                if (node.hasDynamicAttributes()) {
                    dynamicAttributes = true;
                }
            }
        }
        for (EdgeDraftImpl edge : new NullFilterIterable<EdgeDraftImpl>(edgeList)) {
            if (edge.isDynamic()) {
                dynamicGraph = true;
            }
            if (edge.hasDynamicAttributes()) {
                dynamicAttributes = true;
            }
        }

        //Print time interval values to report
//        if (timeIntervalMin != null || timeIntervalMax != null) {
//            if (timeFormat.equals(TimeFormat.DATE) || timeFormat.equals(TimeFormat.DATETIME)) {
//                try {
//                    String message = "[" + (timeIntervalMin != null ? DynamicUtilities.getXMLDateStringFromDouble(timeIntervalMin) : "-inf") + ",";
//                    message += (timeIntervalMax != null ? DynamicUtilities.getXMLDateStringFromDouble(timeIntervalMax) : "+inf") + "]";
//                    report.log(NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerLog.TimeInterval", message));
//                } catch (Exception e) {
//                }
//            } else {
//                String message = "[" + (timeIntervalMin != null ? timeIntervalMin.toString() : "-inf") + ",";
//                message += (timeIntervalMax != null ? timeIntervalMax.toString() : "+inf") + "]";
//                report.log(NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerLog.TimeInterval", message));
//            }
//        }
//
        //Print TimeFormat
        if (dynamicGraph) {
            report.log(
                NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerLog.TimeFormat", timeFormat.toString()));
            report.log(NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerLog.TimeRepresentation",
                timeRepresentation.toString()));
            report.log(
                NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerLog.TimeZone", timeZone.toString()));
        }

        //Print edge label type
        if (lastEdgeType != null) {
            report.log(NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerLog.EdgeLabelType",
                lastEdgeType.getSimpleName()));
        }

        //Print edge types
        if (isMultiGraph()) {
            report.log(NbBundle
                .getMessage(ImportContainerImpl.class, "ImportContainerLog.MultiGraphCount", edgeTypeMap.size() - 1));
        }

        //Check that not all alpha are zeros
        checkColorAlpha(nodeList, "Node");
        checkColorAlpha(edgeList, "Edge");

        return true;
    }

    @Override
    public void closeLoader() {
        //Remove self-loops
        if (!parameters.isSelfLoops() && selfLoops > 0) {
            List<EdgeDraftImpl> l = new ArrayList<>();
            for (EdgeDraftImpl e : edgeList) {
                if (e != null && e.isSelfLoop()) {
                    l.add(e);
                }
            }
            for (EdgeDraftImpl e : l) {
                removeEdge(e);
            }

            report.logIssue(new Issue(NbBundle.getMessage(
                ImportContainerImpl.class, "ImportContainerClose_SelfLoopRemoved", l.size()
            ), Level.WARNING));
        }

        if (directedEdgesCount > 0 && edgeDefault.equals(EdgeDirectionDefault.UNDIRECTED)) {
            int mutualEdgesRemoved = 0;

            //Force undirected
            for (EdgeDraftImpl edge : edgeList.toArray(new EdgeDraftImpl[0])) {
                final boolean notAlreadyRemoved = edge != null
                    && edgeMap.containsKey(edge.getId());

                if (notAlreadyRemoved && !edge.isSelfLoop() && edge.getDirection() != null && edge.getDirection().equals(EdgeDirection.DIRECTED)) {
                    EdgeDraftImpl opposite = getOpposite(edge);
                    if (opposite != null && edgeMap.containsKey(opposite.getId())) {
                        mergeDirectedEdges(opposite, edge);
                        removeEdge(opposite);
                        mutualEdgesRemoved++;
                    }
                }
            }

            report.logIssue(new Issue(NbBundle.getMessage(
                ImportContainerImpl.class, "ImportContainerClose_MutualEdgesRemoved", mutualEdgesRemoved
            ), Level.WARNING));
        }
        //TODO check when mixed is forced

        //Clean autoNode
        if (!allowAutoNode()) {
            for (NodeDraftImpl node : nodeList) {
                if (node != null && node.isCreatedAuto()) {
                    removeNode(node);
                }
            }
            for (EdgeDraftImpl edge : edgeList) {
                if (edge != null && (edge.getSource().isCreatedAuto() || edge.getTarget().isCreatedAuto())) {
                    removeEdge(edge);
                }
            }
        }

        //Sort nodes by height
        if (parameters.isSortNodesBySize()) {
            Collections.sort(nodeList, new Comparator<NodeDraftImpl>() {
                @Override
                public int compare(NodeDraftImpl o1, NodeDraftImpl o2) {
                    return new Float(o2 != null ? o2.getSize() : 0f).compareTo(o1 != null ? o1.getSize() : 0f);
                }
            });
        }

        //Set id as label for nodes that miss label
        if (parameters.isFillLabelWithId()) {
            for (NodeDraftImpl node : nodeList) {
                if (node != null && node.getLabel() == null) {
                    node.setLabel(node.getId());
                }
            }
        }

        //Set random position
        boolean customPosition = false;
        for (NodeDraftImpl node : nodeList) {
            if (node != null) {
                if (Float.isNaN(node.getX())) {
                    node.setX(0);
                }
                if (Float.isNaN(node.getY())) {
                    node.setY(0);
                }
                if (Float.isNaN(node.getZ())) {
                    node.setZ(0);
                }
                if (node.getX() != 0f || node.getY() != 0f) {
                    customPosition = true;
                }
            }
        }
        if (!customPosition) {
            for (NodeDraftImpl node : nodeList) {
                if (node != null) {
                    node.setX((float) ((0.01 + Math.random()) * 1000) - 500);
                    node.setY((float) ((0.01 + Math.random()) * 1000) - 500);
                }
            }
        }

        //MANAGEMENT
    }

    protected void mergeDirectedEdges(EdgeDraftImpl source, EdgeDraftImpl dest) {
        EdgeMergeStrategy mergeStrategy = parameters.getEdgesMergeStrategy();
        double result = dest.getWeight();
        switch (mergeStrategy) {
            case AVG:
                result = (source.getWeight() + dest.getWeight()) / 2.0;
                break;
            case MAX:
                result = Math.max(source.getWeight(), dest.getWeight());
                break;
            case MIN:
                result = Math.min(source.getWeight(), dest.getWeight());
                break;
            case SUM:
                result = source.getWeight() + dest.getWeight();
                break;
            case FIRST:
                result = dest.getWeight();
                break;
            case LAST:
                result = source.getWeight();
                break;
            default:
                break;
        }
        dest.setWeight(result);
    }

    @Override
    public boolean isDynamicGraph() {
        return dynamicGraph;
    }

    @Override
    public boolean hasDynamicAttributes() {
        return dynamicAttributes;
    }

    //REPORT
    @Override
    public Report getReport() {
        return report;
    }

    @Override
    public void setReport(Report report) {
        this.report = report;
    }

    //PARAMS GETTER
    @Override
    public boolean allowAutoNode() {
        return parameters.isAutoNode();
    }

    @Override
    public boolean allowParallelEdges() {
        return parameters.isParallelEdges();
    }

    @Override
    public boolean allowSelfLoop() {
        return parameters.isSelfLoops();
    }

    @Override
    public boolean isFillLabelWithId() {
        return parameters.isFillLabelWithId();
    }

    @Override
    public void setFillLabelWithId(boolean value) {
        parameters.setFillLabelWithId(value);
    }

    @Override
    public EdgeMergeStrategy getEdgesMergeStrategy() {
        return parameters.getEdgesMergeStrategy();
    }

    @Override
    public void setEdgesMergeStrategy(EdgeMergeStrategy edgesMergeStrategy) {
        parameters.setEdgesMergeStrategy(edgesMergeStrategy);
    }

    @Override
    public EdgeDirectionDefault getEdgeDefault() {
        return edgeDefault;
    }

    @Override
    public void setEdgeDefault(EdgeDirectionDefault edgeDefault) {
        if (this.edgeDefault != edgeDefault) {
            this.edgeDefault = edgeDefault;
            report.log(NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerException_Set_EdgeDefault",
                edgeDefault.toString()));
        }
    }

    @Override
    public boolean isMultiGraph() {
        return edgeTypeMap.size() > 1;
    }

    @Override
    public boolean hasSelfLoops() {
        return selfLoops > 0;
    }

    //PARAMS
    @Override
    public void setAllowAutoNode(boolean value) {
        parameters.setAutoNode(value);
    }

    @Override
    public void setAllowParallelEdge(boolean value) {
        parameters.setParallelEdges(value);
    }

    @Override
    public void setAllowSelfLoop(boolean value) {
        parameters.setSelfLoops(value);
    }

    @Override
    public boolean isAutoScale() {
        return parameters.isAutoScale();
    }

    @Override
    public void setAutoScale(boolean autoscale) {
        parameters.setAutoScale(autoscale);
    }

    @Override
    public Class getEdgeTypeLabelClass() {
        return lastEdgeType;
    }

    @Override
    public boolean containsAutoNodes() {
        return reportedUnknownNode;
    }

    //Utility
    private int getEdgeType(Object type) {
        //Verify
        if (type != null) {
            Class cl = type.getClass();
            if (!(cl.equals(Integer.class)
                || cl.equals(String.class)
                || cl.equals(Float.class)
                || cl.equals(Double.class)
                || cl.equals(Short.class)
                || cl.equals(Byte.class)
                || cl.equals(Long.class)
                || cl.equals(Character.class)
                || cl.equals(Boolean.class))) {
                report.logIssue(new Issue(
                    NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerException_Unsupported_Edge_type"),
                    Level.SEVERE));
                type = null;
            }
            if (type != null && lastEdgeType != null && !lastEdgeType.equals(type.getClass())) {
                report.logIssue(new Issue(NbBundle
                    .getMessage(ImportContainerImpl.class, "ImportContainerException_Unsupported_Edge_type_Conflict",
                        type.getClass().getSimpleName(), lastEdgeType.getSimpleName()), Level.SEVERE));
                type = null;
            }
        }

        if (type != null) {
            lastEdgeType = type.getClass();
        }

        if (edgeTypeMap.containsKey(type)) {
            return edgeTypeMap.getInt(type);
        }
        int id = edgeTypeMap.size();
        edgeTypeMap.put(type, id);
        return id;
    }

    private void ensureLongSetArraySize(int type) {
        if (edgeTypeSets.length <= type) {
            Long2ObjectMap[] l = new Long2ObjectMap[type + 1];
            System.arraycopy(edgeTypeSets, 0, l, 0, edgeTypeSets.length);
            edgeTypeSets = l;
            edgeTypeSets[type] = new Long2ObjectOpenHashMap<>();
        }
    }

    private long getLongId(EdgeDraftImpl edge) {
        final boolean directed = isEdgeDirected(edge);
        return getLongId(edge.getSource(), edge.getTarget(), directed);
    }

    private long getLongId(NodeDraftImpl source, NodeDraftImpl target, boolean directed) {
        if (directed) {
            long edgeId = ((long) source.getSequentialId()) << 32;
            edgeId = edgeId | (long) (target.getSequentialId());
            return edgeId;
        } else {
            long edgeId =
                ((long) (Math.max(source.getSequentialId(), target.getSequentialId()))) << 32;
            edgeId = edgeId | (long) (Math.min(source.getSequentialId(), target.getSequentialId()));
            return edgeId;
        }
    }

    private EdgeDraftImpl getOpposite(EdgeDraftImpl edge) {
        Long2ObjectMap<int[]> typeSet = edgeTypeSets[getEdgeType(edge.getType())];
        long longId = getLongId(edge.getTarget(), edge.getSource(), true);
        int[] opposites = typeSet.get(longId);
        if (opposites != null && opposites.length > 0) {
            return edgeList.get(opposites[0]);
        }
        return null;
    }

    private void checkColorAlpha(ObjectList<? extends ElementDraft> objectList, String elementType) {
        if (!objectList.isEmpty()) {
            int validElement = 0;
            int withAlphaZero = 0;
            for (ElementDraft element : objectList) {
                if (element != null && element.getColor() != null) {
                    validElement++;
                    withAlphaZero += element.getColor().getAlpha() == 0 ? 1 : 0;
                }
            }
            if (validElement > 0 && validElement == withAlphaZero) {
                report.logIssue(new Issue(
                    NbBundle.getMessage(ImportContainerImpl.class,
                        "ImportContainerException_" + elementType + "_Color_Alpha_AllZero"),
                    Level.WARNING));
            }
        }
    }

    private void checkElementDraftImpl(ElementDraft elmt) {
        if (elmt == null) {
            throw new NullPointerException();
        }
        if (!(elmt instanceof ElementDraftImpl)) {
            throw new ClassCastException();
        }
    }

    private void checkId(String id) {
        if (id == null) {
            throw new NullPointerException();
        }
        if (id.isEmpty()) {
            throw new IllegalArgumentException("The id can't be empty");
        }
    }

    //UTILITY ITERATOR
    private static class NullFilterIterable<T extends ElementDraft> implements Iterable<T> {

        private final Collection<T> collection;

        public NullFilterIterable(Collection elementCollection) {
            this.collection = elementCollection;
        }

        @Override
        public Iterator<T> iterator() {
            return new NullFilterIterator<>(collection);
        }
    }

    private static class NullFilterIterator<T extends ElementDraft> implements Iterator<T> {

        private final Iterator<T> itr;
        private T pointer;

        public NullFilterIterator(Collection<T> elementCollection) {
            this.itr = elementCollection.iterator();
        }

        @Override
        public boolean hasNext() {
            while (itr.hasNext()) {
                pointer = itr.next();
                if (pointer != null) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public T next() {
            return pointer;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported.");
        }
    }
}
