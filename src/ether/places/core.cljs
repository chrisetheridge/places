(ns places.core
  (:require [rum.core :as rum]))

(rum/defc app []
  [:div
   [:h1 "Hello"]])

(defn start []
  (rum/mount (app)
             (. js/document (getElementById "app"))))

(defn ^:export init []
  (start))

(defn stop []
  (js/console.log "stop"))
