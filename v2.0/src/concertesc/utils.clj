(ns concertesc.utils
  (:require [clj-time.core :as t]
            [clj-time.coerce :as c]
            [clj-time.format :as f])
  (:import [java.lang.Math]))

(def date-formatter (f/formatter "yyyy-MM-dd 'at' hh:mm"))

(defn replace-space [s]
  (clojure.string/replace s #" " "-"))

(defn get-following-or-preceding-date [operator date]
  {:pre [(some #{operator} '(:plus :minus))]}
  (case operator
    :plus (t/plus date (t/days 1))
    :minus (t/minus date (t/days 1))))

(defn degrees->radians [x]
  (* (/ Math/PI 180) x))

(defn- process-d [x]
  (-> x (/ 2) Math/sin (Math/pow 2)))

(defn calculate-distance [a b]   ;;u pozivu metode se pretpostavlja da su kordinate u radijanima
  (let [[dlat dlon] (map - a b)
        res (+ (process-d dlat)
               (* (process-d dlon)
                  (-> a first Math/cos)
                  (-> b first Math/cos)))]
    (* 2
       6373
       (Math/atan2 (Math/sqrt res)
                   (->> res (- 1) Math/sqrt)))))
