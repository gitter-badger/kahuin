# Kahuin

/ka'win/, from Mapudungun kawiñ ‎(“party”)

Anonymous gossip platform over a peer-to-peer network.

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
    lein doo phantom # or just lein doo to test with chrome and firefox

The above command assumes that you have [phantomjs](https://www.npmjs.com/package/phantomjs) installed. However, please note that [doo](https://github.com/bensu/doo) can be configured to run cljs.test in many other JS environments (chrome, ie, safari, opera, slimer, node, rhino, or nashorn).

## Production Build

    lein clean
    lein cljsbuild once min
