(defproject heroku-resque-autoscale-clj "1.0.0-SNAPSHOT"
  :description "FIXME: write description"
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [clj-http "0.2.7"]
                 [resque-clojure "0.2.2"]
                 [org.clojars.tavisrudd/redis-clojure "1.3.1"]]

  :dev-dependencies [[s3-wagon-private "1.0.0"]]

  :repositories {"nfr-releases" "s3p://newfound-mvn-repo/releases/"
                 "nfr-snapshots" "s3p://newfound-mvn-repo/snapshots/"})
