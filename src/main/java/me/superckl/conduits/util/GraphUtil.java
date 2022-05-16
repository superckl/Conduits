package me.superckl.conduits.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.Graph;
import com.google.common.graph.MutableGraph;
import com.google.common.graph.Traverser;

public class GraphUtil {

	/**
	 * Merge all nodes and edges of two graphs.
	 *
	 * @param graph1 A {@link MutableGraph} into which all nodes and edges of {@literal graph2} will be merged
	 * @param graph2 The {@link Graph} whose nodes and edges will be merged into {@literal graph1}
	 * @param <N>    The class of the nodes
	 */
	public static <N> void merge(final MutableGraph<N> graph1, final Graph<N> graph2) {
		for (final N node : graph2.nodes())
			graph1.addNode(node);
		for (final EndpointPair<N> edge : graph2.edges())
			graph1.putEdge(edge.nodeU(), edge.nodeV());
	}

	public static <T> List<Set<T>> floodFill(final Graph<T> graph){
		if(graph.nodes().size() == 0)
			return Collections.emptyList();
		final Set<T> nodes = new HashSet<>(graph.nodes());
		final List<Set<T>> coloredNodes = new ArrayList<>();
		while(!nodes.isEmpty()) {
			final T node = ConduitUtil.removeOne(nodes);
			final Iterable<T> dfs = Traverser.forGraph(graph).depthFirstPreOrder(node);
			final Set<T> connectedNodes = ImmutableSet.copyOf(dfs);
			nodes.removeAll(connectedNodes);
			coloredNodes.add(connectedNodes);
		}
		return coloredNodes;
	}

	public static <T> boolean isConnected(final Graph<T> graph) {
		final Set<T> nodes = graph.nodes();
		if(nodes.size() == 0)
			return true;
		return Iterables.size(Traverser.forGraph(graph).depthFirstPreOrder(ConduitUtil.getOne(nodes))) == nodes.size();
	}

	public static <T> void removeConnectedEdges(final MutableGraph<T> graph, final T node) {
		graph.incidentEdges(node).forEach(graph::removeEdge);
	}

}
