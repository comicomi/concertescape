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
          final_result (list e f)
          inter_res (merge {:event e} f {:total_distance distance})
          total_price (+  (inter_res :price) (-> inter_res :event :Ticket :price))
          total_score  (reduce + (map #(* 0.5 %) (list distance total_price)))]
      (merge {:total_price total_price} inter_res {:total_score total_score}))))


(defn handle-req [artist location]
  (let [events (ev/request-events (util/replace-space artist))]
     (if-not (nil? (:eventerror events))
       (list events)
       (map #(get-result %1 %2 location) events (map #(flight/get-flights  % location) events)))))


