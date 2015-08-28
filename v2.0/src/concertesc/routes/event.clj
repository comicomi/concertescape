(ns concertesc.routes.event
   (:require [cheshire.core :refer :all]
             [clj-http.client :as client]))

(defrecord Ticket [price url])
(defrecord Performer [namep image_url])
(defrecord Place [namep city country location])
(defrecord Event [namep performers date Place Ticket])
(defrecord Flight [origin destination departure_date arrival_date carrier])
(defrecord Trip [Flights price])
(defrecord Result [Event Trip total_price total_distance])

(defn send-request [artist]
  (client/get (str "http://api.seatgeek.com/2/events?per_page=2&page=1&sort=lowest_price.asc&performers.slug=" artist)))

(defn parse-response [response]
  ((-> response :body parse-string) "events"))

(defn process-event [event]
  (let [performers (event "performers")
        venue (event "venue")
        place (->Place (venue "name") (venue "city") (venue "country") (vals (venue "location")))
        ticket (->Ticket ((event "stats") "lowest_price") (event "url"))
        artists (map #(->Performer (% "name") (% "image")) performers)]
    (->Event (event "title") artists (event "datetime_local") place ticket)))
 ;;  (->Event (event "title") artists (subs (event "datetime_local") 0 (.indexOf (event "datetime_local") "T")) place ticket)))

(defn process-response [events]
  (if (empty? events) {:error "No events were found... Please make sure you entered name of the performer correctly."}
  (map process-event events)))

(defn request-events [artist]
 (-> artist send-request parse-response process-response))
