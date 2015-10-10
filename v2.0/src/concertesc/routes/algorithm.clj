(ns concertesc.routes.algorithm
  (:require ;[clojure.data.csv :as csv]
            [clojure-csv.core :as csvs]
         [clojure.java.io :as io])
    (:import [java.lang.Math]))

(defn data-dictionary
  [user artist]
    (cons {user artist}
          (lazy-seq (data-dictionary user artist))))

(defn create-data-dictionary [data]
  (let [dictionary {}]
    (lazy-seq
     (map #(create-data-dictionary-entry % dictionary) data))))

(defn read-data [file] ;ova
;   (let [dictionary (atom  {:1 "#########"})]
  ;   (map #(-> % first flatten);create-data-dictionary-entry)
          (map #(->  % csvs/parse-csv first flatten)
               (line-seq (clojure.java.io/reader file))));););)

(defn sl-korak[izlaz-f]
  (group-by #(map key %) izlaz-f))

(defn create-dictionary [file]
  (group-by first (read-data file)))

(defn create-item-dictionary [file]
  (group-by second (read-data file)))

(defn euclidean[[a b]]
  (Math/pow (- a b) 2))

;(defn get-mutual[line comparison]
 ; (let [artist (second line)
  ;      found (map last (filter #(= artist (second %)) comparison))]
   ; (when (seq found)
    ;  (list  (read-string (last line))  (read-string (first found))))))


(defn get-mutual[line comparison]
  (let [artist (first line)
        found (map last (filter #(= artist (first %)) comparison))]
    (when (seq found)
      (list  (read-string (last line))  (read-string (first found))))))

(defn calculate-euclidean [dictionary user1 user2]
  (let [artists1 (dictionary user1)
        artists2 (dictionary user2)
        mutual-rankings1  (map #(get-mutual % artists2) artists1)
        filtered (filter #(= false (nil? %)) mutual-rankings1)]
    (if (seq filtered)
      (/ 1 (+ 1 (Math/sqrt (apply + (map euclidean filtered)))))
      0)))

(defn get-recommendation [dictionary user n]
 (take n (sort > (map #(calculate-euclidean dictionary user (key %))
    (filter #(not= user (key %)) dictionary)))))
