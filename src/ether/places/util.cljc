(ns ether.places.util
  #?(:clj
     (:require
      [clojure.java.io :as io]
      [clojure.edn :as edn])
     (:import
      [java.util.UUID java.util.Date])))

(defn new-uuid []
  #?(:clj (java.util.UUID/randomUUID)
     :cljs (cljs.core/new-uuid)))

(defn inst []
  #?(:clj (java.util.Date.)
     :cljs (js/Date.)))

(defn throw! [message ex-data]
  (throw (ex-info message ex-data)))
