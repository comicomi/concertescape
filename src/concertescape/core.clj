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
(defrecord Performer [name image_url])
(defrecord Place [name city country location])
(defrecord Event [name performers date Place Ticket])
(defrecord Flight [origin destination departure_date arrival_date carrier])
(defrecord Trip [Flights price])
(defrecord Result [Event Trip total_price total_distance])

(defn load-csv [file delimiter]
  (with-open [file (io/reader file)]
    (csv/read-csv (slurp file) :separator delimiter ,)
    )
  )

(def airports (load-csv "IATAairCodes.csv" \;))

(def locations (load-csv "airports.csv" \, ))

(def airport-codes-map 
  (into (sorted-map) (for [i (range 0 (count airports))] 
                       (let [elem (nth airports i), airport {  [(elem 1) (elem 2)] (elem 3)}]
                         airport
                         )
                       )
        )
  )

(def location-codes-map 
  (let [location-map (into (sorted-map) (for [i (range 0 (count locations))] 
                                          (let [elem (nth locations i), location { (elem 4) (list (elem 6) (elem 7))}]
                                            location
                                            )
                                          )
                           )]
    (zipmap (keys location-map) (map (fn [x] (map (fn [y] (java.lang.Double/parseDouble y)) x)) (vals location-map))))
  )


(defn calculate-distance [a b]
  (defn deg-to-rad [x]
    (* (/ java.lang.Math/PI 180) x)
    )
  (def EARTH_RADIUS (atom  6373))
  (let [dlon (- (deg-to-rad(last a)) (deg-to-rad(last b))),
        dlat (- (deg-to-rad(first a)) (deg-to-rad(first b)))
        res (+ 
              (java.lang.Math/pow 
                (java.lang.Math/sin 
                  (/ dlat 2)
                  ) 2
                ) 
              (* 
                (java.lang.Math/cos (deg-to-rad(first a))) 
                (* 
                  (java.lang.Math/cos (deg-to-rad(first b))) 
                  (java.lang.Math/pow 
                    (java.lang.Math/sin 
                      (/ dlon 2)
                      ) 2)
                  )
                )
              )]
    (* (* (java.lang.Math/atan2 (java.lang.Math/sqrt res) (java.lang.Math/sqrt (- 1 res))) 2) @EARTH_RADIUS)
    )
  )

(defn get-airport-code [place]
  (airport-codes-map (first (filter #(and (.startsWith (% 1) (-> place :country)) (.startsWith (% 0) (-> place :city))) (keys airport-codes-map))))
  )

;(def results (atom (list {:event (->Event "Avicii" (list (->Performer "fdf" "dsds" "http://data2.whicdn.com/images/96950352/thumb.jpg") (->Performer "fdf" "dsds" "http://i.ytimg.com/vi/0WV00zrWoXk/default.jpg")) "ee" (->Place "a" "b" "c" "l") (->Ticket "p" "s")) :flight (list
 ;                                                                                                                                                                                                                                                                              (list (->Flight "BEG" "LCA" "12" "12" "12"))
  ;                                                                                                                                                                                                                                                                            (list (->Flight "LCA" "BEG" "12" "12" "12"))) :price "eweq" :total_price 4123 :total_distance 12321.12312}
   ;                  {:event (->Event "Avicii" (list (->Performer "fdf" "dsds" "http://data2.whicdn.com/images/96950352/thumb.jpg")) "ee" (->Place "a" "b" "c" "l") (->Ticket "p" "s")) :flight (list
    ;                                                                                                                                                                                              (list (->Flight "BEG" "LCA" "12" "12" "12"))
     ;                                                                                                                                                                                             (list (->Flight "LCA" "BEG" "12" "12" "12"))) :price "eweqa" :total_price 1233 :total_distance 123.212} ) ))
;
(def results (atom {}))    
    

 (en/deftemplate homepage
  (en/xml-resource "index.html") [request] 
   [:#combobox :option] (en/clone-for [[airport] airport-codes-map]
                                     (comp (en/content (format "%s , %s"  (airport 0) (airport 1)))
                                            (en/set-attr :value  (airport-codes-map airport )))
                                      )
   [:#results :div.escape]  (en/clone-for [result @results]
                                          [:div.event :.name] 
                                          (en/prepend   (str (:name (result :event)) " - " (:price (:Ticket (result :event))) "€ - " ))
                                         
                                          [:table.artists :tr :td] (en/clone-for  [artist (:performers (result :event))]
                                                                  [:img] (en/set-attr :src (:image_url artist))
                                                                         [:p] (en/content (str (:name artist) ))
                                                                  )
                                          [:div.event :.place] (en/content 
                                                                 (let [place   (:Place (result :event))]
                                                                   (str (:name place) " - " (:city place) ", " (:country place))
                                                                   )
                                                                 )
                                         [:a] (comp 
                                              (en/append (str " Buy now " ))
                                              (en/set-attr :href (:url (:Ticket (result :event))))
                                              )
                                                                           
                                         [:div.trip :table.departure :tbody :tr] (en/clone-for [connection  (first (result :flight))]
                                                                         [:.origin] (en/content (:origin connection) )
                                                                         [:.destination]  (en/content  (:destination connection) )
                                                                         [:.departure_date]  (en/content  (:departure_date connection))
                                                                         [:.arrival_date]  (en/content  (:arrival_date connection))
                                                                         [:.carrier]  (en/content  (:carrier connection) )
                                                                              )
                                         [:div.trip :table.arrival :tbody :tr] (en/clone-for [connection  (last (result :flight))]
                                                                         [:.origin] (en/content (:origin connection) )
                                                                         [:.destination]  (en/content  (:destination connection) )
                                                                         [:.departure_date]  (en/content  (:departure_date connection))
                                                                         [:.arrival_date]  (en/content  (:arrival_date connection))
                                                                         [:.carrier]  (en/content  (:carrier connection) )
                                                                              )
                                          [:div.trip :table.arrival :tbody :tfoot :td :.price] (en/content (str (result :price) "€") )
                                         [:h4.distance] (en/content (str "Aproximate distance: "(java.lang.Math/round (result :total_distance)) "km"))
                                         [:h3.price] (en/content (str "Total price (ticket+flight) per person: " (result :total_price) "€"))
                                          )
 )

(defn get-performers [performers]
  (let [artists {}] 
    (for [j (range 0 (count performers))] 
      (let [per (nth performers j), performer (->Performer (per "name") (per "image"))]
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
                                                 :saleCountry "DE"
                                                 }}))
  (def options {
                :body  requestFlight
                :headers {"X-Api-Version" "2"
                          "Content-Type" "application/json"}
                :content-type :json
                :accept :json
                :throw-entire-message? true}
    )   
  (let [response (client/post "https://www.googleapis.com/qpxExpress/v1/trips/search?key=AIzaSyDYM93xp8iYFCxfTdvfk2z3BpLBfXqDxB0&fields=trips/data(city(code,name),carrier(code,name),airport(code,city)),trips/tripOption(saleTotal,slice/segment/leg(origin,destination,arrivalTime,departureTime),pricing/fare(origin,destination,carrier))"
                              options)]
    
    
    (parse-string (:body response) true)
    )
  
  )

(defn process-flight-response [body]
  (let [tripOption  (first (:tripOption (:trips body))), 
        price (java.lang.Double/parseDouble (subs (:saleTotal tripOption) 3)), 
        fare_carriers  (:fare (first (:pricing tripOption))),
        fares (:slice tripOption),
        data (:data (:trips body)),
        carriers (:carrier data),
        citymap (zipmap (map (fn [city] (:code (first (filter #(= city (:city % 1))  (:airport data))))) (map :code (:city data))) (map :name (:city data)))
        carriermap (zipmap (map :code carriers) (map :name carriers))
        mapa (atom {:price price})] 
    (swap! mapa conj {:flight 
                      (for [i (range 0 (count fares))] 
                        (let [connections (:segment (nth fares i))]
                          (for [j (range 0 (count connections))] 
                            (let [carrier (nth fare_carriers i), 
                                  connection (first (:leg (nth connections j)))
                                  first_connection (first (:leg (first connections)))
                                  last_connection (first (:leg (last connections)))]
                              (if (or (= (:origin carrier) (:origin first_connection)) (= (:destination carrier) (:destination last_connection))) 
                                (let [car (carriermap (:carrier carrier))] 
                                  (->Flight (citymap (:origin connection)) (citymap (:destination connection)) (format-date "yyyy-MM-dd 'at' hh:mm" (parse-date "yyyy-MM-dd'T'hh:mm" (:departureTime connection))) (format-date "yyyy-MM-dd 'at' hh:mm" (parse-date "yyyy-MM-dd'T'hh:mm" (:arrivalTime connection))) car)
                                  ))
                              )
                            ) 
                          ))
                      })                  
    ))

(defn top-level-fun [artist location]
  (let [events (request-events artist), 
        places (map :Place events), 
        dates (map :date events),
        destinations (map get-airport-code places),
        departure_dates  (map format-date (repeat 5 "yyyy-MM-dd") (map get-date (repeat 5 -)(map parse-date (repeat 5 "yyyy-MM-dd") dates))),
        arrival_dates  (map format-date (repeat 5 "yyyy-MM-dd") (map get-date (repeat 5 +)(map parse-date (repeat 5 "yyyy-MM-dd") dates))),
        flights (map process-flight-response (map send-flight-request (repeat 5 location) destinations departure_dates arrival_dates)),
        dest_locations (map location-codes-map destinations),
        distances (map #(calculate-distance (location-codes-map location)  %) dest_locations),
        final_results (map list events flights)]
    (for [i (range (count final_results))]
      (let [result (nth final_results i), 
            event (nth result 0), 
            trip (nth result 1), 
            distance (nth distances i),
            inter_res (merge {:event event} trip {:total_distance distance} ), 
            total_price (+  (inter_res :price) (-> (-> (inter_res :event) :Ticket) :price)),
            total_score  (reduce + (map #(* 0.5 %) (list distance total_price)))]
        (merge {:total_price total_price} inter_res {:total_score total_score})
        )
      )
    
    )
  )

(defroutes app*
  (compojure.route/resources "/")
  (GET "/" request (homepage request))
  (POST "/event-airline-tickets" request
      (let [artist  (:artist (:params request)), location  (:location (:params request))]
       ; (reset! results (top-level-fun  artist location))
        (reset! results (sort-by #(% :total_score) (top-level-fun  artist location)))
     )
     )
  )

(def app (compojure.handler/site app*))

(defn -main []
  (httpserver/run-server #'app {:port 8080 :ip "localhost" :join? false})
  )