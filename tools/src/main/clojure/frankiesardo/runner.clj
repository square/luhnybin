(ns frankiesardo.runner
  (:require
   [clojure.java.io :as io])
  (:gen-class))


(defn sanitize [line] line)

(defn -main
  "Application entry point"
  [& args]
  (with-open [rdr (io/reader *in*)]
    (doseq [line (line-seq rdr)]
      (println (sanitize line)))))
