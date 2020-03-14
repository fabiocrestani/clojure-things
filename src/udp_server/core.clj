;;(import '[java.net DatagramSocket
;;                   DatagramPacket
;;                   InetSocketAddress])

;; (ns udp-server.core
;;     (:gen-class))

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
        (String. (.getData packet)
            0 (.getLength packet))))

(defn udp-receive-loop
    [socket f]
    (future (while true (f (udp-receive socket)))))

(defn -main
    "I don't do a whole lot ... yet."
    [& args]
    (println "Hello, World!!!")
    (def socket (DatagramSocket. 8888))
    (udp-receive-loop socket println)
)



