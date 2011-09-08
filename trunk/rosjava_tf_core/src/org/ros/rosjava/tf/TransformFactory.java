package org.ros.rosjava.tf;
import java.util.ArrayList;
import java.util.Collection;

import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;

import org.ros.message.tf.tfMessage;
import org.ros.rosjava.tf.StampedTransform;

public class TransformFactory {
	
	public static Collection<StampedTransform> fromTfMessage(tfMessage msg) {
		ArrayList<StampedTransform> transforms = new ArrayList<StampedTransform>(msg.transforms.size());
		for(int i = 0; i < msg.transforms.size(); i++) {
			transforms.add(TransformFactory.msg2transform(msg.transforms.get(i)));
		}
		return transforms;
	}

	public static StampedTransform msg2transform(org.ros.message.geometry_msgs.TransformStamped msg) {
		return new StampedTransform(
							msg.header.stamp.totalNsecs(),
							msg.header.frame_id,
							msg.child_frame_id,
							msg2vector(msg.transform.translation),
							msg2quaternion(msg.transform.rotation)
						);
	}
	
	public static Quat4d msg2quaternion(org.ros.message.geometry_msgs.Quaternion q) {
		return new Quat4d(q.x, q.y, q.z, q.w);
	}

	public static Vector3d msg2vector(org.ros.message.geometry_msgs.Vector3 v) {
		return new Vector3d(v.x, v.y, v.z);
	}

	public static org.ros.message.geometry_msgs.TransformStamped tx2msg(StampedTransform tx) {
		org.ros.message.geometry_msgs.TransformStamped msg = new org.ros.message.geometry_msgs.TransformStamped();
		msg.header.frame_id = tx.parentFrame;
		msg.child_frame_id = tx.childFrame;
		msg.header.stamp = org.ros.message.Time.fromNano(tx.timestamp);
		msg.transform = new org.ros.message.geometry_msgs.Transform();
		msg.transform.translation = vector2msg(tx.translation);
		msg.transform.rotation = quaternion2msg(tx.rotation);
		return msg;
	}
	
	public static org.ros.message.geometry_msgs.Vector3 vector2msg(Vector3d v) {
		org.ros.message.geometry_msgs.Vector3 msg = new org.ros.message.geometry_msgs.Vector3();
		msg.x = v.x;
		msg.y = v.y;
		msg.z = v.z;
		return msg;
	}
	
	public static org.ros.message.geometry_msgs.Quaternion quaternion2msg(Quat4d q) {
		org.ros.message.geometry_msgs.Quaternion msg = new org.ros.message.geometry_msgs.Quaternion();
		msg.x = q.x;
		msg.y = q.y;
		msg.z = q.z;
		msg.w = q.w;
		return msg;
	}
	
}
