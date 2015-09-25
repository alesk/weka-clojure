(ns weka-classifier.core
  (:require [weka-classifier.logit-boost :as lb]
            [clojure.data.json :as json]
            [ring.adapter.jetty :as jetty])

  (:import  [java.util Random]
            [java.lang Math]
  [weka.core Attribute Instance Range]
  [weka.core.converters ConverterUtils$DataSource]
  [weka.classifiers CostMatrix Evaluation]
  [weka.classifiers.evaluation ThresholdCurve]
  [weka.classifiers.evaluation.output.prediction PlainText])
  )

(defn read-features [filename label]
        (let [features (-> filename (ConverterUtils$DataSource.) (.getDataSet))]
          (.setClass features (.attribute features label))
          features))

(defn classify []
  (let [
        features (read-features "/Users/ales/tmp/features.base.csv" "outcome")
        cs (doto (lb/create-classifier) (.buildClassifier features))
        curve (lb/cross-validate cs features 10)
        ]

      ;;(.buildClassifier cs features)

      {:classifiers {
                     :low-sensitive   (lb/filter-by-recall curve 0.9)
                     :high-sensitive  (lb/filter-by-recall curve 0.5)
                     }
       :classifiers-text-output (str cs)
       :decision-stumps (lb/parse-state (str cs))
       }))


(defn handler [request]
  {:status 200
   :headers {"Content-Type" "text/json"}
   :body (json/write-str (classify))
   })

(defn -main []
  (jetty/run-jetty handler {:port 3000}))

