(ns weka-classifier.logit-boost
  (:require [clojure.string :as string]
            [clojure.pprint :refer [pprint]])
  (:import java.util.Random
    [weka.classifiers.meta LogitBoost CostSensitiveClassifier]
    [weka.classifiers CostMatrix Evaluation]
    [weka.classifiers.evaluation ThresholdCurve]
    [weka.classifiers.trees DecisionStump]))

(def classifier-instance-values
  [:true-positives
   :false-negatives
   :false-positives
   :true-negatives
   :false-positive-rate
   :true-positive-rate
   :precision
   :recall ;; 7
   :fallout
   :f1 ;; 9
   :samlpe-size
   :lift
   :threshold])

(def defaults {"numIterations" 10
               "weightThreshold" 100
               "numRuns" 2
               "numFolds" 0
               "trueToFalseCost" 8.0
               "falseToTrueCost" 1.0
               "useResampling" false})

(defn create-cost-matrix [class-values true-to-false-cost false-to-true-cost]
  (let [matrix (CostMatrix. 2)]
    (if (= (first class-values) "true")
      (doto matrix (.setCell 0 1 true-to-false-cost) (.setCell 1 0 false-to-true-cost))
      (doto matrix (.setCell 0 1 false-to-true-cost) (.setCell 1 0 true-to-false-cost)))))

(defn classifier-instance-to-map [instance]
  (zipmap classifier-instance-values
          (for [i (range 0 (count classifier-instance-values))] (.value instance i))))


(defn get-classifier-value [key instance] (.value instance (.indexOf classifier-instance-values key)))


(defn create-classifier [class-values params]
  "Creates LogitBost classifier with Decision Stumps"
  ;; build classifier
  (let [{numIterations "numIterations"
         weightThreshold "weightThreshold"
         numRuns "numRuns"
         numFolds "numFolds"
         trueToFalseCost "trueToFalseCost"
         falseToTrueCost "falseToTrueCost"
         useResampling "useResampling"
         } (merge defaults params)
        boostClassifier (doto (LogitBoost.)
                          (.setClassifier (DecisionStump.))
                          (.setNumIterations numIterations)
                          (.setNumRuns numRuns)
                          (.setNumFolds numFolds)
                          (.setWeightThreshold weightThreshold)
                          (.setUseResampling useResampling))
        costMatrix (create-cost-matrix class-values trueToFalseCost falseToTrueCost)]

    (pprint (merge defaults params))

    (doto (CostSensitiveClassifier.)
      (.setClassifier boostClassifier)
      (.setCostMatrix costMatrix) )))

(defn cross-validate [classifier features num-folds]
  (let [ev (Evaluation. features)]
    (.crossValidateModel ev classifier features num-folds (Random. 1) (into-array []))
    ev))



(defn filter-by-recall [curve target-recall]
  (let [positive-tp? (fn [i] (pos? (get-classifier-value :true-positives i) ))
        recall-dist (fn [i] (Math/abs (- (get-classifier-value :recall i) target-recall)))
        instances (for [i (range 0 (.numInstances curve))] (.instance curve i))
        best-instance (->> instances (filter positive-tp?) (apply min-key recall-dist))
        ]
        (classifier-instance-to-map best-instance)))


(defn parse-state [state]
  (let [label (drop 1 (re-find #"(?is)Iteration 1.*?\((\w+)=(\w+)\)" state))
        trim-seq (fn [x] (map clojure.string/trim x))
        stump-pred-reg #"(\S+)\s+(>=|<=|<|>|!=|=|is)\s+(.*?) : (\S+)"
        stumps  (->> state (re-seq stump-pred-reg) (map rest) (map trim-seq) (partition 3))


        parse-stump (fn [idx
                         [[variable operator threshold true-value]
                          [_ _ _ false-value]
                          [_ _ _ na-value]]]

                      (zipmap
                       [:idx :variable :operator :threshold :true_value :false_value :na_value]
                       [idx variable operator threshold
                        (read-string true-value) (read-string false-value) (read-string na-value)]))

        parsed-stumps (map-indexed parse-stump stumps)
        label-false? (= (second label) "false")

        ]
    {:label label
     :stumps (if label-false?
               (filter #(odd? (:idx %)) parsed-stumps)
               (filter #(even? (:idx %)) parsed-stumps))}))
