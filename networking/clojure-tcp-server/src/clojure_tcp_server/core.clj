;; TCP server
;; Author: Fabio Crestani
;; Date: 25.09.2020

(ns clojure-tcp-server.core
  (:gen-class))

(require '[clojure.java.io :as io])
(require '[clojure.set :as set])
(import '[java.net ServerSocket])

(defn test-host-get-voltage[]
  (format "%.3f" (+ 13.5 (float (/ (rand-int 50) 100)))))

(defn tcp-receive
  "Read a line of textual data from the given socket"
  [socket]
  (.readLine (io/reader socket)))

(defn tcp-send
  "Send the given string message out over the given socket"
  [socket msg]
  (let [writer (io/writer socket)]
      (.write writer msg)
      (.flush writer)))

(defn tcp-server
  "TCP server"
  [port handler]
  (with-open [server-sock (ServerSocket. port)
              sock (.accept server-sock)]
    (let [msg-in (tcp-receive sock)
          msg-out (handler msg-in)]
      (tcp-send sock msg-out))))

(defn tcp-handler
  [input]
  (println "tcp-handler called")
  (case input
    "voltage" (test-host-get-voltage)
    "invalid"))

(defn -main
  "usage"
  [& args]
  (println "Starting")
  (while true (tcp-server 1234 tcp-handler)))
  