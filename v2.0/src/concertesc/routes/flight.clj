(ns concertesc.routes.flight
  (:require [concertesc.utils :as util]
            [concertesc.db.core :as db]
            [cheshire.core :refer :all]
            [clj-http.client :as client]))

(defn get-flight-parameters [event location]
  (conj
   (map #(->> (:date event) util/string->date (util/get-following-or-preceding-date %)) [:minus :plus])
   (db/find-destination (select-keys (:Place event) [:city :country]))
   location))

(defn create-request-body [origin-code destination-code departure-date arrival-date]
  (generate-string {:request {:slice [{:origin origin-code
                                       :destination destination-code
                                       :date departure-date}
                                     {:origin destination-code
                                      :destination origin-code
                                      :date arrival-date}]
                              :passengers {:adultCount 1
                                           :infantInLapCount 0
                                           :infantInSeatCount 0
                                           :childCount 0
                                           :seniorCount 0}
                              :solutions 1
                              :refundable false
                              :saleCountry "DE"}}))
(defn send-request [body]
  (let [request {:body body
                 :headers {"X-Api-Version" "2"
                           "Content-Type" "application/json"}
                 :content-type :json
                 :accept :json
                 :throw-entire-message? true}]
    (client/post "https://www.googleapis.com/qpxExpress/v1/trips/search?key=AIzaSyDYM93xp8iYFCxfTdvfk2z3BpLBfXqDxB0&fields=trips/data(city(code,name),carrier(code,name),airport(code,city)),trips/tripOption(saleTotal,slice/segment(flight(carrier),leg(origin,destination,arrivalTime,departureTime)))" request)))

(defn send-flight-request [[origin-code destination-code departure-date arrival-date]]
  (let [body (create-request-body origin-code destination-code departure-date arrival-date)]
    (-> body send-request :body (parse-string true))))

(defn tryprocess [connection]
  {:dep-time (-> connection :leg first :departureTime)
   :arr-time  (-> connection :leg first :arrivalTime)
  :origin  (:city (first (db/get-city-by-airport-code {:iatacode (-> connection :leg first :origin)})))
  :destination  (:city (first (db/get-city-by-airport-code {:iatacode (-> connection :leg first :destination)})))
   :carrier (-> connection :flight :carrier)}
  )

(defn process-flight-connection [connection]
;; (str "la la " connection  "la la "))
  (map tryprocess connection))
 ;   {:dep-time (-> connection :leg first :departureTime)
 ;  :arr-time  (-> connection :leg first :arrivalTime)
 ;  :origin  (:city (first (db/get-city-by-airport-code {:iatacode (-> connection :leg first :origin)})))
 ;  :destination  (:city (first (db/get-city-by-airport-code {:iatacode (-> connection :leg first :destination)})))
;   :carrier (-> connection :flight :carrier)})


(defn process-response [body]
  (if (empty? body) {:error "No flights found"}
  (let [trip-option  (-> body :trips :tripOption first)
        price (-> trip-option :saleTotal (subs 3) java.lang.Double/parseDouble)
        fare-carriers  (-> trip-option :pricing first :fare)
        fares (:slice trip-option)
        data (-> body :trips :data)
        carriers (:carrier data)
        carriermap  (-> data :carrier (select-keys [:code :name]))
        result (atom {:price price})]
    (swap! result conj {:flight (map process-flight-connection (map #(% :segment) fares))}))))

(defn get-flights [event location]
  (let[[origin-code destination-code departure-date arrival-date] (get-flight-parameters event location)]
    (if-not (nil? destination-code) (-> [origin-code destination-code departure-date arrival-date] send-flight-request process-response))))

  ;;(request-flight (get-flight-parameters event location)))




;; inicijalizovati posle letove
;;(->Flight (citymap (:origin connection)) (citymap (:destination connection)) (format-date "yyyy-MM-dd 'at' hh:mm" (parse-date "yyyy-MM-dd'T'hh:mm" (:departureTime connection))) (format-date "yyyy-MM-dd 'at' hh:mm" (parse-date "yyyy-MM-dd'T'hh:mm" (:arrivalTime connection))) car)
