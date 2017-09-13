/*
 Copyright (c) 2014 by Contributors

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package ml.dmlc.xgboost4j.scala.spark

import ml.dmlc.xgboost4j.java.XGBoostError
import org.apache.spark.rdd.RDD
import org.scalatest.FunSuite

class SparkParallelismTrackerSuite extends FunSuite with PerTest {
  val numParallelism: Int = sc.defaultParallelism

  // Can only be run separately. sc in PerTest has a race condition
  // when maven runs tests in parallel
  ignore("tracker should not affect execution result") {
    val nWorkers = numParallelism
    val rdd: RDD[Int] = sc.parallelize(1 to nWorkers)
    val tracker = new SparkParallelismTracker(sc, 1000, nWorkers)
    val disabledTracker = new SparkParallelismTracker(sc, 0, nWorkers)
    assert(tracker.execute(rdd.sum()) == rdd.sum())
    assert(disabledTracker.execute(rdd.sum()) == rdd.sum())
  }

  // Can only be run separately. sc in PerTest has a race condition
  // when maven runs tests in parallel
  ignore("tracker should throw exception if parallelism is not sufficient") {
    val nWorkers = numParallelism * 3
    val rdd: RDD[Int] = sc.parallelize(1 to nWorkers)
    val tracker = new SparkParallelismTracker(sc, 1000, nWorkers)
    intercept[XGBoostError] {
      tracker.execute {
        rdd.map { i =>
          // Test interruption
          Thread.sleep(Long.MaxValue)
          i
        }.sum()
      }
    }
  }
}
