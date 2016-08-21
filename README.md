# Kahuin

/ka'win/, from Mapudungun kawiñ ‎(“party”)

Anonymous gossip platform over a peer-to-peer network.

[Demo](https://kahuin.herokuapp.com/)

[![CircleCI](https://circleci.com/gh/polymeris/kahuin.svg?style=svg)](https://circleci.com/gh/polymeris/kahuin)

## How it works

Kahuin is intended to mimic the distribution of information through rumours in normal human
conversation. This is defined as:

 * The original source of a rumour (or “kahuin”) is unknown.
 * All participant contribute to the distribution of kahuins they find more interesting
 * Every participant can contribute kahuins to the network, and they are indistinguishable from
   the ones they are spreading from other sources.
 * Every rumour eventually dies, but the boring ones do so earlier than the interesting ones.

Unlike real-life rumours, kahuins are immutable. This is to help prevent censorship.

Technically, this is attained by combining peer to peer technologies (WebRTC), ECDSA signing of
messages and a distributed hashtable similar to Kademlia.

### Peer to peer communication

See `kahuin.network.peers` namespace.

Kahuin makes use of [PeerJs](http://peerjs.com/) for its p2p communication needs. This might
eventually be replaced with a custom solution. Either way, initially, a connection to a centralized
server is required to discover peers. This is the only part of the communication that is not
distributed. The ID used to identify nodes corresponds to the first 160 bits of their public key.

### Message signing

See `kahuin.network.ecc` namespace.

Messages are signed using the [Web Cryptography API](https://www.w3.org/TR/WebCryptoAPI/)
ECSDA algorithm, with NIST recommended curve P-256. This limits the number of supported browsers
significantly. Public keys are transmitted along the signed message to be compared with the node's
ID, mitigating the risk of sybil attacks on the DHT.

### Distributed Hash Table

*Under development*

See `kahuin.network.dht` namespace.

The design of the DHT is based on [Kademlia](http://www.scs.stanford.edu/~dm/home/papers/kpos.pdf).
This might need to be modified due to the comparatively short lifespans of nodes or other special
requirements.

Kahuins contain only two fields: optional reference to another kahuin and text. They are stored
under the key corresponding to their hash. This way they are guaranteed to be immutable. A user's
profile which corresponds to their self-assigned nickname, plus a stream of emitted and liked
kahuins is stored under the key corresponding to that user's 160-bit ID. This is signed by the same
method described under Message Signing, above.

In the future, if longer activity streams are required, a linked list structure might be introduced
to store them.

## Development Mode

### Install npm dependencies:

    lein npm install

### Compile css:

Compile css file once.

    lein garden once

Automatically recompile css file on change.

    lein garden auto

### Run application:

    lein clean
    lein figwheel dev

Connect to the figwheel nREPL at localhost:7888, then:

    user=> (go!)

Figwheel will automatically push cljs changes to the browser.

Wait a bit, then browse to [http://localhost:3449](http://localhost:3449).

### Run tests:

    lein clean
    lein doo #to test with chrome and firefox

The above command assumes that you have the browsers, Karma and its plugins installed:

    npm install karma-cljs-test karma-firefox-launcher karma-chrome-launcher --save-dev

## Production build

    lein clean
    lein cljsbuild once min
    
And run the server with:

    node src/js/server.js 