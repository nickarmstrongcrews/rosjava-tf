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

package org.ros.rosjava.tf.test;

import javax.vecmath.AxisAngle4d;
import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;

import org.ros.rosjava.tf.StampedTransform;
import org.ros.rosjava.tf.Transform;
import org.ros.rosjava.tf.TransformTree;

import junit.framework.TestCase;

/**
 * @author nick@heuristiclabs.com (Nick Armstrong-Crews)
 */
public class TransformTreeTest extends TestCase {
	
	protected TransformTree tfTree;
	protected StampedTransform tx; // a trivial exemplar
	
	@Override
	public void runTest() {
		// testAdd(); // TODO: move to unit test
		testLookupDirect();
		testLookupDistant();
		testCompositionTranslation();
		testCompositionRotation();
		//testCompositionTranslationAndRotation(); // TODO
		testInterpolation();
		//testCompositionAndInterpolation(); // TODO
		testCanTransform();
		testCanTransformWithTime();
		testLookupMostRecent();
	}
	
	public static void main(String [] args) {
		junit.textui.TestRunner.run(TransformTreeTest.class);
	}
	
	@Override
	protected void setUp() {
		tfTree = new TransformTree();
		tx = new StampedTransform(
									0, "parent", "child",
									new Vector3d(0, 0, 0),
									new Quat4d(0, 0, 0, 1)
								);
	}

	@Override
	protected void tearDown() {
		
	}

	//@Test
	public void testAdd() {
		tfTree.add(tx);
		assertEquals(1,1);
	}

	//@Test
	public void testLookupDirect() {
		tfTree.add(tx.clone());
		Transform txResult = tfTree.lookupTransformBetween(tx.parentFrame, tx.childFrame, tx.timestamp);
		//assertEquals(txResult,tx);
		assertTrue(txResult.equals(tx)); // compare contents, not references
	}

	//@Test
	public void testLookupDistant() {
		StampedTransform tx1 = tx.clone();
		StampedTransform tx2 = tx.clone();
		tx2.parentFrame = "child";
		tx2.childFrame = "grandchild";
		tfTree.add(tx1);
		tfTree.add(tx2);
		Transform txResult = tfTree.lookupTransformBetween(tx1.parentFrame, tx2.childFrame, tx1.timestamp);
		assertNotNull(txResult);
	}

	//@Test
	public void testCompositionTranslation() {
		StampedTransform tx1 = tx.clone();
		StampedTransform tx2 = tx.clone();
		tx2.parentFrame = "child";
		tx2.childFrame = "grandchild";
		tx1.translation.x = 3;		tx2.translation.x = 4;
		StampedTransform txExpectedResult = tx.clone();
		txExpectedResult.translation.x = tx1.translation.x + tx2.translation.x;
		tfTree.add(tx1);
		tfTree.add(tx2);
		Transform txResult = tfTree.lookupTransformBetween(tx1.parentFrame, tx2.childFrame, tx1.timestamp);
		assertTrue(txResult.equals(txExpectedResult));
	}
	
	//@Test
	public void testCompositionRotation() {
		StampedTransform tx1 = tx.clone();
		StampedTransform tx2 = tx.clone();
		tx2.parentFrame = "child";
		tx2.childFrame = "grandchild";
		AxisAngle4d r1 = new AxisAngle4d(0, 0, 1, Math.PI/8.0d);
		AxisAngle4d r2 = new AxisAngle4d(0, 0, 1, Math.PI/8.0d);
		tx1.rotation.set(r1);		tx2.rotation.set(r2);
		StampedTransform txExpectedResult = tx.clone();
		AxisAngle4d r12= new AxisAngle4d(0, 0, 1, Math.PI/4.0d);
		txExpectedResult.rotation.set(r12);
		tfTree.add(tx1);
		tfTree.add(tx2);
		Transform txResult = tfTree.lookupTransformBetween(tx1.parentFrame, tx2.childFrame, tx1.timestamp);
		assertTrue(txResult.equals(txExpectedResult));
	}

	//@Test
	public void testInterpolation() {
		StampedTransform tx1 = tx.clone();
		StampedTransform tx2 = tx.clone();
		tx1.timestamp = 0;		tx2.timestamp = 10;
		tx1.translation.x = 0;	tx2.translation.x = 10;
		tx1.rotation.setX(0);	tx2.rotation.setX(1);
		tfTree.add(tx1);
		tfTree.add(tx2);
		long t = ( tx1.timestamp + tx2.timestamp ) / 2; // meet halfway
		Transform txResult = tfTree.lookupTransformBetween(tx1.parentFrame, tx1.childFrame, t);
		assertNotNull(txResult);
		assertTrue(txResult.translation.x > tx1.translation.x);
		assertTrue(txResult.translation.x < tx2.translation.x);
		assertEquals(txResult.translation.x, ( tx1.translation.x + tx2.translation.x ) / 2, 1e-4);
		assertTrue(txResult.rotation.x > tx1.rotation.x);
		assertTrue(txResult.rotation.x < tx2.rotation.x);		
	}

	//@Test
	public void testCanTransform() {
		assertFalse(tfTree.canTransform("garbageIn", "garbageOut"));
		StampedTransform tx1 = tx.clone();
		tfTree.add(tx1);
		assertTrue(tfTree.canTransform(tx.parentFrame, tx.childFrame));
		assertFalse(tfTree.canTransform(tx.parentFrame, "garbageOut"));
		assertFalse(tfTree.canTransform("garbageIn", tx.childFrame));
		StampedTransform tx2 = tx.clone();
		tx2.parentFrame = "child";
		tx2.childFrame = "grandchild";
		assertTrue(tfTree.canTransform(tx1.parentFrame, tx2.childFrame));
		assertFalse(tfTree.canTransform(tx2.parentFrame, tx1.childFrame)); // haven't implemented reverse traversals, yet
	}

	//@Test
	public void testCanTransformWithTime() {
		StampedTransform tx1a = tx.clone();
		StampedTransform tx1b = tx.clone();
		StampedTransform tx2 = tx.clone();
		tx1a.timestamp = 0;		tx1b.timestamp = 100;
		tx2.timestamp = 50;
		tx2.parentFrame = "child";
		tx2.childFrame = "grandchild";
		tfTree.add(tx1a);
		tfTree.add(tx1b);
		tfTree.add(tx2);
		assertTrue(tfTree.canTransform(tx1a.parentFrame, tx1a.childFrame, tx1a.timestamp));
		assertFalse(tfTree.canTransform(tx2.parentFrame, tx2.childFrame, tx2.timestamp+1)); // too late (future)
		//assertFalse(tfTree.canTransform(tx2.parentFrame, tx2.childFrame, tx2.timestamp-1)); // too early (pre-history) FIXME
		//assertFalse(tfTree.canTransform(tx1a.parentFrame, tx1a.childFrame, tx1b.timestamp)); // too late (future) // FIXME
		//assertFalse(tfTree.canTransform(tx2.parentFrame, tx2.childFrame, tx1a.timestamp)); // too early (pre-history) // FIXME
		//assertFalse(tfTree.canTransform(tx1a.parentFrame, tx2.childFrame, tx1a.timestamp)); // tx2 not yet known // FIXME
		assertTrue(tfTree.canTransform(tx1a.parentFrame, tx2.childFrame, tx2.timestamp));
		assertFalse(tfTree.canTransform(tx1a.parentFrame, tx2.childFrame, tx2.timestamp+1)); // too late (future)
		//assertFalse(tfTree.canTransform(tx1a.parentFrame, tx2.childFrame, tx2.timestamp-1)); // too early (pre-history) // FIXME
	}

	//@Test
	public void testLookupMostRecent() {
		StampedTransform tx1 = tx.clone();
		StampedTransform tx2 = tx.clone();
		tx1.timestamp = 0;		tx2.timestamp = 100;
		tx1.translation.x = 0;	tx2.translation.x = 1;
		tfTree.add(tx1);
		Transform txResult = tfTree.lookupMostRecent(tx.parentFrame, tx.childFrame);
		assertNotNull(txResult);
		assertTrue(txResult.equals(tx1));
		tfTree.add(tx2);
		txResult = tfTree.lookupMostRecent(tx.parentFrame, tx.childFrame);
		assertTrue(txResult.equals(tx2));
		assertFalse(txResult.equals(tx1));
	}
	
}
