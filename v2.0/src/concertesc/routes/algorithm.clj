(ns concertesc.routes.algorithm
  (:require ;[clojure.data.csv :as csv]
            [clojure-csv.core :as csvs]
         [clojure.java.io :as io])
    (:import [java.lang.Math]))

(defn data-dictionary
  [user artist]
    (cons {user artist}
          (lazy-seq (data-dictionary user artist))))

;(defn create-data-dictionary [data]
 ; (let [dictionary {}]
  ;  (lazy-seq
   ;  (map #(create-data-dictionary-entry % dictionary) data))))

(defn read-data [file] ;ova
  (map #(->  % csvs/parse-csv first flatten)
       (line-seq (clojure.java.io/reader file))));););)

(defn sl-korak[izlaz-f]
  (group-by #(map key %) izlaz-f))

(defn create-dictionary [file]
  (group-by first (read-data file)))

(defn create-item-dictionary [file]
  (group-by second (read-data file)))

(defn euclidean[similiar] ;euklidsko rastojanje daje bolje rezultate kada se najmanji broj usera koji su ocenili i jednog i drugog artist-a veci od 3 tj (and (seq filtered) (> (count filtered) 3))
  (/ 1 (+ 1 (Math/sqrt (apply + (map (fn [[a b]] (Math/pow (- a b) 2)) similiar))))))

(defn tanimoto[similiar]
  (let [dot (apply + (map (fn [[a b]] (* a b)) similiar))
        ina (apply + (->> similiar (map first) (map #(* % %))))
        inb (apply + (->> similiar (map second) (map #(* % %))))]
  (/ dot (- (+ ina inb) dot))))

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

(defn get-mutual[line comparison]
  (let [artist (first line)
        found (map last (filter #(= artist (first %)) comparison))]
    (when (seq found)
      [(read-string (last line)) (read-string (first found))])))

(defn calculate-euclidean [dictionary user1 user2]
  (let [artists1 (dictionary user1)
        artists2 (dictionary user2)
        mutual-rankings1  (map #(get-mutual % artists2) artists1)
        filtered (filter #(= false (nil? %)) mutual-rankings1)]
    [user2
     (if  (and (seq filtered) (> (count filtered) 4))
     ;  (euclidean filtered)
        (tanimoto filtered)
       0)]))

(defn get-recommendation [dictionary user n]
 (take n (reverse (sort-by last
                           (map #(calculate-euclidean dictionary user (key %))
                                (filter #(not= user (key %)) dictionary))))))
