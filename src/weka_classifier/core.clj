(ns weka-classifier.core
  (:require
            [clojure.pprint :refer [pprint]]
            [weka-classifier.logit-boost :as lb]
            [weka-classifier.instances :as ins]
            [clojure.data.json :as json]
            [ring.middleware.json :as  middleware]
            [ring.util.response :refer [response]]
            [ring.adapter.jetty :as jetty]
            [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [org.httpkit.server :refer [run-server]])

  (:import  [java.lang Math]
  [weka.core Attribute Instance Range]
  [weka.classifiers CostMatrix Evaluation]
  [weka.classifiers.evaluation ThresholdCurve]))


(defn classify [filename label classifierParams]
  (let [
        features (ins/read-and-transform-features filename label (get classifierParams "removeFeatures" []))
        cs (doto (lb/create-classifier (ins/attribute-values features label) classifierParams)
             (.buildClassifier features))
        ev (lb/cross-validate cs features 10)
        curve (.getCurve (ThresholdCurve.) (.predictions ev))
        parsed-state (lb/parse-state (str cs))
        low-sensitive   (lb/filter-by-recall curve 0.9)
        high-sensitive  (lb/filter-by-recall curve 0.5)
        r-model (lb/r-model (:stumps parsed-state) (:threshold low-sensitive) (:threshold high-sensitive))
        ]

      (println (str cs))
      (println (str "The element at position i,j in the matrix "
                    "is the penalty for classifying an instance "
                    "of class j (column) as class i (row)."))
      (println (str "Confusion matrix:\n" (.toMatrixString ev)))
      (println r-model)
      (println "\n\n")

      {:classifiers {
                     :low-sensitive   low-sensitive
                     :high-sensitive  high-sensitive
                     }
       :classifiers-text-output (str cs)
       :decision-stumps parsed-state
       :r-model r-model
       }))


(defn handler [request]
  (let [jsonParams (:json-params request)
        {featuresFile "featuresFile"
         experimentId "experimentId"
         label "label"
         classifierParams "classifierParams"} (:json-params request)
        ]

  (pprint jsonParams)
  (response (merge  (classify featuresFile label classifierParams) {:echo jsonParams}))
  ))

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
