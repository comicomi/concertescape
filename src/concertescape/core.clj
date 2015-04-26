(ns concertescape.core
  (use (compojure handler [core :only (GET POST defroutes)]))
  (require [compojure.route])
  (require [net.cgrand.enlive-html :as en])
  (require [org.httpkit.server :as httpserver])   
  (require [clj-http.client :as client])
  (require [cheshire.core :refer :all])
  (require [clojure.data.csv :as csv])
  (require [clojure.java.io :as io])
  )
(defrecord Ticket [price url])
(defrecord Performer [name genre image_url])
(defrecord Place [name city country location])
(defrecord Event [name performer Place Ticket])
(defrecord Flight [origin destination carrier departure_date arrival_date price url])
(defrecord Result [Event Flight total_price total_distance])

(defn load-airport-codes [] 
  
  (def a (with-open [file (io/reader "airports.csv")]
           (csv/read-csv (slurp file) :separator \,)
           ))
  a  
  )

(defn get-airport-codes-map [list]
  (
    (def aiportmap (atom {}))
    (for [i (range 0 (count list))] 
      (let [elem (nth list i)]
        (swap! aiportmap assoc (elem 3) (elem 1)) i)
      )
    (def mapair (zipmap (map keyword (keys @aiportmap)) (vals @aiportmap)))
    )mapair
  )

(en/deftemplate homepage
  (en/xml-resource "index.html") [request] 
  )

(defn request-events [artist]
  (def events ((parse-string (:body (client/get (str "http://api.seatgeek.com/2/events?per_page=5&page=1&sort=lowest_price.asc&performers.slug=" artist)))) "events"))
  (def eventsmap (atom{}))
  (for [i (range 0 (count events))] 
    (let [el (nth events i), performers (el "performers"), venue (el "venue")]
      (def artists (atom []))
      (for [j (range (count performers))] 
        (let [per (nth performers j)]
          (def a (swap! artists conj (->Performer (per "name") (((per "genres") 0) "name") (per "image")))))
        )
      (def ven (->Place ((el "venue") "name") ((el "venue") "city") ((el "venue") "country") (vals ((el "venue") "location"))))
      (def tick (->Ticket ((el "stats") "lowest_price") (el "url") ))
      (def event (->Event (el "title") a ven tick))
      (swap! eventsmap assoc i event) 
      )
    )
  )

(defn get-events-map [map]
  (
    (def eventstmap (atom {}))
    (for [i (range 0 (count list))] 
      (let [elem (nth list i)]
        (swap! aiportmap assoc (elem 3) (elem 1)) i)
      )
    (def mapair (zipmap (map keyword (keys @aiportmap)) (vals @aiportmap)))
    )mapair
  )

;(defn request-flights [origin destination departure_date arrival_date])

(defn send-flight-request [origin destination departure_date arrival_date]
  (
    (def requestFlight (generate-string  {:request{
                                                   :slice [
                                                           {
                                                            :origin "BEG",
                                                            :destination "BCN",
                                                            :date "2015-04-21"
                                                            },
                                                           {
                                                            :origin "BCN",
                                                            :destination "BEG",
                                                            :date "2015-04-28"
                                                            }
                                                           ],
                                                   :passengers {
                                                                :adultCount 1,
                                                                :infantInLapCount 0,
                                                                :infantInSeatCount 0,
                                                                :childCount 0,
                                                                :seniorCount 0
                                                                },
                                                   :solutions 1,
                                                   :refundable false
                                                   }}))
    (def options {
                  :body  requestFlight
                  :headers {"X-Api-Version" "2"
                            "Content-Type" "application/json"}
                  :content-type :json
                  :accept :json
                  :throw-entire-message? true}
      )   
    (def response (client/post "https://www.googleapis.com/qpxExpress/v1/trips/search?key=AIzaSyDYM93xp8iYFCxfTdvfk2z3BpLBfXqDxB0&fields=trips/tripOption(saleTotal,slice/segment/leg(origin,destination,arrivalTime,departureTime),pricing/fare(origin,destination,carrier))"
                               options))
    
    
    (def body (parse-string (:body response) true))
    
    
    ))

(defn process-flight-response [body] 
  (  (def price {:price (:saleTotal (first (:tripOption (:trips body))))})
    (def fare (:segment (first (:slice (first (:tripOption (:trips body)))))))
    (def fareInformation (for [i (range 0 (count fare))] (first(:leg (fare i)))))
    ))

(defroutes app*
  (compojure.route/resources "/")
  (GET "/" request (homepage request))
  (POST "/event-airline-tickets" request
        (request-artist (-> request :params :artist))
        )
  )

(def app (compojure.handler/site app*))

(defn -main [& args] "connects to server"
  (httpserver/run-server #'app {:port 8080 :ip "localhost" :join? false}))