# Kafka Light Weight Broker.

Want to do some local kafka development?

Can't be asked to check which is the best docker image to use?

Then this is for you.

### Pre-requisites

- java installed.


### Installation

    git clone https://github.com/greenstevester/lightweight-kafka-broker
    ./gradlew clean build

### Running

Option 1: Run using an IDE, like intellij

1. In Intellij: File=> New Module form existing sources => import with gradle
2. Run this app as a spring boot application

Option 2: running from cmdline

    gradle clean bootrun

