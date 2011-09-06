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

package org.ros.rosjava.android.tf.demo;

import java.net.URI;

import android.app.Activity;
import android.hardware.SensorManager;
import android.os.Bundle;

import org.ros.message.tf.tfMessage;
import org.ros.node.DefaultNodeFactory;
import org.ros.node.Node;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeRunner;
import org.ros.rosjava.android.MessageCallable;
import org.ros.rosjava.android.OrientationPublisher;
import org.ros.rosjava.android.views.RosTextView;
import org.ros.rosjava.android.tf.android_tf_tools.OrientationPublisherTf;
import org.ros.rosjava.android.tf.android_tf_tools.R;
import org.ros.rosjava.android.tf.android_tf_tools.R.id;
import org.ros.rosjava.android.tf.android_tf_tools.R.layout;
import org.ros.rosjava.tf.StampedTransform;
import org.ros.rosjava.tf.Transform;
import org.ros.rosjava.tf.TransformTree;
import org.ros.rosjava.tf.adt.TransformFactory;
import org.ros.rosjava.tf.pubsub.TransformListener;

/**
 * @author nick@heuristiclabs.com (Nick Armstrong-Crews)
 */
public class MainActivity extends Activity {

  protected final NodeRunner nodeRunner;
  
  protected RosTextView<org.ros.message.tf.tfMessage> tfPrinter;
  protected OrientationPublisherTf oPubTf;
  
  protected TransformTree tfTree;
  
  public MainActivity() {
	super();
    nodeRunner = NodeRunner.newDefault();
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    /*
    tfPrinter = (RosTfView) findViewById(R.id.text);
 	*/
    tfPrinter = (RosTextView<org.ros.message.tf.tfMessage>) findViewById(R.id.text);
    tfPrinter.setTopicName("/tf");
    tfPrinter.setMessageType("tf/tfMessage");
    tfPrinter
        .setMessageToStringCallable(new MessageCallable<String, org.ros.message.tf.tfMessage>() {
          @Override
          public String call(org.ros.message.tf.tfMessage msg) {
        	  String s = "";
        	  //String s = "{ ";
        	  for( org.ros.message.geometry_msgs.TransformStamped tx :  msg.transforms ) {
        		  tfTree.add(TransformFactory.fromTfMessage(msg));
        		  //s += tx.header.stamp + ": " + tx.header.frame_id + "->" + tx.child_frame_id;
        		  //s += "\n" + TransformFactory.fromTfMessage(msg).size() + "\n";
        		  //s += tx.toString();
        		  //Transform tx2 = tfTree.lookupTransformBetween(tx.header.frame_id, tx.child_frame_id, tx.header.stamp.totalNsecs());
        		  //Transform tx2 = tfTree.lookupTransformBetween(tx.header.frame_id, tx.child_frame_id, tx.header.stamp.totalNsecs() - 200000000l);
        		  //Transform tx2 = tfTree.lookupTransformBetween("/world", "/robot", tx.header.stamp.totalNsecs() - 200000000l);
//        		  if(tfTree.canTransform("/world", "/odom", tx.header.stamp.totalNsecs())) s += "yes!\n";
//        		  else s += "no!\n";        		  
        		  //if(tfTree.canTransform("/world", "/robot")) s += "yes!\n";
        		  //else s += "no!\n";
        	  }
    		  if(tfTree.canTransform("/world", "/phone_oriented")) {
    			  Transform tx2 = tfTree.lookupMostRecent("/world", "/phone_oriented");
    			  s += tx2.toString();
//    			  s += "OK!\n";
    		  } else {
    			  s += "not OK :(\n";
    		  }
        	  //s += "\n}";
            return s;
          }
        });
  }
  
  @Override
  protected void onPause() {
    super.onPause();
    tfPrinter.shutdown();
    oPubTf.shutdown();
  }
    
  @Override
  protected void onResume() {
    super.onResume();
    try {
      URI masterUri = new URI("http://192.168.43.171:11311");
      NodeConfiguration nodeConfiguration =
//    	  NodeConfiguration.newPublic(InetAddressFactory.newNonLoopback().getHostName(), masterUri);
	  NodeConfiguration.newPublic("192.168.43.1", masterUri);
//      nodeConfiguration.setMasterUri(new URI("http://192.168.1.227:11311"));
      tfTree = new TransformTree();
      nodeRunner.run(tfPrinter, nodeConfiguration);
//      Node node = new DefaultNodeFactory().newNode("android/tfl", nodeConfiguration);
//      tfl = new TransformListener(node);
      oPubTf = new OrientationPublisherTf((SensorManager) getSystemService(SENSOR_SERVICE));
      nodeRunner.run(oPubTf, nodeConfiguration);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
