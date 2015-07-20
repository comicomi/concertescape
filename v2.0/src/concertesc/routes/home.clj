(ns concertesc.routes.home
  (:require [concertesc.layout :as layout]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.http-response :refer [ok]]
            [clojure.java.io :as io]
            [concertesc.db.core :as db]))

(defn home-page []
  (layout/render
    "home.html" {:docs (-> "docs/docs.md" io/resource slurp)}))

(defn about-page []
  (layout/render "about.html"))

(defn index-page []
  (layout/render "index.html"
   (merge {:airports (db/get-airports)})))


(defroutes home-routes
  (GET "/" [] (index-page))
  (POST "/event-airline-tickets" request (map str (select-keys (:params request) [:artist :location]))))
  (GET "/about" [] (about-page))
  (GET "/index" [] (index-page))
  (GET "/air" [] (map str (db/get-airports)))
  (POST "/event-airline-tickets" request (map str (select-keys (:params request) [:artist :location]))))
