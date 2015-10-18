(ns concertesc.routes.improvement
  (:require [criterium.core :as criterium]
            [concertesc.utils :as util]
            [concertesc.routes.flight :as flight]
            [concertesc.routes.event :as ev]
            [concertesc.routes.distance :as dist]
            [concertesc.routes.artist :as art]
            [concertesc.routes.dataset :as data]
            [concertesc.routes.algorithm :as alg]))

;utils

;(def param (util/string->date "yyyy-MM-dd" "2015-05-21"))

;(criterium/with-progress-reporting (criterium/bench (util/get-date-formatter "yyyy-MM-dd") :verbose))

;(criterium/with-progress-reporting (criterium/bench (util/string->date "yyyy-MM-dd" "2015-05-21") :verbose))

;(criterium/with-progress-reporting (criterium/bench (util/date->string "yyyy-MM-dd" param) :verbose))

;(criterium/with-progress-reporting (criterium/bench (util/iterator->seq (.iterator (java.util.ArrayList. [1 2 3]))) :verbose))

;(criterium/with-progress-reporting (criterium/bench (util/replace-space "tove lo") :verbose))

;(criterium/with-progress-reporting (criterium/bench (util/get-following-or-preceding-date :plus 1 param) :verbose))

;(criterium/with-progress-reporting (criterium/bench (util/degrees->radians 60) :verbose))

;(criterium/with-progress-reporting (criterium/bench (util/process-d 3.14) :verbose))

;(criterium/with-progress-reporting (criterium/bench (util/calculate-distance [3.14 2.15] [30.15 8.11]) :verbose))

;(criterium/with-progress-reporting (criterium/bench util/future-date :verbose))

;flight

;(def param1 (-> "tove-lo" ev/request-events first))

;(criterium/with-progress-reporting (criterium/bench (flight/get-flight-parameters param1 "BCN") :verbose))

;(criterium/with-progress-reporting (criterium/bench (flight/create-request-body "BCN" "MUC" "2015-10-05" "2015-10-07") :verbose))

;(def param2 (flight/create-request-body "BCN" "MUC" "2015-10-05" "2015-10-07"))

;(criterium/with-progress-reporting (criterium/bench (flight/send-request param2) :verbose))

;(criterium/with-progress-reporting (criterium/bench (flight/send-flight-request ["BCN" "MUC" "2015-10-05" "2015-10-07"]) :verbose))

;(def body (flight/send-flight-request ["BCN" "MUC" "2015-10-05" "2015-10-07"]))

;(def connection (-> body :trips :tripOption first :slice first :segment))

;(def carriermap (-> body :trips :data :carrier))

;(def param3 (first connection))

;(def param4 (-> param3 :flight :carrier ))

;(criterium/with-progress-reporting (criterium/bench (flight/process-city param3 :origin) :verbose))

;(criterium/with-progress-reporting (criterium/bench (flight/find-carrier param4 carriermap) :verbose))

;(criterium/with-progress-reporting (criterium/bench (flight/process-carrier param3 carriermap) :verbose))

;(criterium/with-progress-reporting (criterium/bench (flight/process-date param3 :departureTime) :verbose))

;(criterium/with-progress-reporting (criterium/bench (flight/process-connection param3 carriermap) :verbose))

;(criterium/with-progress-reporting (criterium/bench (flight/process-flight-connection connection carriermap) :verbose))

;(def param5 (flight/send-flight-request ["BCN" "MUC" "2015-10-05" "2015-10-07"]))

;(criterium/with-progress-reporting (criterium/bench (flight/process-response param5) :verbose))

;(criterium/with-progress-reporting (criterium/bench (flight/get-flights param1 "BCN") :verbose))

;event

;(criterium/with-progress-reporting (criterium/bench (ev/send-request "tove-lo") :verbose))

;(def param1 (ev/send-request "tove-lo"))

;(criterium/with-progress-reporting (criterium/bench (ev/parse-response param1) :verbose))

;(def param2 (-> "tove-lo" ev/send-request ev/parse-response first))

;(criterium/with-progress-reporting (criterium/bench (ev/process-event param2) :verbose))

;(def param3 (-> "tove-lo" ev/send-request ev/parse-response))

;(criterium/with-progress-reporting (criterium/bench (ev/process-response param3) :verbose))

;(criterium/with-progress-reporting (criterium/bench (ev/request-events "tove-lo") :verbose))

;distance

;(criterium/with-progress-reporting (criterium/bench (dist/get-coordinates "BCN") :verbose))

;(criterium/with-progress-reporting (criterium/bench (dist/degrees->radians [60 90]) :verbose))

;(criterium/with-progress-reporting (criterium/bench (dist/calculate-distance "BCN" [90 120]) :verbose))

;artist

;(criterium/with-progress-reporting (criterium/bench (art/resolve-artist-by-name "madonna") :verbose))

;(criterium/with-progress-reporting (criterium/bench (art/resolve-artist-by-id 14) :verbose))

;data

;(criterium/with-progress-reporting (criterium/bench (data/get-precomputed-recommender "./data/preparedData.csv" "./data/precomputedSimilaritiesL.csv") :verbose))

;(criterium/with-progress-reporting (criterium/bench (data/get-recommendation 14 5 "./data/preparedData.csv" "./data/precomputedSimilaritiesL.csv") :verbose))

;(criterium/with-progress-reporting (criterium/bench (data/get-recommendation 14 5 "./data/preparedData.csv" "./data/precomputedSimilaritiesT.csv") :verbose))

;algorithm

;(criterium/with-progress-reporting (criterium/bench (alg/read-data "./data/preparedData.csv") :verbose))

;(criterium/with-progress-reporting (criterium/bench (alg/create-dictionary "./data/preparedData.csv") :verbose))

;(criterium/with-progress-reporting (criterium/bench (alg/create-item-dictionary "./data/preparedData.csv") :verbose))

;(def param (alg/create-item-dictionary "./data/preparedData.csv"))

;(def param1 (param "49624"))

;(def param11 (first (param "49624")))

;(def param2 (param "49610"))

;(def param3 (filter #(= false (nil? %))  (map #(alg/get-mutual % param2) param1)))

;(criterium/with-progress-reporting (criterium/bench (alg/euclidean param3) :verbose))

;(criterium/with-progress-reporting (criterium/bench (alg/tanimoto param3) :verbose))

;(criterium/with-progress-reporting (criterium/bench (alg/cosine param3) :verbose))

;(criterium/with-progress-reporting (criterium/bench (alg/get-mutual param11 param2) :verbose))

;(criterium/with-progress-reporting (criterium/bench (alg/calculate param "49624" "49610") :verbose))

;(criterium/with-progress-reporting (criterium/bench (alg/get-recommendation param "49624" 5) :verbose))
