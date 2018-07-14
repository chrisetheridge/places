(ns ether.places.system
  (:require [com.stuartsierra.dependency :as dep]
            [ether.places.util :as util]))

(defonce *state (atom {:systems {}}))

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
      (let [stop-fn @(:system/service system)]
        (stop-fn)
        (swap! *systems assoc system-key :system/running? false)
        (assoc system :system/running? false)))))

(defn stop-all-systems! []
  (doseq [sys-key (reverse (:sorted-systems @*state))]
    (stop-system! sys-key)
    @*systems))

(defn start-system! [system-key]
  (let [sys (get (:systems @*state) system-key)]
    (if-not sys
      (util/throw! "No system found." {:system/key system-key})
      (let [start   (:system/start-fn sys)
            service (start)
            sys'    (merge {:system/service    start
                            :system/running?   true
                            :system/start-time (util/inst)})]
        (swap! *systems assoc system-key sys')
        sys'))))

(defn start-all-systems! []
  (doseq [sys-key (:sorted-systems @*state)]
    (start-system! sys-key)
    @*systems))

(defn- system-dep-tree [systems]
  (let [graph (dep/graph)]
    (reduce
     (fn [graph [sys-key system]]
       (reduce (fn [graph dep]
                 (dep/depend graph sys-key dep))
               graph
               (:system/depends system)))
     graph
     systems)))

(defn- update-dependency-tree [state]
  (let [{:keys [systems]} state
        _                 (prn :s systems)
        dep-tree          (system-dep-tree systems)]
    (->  (assoc state :tree dep-tree)
         (assoc :system-order (dep/topo-sort dep-tree)))))

(defn register-system! [system-key start-fn stop-fn depends]
  (if (get (:systems @*state) system-key)
    (util/throw! "System already exists." {:system/key system-key})
    (let [sys (new-system system-key start-fn stop-fn depends)]
      (swap! *state update :systems assoc system-key sys)
      (swap! *state update-dependency-tree)
      @*state
      #_sys)))

(comment

  (let [system1 {:system/key ::system1}
        system2 {:system/key ::system2}
        systema {:system/key     ::systemA
                 :system/depends #{::system1}}
        systemb {:system/key     ::systemB
                 :system/depends #{::system2}}
        systemc {:system/key     ::systemC
                 :system/depends #{::systemb ::system1}}
        systems (->> (map #(hash-map (:system/key %) %)
                          [system1 system2 systema systemb systemc])
                     (into {}))]

    (dep/topo-sort (system-dep-tree systems)))

  (let [_          (reset! *state  {:systems {}})
        system-key ::db
        start      #(let [a (atom {::started "yes"})]
                      (prn "started")
                      a)
        stop       #(let [s (deref %)]
                      (prn "stoped" s)
                      (reset! % :none))]
    (register-system! system-key start stop)
    @*state)

  )
