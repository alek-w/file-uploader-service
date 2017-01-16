## File Upload Service using a kafka notification

###Used versions:

Play framework 2.5

kafka_2.11-0.10.1.1

Vagrant 1.8.1

Oracle VM VirtualBox 5.0.24

Oracle Java 1.8.0_111

### Prerequisites:

Kafka, Zookeeper, Java should be installed on local machine.
Kafka, Zookeeper should be started with default settings. Example how to start kafka: 

` sudo /opt/kafka_2.11-0.10.1.1/bin/kafka-server-start.sh /opt/kafka_2.11-0.10.1.1/config/server.properties`

You could use prepared virtual box environment as file upload service. Url for downloading:

`https://drive.google.com/drive/folders/0B1AndzhCiNysU2RSUmRrV2lXZkE?usp=sharing`


import of vagrant box:
``
vagrant box add myubuntu package.box
``

init box as virtual environment:
``
vagrant init myubuntu
``

Start up the box:
``
vagrant up --provider virtualbox
``

Connect to the box:
``
vagrant ssh
``

Execute: 
`~/prj/fileupload/sbt clean run
`
Open:
`http://10.110.0.10:9000/` in case of using vagrant

or just

`http://127.0.0.1:9000/` 

#
* How to run tests:
~~~
~/prj/fileupload/sbt test
~~~

#
access to outer localhost from virtual environment:
 10.0.2.2
 
access to inner localhost of virtual environment: 
10.110.0.10