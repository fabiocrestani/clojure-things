; TCP client example using Aleph
; Reference: https://aleph.io/examples/literate.html#aleph.examples.tcp


(ns tcp-client.core
  (:require
    [manifold.deferred :as d]
    [manifold.stream :as s]
    [clojure.edn :as edn]
    [aleph.tcp :as tcp]
    [gloss.core :as gloss]
    [gloss.io :as io])
  (:gen-class))

; Here, we define a simple protocol where each frame starts 
; with a 32-bit integer describing the length of the string
; which follows. We assume the string is EDN-encoded, and so
; we define a pre-encoder of pr-str, which will turn our
; arbitrary value into a string, and a post-decoder of 
; clojure.edn/read-string, which will transform our string
; into a data structure.
; (def protocol
;   (gloss/compile-frame
;     (gloss/finite-frame :uint32 (gloss/string :utf-8)) pr-str
;     edn/read-string))

(def protocol
  (gloss/compile-frame
    (gloss/string :utf-8)
      pr-str
      edn/read-string))
      
;; This function takes a raw TCP **duplex stream** which represents bidirectional communication
;; via a single stream.  Messages from the remote endpoint can be consumed via `take!`, and
;; messages can be sent to the remote endpoint via `put!`.  It returns a duplex stream which
;; will take and emit arbitrary Clojure data, via the protocol we've just defined.
;;
;; First, we define a connection between `out` and the raw stream, which will take all the
;; messages from `out` and encode them before passing them onto the raw stream.
;;
;; Then, we `splice` together a separate sink and source, so that they can be presented as a
;; single duplex stream.  We've already defined our sink, which will encode all outgoing
;; messages.  We must combine that with a decoded view of the incoming stream, which is
;; accomplished via `gloss.io/decode-stream`.
(defn wrap-duplex-stream
  [protocol s]
  (let [out (s/stream)]
    (s/connect
      (s/map #(io/encode protocol %) out)
      s)

    (s/splice
      out
      (io/decode-stream s protocol))))

;; The call to `aleph.tcp/client` returns a deferred, which will yield a duplex stream that
;; can be used to both send and receive bytes. We asynchronously compose over this value using
;; `manifold.deferred/chain`, which will wait for the client to be realized, and then pass
;; the client into `wrap-duplex-stream`.  The call to `chain` will return immediately with a
;; deferred value representing the eventual wrapped stream.
(defn client
  [host port]
  (d/chain (tcp/client {:host host, :port port})
    #(wrap-duplex-stream protocol %)))

;; Takes a two-argument `handler` function, which takes a stream and information about the
;; connection, and sets up message handling for the stream.  The raw stream is wrapped in the
;; Gloss protocol before being passed into `handler`.
(defn start-server
  [handler port]
  (tcp/start-server
    (fn [s info]
      (handler (wrap-duplex-stream protocol s) info))
    {:port port}))

;; ## echo servers

;; This creates a handler which will apply `f` to any incoming message, and immediately
;; send back the result.  Notice that we are connecting `s` to itself, but since it is a duplex
;; stream this is simply an easy way to create an echo server.
(defn fast-echo-handler
  [f]
  (fn [s info]
    (s/connect
      (s/map f s)
      s)))

;; ### demonstration



(defn -main
  "A simple TCP client."
  [& args]  

  (def host "localhost")
  (def port 12345)
  (println (str "Trying to connect to " host ":" port))
  
  ;; We start a server `s` which will return incremented numbers.
  (def s
    (start-server
     (fast-echo-handler inc)
      port))
      ;1)

  ;; We connect a client to the server, dereferencing the deferred value returned such that `c`
  ;; is simply a duplex stream that takes and emits Clojure values.
  (def c @(client "localhost" 1234))

  ;; We `put!` a value into the stream, which is encoded to bytes and sent as a TCP packet.  Since
  ;; TCP is a streaming protocol, it is not guaranteed to arrive as a single packet, so the server
  ;; must be robust to split messages.  Since both client and server are using Gloss codecs, this
  ;; is automatic.
  (println @(s/put! c "temperature"))  ; => true

  ;; The message is parsed by the server, and the response is sent, which again may be split
  ;; while in transit between the server and client.  The bytes are consumed and parsed by
  ;; `wrap-duplex-stream`, and the decoded message can be received via `take!`.
  ;@(s/take! c)   ; => 2

  ;; The server implements `java.io.Closeable`, and can be stopped by calling `close()`.
  (.close s)


  (println "Done")
  
)