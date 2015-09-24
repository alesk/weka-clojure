(ns weka-classifier.logit-boost-test
  (:use clojure.test)
  (:require [weka-classifier.logit-boost :as lb]))


(def classifier-state-test "
Iteration 1
 Class 1 (outcome=false)

 Decision Stump

 Classifications

 emailCat <= 0.186 : 1.901
 emailCat > 0.186 : 1.970
 emailCat is missing : 1.929

 emailType = gmail : 1.901
 emailType != gmail : 1.970
 emailType is missing : 1.929
")

(deftest parse-state-test
  (let [p (lb/parse-state classifier-state-test)
        stumps (:stumps p)]

    (is (= '(:label :stumps) (keys p)) "keys for parse-state")
    (is (= 2 (count stumps)) "should return two stumps")
    (is (= (first stumps) ["emailCat" "<=" "0.186" "1.901" "1.970" "1.929"]))
    (is (= (second stumps) ["emailType" "=" "gmail" "1.901" "1.970" "1.929"]))
  ))
