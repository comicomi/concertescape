(ns concertesc.routes.distance
   (:require [concertesc.utils :as util]
            [concertesc.db.core :as db]))

(defn get-coordinates [iatacode]
  (-> {:iatacode iatacode} db/get-coordinates first vals))

(defn degrees->radians [coordinates]
  (map util/degrees->radians coordinates))

(defn calculate-distance [origin destination]
   (util/calculate-distance (-> origin get-coordinates degrees->radians)
                                            (degrees->radians destination)))
