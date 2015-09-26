(ns weka-classifier.logit-boost
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
   :recall
   :fallout
   :threshold])

(defn classifier-instance-to-map [instance]
  (zipmap classifier-instance-values
          (for [i (range 0 10)] (.value instance i))))


(defn get-classifier-value [key instance] (.value instance (.indexOf classifier-instance-values key)))


(defn create-classifier []
  "Creates LogitBost classifier with Decision Stumps"
  ;; build classifier
  (let [boostClassifier (doto (LogitBoost.)
                          (.setClassifier (DecisionStump.))
                          (.setNumIterations 10)
                          (.setWeightThreshold 100)
                          (.setUseResampling false))
        costMatrix (CostMatrix/parseMatlab "[0.0 1.0; 8.0 0.0]")]

    (doto (CostSensitiveClassifier.)
      (.setClassifier boostClassifier)
      (.setCostMatrix costMatrix) )))

(defn cross-validate [classifier features num-folds]
  (let [ev (Evaluation. features)
        tc (ThresholdCurve.)]
    (.crossValidateModel ev classifier features 10 (Random. 1) (into-array []))
    (.getCurve tc (.predictions ev))))



(defn filter-by-recall [curve target-recall]
  (let [positive-tp? (fn [i] (pos? (get-classifier-value :true-positives i) ))
        recall-dist (fn [i] (Math/abs (- (get-classifier-value :recall i) target-recall)))
        instances (for [i (range 0 (.numInstances curve))] (.instance curve i))
        best-instance (->> instances (filter positive-tp?) (apply min-key recall-dist))
        ]
        (classifier-instance-to-map best-instance)))


;; Extracts decision stumps
;;
;; CostSensitiveClassifier using reweighted training instances
;;
;; weka.classifiers.meta.LogitBoost -P 100 -L -1.7976931348623157E308 -H 1.0 -Z 3.0 -O 1 -E 1 -S 1 -I 10 -W weka.classifiers.trees.DecisionStump
;;
;; Classifier Model
;; LogitBoost: Base classifiers and their weights:
;;
;; Iteration 1
;; 	Class 1 (outcome=false)
;;
;; Decision Stump
;;
;; Classifications
;;
;; emailType_lev_x.gmail <= 0.1868636702259745 : 1.9014956942138093
;; emailType_lev_x.gmail > 0.1868636702259745 : 1.9706482143168675
;; emailType_lev_x.gmail is missing : 1.9291591130944088
;;
;; Two-class case: second classifier predicts additive inverse of first classifier and is not explicitly computed.


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
                       [idx variable operator threshold true-value false-value na-value]))
        ]
    {:label label
     :stumps (map-indexed parse-stump stumps)}))
