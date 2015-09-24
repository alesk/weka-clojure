(ns weka-classifier.core
  (:require [weka-classifier.logit-boost :as lb])
  (:require [clojure.data.json :as json])
  (:import [java.util Random])
  (:import [java.lang Math])
  (:import [weka.core Attribute Instance Range])
  (:import [weka.core.converters ConverterUtils$DataSource])
  (:import [weka.classifiers CostMatrix Evaluation])
  (:import [weka.classifiers CostMatrix Evaluation])
  (:import [weka.classifiers.evaluation ThresholdCurve])
  (:import [weka.classifiers.evaluation.output.prediction PlainText])
  )


(defn main []
  (let [
        features (-> "/Users/ales/tmp/features.base.csv" (ConverterUtils$DataSource.) (.getDataSet))
        cs (lb/create-classifier)]

    (do
      (.setClass features (.attribute features "outcome"))
      (.buildClassifier cs features)
      {:classifiers {
                     :low-sensitive   (lb/cross-validate cs features 0.9)
                     :high-sensitive  (lb/cross-validate cs features 0.5)
                     }
       :classifiers-text-output (str cs)
       :decision-stumps (lb/parse-state (str cs))
       })))

(def classifier-output (main))

classifier-output


