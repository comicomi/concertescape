# concertescape

1. About the application
========================

This is a web application which based on user’s input about a favorite artist and current location returns recommendations of concerts (performances) and return flights to the place where the concert is held  for two artists most similar to the one user defined as his/her favorite. The set of top 5 concerts with return flights to the place where they are held is returned sorted by the following criteria: 
1.	The cheapest total price (total price = return flight ticket + concert ticket);
2.	The shortest distance from user’s current location to the place where concert is held.
Both of these criteria currently have the same relevance (weight factor for both is equal to 0.5).

The dataset used to build a model for making recommendations about most similar artists to the one user  defined as his/her favorite  is Last.fm Dataset - 360K users (user top artists).

Similar artist recommenders that were created based on model previously built use the following measures for similarity:
1. Tanimoto Coefficient
2. LogLikelihood Similarity

The APIs used for getting relevant data for providing user with results about concrets and return flights are the followinf:
1. SeatGeek Platform API - to get events data;
2. QPX Express API - to get flights data.

2. Dataset
==========

## Usage



## License

Copyright © 2015 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
