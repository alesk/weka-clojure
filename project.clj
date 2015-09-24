(defproject weka-classifier "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [
                 [org.clojure/clojure "1.7.0"]
                 [org.clojure/data.json "0.2.6"]
                 ;;[nz.ac.waikato.cms.weka/weka-stable "3.6.13"]
                 [nz.ac.waikato.cms.weka/weka-dev "3.7.13"]
                 ]
  :main weka-classifier.core/main
  )
