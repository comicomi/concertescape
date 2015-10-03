(ns concertesc.routes.flight
  (:require [concertesc.utils :as util]
            [concertesc.db.core :as db]
            [cheshire.core :refer :all]
            [clj-http.client :as client]))

(defn get-flight-parameters [event location]
  (conj
   (map #(->> (:date event) (util/string->date "yyyy-MM-dd'T'HH:mm:ss") (util/get-following-or-preceding-date %)) [:minus :plus])
   (db/find-destination (select-keys (:Place event) [:city :country]))
   location))

(defn create-request-body [origin-code destination-code departure-date arrival-date]
  (generate-string {:request {:slice [{:origin origin-code ;"BCN";
                                       :destination destination-code ;"MUC" ;
                                       :date departure-date}
                                      {:origin destination-code ;"YTO" ;
                                       :destination origin-code ;"BEG" ;
                                       :date arrival-date}]
                              :passengers {:adultCount 1
                                           :infantInLapCount 0
                                           :infantInSeatCount 0
                                           :childCount 0
                                           :seniorCount 0}
                              :solutions 1
                              :refundable false
                              :saleCountry "US"}}))
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

(defn process-city [connection k]
  (-> {:iatacode (-> connection :leg first k)} db/get-city-by-airport-code first :city))

(defn find-carrier [carrier carriermap]
  ((first (filter #(= (% :code) carrier) carriermap)) :name))

(defn process-carrier [connection carriermap]
  (-> connection :flight :carrier (find-carrier carriermap)))

(defn process-date [connection k]
  (->> connection :leg first k (util/string->date "yyyy-MM-dd'T'HH:mmZ") (util/date->string "dd.MM.yyyy' at 'HH:mm")))

(defn process-connection [connection carriermap]
  {:dep-time (process-date connection :departureTime)
   :arr-time  (process-date connection :arrivalTime)
   :origin  (process-city connection :origin)
   :destination  (process-city connection :destination)
   :carrier (process-carrier connection carriermap)})

(defn process-flight-connection [connection carriermap]
  (map  #(process-connection % carriermap) connection))

(defn process-response [body]
  (if (empty? body)
    {:flighterror "No flights were found."}
    (let [trip-option  (-> body :trips :tripOption first)
          price (-> trip-option :saleTotal (subs 3) java.lang.Double/parseDouble)
          fares (:slice trip-option)
          carriers (-> body :trips :data :carrier)]
      (conj {:price price} {:flight (map #(-> % :segment (process-flight-connection carriers)) fares)}))))

(defn get-flights [event location]
  (let[[origin-code destination-code departure-date arrival-date] (get-flight-parameters event location)]
    (if-not (nil? destination-code)
      (-> [origin-code destination-code departure-date arrival-date] send-flight-request process-response)
      {:flighterror "No dest flights were found."})))
