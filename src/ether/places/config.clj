(ns ether.places.config
  (:require [ether.places.util :as util]
            [ether.places.system :as system]
            [clojure.java.io :as io]
            [clojure.edn :as edn]))

(defn read-config [_]
  (if-let [f (io/resource "config/places.env.edn")]
    (atom (edn/read-string (slurp f)))
    (util/throw! "config/places.env.edn required to start places." {})))

(system/register-system! ::system/config read-config (fn [] ::noop) #{})

(comment

  (slurp (io/resource "config/places.env.edn"))

  )
