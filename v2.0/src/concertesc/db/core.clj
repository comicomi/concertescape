
(ns concertesc.db.core
  (:require
    [yesql.core :refer [defqueries]]
    [clojure.java.jdbc :as jdbc])
  (:import [java.sql PreparedStatement]))

(def db-spec
  {:classname "com.mysql.jdbc.Driver"
   :subprotocol "mysql"
   :subname "//localhost:3306/concertescape"
   :user "root"
   :password ""})

(defqueries "sql/queries.sql" {:connection db-spec})

(defn to-date [sql-date]
  (-> sql-date (.getTime) (java.util.Date.)))

(defn find-destination [destination]
  (-> (get-airport destination) first :iatacode))

;(defn store-recommendation [params]
 ; (store-loglikelihood! params))

(defn resolve-artist-by-name [artist]
  (-> (get-artist-by-name artist) first :id))

(defn resolve-artist-by-id [artistid]
  (-> (get-artist-by-id artistid) first :name))

(extend-protocol jdbc/IResultSetReadColumn
  java.sql.Date
  (result-set-read-column [v _ _] (to-date v))

  java.sql.Timestamp
  (result-set-read-column [v _ _] (to-date v)))

(extend-type java.util.Date
  jdbc/ISQLParameter
  (set-parameter [v ^PreparedStatement stmt idx]
    (.setTimestamp stmt idx (java.sql.Timestamp. (.getTime v)))))
