(ns weka-classifier.model
  (:require
            [clojure.java.jdbc :as sql]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [environ.core :refer [env]])

  (:import java.io.StringWriter)
  )

(def database-url (or (env :datbase-url) "jdbc:postgres://localhost/toptal_development"))

(defn white-listed-countries []
  (let [recs (sql/query database-url
               ["SELECT LOWER(name) as name FROM countries
                 WHERE NOT (whitelist = FALSE OR blacklist = TRUE);"])]
    (map :name recs)))




(defn- extract-app-info [text]
  (let [
        state (re-find #"state: ([^\n]+)" text)
        city (re-find #"city: ([^\n]+)" text)
        adnetwork (re-find #"adnetwork=([^&\n]+)" text)
        connection_type (re-find #"connection_type: ([^\n]+)" text)
        ]
      (map #(when % (second %))[state city adnetwork connection_type])))

(defn application-info []
  (let [raw (sql/query database-url "SELECT r.id as role_id, a.created_at, data FROM application_infos a JOIN roles r ON r.user_id = a.user_id ORDER BY created_at DESC")]
    (map (fn [{role_id :role_id created_at :created_at data :data}] (into [role_id created_at] (extract-app-info data))) raw)))


(defn application-info-csv []
  (let [writer (StringWriter.)]

    (csv/write-csv writer (concat [["companyId" "createdAt" "state" "city" "adnetwork" "connectionType"]] (application-info)))
  writer))

