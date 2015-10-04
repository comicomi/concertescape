# concertescape

1. About the application
========================

This is a web application which based on user’s input about a favorite artist and current location returns recommendations of concerts (performances) and return flights to the place where the concert is held  for two artists most similar to the one user defined as his/her favorite. The set of top 5 concerts with return flights to the place where they are held is returned sorted by the following criteria: 
1.	The cheapest total price (total price = return flight ticket + concert ticket);
2.	The shortest distance from user’s current location to the place where concert is held.
Both of these criteria currently have the same relevance (weight factor for both is equal to 0.5).

The dataset used to build a model for making recommendations about most similar artists to the one user  defined as his/her favorite  is [Last.fm Dataset - 360K users (user top artists)](http://www.dtic.upf.edu/~ocelma/MusicRecommendationDataset/index.html).

Similar artist recommenders that were created based on model previously built use the following measures for similarity:
1. Tanimoto Coefficient
2. LogLikelihood Similarity

The APIs used for getting relevant data for providing user with results about concrets and return flights are the followinf:
1. [SeatGeek Platform API](http://platform.seatgeek.com/) - to get concerts data;
2. [QPX Express API](https://developers.google.com/qpx-express/) - to get flights data.

2. Dataset
==========
The dataset downloaded to build recommendation system contains <user, artist-mbid, artist-name, total-plays> tuples (for ~360,000 users). Considering the size of the dataset and the requirements of this project, downloaded dataset was reduced to 60000 records. The dataset had to be adjusted so that it can be used with [Apache Mahout™](http://mahout.apache.org/) library for machine learning. In order to build a model for creating the recommender this library requiers all the data in the dataset to by numerical. That is why the following modifications were made:
* userids had to be converted from string to integer type;
* artistids had to be converted from string to integer type;
* playcount was used to derive the formula for measuring users preference about each artist he listened to so far
* all other existing data was removed.
The number of playcount of one artist by the user was divided by the total playcounts of that user. 
All modifications to originally downloaded dataset were made by creating appropriate formulas in [Excel](https://office.live.com/start/Excel.aspx) tool. After these modifications, new file containing the newly generated artistids and artistnames was created.

2. Technical realisation
========================
Technical realization of this application can be devided in 3 phases;
* creating the recommender for making recommendations about similar artistss to the user's favorite;
* retrieving events data based on either recommended artists or queried artist;
* retrieving return flights to the place where the concert is held;
* displaying the obtained results to the user.

3. Acknowledgements 
===================
This application has been developed as a part of the project assignment for the course [Tools and methods of software engineering](http://ai.fon.bg.ac.rs/master/alati-i-metode-softverskog-inzenjerstva/) in master degree program Software engineering at the [Faculty of Organization Sciences](http://fon.rs), University of Belgrade, Serbia.
