(ns concertescape.app
  (use (compojure handler [core :only (GET POST defroutes)]))
  (require [ring.adapter.jetty :as jetty]
   [compojure.route]
   [net.cgrand.enlive-html :as en]
   [ring.util.response :as response]) 
)


(en/deftemplate homepage
  (en/xml-resource "index.html") [request] 
 )

(defroutes app*
  (compojure.route/resources "/")
  (GET "/" request (homepage request))
 )

(def app (compojure.handler/site app*))

(defn -main []
  (jetty/run-jetty #'app {:port 8080 :join? false}))