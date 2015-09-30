(defproject weka-classifier "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [
                 [org.clojure/clojure "1.7.0"]
                 [org.clojure/data.json "0.2.6"]
                 [nz.ac.waikato.cms.weka/weka-stable "3.6.11"]
                 ;;[nz.ac.waikato.cms.weka/weka-dev "3.7.13"]
                 [ring/ring-core "1.4.0"]
                 [ring/ring-devel "1.4.0"]
                 [ring/ring-json "0.4.0"]
                 [ring/ring-jetty-adapter "1.4.0"]
                 [http-kit "2.1.18"]
                 [compojure "1.4.0"]
                 ]
  :plugins [[lein-ring "0.8.11"]]
  :ring {:handler weka-classifier.core/app}
  :main weka-classifier.core
  )
