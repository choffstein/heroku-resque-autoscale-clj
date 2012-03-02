(ns heroku-resque-autoscale-clj.core
  (:require [clj-http.client :as http-client]
            [redis.core :as redis]))

(def ^:dynamic *heroku-api-key* (System/getenv "HEROKU_API_KEY"))
(def heroku-api-url "https://api.heroku.com/apps/")
(def ^:dynamic *queue-prefix* "resque:queue:")

(defn server-map
  {:host (System/getenv "REDIS_HOST")
   :port (System/getenv "REDIS_PORT")
   :password (System/getenv "REDIS_PASSWORD")})

(defn scale-process [app type qty]
  (let [post-map {:basic-auth ["",*heroku-api-key*]
                  :accept :json
                  :body (str "{ \"type:\"" type "\"qty:\"" qty "}")}]
    (client/post heroku-api-url post-map)))

(defn count-jobs-in-queue [queue]
  (redis/with-server server-map
    (redis/llen (str *queue-prefix* queue))))

(defn identify-scale-level [scale-levels jobs-in-queue]
  (let [capped-scale-levels (conj (cons 0 scale-levels) 0)
        level (filter (fn [[l r]] (and (>= jobs-in-queue l)
                                     (< jobs-in-queue r)))
                      (partition 2 1 capped-scale-levels))]
    (first (first level))))

(defn watch [app queue-map]
  (let [process-name (:process queue-map)
        jobs-in-queue (count-jobs-in-queue queue)]
    (if-let [scale-fn (:scale-fn queue-map)]
      (scale-process app process-name (scale-fn jobs-in-queue))
      (let [scale-level-keys (vec (sort (keys (:scale-levels queue-map))))
            scale-level (identify-scale-level scale-level-keys jobs-in-queue)
            qty (if (= 0 scale-level)
                  (or (:min-workers queue-map) 0)
                  (scale-level queue-map))]
        (scale-process app process-name qty)))))
