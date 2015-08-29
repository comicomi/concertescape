(ns concertesc.routes.service
  (:require [concertesc.utils :as util]
            [concertesc.routes.flight :as flight]
            [concertesc.routes.event :as ev]
            [concertesc.routes.distance :as dist]))

(defn get-result [e  f location]
  (if-not (nil? (:flighterror f))
    (merge {:event e} f)
    (let [place (:Place e),
          date (:date e),
          distance (dist/calculate-distance location (:location place))
          final-result (list e f)
          inter-res (merge {:event e} f {:total_distance distance})
          total-price (+  (inter-res :price) (-> inter-res :event :Ticket :price))
          total-score  (reduce + (map #(* 0.5 %) (list distance total-price)))]
      (merge {:total_price total-price} inter-res {:total_score total-score}))))

(defn handle-req [artist location]
  (let [events (ev/request-events (util/replace-space artist))]
     (if-not (nil? (:eventerror events))
       (list events)
       (sort-by #(% :total_score) (map #(get-result %1 %2 location) events (map #(flight/get-flights  % location) events))))))


