(ns weka-classifier.core
  (:require [weka-classifier.logit-boost :as lb]
            [clojure.data.json :as json]
            [ring.adapter.jetty :as jetty])

  (:import  [java.util Random]
            [java.lang Math]
  [weka.core Attribute Instance Range]
  [weka.core.converters ConverterUtils$DataSource]
  [weka.classifiers CostMatrix Evaluation]
  [weka.classifiers CostMatrix Evaluation]
  [weka.classifiers.evaluation ThresholdCurve]
  [weka.classifiers.evaluation.output.prediction PlainText])
  )



(defn classify []
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

(defn handler [request]
  {:status 200
   :headers {"Content-Type" "text/json"}
   :body (json/write-str (classify))
   })

(defn -main []
  (jetty/run-jetty handler {:port 3000}))


