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
    (future (while true (f (udp-receive socket)))))

(defn udp-receive-callback
    [^DatagramPacket packet]
    (def data 
         (String. (.getData packet) 0 (.getLength packet)))
    (def length (.getLength packet))
    (def address (.getSocketAddress packet))
    (println "<" data "(" length ")" address)
    
    ;; (udp-siend socket input "localhost" 1234)
)

(defn -main
    "Simple UDP echo."
    [& args]
    (def port 1234)
    (println "Starting listener on UDP port" port)
    (def socket (DatagramSocket. port))
    (udp-receive-loop socket udp-receive-callback)
)



