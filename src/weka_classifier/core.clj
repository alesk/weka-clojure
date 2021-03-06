(ns weka-classifier.core
  (:require
      [weka-classifier.logit-boost :as lb]
      [weka-classifier.instances :as ins]
      [weka-classifier.model :as model]

      [selmer.parser :as selmer]
      [selmer.util :refer [turn-off-escaping!]]
      [clojure.string :as string]

      ;;[clojure.data.json :as json]
      [ring.middleware.json :as  middleware]
      [ring.util.response :refer [response content-type]]
      [ring.adapter.jetty :as jetty]
      [compojure.core :refer :all]
      [compojure.handler :as handler]
      [compojure.route :as route]
      [org.httpkit.server :refer [run-server]]

      ;; debugging
      [clojure.pprint :refer [pprint]]
   )

  (:import
      java.lang.Math
      java.util.Date
      [weka.core Attribute Instance Range]
      [weka.classifiers CostMatrix Evaluation]
      [weka.classifiers.evaluation ThresholdCurve]))


(def classifier-params-defaults {
               "numIterations" 10
               "weightThreshold" 100
               "numRuns" 2
               "numFolds" 0
               "trueToFalseCost" 8.0
               "falseToTrueCost" 1.0
               "lowRecall" 0.9
               "highRecall" 0.5
               "removeFeatures" []
               "useResampling" false})

;; add tag that removes all new line chars
(selmer/add-tag! :squash
          (fn [args context-map content]
            (let [text (get-in content [:squash :content])]
                  (-> text
                      (string/replace #"[\n\t]" "")
                      (string/replace #"^\s+" "")
                      (string/replace #"\s+$" ""))))
          :endsquash)
(turn-off-escaping!)


(defn classify [filename label classifierParams]
  (let [
        params (merge classifier-params-defaults classifierParams)

        features (ins/read-and-transform-features filename label (params "removeFeatures"))
        cs (doto (lb/create-classifier (ins/attribute-values features label) params) (.buildClassifier features))
        ev (lb/cross-validate cs features 10)
        curve (.getCurve (ThresholdCurve.) (.predictions ev))
        parsed-state (lb/parse-state (str cs))
        low-sensitive   (lb/filter-by-recall curve (params "lowRecall"))
        high-sensitive  (lb/filter-by-recall curve (params "highRecall"))
        created-at (new java.util.Date)
        classifier-data {:white-listed-countries (model/white-listed-countries)
                         :stumps (parsed-state :stumps)
                         :created-at created-at
                         :high-threshold (:threshold high-sensitive)
                         :ambiguous-threshold (:threshold low-sensitive)}

        r-model (selmer/render-file "LeadClassifier.R" classifier-data)
        scala-model (selmer/render-file "LeadClassifier.scala" classifier-data)]

      (println (str cs))
      (println r-model)
      (println "\n\n")

      {:classifiers {
                     :low-sensitive   low-sensitive
                     :high-sensitive  high-sensitive
                     }
       :classifiers-text-output (str cs)
       :decision-stumps parsed-state
       :r-model r-model
       :scala-model scala-model
       :performance (lb/curve-to-map curve)
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
  (GET "/application-info" [] (-> (response (.toString (model/application-info-csv))) (content-type "text/csv")))
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
