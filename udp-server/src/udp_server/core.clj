;; core.clj
;; udp-server
;; Author: Fabio Crestani
;; Date: 15.03.2020
;; Description: Simple UDP server which echoes back every received message

(ns udp-server.core
    (:import (java.net InetAddress DatagramPacket DatagramSocket InetSocketAddress)
             (java.nio.charset Charset))
    (:gen-class))

(defn udp-send
    "Sends a UDP message"
    [^DatagramSocket socket msg length host port]
    (def length (min length 512))
    ;;(let [payload (.getBytes msg)
      ;;  length2 (min length 512)
       ;; address (InetSocketAddress. host port)
       ;; packet (DatagramPacket. payload length2 address)]
       ;; (.send socket packet)))
    (def packet (DatagramPacket. msg length (InetSocketAddress. host port)))
    (.send socket packet))

(defn udp-receive
    "Block until a UDP message is received"
    [^DatagramSocket socket]
    (let [buffer (byte-array 512)
        packet (DatagramPacket. buffer 512)]
        (.receive socket packet)
        (def data 
            (String. (.getData packet) 0 (.getLength packet)))
        (or packet true)
    ))

(defn udp-receive-loop
    [socket f]
    (future (while true (f (udp-receive socket) socket))))

(defn udp-receive-callback
    "Callback to handle a received DatagramPacket. This examples echoes the input"
    [^DatagramPacket packet ^DatagramSocket socket]
    (def data (.getData packet))
    (def length (.getLength packet))
    (def data-str (String. data 0 length))
    (println "<" data-str)
    (def hostname (.getHostName (cast InetSocketAddress (.getSocketAddress packet))))
    (def port (.getPort (cast InetSocketAddress (.getSocketAddress packet))))
    (println ">" data-str)
    (udp-send socket data length hostname port))

(defn -main
    "Simple UDP echo."
    [& args]
    (def port 1234)
    (println "Starting listener on UDP port" port)
    (def socket (DatagramSocket. port))
    (udp-receive-loop socket udp-receive-callback))



