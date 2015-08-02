(ns concertesc.routes.service
  (:require [concertesc.utils :as util]
            [concertesc.routes.flight :as flight]
            [concertesc.routes.event :as ev]
            [concertesc.routes.distance :as dist]))


(defn get-result [e  f location]
  (let [place (:Place e),
        date (:date e),
        distance (dist/calculate-distance location (:location place))
        final_result (list e f),
        inter_res (merge {:event e} f {:total_distance distance})]
      ;;  total_price (+  (inter_res :price) (-> inter_res :event :Ticket :price))]
    (str (-> inter_res :event :Ticket :price) "dadsad" (inter_res :price))))
  ;      total_score  (reduce + (map #(* 0.5 %) (list distance total_price)))]
; (merge {:total_price total_price} inter_res {:total_score total_score})))


(defn handle-req [artist location]
  (let [events (ev/request-events (util/replace-space artist))
        flights (map #(flight/get-flights  % location) events)]
   (map #(get-result %1 %2 location) events flights)))

(defrecord Ticket [price url])
(defrecord Performer [namep image_url])
(defrecord Place [namep city country location])
(defrecord Event [namep performers date Place Ticket])
(defrecord Flight [origin destination departure_date arrival_date carrier])
(defrecord Trip [Flights price])
(defrecord Result [Event Trip total_price total_distance])


