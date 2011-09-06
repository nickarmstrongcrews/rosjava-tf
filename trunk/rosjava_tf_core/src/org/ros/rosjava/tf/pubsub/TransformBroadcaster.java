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

package org.ros.rosjava.tf.pubsub;

import java.util.ArrayList;

import com.google.common.base.Preconditions;

import org.ros.message.geometry_msgs.TransformStamped;
import org.ros.message.tf.tfMessage;
import org.ros.node.Node;
import org.ros.node.topic.Publisher;

/**
 * @author nick@heuristiclabs.com (Nick Armstrong-Crews)
 * @brief This is a simple class to provide sendTransform() (akin to rospy and roscpp versions); it handles creation of publisher and advertising for you.
 */

public class TransformBroadcaster {

	protected final Node node;
	protected Publisher<tfMessage> pub;
	
	public TransformBroadcaster(Node node) {
		this.node = node;
		advertise();
	}
	
	protected void advertise() {
		Preconditions.checkNotNull(node);
		this.pub = node.newPublisher("/tf", "tf/tfMessage");
		this.pub.setLatchMode(true);
		node.getLog().debug("TransformBroadcaster advertised on /tf.");
	}

	public void sendTransform(
									String parentFrame, String childFrame,
									long t, // in nanoseconds
									double v_x, double v_y, double v_z,
									double q_x, double q_y, double q_z, double q_w // quaternion
									) {
		
		// WARN if quaternion not normalized, and normalize it
		// WARN if time is in the future, or otherwise looks funky (negative? more than a year old?)
		Preconditions.checkNotNull(node);

//		Vector3d v = new Vector3d(v_x, v_y, v_z);
//		Quat4d q = new Quat4d(q_w, q_x, q_y, q_z);
//		StampedTransform tx = new StampedTransform(t, parentFrame, childFrame,);
//		org.ros.message.geometry_msgs.TransformStamped txMsg = TransformFactory.tx2msg(tx);
		
		org.ros.message.geometry_msgs.TransformStamped txMsg = new org.ros.message.geometry_msgs.TransformStamped();

		txMsg.header.stamp = org.ros.message.Time.fromNano(t);
		txMsg.header.frame_id = parentFrame;
		txMsg.child_frame_id = childFrame;
		// TODO: invert transform, if it is not cool (have to add tfTree here, then...)
		
		txMsg.transform.translation = new org.ros.message.geometry_msgs.Vector3();
		txMsg.transform.translation.x = v_x;
		txMsg.transform.translation.y = v_y;
		txMsg.transform.translation.z = v_z;

		txMsg.transform.rotation = new org.ros.message.geometry_msgs.Quaternion();
		txMsg.transform.rotation.x = q_x;
		txMsg.transform.rotation.y = q_y;
		txMsg.transform.rotation.z = q_z;
		txMsg.transform.rotation.w = q_w;

		tfMessage msg = new tfMessage();
		msg.transforms = new ArrayList<TransformStamped>(1);
		msg.transforms.add(txMsg);

		this.pub.publish(msg);
	}

}
