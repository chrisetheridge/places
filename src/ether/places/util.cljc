(ns ether.places.util
  #?(:clj
     (:import [java.util Date UUID])))

(defn new-uuid []
  #?(:clj (UUID/randomUUID)
     :cljs (cljs.core/new-uuid)))

(defn inst []
  #?(:clj  (Date.)
     :cljs (js/Date.)))

(defn throw! [message ex-data]
  q(throw (ex-info message ex-data)))
