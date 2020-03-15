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
    [^DatagramSocket socket msg host port]
    (let [payload (.getBytes msg)
        length (min (alength payload) 512)
        address (InetSocketAddress. host port)
        packet (DatagramPacket. payload length address)]
        (.send socket packet)))

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
    (def data 
         (String. (.getData packet) 0 (.getLength packet)))
    (println "<" data)
    (def data_response (str "!" data)) 
    (def hostname (.getHostName (cast InetSocketAddress (.getSocketAddress packet))))
    (def port (.getPort (cast InetSocketAddress (.getSocketAddress packet))))
    (println ">" data_response)
    (udp-send socket data_response hostname port))

(defn -main
    "Simple UDP echo."
    [& args]
    (def port 1234)
    (println "Starting listener on UDP port" port)
    (def socket (DatagramSocket. port))
    (udp-receive-loop socket udp-receive-callback))



