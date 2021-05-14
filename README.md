# Towards an Architecture to Support Data Access in Research Data Spaces
This is the corresponding implementation for the paper "Towards an Architecture to Support Data Access in Research Data Spaces" by Julius
Möller, Dennis Jankowski and Axel Hahn.

Note: This is an experimental implementation for demonstrating the proposed concept of the paper. The implementation 
consists in total on 4 different components (3 Data Source Connectors and the Data Space Access Support System (DSASS)). 
Also please note, that we cannot provide public access to all the data sources which we used in our paper due to license reasons.
However, exemplary AIS data recorded by the U.S. Coast Guard can be obtained from MarineCadastre (https://marinecadastre.gov/ais/).
In addition, AIS data can also be acquired free of charge from the Danish Maritime Administration
(https://www.dma.dk/SikkerhedTilSoes/Sejladsinformation/AIS/Sider/default.aspx).

# Components
``dsass``= Implementation of the Data Space Access Support System. 
Realization of the concept from the presented paper can be found here (e.g. creation of (weak) vocabularies, mapping approach, keyword search)
``nb_ais``= Connects to a PostgreSQL database, which persists AIS data from the German Bight. 
Data is then made available to DSASS via a GraphQL interface.
``nb_radar``= Connects to to a PostgreSQL database, which persists Radar data from the German Bight. 
Data is then made available to DSASS via a GraphQL interface.
``usa_ais``= Imports a CSV file with AIS data from the USA and makes it available as a GraphQL interface for the DSASS.

This implementation is mainly based on GraphQL (https://github.com/graphql-java/graphql-java) and the Spring framework (https://spring.io/).

# Installation
0. Adapt the connectors to your needs (e.g. credential can be found in the application.yml files in _src/main/resources_)
1. Start building each of the 4 seperate components (``dsass``, ``nb_ais``, ``nb_radar``, ``usa_ais``) of the project with: ``gradle clean build``
2. Start the 3 connectors (``nb_ais``, ``nb_radar``, ``usa_ais``) by running _src/main/java/de.uol.dssp.source/Application.java_ in each connector
3. Start the DSASS by running _src/main/java/de.uol.dssp.source/DsassApplication.java_

You can find the matching experiment that was conducted in our paper in (_src/main/java/de.uol.dsass/DataSourceRepository.java_).

If no matching attribute could be found in the pre-defined vocabulary, the attribute is tried to be assigned to a Weak Vocabulary (cf. paper). 
In this manner, new concepts that were not covered by the manually defined vocabulary can also be recognized by the DSASS and thus also be queried by the user.
Subsequently, the user can query data from the distributed data sources in a uniform manner by using the vocabularies _and_ the weak Vocabularies.