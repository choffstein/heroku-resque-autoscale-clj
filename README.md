# heroku-resque-autoscale-clj

A clojure library to autoscale resque workers

## Assumptions 

This library assumes that `resque-clojure` is being used to manage resque workers.  If not, `*namespace-key*` and `*queue-prefix*` might need to be re-bound.

The library also assumes that there is a specific worker process for each queue.

## Configuration

Assumes `HEROKU_API_KEY`, `REDIS_HOST`, `REDIS_PORT`, and `REDIS_PASSWORD` are set at the command line.

## Usage

Create a script similar to the following:

	(ns task-master
		(:require [heroku-resque-autoscale-clj.core :as autoscale]))
		
	(defn watch-database-compressor []
		(autoscale/watch "my-app-name" "compression-queue" {:process "database-compressor"
						  			    		            :min-workers 0
	                      			    	                 :scale-levels { 5  1
	                      				 		                            10  2
	                      				                                    20  4}))
	                      				                     
	(defn watch-graphic-rendering []
		(autoscale/watch "my-app-name" "render-queue" {:process "render-worker"
	 									               :min-workers 1
	 									               :scale-fn (fn [num-jobs] 
	 									                           (/ num-jobs 10)) }))
	 									                           

Where "my-app-name" is the name of your Heroku app.

*Note that we can either specify scale levels or a scaling function.  In specifying scale levels, the key is the number of jobs in the queue, the value is the number of workers to scale once that level is exceeded.*

Then create a scheduled job (with Heroku Scheduler) similar to the following

	lein trampoline -r task-master.watch-database-compressor
	lein trampoline -r task-master.watch-graphing-rendering

