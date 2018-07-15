(ns ether.places.system
  (:require [com.stuartsierra.dependency :as dep]
            [ether.lib.logging :as logging]
            [ether.places.util :as util]))

(defonce *state (atom {:systems {}}))

(defn- -dispatch [sys-key & args] sys-key)

(defmulti start!      [sys-key system-map]   -dispatch)
(defmulti stop!       [sys-key system]       -dispatch)
(defmulti init        [sys-key]              -dispatch)
(defmulti heartbeat   [sys-key system]       -dispatch)
(defmulti catch-error [sys-key error system] -dispatch)

(defn- new-system [system-key start stop depends]
  {:system/key      system-key
   :system/start-fn start
   :system/stop-fn  stop
   :system/uuid     (util/new-uuid)
   :system/depends  depends})

(defn stop-system! [system-key]
  (let [systems (:systems @*state)
        system  (get systems system-key)]
    (if-not system
      (util/throw! "No system found." {:system/key system-key})
      (let [_ (logging/info "Stopping system: " system-key)]
        (stop! system)
        (swap! *state update :systems assoc system-key :system/running? false)
        (assoc system :system/running? false)))))

(defn stop-all-systems! []
  (doseq [sys-key (reverse (:sorted-systems @*state))]
    (stop-system! sys-key)
    (:systems @*state)))

(defn start-system! [system-key]
  (let [systems (:systems @*state)
        sys     (get systems system-key)]
    (if-not sys
      (util/throw! "No system found." {:system/key system-key})
      (let [_       (logging/info "Starting system: " system-key)
            service (start! systems)
            sys'    (merge sys {:system/service    service
                                :system/running?   true
                                :system/start-time (util/inst)})]
        (logging/info "System started: " system-key sys')
        (swap! *state update :systems assoc system-key sys')
        sys'))))

(defn start-all-systems! []
  (doseq [sys-key (:system-order @*state)]
    (start-system! sys-key)
    (:systems @*state)))

(defn dependency-tree [systems]
  (let [graph (dep/graph)]
    (reduce
     (fn [graph [sys-key system]]
       (reduce #(dep/depend %1 sys-key %2)
               graph
               (:system/depends system)))
     graph
     systems)))

(defn update-dependency-tree [state]
  (let [{:keys [systems]} state
        dep-tree          (dependency-tree systems)]
    (->  (assoc state :tree dep-tree)
         (assoc :system-order (dep/topo-sort dep-tree)))))

(defn register-system! [system-key start-fn stop-fn depends]
  (if-let [sys (get (:systems @*state) system-key)]
    (util/throw! "System has already been registered."
                 {:system-key        system-key
                  :registered-system (dissoc sys :system/stop-fn)})
    (let [sys (new-system system-key start-fn stop-fn depends)]
      (swap! *state update :systems assoc system-key sys)
      (swap! *state update-dependency-tree)
      @*state
      #_sys)))
