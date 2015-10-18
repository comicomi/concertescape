(ns concertesc.routes.algorithm
  (:require ;[clojure.data.csv :as csv]
            [clojure-csv.core :as csvs]
            [clojure.java.io :as io])
    (:import [java.lang.Math]))

(defn read-data [file]
  (map #(->  % csvs/parse-csv first flatten)
       (line-seq (clojure.java.io/reader file))));););)

(defn create-dictionary [file]
  (group-by first (read-data file)))

(defn create-item-dictionary [file]
  (group-by second (read-data file)))

(defn euclidean [similiar]
  (/ 1 (+ 1 (Math/sqrt (apply + (map (fn [[a b]] (Math/pow (- a b) 2)) similiar))))))

(defn tanimoto [similiar]
  (let [dot (apply + (map (fn [[a b]] (* a b)) similiar))
        intensity (fn [x] (apply + (->> similiar (map x) (map #(* % %)))))
        ina (intensity first)
        inb (intensity second)]
    (/ dot (- (+ ina inb) dot))))

(defn cosine [similiar]
  (let [dot (apply + (map (fn [[a b]] (* a b)) similiar))
        norm (fn [x] (Math/sqrt (apply + (->> similiar (map x) (map #(* % %))))))
        nora (norm first)
        norb (norm second)]
    (/ dot (* nora norb))))

;(defn get-mutual[line comparison]
 ; (let [artist (second line)
  ;      found (map last (filter #(= artist (second %)) comparison))]
   ; (when (seq found)
    ;  (list  (read-string (last line))  (read-string (first found))))))

;(defn get-mutual[line comparison]
 ; (let [artist (second line)
 ;       found (map last (filter #(= artist (second %)) comparison))]
  ;  (when (seq found)
   ;   [(first line) [(read-string (last line))  (read-string (first found))]])))

(defn get-mutual [line comparison]
  (let [artist (first line)
        found (map last (filter #(= artist (first %)) comparison))]
    (when (seq found)
      [(read-string (last line)) (read-string (first found))])))

(defn calculate [dictionary user1 user2]
  (let [artists1 (dictionary user1)
        artists2 (dictionary user2)
        mutual-rankings1  (map #(get-mutual % artists2) artists1)
        filtered (filter #(= false (nil? %)) mutual-rankings1)]
    [user2
     (if (and (seq filtered) (> (count filtered) 4))
     ;  (euclidean filtered)
      ; (tanimoto filtered)
       (cosine filtered)
       0)]))

(defn get-recommendation [dictionary user n]
  (take n (reverse (sort-by last
                            (map #(calculate dictionary user (key %))
                                 (filter #(not= user (key %)) dictionary))))))
