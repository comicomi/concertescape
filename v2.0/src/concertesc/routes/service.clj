(ns concertesc.routes.service
  (:require [concertesc.utils :as util]
            [concertesc.routes.flight :as flight]
            [concertesc.routes.event :as ev]))

(defn handle-req [artist location]
  (map #(flight/get-flights  % location) (ev/request-events (util/replace-space artist))))


;;(defn get-artists-events [artist location]
  ;;(let [events (filter #(.after  (parse-date "yyyy-MM-dd" (:date %)) (ev/now)) (request-events (replace-space artist)))]
   ;; (reset! results  (sort-by #(% :total_score) (map get-result events (repeat (count events) location))))))

;;(defn get-result [event location]
 ;; (let [place (:Place event),
   ;;     date (:date event),
     ;;   destination (get-airport-code place),
      ;;  departure_date (format-date "yyyy-MM-dd" (get-date - (parse-date "yyyy-MM-dd" date))),
      ;;  arrival_date  (format-date "yyyy-MM-dd" (get-date + (parse-date "yyyy-MM-dd" date))),
       ;; flight (process-flight-response (send-flight-request location destination departure_date arrival_date)),
      ;;  dest_location (location-codes-map destination),
      ;;  distance (calculate-distance (location-codes-map location) dest_location),
      ;;  final_result (list event flight),
      ;;  inter_res (merge {:event event} flight {:total_distance distance} ),
      ;;  total_price (+  (inter_res :price) (-> (-> (inter_res :event) :Ticket) :price)),
       ;; total_score  (reduce + (map #(* 0.5 %) (list distance total_price)))]
   ;; (merge {:total_price total_price} inter_res {:total_score total_score})))

