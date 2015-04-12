(ns concertescape.core
  (use (compojure handler [core :only (GET POST defroutes)]))
  (require [compojure.route])
  (require [net.cgrand.enlive-html :as en])
  (require [org.httpkit.server :as httpserver])   
  (require [clj-http.client :as client])
  (require [cheshire.core :refer :all])
  )



(en/deftemplate homepage
  (en/xml-resource "index.html") [request] 
  )

(defn request-artist [artist]
  ((let [response (client/get (str "http://api.viagogo.net/Public/Event/Search?searchText=" artist))]
     ;; Handle responses one-by-one, blocking as necessary
     ;; Other keys :headers :body :error :opts
     (@response)
     ))
  )

(defn request-flights [origin destination departure_date arrival_date]
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
    
    
    (let [response (client/post "https://www.googleapis.com/qpxExpress/v1/trips/search?key=AIzaSyDYM93xp8iYFCxfTdvfk2z3BpLBfXqDxB0"
                                options)
          ]
      
      :body response)
    
    
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