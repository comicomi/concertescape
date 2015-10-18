# concertescape

1. About the application
========================

This is a web application which based on user’s input about a favorite artist and current location returns recommendations of concerts (performances) and return flights to the place where the concert is held  for two artists most similar to the one user defined as his/her favorite. The set of top 5 concerts with return flights to the place where they are held is returned sorted by the following criteria:

1. The cheapest total price (total price = return flight ticket + concert ticket); 
2. The shortest distance from user’s current location to the place where concert is held. 

Both of these criteria currently have the same relevance (weight factor for both is equal to 0.5).

The dataset used to build a model for making recommendations about most similar artists to the one user  defined as his/her favorite  is [Last.fm Dataset - 360K users (user top artists)](http://www.dtic.upf.edu/~ocelma/MusicRecommendationDataset/index.html).

Similar artist recommenders that were created based on model previously built use the following measures for similarity:

1. Euclidean Similarity;
2. Tanimoto Coefficient;
3. Cosine Correlation.

The APIs used for getting relevant data for providing user with results about concrets and return flights are the followinf:

1. [SeatGeek Platform API](http://platform.seatgeek.com/) - to get concerts data;
2. [QPX Express API](https://developers.google.com/qpx-express/) - to get flights data.

2. Dataset
==========
The dataset downloaded to build recommendation system contains <user, artist-mbid, artist-name, total-plays> tuples (for ~360,000 users). Considering the size of the dataset and the requirements of this project, downloaded dataset was reduced to 60000 records. The dataset was adjusted according to the format that [Apache Mahout™](http://mahout.apache.org/) library for machine learning recommends. This means that all the data in the dataset had to become numerical. That is why the following modifications were made:

* userids had to be converted from string to integer type;
* artistids had to be converted from string to integer type;
* playcount was used to derive the formula for measuring users preference about each artist he listened to so far
* all other existing data was removed.

The number of playcount of one artist by the user was divided by the total playcounts of that user. 
All modifications to originally downloaded dataset were made by creating appropriate formulas in [Excel](https://office.live.com/start/Excel.aspx) tool. After these modifications, new file containing the newly generated artistids and artistnames was created.

3. Item-to-item collaborative filtering implementation
======================================================
Since based on user's input, application should recommend concerts and flights for artists who are most similiar to the one whose name user provided as an input, it was needed to create item-to-item recommender. Created item-to-item recommender implements  item-based collaborative filtering, in which the similarity between items is calculated based on users ratings of those items. Three similarity measures were considered:

1. Euclidean Similarity;
2. Tanimoto Coefficient;
3. Cosine Correlation.
 
**Euclidean Similarity** was calculated by dividing 1 with the result of adding 1 to *Euclidean distance* between each pair of artists, which is easily presented with the following formula:

![Euclidean Similarity](http://latex.codecogs.com/gif.latex?%5Cfrac%7B1%7D%7B%281&plus;E%28a%2Cb%29%29%7D%3D%5Cfrac%7B1%7D%7B%281&plus;%5Csqrt%7B%28a_1-b_1%29%5E2&plus;...&plus;%28a_n-b_n%29%5E2%7D%29%7D)

This similarity measure takes values from 0 to 1, where 1 means perfect match, while 0 means total opposites.

**Tanimoto Coefficient** was calculated by dividing dot product of vectors containing users rankings for a pair of artists with the difference between the sum of norms of both vectors and dot product of those vectors, which is easily presented with the following formula:

![Tanimoto Coefficient](http://latex.codecogs.com/gif.latex?%5Cfrac%7B%5Csum_%7Bj%3D1%7D%5E%7Bk%7Da_%7Bj%7D%5Ctimes%20b_%7Bj%7D%7D%7B%5Csum_%7Bj%3D1%7D%5E%7Bk%7Da_%7Bj%7D%5E2&plus;%5Csum_%7Bj%3D1%7D%5E%7Bk%7Db_%7Bj%7D%5E2-%5Csum_%7Bj%3D1%7D%5E%7Bk%7Da_%7Bj%7D%5Ctimes%20b_%7Bj%7D%20%7D)

This similarity measure takes values from 0 to 1, where 1 means perfect match, while 0 means total opposites.

**Cosine Correlation** was calculated by dividing dot product of vectors containing users rankings for a pair of artists with the dot product of the norms of those vectors, which is easily presented with the following formula:

![Cosine Correlation](http://latex.codecogs.com/gif.latex?%5Cfrac%7B%5Csum_%7Bj%3D1%7D%5E%7Bk%7Da_%7Bj%7D%5Ctimes%20b_%7Bj%7D%7D%7B%5Csum_%7Bj%3D1%7D%5E%7Bk%7Da_%7Bj%7D%5E2%5Ctimes%20%5Csum_%7Bj%3D1%7D%5E%7Bk%7Db_%7Bj%7D%5E2%7D)

This similarity measure takes values from -1 to 1, where 1 means perfect match, while -1 means total opposites.

4. Application realization
==========================
This application was written in programming language Clojure using [Luminus](http://www.luminusweb.net/) framework. LightTable was used as code editor and run and stop commands to the server were issued via OS command line. 

Application workflow consists of the following 4 steps:

1. *Creating Item-to-item recommender* for making recommendations about similar artists to the user's favorite;
2. *Retrieving events data* based on either recommended artists or queried artist;
3. *Retrieving return flights* to the place where the concert is held *data*;
4. *Displaying results* to the user.

Results are displayed for all the artist similar to the one that user defined as his/her favorite.

5. Acknowledgements 
===================
This application has been developed as a part of the project assignment for the course [Tools and methods of software engineering](http://ai.fon.bg.ac.rs/master/alati-i-metode-softverskog-inzenjerstva/) in master degree program Software engineering at the [Faculty of Organization Sciences](http://fon.rs), University of Belgrade, Serbia.
