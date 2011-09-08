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

package org.ros.rosjava.android.tf.android_tf_tools;

import org.ros.message.MessageListener;
import org.ros.message.tf.tfMessage;
import org.ros.node.DefaultNodeFactory;
import org.ros.node.Node;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMain;
import org.ros.rosjava.android.views.RosTextView;
import org.ros.rosjava.tf.StampedTransform;
import org.ros.rosjava.tf.TransformFactory;
import org.ros.rosjava.tf.TransformTree;

import com.google.common.base.Preconditions;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * @author nick@heuristiclabs.com (Nick Armstrong-Crews)
 */
public class RosTfView extends TextView implements NodeMain /*extends RosTextView<tfMessage>*/ {

	protected TransformTree tfTree;
	protected Node node;
	
	  public RosTfView(Context context) {
	    super(context);
	  }

	  public RosTfView(Context context, AttributeSet attrs) {
	    super(context, attrs);
	  }

	  public RosTfView(Context context, AttributeSet attrs, int defStyle) {
	    super(context, attrs, defStyle);
	  }
	  
	  @Override
	  public void main(NodeConfiguration nodeConfiguration) {

		    Preconditions.checkState(node == null);
		    node = new DefaultNodeFactory().newNode("android/tf_view", nodeConfiguration);
		    node.newSubscriber("/tf", "tf/tfMessage", new MessageListener<tfMessage>() {
		      @Override
		      public void onNewMessage(final tfMessage msg) {
		          post(new Runnable() {
		            @Override
		            public void run() {
		              String s = "";
		        	  for( org.ros.message.geometry_msgs.TransformStamped tx :  msg.transforms ) {
		        		  s += tx.header.stamp + ": " + tx.header.frame_id + "->" + tx.child_frame_id;
		        		  //s += TransformFactory.msg2transform(tx).toString();
		        	  }
		              setText(s);
		            }
		          });
		        postInvalidate();
		      }
		    });

	  }
	  
	    @Override
	    public void shutdown() {
	        Preconditions.checkNotNull(node);
	    	node.shutdown();
	    	node = null;
	    }

}
