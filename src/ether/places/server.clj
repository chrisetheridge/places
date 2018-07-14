(ns ether.places.server
  (:require [ether.places.system :as system]
            [org.httpkit.server :as http.server]
            [clojure.java.io :as io]))

(def ^:dynamic *run-env* :ether.env/none)

(def ^:dynamic *system* (atom {}))

(defn index [request]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    (slurp (io/resource "resources/public/index.html"))})

(defn start! [args]
  (let [start #(http.server/run-server index {:port (:port args 3000)})]
    ()))

(defn -main [& args])
