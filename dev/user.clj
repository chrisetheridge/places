(ns user
  (:require [clojure.tools.namespace.repl :as repl]
            [ether.places.system :as system]))

(defn start! []
  (system/start-all-systems!)
  system/*state)

(defn stop! []
  (system/stop-all-systems!)
  system/*state)

(defn reset []
  (stop!)
  (start!)
  (repl/refresh-all))
