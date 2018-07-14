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

(defn- start-server [systems]
  (let [config (some-> (get-in systems [::system/config :system/service])
                       deref)]
    (atom (http.server/run-server index {:port (:port config 3000)}))))

(defn- stop-server [{:system/keys [service]}]
  (let [stop-server (deref service)]
    (stop-server :timeout 500)))

#_(defn -main [& args]
  (start! args))


(comment

  (system/register-system! ::system/server
                           start-server
                           stop-server
                           #{::system/config})

  )
