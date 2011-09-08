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

import org.ros.node.DefaultNodeFactory;
import org.ros.node.Node;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeRunner;
import org.ros.rosjava.android.tf.android_tf_tools.OrientationPublisherTf;
import org.ros.rosjava.android.tf.android_tf_tools.R;
import org.ros.rosjava.android.tf.android_tf_tools.RosTfView;
import org.ros.rosjava.tf.pubsub.TransformListener;

/**
 * @author nick@heuristiclabs.com (Nick Armstrong-Crews)
 */
public class MainActivity extends Activity {

  protected final NodeRunner nodeRunner;
  
  protected RosTfView tfPrinter;
  protected OrientationPublisherTf oPubTf;
  
  //protected final TransformTree tfTree;
  
  public MainActivity() {
	super();
    nodeRunner = NodeRunner.newDefault();
    //tfTree = new TransformTree();
  }
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    tfPrinter = (RosTfView) findViewById(R.id.text);
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
      //tfTree = new TransformTree();
      nodeRunner.run(tfPrinter, NodeConfiguration.newPublic("192.168.43.1", masterUri));
      oPubTf = new OrientationPublisherTf((SensorManager) getSystemService(SENSOR_SERVICE));
      nodeRunner.run(oPubTf, NodeConfiguration.newPublic("192.168.43.1", masterUri));
      //Node node = new DefaultNodeFactory().newNode("android/tfl", NodeConfiguration.newPublic("192.168.43.1", masterUri));
      //TransformListener tfl = new TransformListener(node);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
