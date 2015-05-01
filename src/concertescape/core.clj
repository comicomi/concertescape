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
(import 'java.util.Date)
(import 'java.text.SimpleDateFormat)
(defrecord Ticket [price url])
(defrecord Performer [name genre image_url])
(defrecord Place [name city country location])
(defrecord Event [name performers date Place Ticket])
(defrecord Flight [origin destination departure_date arrival_date carrier])
(defrecord Trip [Flights price])
(defrecord Result [Event Trip total_price total_distance])

(def airports (with-open [file (io/reader "IATAairCodes.csv")]
                (csv/read-csv (slurp file) :separator \;,)
                )
  )

(def airport-codes-map 
  (into {} (for [i (range 0 (count airports))] 
             (let [elem (nth airports i), airport {  [(elem 1) (elem 2)] (elem 3)}]
               airport
               )
             )
        )
  )


(defn get-airport-code [place]
   (airport-codes-map (first (filter #(and (.startsWith (% 1) (-> place :country)) (.startsWith (% 0) (-> place :city))) (keys airport-codes-map))))
  )

(en/deftemplate homepage
  (en/xml-resource "index.html") [request] 
  )

(defn get-performers [performers]
  (let [artists {}] 
    (for [j (range 0 (count performers))] 
      (let [per (nth performers j), performer (->Performer (per "name") (((per "genres") 0) "name") (per "image"))]
        (conj artists performer)
        )
      )
    )
  )

(defn request-events [artist]
  (let [events ((parse-string (:body (client/get (str "http://api.seatgeek.com/2/events?per_page=5&page=1&sort=lowest_price.asc&performers.slug=" artist)))) "events")]
    (for [i (range 0 (count events))] 
      (let [el (nth events i), 
            performers (el "performers"), 
            venue (el "venue"), 
            ven (->Place (venue "name") (venue "city") (venue "country") (vals (venue "location"))),
            tick (->Ticket ((el "stats") "lowest_price") (el "url")),
            artists (get-performers performers)] 
        (->Event (el "title") artists (subs (el "datetime_local") 0 (.indexOf (el "datetime_local") "T")) ven tick)     
        )
      )
    )
  )



(defn get-events-map [map]
  ((def aiportmap (atom {}))
    (def eventstmap (atom {}))
    (for [i (range 0 (count list))] 
      (let [elem (nth list i)]
        (swap! aiportmap assoc (elem 3) (elem 1)) i)
      )
    (def mapair (zipmap (map keyword (keys @aiportmap)) (vals @aiportmap)))
    )mapair
  )

;(defn request-flights [origin destination departure_date arrival_date])
;(defn get-date [date choice] 
; (Date. (choice (.getTime (.parse (SimpleDateFormat. "yyyy-MM-dd") date)) (* 1000 60 60 24)))
;)

(defn parse-date [pattern date]
  (Date. (.getTime (.parse (SimpleDateFormat. pattern) date)))
  )
(defn get-date [choice date]
  (Date.(choice (.getTime date)  (* 1000 60 60 24) ))
  )

(defn format-date [pattern date] ;"yyyy-MM-dd 'at' hh:mm"
  (.format (SimpleDateFormat. pattern) date)
  )

(defn send-flight-request [origin_code destination_code departure_date arrival_date]
  
  (def requestFlight (generate-string  {:request{
                                                 :slice [
                                                         {
                                                          :origin origin_code,
                                                          :destination destination_code,
                                                          :date departure_date
                                                          },
                                                         {
                                                          :origin destination_code,
                                                          :destination origin_code,
                                                          :date arrival_date
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
  (def response (client/post "https://www.googleapis.com/qpxExpress/v1/trips/search?key=AIzaSyDYM93xp8iYFCxfTdvfk2z3BpLBfXqDxB0&fields=trips/data(city(code,name),carrier(code,name),airport(code,city)),trips/tripOption(saleTotal,slice/segment/leg(origin,destination,arrivalTime,departureTime),pricing/fare(origin,destination,carrier))"
                             options))
  
  
  (def body (parse-string (:body response) true))
  
  
  )

(defn process-flight-response [body]
  (let [tripOption  (first (:tripOption (:trips body))), 
        price (:saleTotal tripOption), 
        fare_carriers  (:fare (first (:pricing tripOption))),
        fares (:slice tripOption)
        data (:data (:trips body)),
        carriers (:carrier data),
        citymap (zipmap (map :code (:airport data)) (map :name (:city data)))
        carriermap (zipmap (map :code carriers) (map :name carriers))
        ] 
    (for [i (range 0 (count fares))] 
      (let [connections (:segment (nth fares i))]
        (for [j (range 0 (count connections))] 
          (let [carrier (nth fare_carriers i), connection (first (:leg (nth connections j)))]
            (if (or (= (:origin carrier) (:origin connection)) (= (:destination carrier) (:destination connection))) 
              (let [car (carriermap (:carrier carrier))] 
                (->Flight (citymap (:origin connection)) (citymap (:destination connection)) (format-date (parse-date "yyyy-MM-dd'T'hh:mm" (:departureTime connection))) (format-date (parse-date "yyyy-MM-dd'T'hh:mm" (:arrivalTime connection))) car)
                ))
            )
          ) 
        ))
    ))

(defn top-level-fun [artist location]
  (let [response (request-events "avicii"), 
        places (map :Place response), 
        dates (map :date response),
        destinations (map get-airport-code places),
        departure_dates  (map format-date (repeat 5 "yyyy-MM-dd") (map get-date (repeat 5 -)(map parse-date (repeat 5 "yyyy-MM-dd") dates))),
        arrival_dates  (map format-date (repeat 5 "yyyy-MM-dd") (map get-date (repeat 5 +)(map parse-date (repeat 5 "yyyy-MM-dd") dates)))]
    (map send-flight-request (repeat 5 "BEG") destinations departure_dates arrival_dates)
    )
  )

(defroutes app*
  (compojure.route/resources "/")
  (GET "/" request (homepage request))
  (POST "/event-airline-tickets" request
        (top-level-fun (:artist (:params request)) (:location (:params request)))
        )
  )

(def app (compojure.handler/site app*))

(defn -main []
  (httpserver/run-server #'app {:port 8080 :ip "localhost" :join? false})
  ; (println "dadad")
  )