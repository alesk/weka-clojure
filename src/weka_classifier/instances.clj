(ns weka-classifier.instances
  (:import
    [weka.core.converters ConverterUtils$DataSource]))


(defn attribute-names [instances]
  (map #(.name %) (enumeration-seq (.enumerateAttributes instances))))

;; deleteAttributeByName
(defn attribute-name-to-index [instances name]
   (let [idx (.indexOf (attribute-names instances) name)]
     (if (= -1 idx) nil (+ 1 idx))))

(defn delete-attribute! [instances attribute]
  (when-let [idx (attribute-name-to-index instances attribute)]
    (.deleteAttributeAt instances idx)))

(defn read-features [filename label]
        (let [features (-> filename (ConverterUtils$DataSource.) (.getDataSet))]
          (.setClass features (.attribute features label))
          features))

(defn read-and-transform-features [filename label attrs-to-remove]
  (let [features (read-features filename label)]
    (doall #(delete-attribute! features %) attrs-to-remove)
    features))


(defn attribute-values [instances name]
  (map str (enumeration-seq (.enumerateValues (.attribute instances name)))))


