/* 
 * Copyright 2011 Heuristic Labs, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *   
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ros.rosjava.tf.tools.viz;

import org.ros.node.DefaultNodeFactory;
import org.ros.node.Node;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMain;
import org.ros.node.NodeRunner;
import org.ros.rosjava.tf.TransformBuffer;
import org.ros.rosjava.tf.pubsub.TransformListener;

import com.google.common.base.Preconditions;
import com.touchgraph.graphlayout.Edge;

import java.awt.Dimension;
import java.net.URI;
import java.util.HashMap;

import javax.swing.JFrame;


import org.jgrapht.Graph;
import org.jgrapht.event.GraphEdgeChangeEvent;
import org.jgrapht.event.GraphListener;
import org.jgrapht.event.GraphVertexChangeEvent;
import org.jgrapht.experimental.touchgraph.TouchgraphPanel;
import org.jgrapht.graph.SimpleDirectedGraph;

import org.jgrapht.graph.DefaultEdge;

	/**
	 * A real-time, interactive graph visualizer for rosjava_tf
	 *
	 * @author nick@heuristiclabs.com (Nick Armstrong-Crews)
	 *
	 * @since Sep 5, 2011
	 */
	public class TfViz implements NodeMain, GraphListener<String,TransformBuffer> {		

		protected static String laptopMasterUri = "http://192.168.43.171:11311";
		
	    public static void main(String [] args) {
			try {
				NodeRunner nodeRunner = NodeRunner.newDefault();
				URI masterUri = new URI(laptopMasterUri);
				NodeConfiguration nodeConfiguration =
					NodeConfiguration.newPublic("localhost", masterUri);
				nodeRunner.run(new TfViz(), nodeConfiguration);
			} catch(Exception e) {
				e.printStackTrace();
			}			
	    }

	    private Node node;
	    private TransformListener tfl;
	    	  
	    private final JFrame frame;
	    	  
	    protected TouchgraphPanel<String, DefaultEdge> tgp;
	    protected final Graph<String,DefaultEdge> g;
	    //protected final HashMap<String,Edge> edges;
	    protected final HashMap<String,com.touchgraph.graphlayout.Node> vertices;
	    	  
	    public TfViz() {
	    	frame = new JFrame();
	    	g = new SimpleDirectedGraph<String,DefaultEdge>(DefaultEdge.class);
	    	g.addVertex("NONE");
	    	tgp = new TouchgraphPanel<String, DefaultEdge>(g, false);
	    	//edges = new HashMap<String,Edge>();
	    	vertices = new HashMap<String,com.touchgraph.graphlayout.Node>();
	    }
	    
	    @Override
	    public void main(NodeConfiguration configuration) {
	    	Preconditions.checkState(node == null);
	    	Preconditions.checkNotNull(configuration);
	    	try {
	    		node = new DefaultNodeFactory().newNode("tfviz", configuration);
	    		tfl = new TransformListener(node);
	    		tfl.addListener(this);

	    		frame.getContentPane().add(tgp);
		        
	    		frame.setPreferredSize(new Dimension(800, 800));
				frame.setSize(600,600);
		        frame.setTitle("tfviz");
		        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		        frame.addWindowListener(new WindowAdapter() {
//		            public void windowClosing(WindowEvent e) {this.shutdown(); System.exit(0);}
//		        });
		        frame.pack();
		        frame.setVisible(true);
		        
	    	} catch (Exception e) {
	    		if (node != null) {
	    			node.getLog().fatal(e);
	    	    } else {
	    	    	e.printStackTrace();
	    	    }
	    	}
	    }

	    @Override
	    public void shutdown() {
	    	node.shutdown();
	    	node = null;
	    	frame.dispose();
	    }

	    @Override
	    public void edgeAdded(GraphEdgeChangeEvent<String, TransformBuffer> e) {
	    	try {
	    		if(vertices.size() == 0)
	    			tgp.getTGPanel().deleteNodeById("NONE");
					TransformBuffer txBuff = e.getEdge();
					com.touchgraph.graphlayout.Node parentVizNode;
					com.touchgraph.graphlayout.Node childVizNode;
					if(!vertices.containsKey(txBuff.parentFrame))
						parentVizNode = new com.touchgraph.graphlayout.Node(txBuff.parentFrame);
					else parentVizNode = vertices.get(txBuff.parentFrame);
					if(!vertices.containsKey(txBuff.childFrame))
						childVizNode = new com.touchgraph.graphlayout.Node(txBuff.childFrame);
					else childVizNode = vertices.get(txBuff.childFrame);
					Edge vizEdge = new Edge(parentVizNode, childVizNode, 1);
					tgp.getTGPanel().addEdge(vizEdge);
					tgp.getTGPanel().validate();
	    	} catch(Exception exp) {exp.printStackTrace();}
	    }

	    @Override
	    public void edgeRemoved(GraphEdgeChangeEvent<String, TransformBuffer> e) {} // we never remove edges

	    @Override
	    public void vertexAdded(GraphVertexChangeEvent<String> e) {
	    	try {
	    		if(vertices.size() == 0)
	    			tgp.getTGPanel().deleteNodeById("NONE");
	    		String frame = e.getVertex();
	    		com.touchgraph.graphlayout.Node vertex = new com.touchgraph.graphlayout.Node(frame);
	    		tgp.getTGPanel().addNode(vertex);
	    		tgp.getTGPanel().validate();
	    		vertices.put(frame, vertex);
			} catch(Exception exp) {exp.printStackTrace();}
	    }

	    @Override
	    public void vertexRemoved(GraphVertexChangeEvent<String> e) {}  // we never remove nodes
	    
}
