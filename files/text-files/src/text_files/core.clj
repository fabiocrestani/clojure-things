(ns text-files.core
  (:require [clojure.java.io :as io]
            [clojure.data.json :as json])
  (:gen-class))

(defn get-parameter [req pname]
  (get (:params req) pname))

(defn -main
  "Reads and writes json file"
  [& args]
   
  (println (if (.exists (io/file "file.json")) "File exists" "File doesn't exist"))

  ; Write to json file  
  (def output-json (json/write-str {:key1 1 :key2 "two"}))
  (spit "file.json" (str output-json "\n"))

  ; Read from json file
  (def input-json (json/read-str (slurp "file.json") :key-fn keyword))
  (println "key1 is" (get input-json :key1))
  (println "key2 is" (get input-json :key2))

)
