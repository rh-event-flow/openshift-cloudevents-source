# Heptio Cloud Event Source

This application listens to an HTTP endpoint for events coming from the [Heptio Event Router](https://github.com/heptiolabs/eventrouter) deployed on OpenShift.
The Heptio events will be converted into [CNCF CloudEvents](http://cloudevents.io) and forwarded to an Apache Kafka topic.

This is an internal Proof of Concept and has a number of known issues with it. 
These will be fixed but the project is being shared to allow others to see/test it.
Feedback, suggestions, reviews are welcomed!

## Installation

At the moment, these steps need to be completed in order as the IP address of the heptio-source needs to be known before deploying the Heptio Event Router.
There may be a better way of deploying these with the correct namespaces and permissions

1. Deploy [Strimzi](http://strimzi.io).

2. Deploy the EnvironmentVariable Operator as this project uses it to inject the configuration from ConfigMaps

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
You will need to edit the `./yaml/eventrouter.yaml` to set the IP address of the Heptio Event Source, i.e. the container you started in the previous step.
The Router will be deployed into the `kube-system` namespace but will forward events to the endpoint configured in the previous step.

```bash
$ oc login -u system:admin
$ oc create -f ./yaml/eventrouter.yaml
$ oc login -u developer
```

If you need to remove the Router `oc delete -f ./yaml/eventrouter.yaml`.


## Issues / Ideas

1. The IP address of the heptio-source is needed to be known. 
I think that this is because the Heptio Event Router is deployed to a different namespace and so can't communicate with the OpenShift Route/Service.
However, it's possible that it can but I didn't wait long enough for message to appear.
Problems with asynchronous systems...

1. The namespaces and permissions are probably sub-optimal

1. It is possible/likely that there is a race condition when using the EnvironmentVariable Operator and the maven fabric8 plugin.
In testing, the deployment config does not always appear to have the environment variables injected into it following a re-deploy.
This needs more testing (I think that Strimzi has a reconciliation process) but if you edit the ConfigMap (eg. add a label) the change will be picked up and the application redeployed.

1. Can the Heptio Event Router be deployed into another namespace?

1. The endpoint name needs updating

1. Is there a schema for the Heptio events or the source of the events? Can this be used to turn them into something more meaningful for CloudEvents?

1. The Heptio events might be better represented as Java Objects rather than a Map<String, Object>. 
However, with an unknown schema I couldn't get them to serialsie nicely.

1. There will be a better mapping from Heptio Event -> CloudEvent but this is a first pass. 
To be discussed / improved.

1. The whole system might be better deployed via a different method (Deployment?) that can include the Heptio Event Router and the endpoint?