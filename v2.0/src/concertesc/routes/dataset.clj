(ns concertesc.routes.dataset
  (:require [clojure.java.io :as io]
            [clojure-csv.core :as csv]
            [concertesc.utils :as util]
            [concertesc.db.core :as db])
  (:import [java.io File]
           [java.util List]
           [org.apache.mahout.cf.taste.recommender RecommendedItem]
           [org.apache.mahout.cf.taste.impl.model.file FileDataModel]
           [org.apache.mahout.cf.taste.impl.similarity EuclideanDistanceSimilarity PearsonCorrelationSimilarity LogLikelihoodSimilarity TanimotoCoefficientSimilarity]
           [org.apache.mahout.cf.taste.impl.neighborhood NearestNUserNeighborhood]
           [org.apache.mahout.cf.taste.recommender Recommender ]
           [org.apache.mahout.cf.taste.impl.recommender GenericUserBasedRecommender GenericItemBasedRecommender]
           [org.apache.mahout.cf.taste.impl.similarity.precompute MultithreadedBatchItemSimilarities FileSimilarItemsWriter]
           [org.apache.mahout.cf.taste.impl.similarity.file FileItemSimilarity]))

(defn load-model [path]
  (FileDataModel. (File. path)))

(defn log-likelihood [model]
  (LogLikelihoodSimilarity. model))

(defn tanimoto [model]
  (TanimotoCoefficientSimilarity. model))

(defn item-recommender [model similarity]
  (GenericItemBasedRecommender. model similarity))

(defn recommend [item x recommender]
  (.mostSimilarItems recommender item x))

;(defn get-similiar [item x simfn model]
 ; (->> model simfn (item-recommender model) (recommend item x)))

;(defn store-recommendation [recommendation item]
;  (let [item2 (.getItemID recommendation)
;        similarity (.getValue recommendation)]
  ;  (db/store-recommendation {:item1 item :item2 item2 :similarity similarity})))

;(defn store-all-recommendations-for-item [recommendations item]
;  (map #(store-recommendation % item) recommendations))

;(defn get-all-recommendations [simfn x table]
;   (let [model (load-model "./data/preparedData.csv")
  ;       items (-> model .getItemIDs util/iterator->seq)]
  ;   (map #(-> % (get-similiar x simfn model) (store-all-recommendations-for-item %)) items)))

(defn precompute-recommendations [model simfn x file]
  (let [resultFile (File. file)]
    (comp (if (.exists resultFile) (.delete resultFile))
          (.computeItemSimilarities (MultithreadedBatchItemSimilarities. (->> model simfn (item-recommender model)) x)
                                    (.availableProcessors (. Runtime getRuntime))
                                    1
                                    (FileSimilarItemsWriter. resultFile)))))

(precompute-recommendations (load-model "./data/preparedData.csv") log-likelihood 5 "./data/precomputedSimilaritiesL.csv")
(precompute-recommendations (load-model "./data/preparedData.csv") log-likelihood 5 "./data/precomputedSimilaritiesT.csv")


(defn get-precomputed-recommender [modelFile similaritiesFile]
  (GenericItemBasedRecommender. (load-model modelFile) (-> similaritiesFile File. FileItemSimilarity.)))

(defn get-recommendation [item x modelFile similaritiesFile]
  (map #(.getItemID %) (recommend item x (get-precomputed-recommender modelFile similaritiesFile))))
