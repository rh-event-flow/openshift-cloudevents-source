# OpenShift Cloud Event Source

This application listens to an HTTP endpoint for events coming from the [Heptio Event Router](https://github.com/heptiolabs/eventrouter) deployed on OpenShift.
The Heptio events will be converted into [CNCF CloudEvents](http://cloudevents.io) and forwarded to an Apache Kafka topic.

This is an internal Proof of Concept and has a number of known issues with it. 
These will be fixed but the project is being shared to allow others to see/test it.
Feedback, suggestions, reviews are welcomed!

## Installation

### Automated

Use the [OCP Broker](https://github.com/project-streamzi/ocp-broker).

### Manual

1. Deploy [Strimzi](http://strimzi.io).

2. Deploy the EnvironmentVariable Operator as this project uses it to inject the configuration from ConfigMaps

If you have the [OCP Broker](https://github.com/project-streamzi/ocp-broker) installed the Environment Variable Operator will be available from the Service Catalog.
If not, follow the steps below.

```bash
$ git clone https://github.com/project-streamzi/EnvironmentVariableOperator.git
$ cd EnvironmentVariableOperator
$ oc login -u system:admin
$ oc adm policy add-cluster-role-to-user cluster-admin system:serviceaccount:myproject:default
$ oc login -u developer
$ mvn clean pacakge fabric8:deploy -Popenshift
```

3. Deploy the heptio-event-source (this project). Optionally edit `./src/main/fabric8/heptop-source-cm.yml` to set the Apache Kafka Topic that Cloud Events will be sent to.

```bash
$ oc login -u developer
$ mvn clean package fabric8:deploy
```

4. Deploy the Heptio Event Router

The Event Router will be deployed to `myproject`. 
Edit the `./yaml/eventrouter.yaml` to change this.

```bash
$ oc create -f ./yaml/eventrouter.yaml
```

If you need to remove the Router `oc delete -f ./yaml/eventrouter.yaml`.

## Issues / Ideas

1. Is there a schema for the Heptio events or the source of the events? Can this be used to turn them into something more meaningful for CloudEvents?

1. The Heptio events might be better represented as Java Objects rather than a Map<String, Object>. 
However, with an unknown schema I couldn't get them to serialise nicely.

1. There will be a better mapping from Heptio Event -> CloudEvent but this is a first pass. 
To be discussed / improved.
