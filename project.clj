(defproject ether/places "0.0.1-SNAPSHOT"
  :description "A sample project"
  :url ""

  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.10.339"]
                 [http-kit "2.3.0"]
                 [rum "0.11.2"]
                 [com.datomic/datomic-free "0.9.5697"]
                 [com.stuartsierra/dependency "0.2.0"]]
  ;; :plugins []

  :profiles {:bundle {:global-vars {*warn-on-reflection* true}}}
  ;;  :aliases {}

  :source-paths ["src"]
  :test-paths ["test"]
  :resource-paths ["resources"]

  :repl-options {:port 6661}
  :main ether.places.server)
