(ns concertesc.routes.dataset
  (:require [clojure.java.io :as io]
          ;  [clojure-csv.core :as csv]
            [concertesc.utils :as util]
            [concertesc.db.core :as db])
  (:import [java.io File]
           [java.util List]
           [org.apache.mahout.cf.taste.recommender RecommendedItem]
           [org.apache.mahout.cf.taste.impl.model.file FileDataModel]
           [org.apache.mahout.cf.taste.impl.similarity LogLikelihoodSimilarity TanimotoCoefficientSimilarity]
           [org.apache.mahout.cf.taste.recommender Recommender]
           [org.apache.mahout.cf.taste.impl.recommender GenericItemBasedRecommender]
           [org.apache.mahout.cf.taste.impl.similarity.precompute MultithreadedBatchItemSimilarities FileSimilarItemsWriter]
           [org.apache.mahout.cf.taste.impl.similarity.file FileItemSimilarity]
           [org.apache.mahout.cf.taste.eval RecommenderBuilder]
           [org.apache.mahout.cf.taste.impl.eval AverageAbsoluteDifferenceRecommenderEvaluator]))

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

(defn precompute-recommendations [model simfn x file]
  (let [resultFile (File. file)]
    (comp (if (.exists resultFile) (.delete resultFile))
          (.computeItemSimilarities (MultithreadedBatchItemSimilarities. (->> model simfn (item-recommender model)) x)
                                    (.availableProcessors (. Runtime getRuntime))
                                    1
                                    (FileSimilarItemsWriter. resultFile)))))

;(precompute-recommendations (load-model "./data/preparedData.csv") log-likelihood 5 "./data/precomputedSimilaritiesL.csv")
;(precompute-recommendations (load-model "./data/preparedData.csv") tanimoto 5 "./data/precomputedSimilaritiesT.csv")


(defn get-precomputed-recommender [modelFile similaritiesFile]
  (GenericItemBasedRecommender. (load-model modelFile) (-> similaritiesFile File. FileItemSimilarity.)))

(defn get-recommendation [item x modelFile similaritiesFile]
  (map #(.getItemID %) (recommend item x (get-precomputed-recommender modelFile similaritiesFile))))

(defn evaluate [model simfn]
  (let [rec-builder (proxy [RecommenderBuilder] [] (buildRecommender ([model] (item-recommender model (simfn model)))))]
    (.evaluate (AverageAbsoluteDifferenceRecommenderEvaluator. ) rec-builder nil model 0.7 1.0)))

;(evaluate (load-model "./data/preparedData.csv") log-likelihood)
;(evaluate (load-model "./data/preparedData.csv") tanimoto)
