/*
 * Copyright (C) 2011 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.ros.rosjava.android.tf.android_tf_tools;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import org.ros.node.DefaultNodeFactory;
import org.ros.node.Node;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMain;
import org.ros.rosjava.tf.pubsub.TransformBroadcaster;

/**
 * @author damonkohler@google.com (Damon Kohler), nick@heuristiclabs.com (Nick Armstrong-Crews)
 */
public class OrientationPublisherTf implements NodeMain {

  private final SensorManager sensorManager;

  private Node node;
  private OrientationListener orientationListener;
  private TransformBroadcaster tfb;

  private final class OrientationListener implements SensorEventListener {

    private final TransformBroadcaster tfb;

    private OrientationListener(TransformBroadcaster tfb) {
      this.tfb = tfb;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public void onSensorChanged(SensorEvent event) {
      if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
    	float[] q = new float[4];
        SensorManager.getQuaternionFromVector(q, event.values);
        tfb.sendTransform(	"/phone", "/phone_oriented",
        					((long) System.currentTimeMillis())*1000000, // nanoseconds
        					0, 0, 0,
        					q[1], q[2], q[3], q[0] // different order
        				);
      }
    }
  }

  public OrientationPublisherTf(SensorManager sensorManager) {
    this.sensorManager = sensorManager;
  }

  @Override
  public void main(NodeConfiguration configuration) throws Exception {
    try {
      node = new DefaultNodeFactory().newNode("android/orientation_publisher_tf", configuration);
      tfb = new TransformBroadcaster(node);
      orientationListener = new OrientationListener(tfb);
      Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
      sensorManager.registerListener(orientationListener, sensor, 500000); // 10 Hz
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
    sensorManager.unregisterListener(orientationListener);
    node.shutdown();
  }

}
