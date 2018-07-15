(ns ether.places.systems.server
  (:require
   [clojure.java.io :as io]
   [ether.places.system :as system]
   [org.httpkit.server :as http.server]))

(def ^:dynamic *run-env* :ether.env/none)

(def ^:dynamic *system* (atom {}))

(defn index [request]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    (slurp (io/resource "resources/public/index.html"))})

(defmethod system/start! ::system/server [_ systems]
  (let [config (some-> (get-in systems [::system/config :system/service])
                       deref)]
    (prn :config config)
    (atom (http.server/run-server index {:port (:port config 3000)}))))

(defmethod system/stop! ::system/server [{:system/keys [service]}]
  (let [stop (deref service)]
    (stop)))
