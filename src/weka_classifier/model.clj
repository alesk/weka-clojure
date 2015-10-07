(ns weka-classifier.model
  (:require
            [clojure.java.jdbc :as sql]
            [environ.core :refer [env]]))

(def database-url (or (env :datbase-url) "jdbc:postgres://localhost/toptal_development"))

(defn white-listed-countries []
  (let [recs (sql/query database-url
               ["SELECT LOWER(name) as name FROM countries
                 WHERE NOT (whitelist = FALSE OR blacklist = TRUE);"])]
    (map :name recs)))

