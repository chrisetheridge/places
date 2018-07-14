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
        (swap! *state update :systems assoc system-key :system/running? false)
        (assoc system :system/running? false)))))

(defn stop-all-systems! []
  (doseq [sys-key (reverse (:sorted-systems @*state))]
    (stop-system! sys-key)
    (:systems @*state)))

(defn start-system! [system-key]
  (let [systems (:systems @*state)
        sys (get systems system-key)]
    (if-not sys
      (util/throw! "No system found." {:system/key system-key})
      (let [start   (:system/start-fn sys)
            service (start systems)
            sys'    (merge {:system/service service
                            :system/running?   true
                            :system/start-time (util/inst)})]
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
        _                 (prn :s systems)
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

  (stop-all-systems!)

  (start-all-systems!)


  @*state
  )
