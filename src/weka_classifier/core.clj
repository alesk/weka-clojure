(ns weka-classifier.core
  (:require [weka-classifier.logit-boost :as lb]
            [clojure.data.json :as json]
            [ring.middleware.json :as  middleware]
            [ring.util.response :refer [response]]
            [ring.adapter.jetty :as jetty]
            [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [org.httpkit.server :refer [run-server]])

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

(defn classify [filename]
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
  (let [params (:json-params request)
        features-file (:features_file params)]

  (response (classify features-file))))

(defn evaluate-classifier [request])

(defroutes app-routes
  (GET "/" [] "Simple classifier evaluation")
  (GET "/hello" [] "Hello")
  (POST "/evaluate" [] handler)
  )


        ;; define the ring application
(def app
  (-> (handler/api app-routes)
      (middleware/wrap-json-body)
      (middleware/wrap-json-params)
      (middleware/wrap-json-response)))

(defn -main []
  (run-server app {:port 3000 :join? false}))
