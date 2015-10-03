(ns concertesc.routes.artist
  (:require [concertesc.db.core :as db]))

(defn resolve-artist-by-name [artist]
  (db/resolve-artist-by-name  {:name (str "%" artist "%")}))

(defn resolve-artist-by-id [artistid]
  (db/resolve-artist-by-id {:id artistid}))
