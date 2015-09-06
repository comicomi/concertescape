(ns concertesc.routes.service
  (:require [concertesc.utils :as util]
            [concertesc.routes.flight :as flight]
            [concertesc.routes.event :as ev]
            [concertesc.routes.distance :as dist]
           ; [concertesc.routes.improvement :as imp]
            [criterium.core :as criterium]))

(defn get-result [e  f location]
  (if-not (nil? (:flighterror f))
    (merge {:event e} f)
    (let [place (:Place e),
          date (:date e),
          distance (dist/calculate-distance location (:location place)),
          inter-res (merge {:event e} f {:total-distance distance}),
          total-price (+  (inter-res :price) (-> inter-res :event :Ticket :price)),
          total-score  (reduce + (map #(* 0.5 %) (list distance total-price)))]
      (merge {:total-price total-price} inter-res {:total-score total-score}))))

(defn handle-req [artist location]
  (let [events (ev/request-events (util/replace-space artist))]
     (if-not (nil? (:eventerror events))
       (list events)
       (sort-by #(% :total-score) (map #(get-result %1 %2 location) events (map #(flight/get-flights  % location) events))))))

;(def param1 (-> "tove-lo" ev/request-events first))

;(def param2 (-> param1 (flight/get-flights "BCN") first))

;(criterium/with-progress-reporting (criterium/bench (get-result param1  param2  "BCN") :verbose))

;(criterium/with-progress-reporting (criterium/bench (handle-req "tove-lo"  "BCN") :verbose))
