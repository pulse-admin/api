# pulse-api

The PULSE API

# Installation instructions

## Clone the repository

```sh
$ git clone https://github.com/pulse-admin/api.git
```

## Configure

```sh
$ cd pulse/broker/src/main/resources/META-INF
$ cp persistence.template.xml persistence.xml
$ nano persistence.xml
$ # Set up database un/pw information, as well as access URL
$ cd ../
$ cp application.properties.template application.properties
$ nano application.properties
$ # Set up database un/pw, server port, spring security password, etc.
```

An API key is requried when connecting to Carequality Directory Services, and will need to be provided in the broker's application.properties file, in a format like:

```sh
sequoiaCarequalityOrganizationDirectoryUrl=https://INSERT URL HERE/Organization?apikey=INSERT_API_KEY_HERE&_format=json&_count=5
sequoiaEHexOrganizationDirectoryUrl=https://INSERT URL HERE/Organization?apikey=INSERT_API_KEY_HERE&_format=json&_count=5
```

## Build the jar file

### Command Line

```sh
$ cd pulse/broker
$ gradlew build
```

### Eclipse
1. Right click the mock project -> Gradle (STS) -> Refresh Dependencies
2. Right click the mock project -> Run As -> Gradle (STS) Build

## Deploy the Broker

```sh
$ cd pulse/broker
$ gradlew bootRun
```

## API Methods 
- POST /acfs/create  create a new ACF
	* PostBody includes name, phone number and address objects

- POST /acfs/{id}/edit  update an existing ACF
	* PostBody includes same info as create

- GET /acfs return a list of ACFS
- GET /acfs/{id} get the info for one ACF

- GET /organizations get all organizations

- POST /search finds possible patient records
	* PostBody has search parameters

- GET /queries gets all queries for logged in user; query object includes organization query statues which include a list of patient record results
- GET /queries/{id} gets info for one query; query object includes organization query statues which include a list of patient record results
- POST /queries/{id}/delete delete this query
- POST /queries/{id}/stage
	* PostBody is a list of patient record ids that came back from a query AND a Patient object with what should be saved for that merged patient
	* POST associates that new Patient with logged in user's ACF
	* At this point, any other PatientRecords from that query can be deleted from the db
			
- GET /patients gets all patients at the logged-in user's ACF
- POST /patients/{id}/delete delete this patient
- GET /patients/{id}/documents search all patient records associated with that patient object and return a list of documents
- GET /patients/{id}/documents/{id}?cacheOnly=true/false cache and/or return a single document
	* cacheOnly defaults to "true"
