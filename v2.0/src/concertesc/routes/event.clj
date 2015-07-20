(ns concertesc.routes.event)

(defrecord Ticket [price url])
(defrecord Performer [name image_url])
(defrecord Place [name city country location])
(defrecord Event [name performers date Place Ticket])
(defrecord Flight [origin destination departure_date arrival_date carrier])
(defrecord Trip [Flights price])
(defrecord Result [Event Trip total_price total_distance])


(defn send-request-events [artist]
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

(defn send-request [artist]
  (client/get (str "http://api.seatgeek.com/2/events?per_page=5&page=1&sort=lowest_price.asc&performers.slug=" artist)))

(defn parse-response [response]
  ((-> response :body parse-string) "events"))

(defn process-response [events]
  (map process-event events))

(defn process-event [event]
  (let [performers (event "performers")
        venue (event "venue")
        place (->Place (venue "name") (venue "city") (venue "country") (vals (venue "location")))
        ticket (->Ticket ((el "stats") "lowest_price") (event "url"))
        artists (map #(->Performer (% "name") (% "image")) performers)]
    (->Event (event "title") artists (subs (event "datetime_local") 0 (.indexOf (event "datetime_local") "T")) place ticket)))

(defn request-events [artist]
  (-> artist send-request parse-response))
